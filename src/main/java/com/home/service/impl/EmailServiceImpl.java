package com.home.service.impl;

import com.home.config.NotificationConfig;
import com.home.entity.DTO.ReservationNotificationDTO;
import com.home.service.EmailService;
import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 邮件发送服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NotificationConfig notificationConfig;

    @Override
    public boolean sendMovieOnlineEmail(ReservationNotificationDTO notificationDTO) {
        try {
            // 构建模板上下文
            Context context = new Context();
            context.setVariable("username", notificationDTO.getUsername());
            context.setVariable("movieTitle", notificationDTO.getTitle());
            context.setVariable("actionUrl", notificationDTO.getActionUrl());
            context.setVariable("reservationDate", notificationDTO.getReservationDate());
            
            // 渲染HTML模板
            String htmlContent = templateEngine.process("email/movie-online-notification", context);
            
            // 发送邮件
            String subject = "您预约的电影《" + notificationDTO.getTitle() + "》已上线！";
            boolean success = sendHtmlEmail(notificationDTO.getNotifyEmail(), subject, htmlContent);
            
            if (success) {
                LogUtils.info("电影上线通知邮件发送成功 - 收件人:{}, 电影:{}", 
                    notificationDTO.getNotifyEmail(), notificationDTO.getTitle());
            } else {
                LogUtils.warn("电影上线通知邮件发送失败 - 收件人:{}, 电影:{}", 
                    notificationDTO.getNotifyEmail(), notificationDTO.getTitle());
            }
            
            return success;
        } catch (Exception e) {
            LogUtils.error("发送电影上线通知邮件异常 - 收件人:{}, 电影:{}, 错误:{}", 
                notificationDTO.getNotifyEmail(), notificationDTO.getTitle(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(notificationConfig.getEmail().getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            LogUtils.info("简单邮件发送成功 - 收件人:{}, 主题:{}", to, subject);
            return true;
        } catch (Exception e) {
            LogUtils.error("简单邮件发送失败 - 收件人:{}, 主题:{}, 错误:{}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(notificationConfig.getEmail().getFrom(), notificationConfig.getEmail().getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示HTML格式
            
            mailSender.send(mimeMessage);
            LogUtils.info("HTML邮件发送成功 - 收件人:{}, 主题:{}", to, subject);
            return true;
        } catch (Exception e) {
            LogUtils.error("HTML邮件发送失败 - 收件人:{}, 主题:{}, 错误:{}", to, subject, e.getMessage(), e);
            return false;
        }
    }
}

