package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.DTO.AiConfigResponseDTO;
import com.home.entity.DTO.UserInfoDTO;
import com.home.service.UserAiConfigService;
import com.home.utils.JsonSerialization;
import com.home.utils.JWTUtils;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 用户AI配置Controller（个人中心「AI聊天设置」- API Key配置）
 * API Key经AES-GCM加密后存入user_ai_config表，接口只返回脱敏形式。
 */
@RestController
@Api(description = "用户AI配置相关接口")
@CrossOrigin
@RequestMapping("ai-chat/config")
public class AiConfigController {

    @Autowired
    private UserAiConfigService userAiConfigService;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private JsonSerialization jsonSerialization;

    @GetMapping
    @ApiOperation("查询用户AI配置（API Key脱敏）")
    public R<AiConfigResponseDTO> getConfig(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = resolveUserId(authorization);
        if (userId == null) {
            return R.failed("请先登录后再配置API Key");
        }
        try {
            return R.ok(userAiConfigService.getConfigInfo(userId)).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("查询AI配置失败 - userId:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("查询配置失败: " + e.getMessage());
        }
    }

    @PostMapping
    @ApiOperation("保存/更新用户AI配置")
    public R<Boolean> saveConfig(@RequestHeader(value = "Authorization", required = false) String authorization,
                                 @RequestBody Map<String, String> body) {
        String userId = resolveUserId(authorization);
        if (userId == null) {
            return R.failed("请先登录后再配置API Key");
        }
        if (body == null) {
            return R.failed("参数不能为空");
        }
        try {
            userAiConfigService.saveConfig(userId,
                    body.get("provider"), body.get("baseUrl"), body.get("model"), body.get("apiKey"));
            return R.ok(true).setCode(ResponMsg.Success.status());
        } catch (IllegalArgumentException e) {
            return R.failed(e.getMessage());
        } catch (Exception e) {
            LogUtils.error("保存AI配置失败 - userId:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("保存配置失败: " + e.getMessage());
        }
    }

    @DeleteMapping
    @ApiOperation("删除用户AI配置")
    public R<Boolean> deleteConfig(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String userId = resolveUserId(authorization);
        if (userId == null) {
            return R.failed("请先登录");
        }
        try {
            return R.ok(userAiConfigService.deleteConfig(userId)).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("删除AI配置失败 - userId:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 从Bearer token解析用户ID；配置接口必须登录（未登录/游客返回null）
     */
    private String resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7).trim();
            String userJson = jwtUtils.getUserFromToken(token);
            UserInfoDTO userInfo = jsonSerialization.deserializeFromJson(userJson, UserInfoDTO.class);
            if (userInfo != null && userInfo.getId() != null) {
                return String.valueOf(userInfo.getId());
            }
        } catch (Exception e) {
            LogUtils.warn("AI配置token解析失败: {}", e.getMessage());
        }
        return null;
    }
}
