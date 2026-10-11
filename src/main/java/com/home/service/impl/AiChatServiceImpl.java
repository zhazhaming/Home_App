package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.home.Exception.ServiceException;
import com.home.Enum.ResponMsg;
import com.home.entity.AiConversation;
import com.home.entity.AiMessage;
import com.home.mapper.AiConversationMapper;
import com.home.mapper.AiMessageMapper;
import com.home.service.AiChatService;
import com.home.service.AiReplyProvider;
import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: AI聊天服务实现类
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    @Autowired
    private AiConversationMapper conversationMapper;

    @Autowired
    private AiMessageMapper messageMapper;

    @Autowired
    private AiReplyProvider aiReplyProvider;

    @Override
    public List<AiConversation> listConversations(String userId) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
               .orderByDesc(AiConversation::getUpdatedAt);
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public AiConversation createConversation(String userId) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(conversation);
        LogUtils.info("创建AI对话 - userId:{}, conversationId:{}", userId, conversation.getId());
        return conversation;
    }

    @Override
    public List<AiMessage> listMessages(String userId, Long conversationId) {
        checkConversationOwner(userId, conversationId);
        LambdaQueryWrapper<AiMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiMessage::getConversationId, conversationId)
               .orderByAsc(AiMessage::getCreatedAt)
               .orderByAsc(AiMessage::getId);
        List<AiMessage> messages = messageMapper.selectList(wrapper);
        // 前端按timestamp字段渲染时间
        for (AiMessage message : messages) {
            message.setTimestamp(message.getCreatedAt());
        }
        return messages;
    }

    @Override
    public boolean renameConversation(String userId, Long conversationId, String title) {
        AiConversation conversation = checkConversationOwner(userId, conversationId);
        conversation.setTitle(title);
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversationMapper.updateById(conversation) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(String userId, Long conversationId) {
        checkConversationOwner(userId, conversationId);
        LambdaQueryWrapper<AiMessage> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(AiMessage::getConversationId, conversationId);
        messageMapper.delete(messageWrapper);
        return conversationMapper.deleteById(conversationId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiMessage sendMessage(String userId, Long conversationId, String content) {
        AiConversation conversation = checkConversationOwner(userId, conversationId);

        // 1. 保存用户消息
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole("user");
        userMessage.setContent(content);
        userMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(userMessage);

        // 2. 生成并保存AI回复
        String reply = aiReplyProvider.generateReply(content, userId);
        AiMessage aiMessage = new AiMessage();
        aiMessage.setConversationId(conversationId);
        aiMessage.setRole("ai");
        aiMessage.setContent(reply);
        aiMessage.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(aiMessage);

        // 3. 更新对话预览与标题（首次发消息时用内容做标题）
        conversation.setPreview(truncate(reply, 200));
        if (conversation.getTitle() == null || conversation.getTitle().trim().isEmpty()) {
            conversation.setTitle(truncate(content, 20));
        }
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        aiMessage.setTimestamp(aiMessage.getCreatedAt());
        LogUtils.info("AI消息处理完成 - userId:{}, conversationId:{}", userId, conversationId);
        return aiMessage;
    }

    /**
     * 校验对话归属，防止越权访问
     */
    private AiConversation checkConversationOwner(String userId, Long conversationId) {
        AiConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null || !userId.equals(conversation.getUserId())) {
            throw new ServiceException(ResponMsg.PARAMETER_ERROR.status(), "对话不存在或无权访问");
        }
        return conversation;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        String trimmed = text.replaceAll("\\s+", " ").trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) + "..." : trimmed;
    }
}
