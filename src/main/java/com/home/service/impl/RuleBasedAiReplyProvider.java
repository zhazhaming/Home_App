package com.home.service.impl;

import com.home.entity.Movies;
import com.home.service.AiReplyProvider;
import com.home.service.MovieService;
import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * @Author: zhazhaming
 * @Date: 2026/10/11
 * @Description: 规则式AI回复实现（本地演示版）
 * 无需外部API即可让聊天功能完整可用；推荐类问题会查询真实电影数据。
 * 用户在个人中心配置了个人API Key时由OpenAiCompatAiReplyProvider(@Primary)接管大模型回复，
 * 未配置Key或大模型调用失败时回落到本实现。
 */
@Service
public class RuleBasedAiReplyProvider implements AiReplyProvider {

    @Autowired
    private MovieService movieService;

    private static final Random RANDOM = new Random();

    @Override
    public String generateReply(String content, String userId) {
        String msg = content == null ? "" : content.trim();
        String lower = msg.toLowerCase();

        try {
            // 电影推荐类问题：查询真实电影数据
            if (lower.contains("推荐") || lower.contains("好看") || lower.contains("电影")
                    || lower.contains("看什么") || lower.contains("片")) {
                return buildMovieRecommendation();
            }
            // 预约功能咨询
            if (lower.contains("预约") || lower.contains("上映") || lower.contains("通知")) {
                return "关于预约功能：\n\n"
                        + "1. 在「电影影厅」找到感兴趣的电影，点击「立即预约」或进入详情页预约；\n"
                        + "2. 预约时填写通知邮箱；\n"
                        + "3. 电影上线后，系统会通过邮件和站内消息第一时间通知你。\n\n"
                        + "现在就可以去电影影厅试试哦～";
            }
            // 打招呼
            if (lower.contains("你好") || lower.contains("您好") || lower.contains("hi")
                    || lower.contains("hello") || lower.contains("在吗")) {
                return "你好呀！我是 MY HOME 的 AI 助手 🤖\n\n"
                        + "我可以帮你：\n"
                        + "· 🎬 推荐电影（试试\"推荐几部电影\"）\n"
                        + "· 🎫 介绍预约上映提醒功能\n"
                        + "· ✍️ 写首诗、讲个故事\n\n"
                        + "有什么想聊的尽管说～";
            }
            // 写诗
            if (lower.contains("诗")) {
                return "好呀，送你一首小诗：\n\n"
                        + "《光影》\n"
                        + "银幕亮起的时候，\n"
                        + "世界安静成一片海。\n"
                        + "我们在别人的故事里，\n"
                        + "流自己的泪，做自己的梦。\n\n"
                        + "——愿你也喜欢电影 🎬";
            }
            // 讲故事
            if (lower.contains("故事")) {
                return "给你讲个小故事：\n\n"
                        + "小镇上有家老电影院，只放一部电影，票根是蓝色的。有人问老板为什么，老板笑着说："
                        + "\"因为每个来看它的人，都是第一次看。\"\n\n"
                        + "同一部电影，在每个观众心里都是全新的故事。你最近想看什么电影呢？🎬";
            }
            // 天气（无实时数据，如实告知）
            if (lower.contains("天气")) {
                return "抱歉，我目前是本地演示版助手，暂时查不到实时天气 🌤️\n"
                        + "建议你看窗外的天空，或者出门前带上伞～\n\n"
                        + "倒是可以告诉我你想看什么类型的电影，下雨天和电影很配哦。";
            }
            // 感谢
            if (lower.contains("谢谢") || lower.contains("感谢")) {
                return "不客气！有需要随时找我 😊";
            }
            // 默认回复
            return "收到！不过我目前是本地演示版助手，理解能力有限 🙈\n\n"
                    + "你可以试试：\n"
                    + "· \"推荐几部电影\" —— 我会从片库挑几部热门的给你\n"
                    + "· \"预约上映提醒怎么用\" —— 介绍预约功能\n"
                    + "· \"写首诗\" / \"讲个故事\" —— 文艺一把\n\n"
                    + "或者直接在电影影厅逛逛吧！";
        } catch (Exception e) {
            LogUtils.error("生成AI回复异常: {}", e.getMessage(), e);
            return "抱歉，我刚刚走神了，请再试一次 🙈";
        }
    }

    /**
     * 查询真实电影数据生成推荐回复
     */
    private String buildMovieRecommendation() throws Exception {
        List<Movies> hotMovies = movieService.getHotMovie(1, 5);
        if (hotMovies == null || hotMovies.isEmpty()) {
            return "片库暂时没有热门电影数据，你可以去电影影厅逛逛～🎬";
        }
        StringBuilder sb = new StringBuilder("给你推荐几部热门电影 🎬：\n\n");
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        int index = 1;
        for (Movies movie : hotMovies) {
            sb.append(index++).append(". ").append(movie.getName());
            if (movie.getDate() != null) {
                sb.append("（").append(dateFormat.format(movie.getDate())).append("）");
            }
            sb.append("\n");
        }
        sb.append("\n在「电影影厅」可以查看详情并预约上映提醒哦～");
        return sb.toString();
    }
}
