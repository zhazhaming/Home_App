package com.home.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.home.entity.DTO.AiConfigResponseDTO;
import com.home.service.AiReplyProvider;
import com.home.service.UserAiConfigService;
import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 基于OpenAI兼容接口的AI回复实现（DeepSeek/智谱/通义/OpenAI等）
 * 用户在个人中心「AI聊天设置」配置了个人API Key时调用大模型；
 * 未配置或调用失败时自动回落到规则式回复(RuleBasedAiReplyProvider)，保证聊天始终可用。
 */
@Service
@Primary
public class OpenAiCompatAiReplyProvider implements AiReplyProvider {

    /** 各服务商默认配置（用户未填base_url/model时兜底） */
    private static final Map<String, String[]> PROVIDER_DEFAULTS = new HashMap<>();
    static {
        // provider -> {默认baseUrl, 默认model}
        PROVIDER_DEFAULTS.put("deepseek", new String[]{"https://api.deepseek.com", "deepseek-chat"});
        PROVIDER_DEFAULTS.put("mimo", new String[]{"https://platform.xiaomimimo.com/v1", "mimo-v2.5"});
        PROVIDER_DEFAULTS.put("zhipu", new String[]{"https://open.bigmodel.cn/api/paas/v4", "glm-4-flash"});
        PROVIDER_DEFAULTS.put("qwen", new String[]{"https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"});
        PROVIDER_DEFAULTS.put("openai", new String[]{"https://api.openai.com/v1", "gpt-4o-mini"});
        PROVIDER_DEFAULTS.put("custom", new String[]{"https://api.deepseek.com", "deepseek-chat"});
    }

    private static final String SYSTEM_PROMPT = "你是 MY HOME 电影平台的AI助手，回答简洁友好，可以用Markdown换行排版。"
            + "用户可能咨询电影推荐、预约上映提醒等功能。";

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 60000;

    @Autowired
    private UserAiConfigService userAiConfigService;

    @Autowired
    private RuleBasedAiReplyProvider ruleBasedProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    public OpenAiCompatAiReplyProvider() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String generateReply(String content, String userId) {
        String apiKey = null;
        try {
            apiKey = userAiConfigService.getDecryptedApiKey(userId);
        } catch (Exception e) {
            LogUtils.error("读取用户API Key失败 - userId:{}, 错误:{}", userId, e.getMessage(), e);
        }

        // 未配置个人Key，走内置规则回复
        if (apiKey == null || apiKey.isEmpty()) {
            return ruleBasedProvider.generateReply(content, userId);
        }

        try {
            String reply = callChatCompletions(userId, apiKey, content);
            if (reply != null && !reply.trim().isEmpty()) {
                return reply;
            }
            LogUtils.warn("大模型返回空回复，回落规则回复 - userId:{}", userId);
        } catch (Exception e) {
            LogUtils.error("大模型调用失败，回落规则回复 - userId:{}, 错误:{}", userId, e.getMessage(), e);
            return buildFallbackReply(e) + "\n\n" + ruleBasedProvider.generateReply(content, userId);
        }
        return ruleBasedProvider.generateReply(content, userId);
    }

    /**
     * 调用OpenAI兼容的chat/completions接口
     */
    private String callChatCompletions(String userId, String apiKey, String content) throws Exception {
        AiConfigResponseDTO configInfo = userAiConfigService.getConfigInfo(userId);
        String provider = configInfo.getProvider();
        String[] defaults = PROVIDER_DEFAULTS.getOrDefault(provider == null ? "custom" : provider,
                PROVIDER_DEFAULTS.get("custom"));

        String baseUrl = configInfo.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = defaults[0];
        }
        String model = configInfo.getModel();
        if (model == null || model.trim().isEmpty()) {
            model = defaults[1];
        }

        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", model);
        request.put("stream", false);
        ArrayNode messages = request.putArray("messages");
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";
        String responseBody = restTemplate.postForObject(url,
                new HttpEntity<>(objectMapper.writeValueAsString(request), headers), String.class);

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode choiceContent = root.path("choices").path(0).path("message").path("content");
        if (choiceContent.isMissingNode()) {
            throw new IllegalStateException("响应缺少choices[0].message.content: "
                    + truncate(responseBody, 300));
        }
        return choiceContent.asText();
    }

    private String buildFallbackReply(Exception e) {
        String reason;
        if (e instanceof HttpStatusCodeException) {
            reason = "接口返回 " + ((HttpStatusCodeException) e).getStatusCode().value()
                    + "（请检查API Key、模型名或Base URL是否正确）";
        } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
            reason = "请求超时";
        } else {
            reason = "网络或服务异常";
        }
        return "⚠️ 你配置的AI接口暂时调用失败（" + reason + "），以下为内置助手的回复：";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
