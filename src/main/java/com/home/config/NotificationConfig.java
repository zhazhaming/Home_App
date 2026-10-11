package com.home.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知服务配置类
 */
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationConfig {

    private Email email = new Email();
    private SiteMessage siteMessage = new SiteMessage();
    private Frontend frontend = new Frontend();

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public SiteMessage getSiteMessage() {
        return siteMessage;
    }

    public void setSiteMessage(SiteMessage siteMessage) {
        this.siteMessage = siteMessage;
    }

    public Frontend getFrontend() {
        return frontend;
    }

    public void setFrontend(Frontend frontend) {
        this.frontend = frontend;
    }

    /**
     * 邮件配置
     */
    public static class Email {
        private String from;
        private String fromName;
        private Integer retryTimes;
        private Long retryInterval;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public Integer getRetryTimes() {
            return retryTimes;
        }

        public void setRetryTimes(Integer retryTimes) {
            this.retryTimes = retryTimes;
        }

        public Long getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(Long retryInterval) {
            this.retryInterval = retryInterval;
        }
    }

    /**
     * 站内消息配置
     */
    public static class SiteMessage {
        private Integer retentionDays;

        public Integer getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(Integer retentionDays) {
            this.retentionDays = retentionDays;
        }
    }

    /**
     * 前端配置
     */
    public static class Frontend {
        private String baseUrl;
        private String movieDetailPath;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getMovieDetailPath() {
            return movieDetailPath;
        }

        public void setMovieDetailPath(String movieDetailPath) {
            this.movieDetailPath = movieDetailPath;
        }

        /**
         * 构建电影详情页完整URL
         */
        public String buildMovieDetailUrl(Integer movieId) {
            return baseUrl + movieDetailPath + "?id=" + movieId;
        }
    }
}

