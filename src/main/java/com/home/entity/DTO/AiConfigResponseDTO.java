package com.home.entity.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 用户AI配置响应DTO（API Key只返回脱敏形式，绝不返回明文）
 */
@ApiModel(value = "AiConfigResponseDTO", description = "用户AI配置信息")
public class AiConfigResponseDTO {

    @ApiModelProperty("是否已配置API Key")
    private Boolean configured;

    @ApiModelProperty("AI服务提供商: deepseek/zhipu/qwen/openai/custom")
    private String provider;

    @ApiModelProperty("OpenAI兼容API基础地址")
    private String baseUrl;

    @ApiModelProperty("模型名称")
    private String model;

    @ApiModelProperty("脱敏后的API Key(如: sk-1****abcd)")
    private String maskedApiKey;

    @ApiModelProperty("配置更新时间")
    private String updatedAt;

    public Boolean getConfigured() {
        return configured;
    }

    public void setConfigured(Boolean configured) {
        this.configured = configured;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMaskedApiKey() {
        return maskedApiKey;
    }

    public void setMaskedApiKey(String maskedApiKey) {
        this.maskedApiKey = maskedApiKey;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
