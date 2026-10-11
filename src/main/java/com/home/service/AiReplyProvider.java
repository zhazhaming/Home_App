package com.home.service;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: AI回复生成器接口（可插拔）
 * 当前默认实现为规则式回复(RuleBasedAiReplyProvider)，
 * 后续接入大模型(如通义千问/OpenAI/本地Ollama)时新增实现类并标注@Service替换即可。
 */
public interface AiReplyProvider {

    /**
     * 根据用户消息生成AI回复
     * @param content 用户消息内容
     * @param userId 用户ID(可用于个性化回复)
     * @return AI回复内容
     */
    String generateReply(String content, String userId);
}
