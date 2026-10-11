package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.home.entity.DTO.AiConfigResponseDTO;
import com.home.entity.UserAiConfig;
import com.home.mapper.UserAiConfigMapper;
import com.home.service.UserAiConfigService;
import com.home.utils.LogUtils;
import com.home.utils.SecretCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 用户AI聊天配置服务实现类
 * API Key加密后落库，查询接口只返回脱敏形式；解密仅限后端调用大模型时内部使用。
 */
@Service
public class UserAiConfigServiceImpl implements UserAiConfigService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserAiConfigMapper userAiConfigMapper;

    @Autowired
    private SecretCryptUtils secretCryptUtils;

    @Override
    public void saveConfig(String userId, String provider, String baseUrl, String model, String apiKey) {
        UserAiConfig existing = selectByUserId(userId);

        UserAiConfig config = existing == null ? new UserAiConfig() : existing;
        config.setUserId(userId);
        config.setProvider(provider == null || provider.trim().isEmpty() ? "custom" : provider.trim());
        config.setBaseUrl(baseUrl == null || baseUrl.trim().isEmpty() ? null : trimTrailingSlash(baseUrl.trim()));
        config.setModel(model == null || model.trim().isEmpty() ? null : model.trim());

        // apiKey为空且已有配置时保留原Key，仅更新其他字段
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            config.setApiKeyCipher(secretCryptUtils.encrypt(apiKey.trim()));
        } else if (existing == null) {
            throw new IllegalArgumentException("API Key不能为空");
        }

        if (existing == null) {
            config.setCreatedAt(LocalDateTime.now());
            userAiConfigMapper.insert(config);
            LogUtils.info("保存AI配置(新增) - userId:{}, provider:{}", userId, config.getProvider());
        } else {
            config.setUpdatedAt(LocalDateTime.now());
            userAiConfigMapper.updateById(config);
            LogUtils.info("保存AI配置(更新) - userId:{}, provider:{}", userId, config.getProvider());
        }
    }

    @Override
    public AiConfigResponseDTO getConfigInfo(String userId) {
        UserAiConfig config = selectByUserId(userId);
        AiConfigResponseDTO dto = new AiConfigResponseDTO();
        if (config == null) {
            dto.setConfigured(false);
            return dto;
        }
        dto.setConfigured(true);
        dto.setProvider(config.getProvider());
        dto.setBaseUrl(config.getBaseUrl());
        dto.setModel(config.getModel());
        try {
            dto.setMaskedApiKey(secretCryptUtils.mask(secretCryptUtils.decrypt(config.getApiKeyCipher())));
        } catch (Exception e) {
            // 密钥轮换等导致解密失败时仍返回已配置状态，但不泄露内容
            LogUtils.error("AI配置API Key解密失败 - userId:{}", userId, e);
            dto.setMaskedApiKey("****");
        }
        dto.setUpdatedAt(config.getUpdatedAt() == null ? null : config.getUpdatedAt().format(TIME_FORMATTER));
        return dto;
    }

    @Override
    public boolean deleteConfig(String userId) {
        UserAiConfig config = selectByUserId(userId);
        if (config == null) {
            return false;
        }
        boolean removed = userAiConfigMapper.deleteById(config.getId()) > 0;
        LogUtils.info("删除AI配置 - userId:{}, 结果:{}", userId, removed);
        return removed;
    }

    @Override
    public boolean hasConfig(String userId) {
        return selectByUserId(userId) != null;
    }

    @Override
    public String getDecryptedApiKey(String userId) {
        UserAiConfig config = selectByUserId(userId);
        if (config == null) {
            return null;
        }
        return secretCryptUtils.decrypt(config.getApiKeyCipher());
    }

    /**
     * 查询用户配置（user_id有唯一索引）
     */
    private UserAiConfig selectByUserId(String userId) {
        LambdaQueryWrapper<UserAiConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAiConfig::getUserId, userId);
        return userAiConfigMapper.selectOne(wrapper);
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
