package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.AiConversation;
import com.home.entity.AiMessage;
import com.home.entity.DTO.UserInfoDTO;
import com.home.service.AiChatService;
import com.home.utils.JsonSerialization;
import com.home.utils.JWTUtils;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: AI聊天Controller（配套home-web前端 /ai-chat 页面）
 * 用户身份从Bearer token解析；无token或token无效时以游客身份(0)使用。
 */
@RestController
@Api(description = "AI聊天相关接口")
@CrossOrigin
@RequestMapping("ai-chat")
public class AiChatController {

    /** 未登录游客使用的用户ID */
    private static final String GUEST_USER_ID = "0";

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private JsonSerialization jsonSerialization;

    @GetMapping("/history")
    @ApiOperation("查询用户对话列表")
    public R<List<AiConversation>> history(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = resolveUserId(authorization);
        LogUtils.info("查询AI对话列表 - userId:{}", userId);
        try {
            return R.ok(aiChatService.listConversations(userId)).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("查询AI对话列表失败 - userId:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("查询对话列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建新对话")
    public R<AiConversation> create(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = resolveUserId(authorization);
        LogUtils.info("创建AI对话 - userId:{}", userId);
        try {
            return R.ok(aiChatService.createConversation(userId)).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("创建AI对话失败 - userId:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("创建对话失败: " + e.getMessage());
        }
    }

    @GetMapping("/messages/{conversationId}")
    @ApiOperation("查询对话消息列表")
    public R<List<AiMessage>> messages(@RequestHeader(value = "Authorization", required = false) String authorization,
                                       @PathVariable Long conversationId) {
        String userId = resolveUserId(authorization);
        try {
            return R.ok(aiChatService.listMessages(userId, conversationId)).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("查询AI消息失败 - userId:{}, conversationId:{}, 错误:{}", userId, conversationId, e.getMessage(), e);
            return R.failed("查询消息失败: " + e.getMessage());
        }
    }

    @PutMapping("/rename/{conversationId}")
    @ApiOperation("重命名对话")
    public R<Boolean> rename(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable Long conversationId,
                             @RequestBody Map<String, String> body) {
        String userId = resolveUserId(authorization);
        String title = body == null ? null : body.get("title");
        if (title == null || title.trim().isEmpty()) {
            return R.failed("标题不能为空");
        }
        try {
            return R.ok(aiChatService.renameConversation(userId, conversationId, title.trim()))
                    .setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("重命名AI对话失败 - userId:{}, conversationId:{}, 错误:{}", userId, conversationId, e.getMessage(), e);
            return R.failed("重命名失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{conversationId}")
    @ApiOperation("删除对话")
    public R<Boolean> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                             @PathVariable Long conversationId) {
        String userId = resolveUserId(authorization);
        try {
            return R.ok(aiChatService.deleteConversation(userId, conversationId)).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("删除AI对话失败 - userId:{}, conversationId:{}, 错误:{}", userId, conversationId, e.getMessage(), e);
            return R.failed("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/message/{conversationId}")
    @ApiOperation("发送消息并获取AI回复")
    public R<AiMessage> sendMessage(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @PathVariable Long conversationId,
                                    @RequestBody Map<String, String> body) {
        String userId = resolveUserId(authorization);
        String content = body == null ? null : body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return R.failed("消息内容不能为空");
        }
        try {
            return R.ok(aiChatService.sendMessage(userId, conversationId, content.trim()))
                    .setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("AI消息处理失败 - userId:{}, conversationId:{}, 错误:{}", userId, conversationId, e.getMessage(), e);
            return R.failed("发送失败: " + e.getMessage());
        }
    }

    /**
     * 从Bearer token解析用户ID，解析失败按游客处理
     */
    private String resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return GUEST_USER_ID;
        }
        try {
            String token = authorization.substring(7).trim();
            String userJson = jwtUtils.getUserFromToken(token);
            UserInfoDTO userInfo = jsonSerialization.deserializeFromJson(userJson, UserInfoDTO.class);
            if (userInfo != null && userInfo.getId() != null) {
                return String.valueOf(userInfo.getId());
            }
        } catch (Exception e) {
            LogUtils.warn("AI聊天token解析失败，按游客处理: {}", e.getMessage());
        }
        return GUEST_USER_ID;
    }
}
