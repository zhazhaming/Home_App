package com.home.service;

import com.home.entity.DTO.AiConfigResponseDTO;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 用户AI聊天配置服务接口（个人API Key，AES-GCM加密存储）
 */
public interface UserAiConfigService {

    /**
     * 保存/更新用户AI配置
     * @param apiKey 明文API Key（为空且已有配置时仅更新其他字段，保留原Key）
     */
    void saveConfig(String userId, String provider, String baseUrl, String model, String apiKey);

    /**
     * 查询用户AI配置（API Key脱敏展示）
     */
    AiConfigResponseDTO getConfigInfo(String userId);

    /**
     * 删除用户AI配置
     */
    boolean deleteConfig(String userId);

    /**
     * 是否已配置API Key
     */
    boolean hasConfig(String userId);

    /**
     * 获取解密后的API Key（仅供后端调用大模型使用，不对外暴露）
     */
    String getDecryptedApiKey(String userId);
}
