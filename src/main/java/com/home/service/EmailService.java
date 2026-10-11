package com.home.service;

import com.home.entity.DTO.ReservationNotificationDTO;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 邮件发送服务接口
 */
public interface EmailService {

    /**
     * 发送电影上线通知邮件
     * @param notificationDTO 通知信息
     * @return 是否发送成功
     */
    boolean sendMovieOnlineEmail(ReservationNotificationDTO notificationDTO);

    /**
     * 发送简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 是否发送成功
     */
    boolean sendSimpleEmail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 是否发送成功
     */
    boolean sendHtmlEmail(String to, String subject, String htmlContent);
}

