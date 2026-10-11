package com.home.service;

import com.home.entity.AiConversation;
import com.home.entity.AiMessage;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: AI聊天服务接口
 */
public interface AiChatService {

    /**
     * 查询用户对话列表（按更新时间倒序）
     */
    List<AiConversation> listConversations(String userId);

    /**
     * 创建新对话
     */
    AiConversation createConversation(String userId);

    /**
     * 查询对话的消息列表
     */
    List<AiMessage> listMessages(String userId, Long conversationId);

    /**
     * 重命名对话
     */
    boolean renameConversation(String userId, Long conversationId, String title);

    /**
     * 删除对话（连同消息）
     */
    boolean deleteConversation(String userId, Long conversationId);

    /**
     * 发送消息并获取AI回复
     * @return AI回复消息
     */
    AiMessage sendMessage(String userId, Long conversationId, String content);
}
