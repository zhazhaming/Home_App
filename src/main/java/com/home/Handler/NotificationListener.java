package com.home.Handler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.home.Enum.NotificationStatusEnum;
import com.home.Enum.NotificationTypeEnum;
import com.home.config.RabbitMQConfig;
import com.home.entity.DTO.ReservationNotificationDTO;
import com.home.entity.Reservation;
import com.home.mapper.ReservationMapper;
import com.home.service.NotificationLogService;
import com.home.service.NotificationService;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知消息监听器
 */
@Component
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationLogService notificationLogService;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private JsonSerialization jsonSerialization;

    /**
     * 监听通知队列，处理电影上线通知
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE, concurrency = "3-10")
    public void handleNotification(String message) {
        LogUtils.info("接收到通知消息: {}", message);
        
        try {
            // 反序列化消息（使用支持Java8时间类型的方法）
            ReservationNotificationDTO notificationDTO = jsonSerialization.deserializeFromJson(
                message, ReservationNotificationDTO.class
            );
            
            if (notificationDTO == null) {
                LogUtils.error("通知消息反序列化失败: {}", message);
                return;
            }
            
            LogUtils.info("开始处理通知 - 预约ID:{}, 用户:{}, 电影:{}", 
                notificationDTO.getReservationId(),
                notificationDTO.getUsername(),
                notificationDTO.getTitle());
            
            // 发送通知（邮件+站内消息）
            boolean success = notificationService.sendNotification(notificationDTO);
            
            if (success) {
                LogUtils.info("通知处理成功 - 预约ID:{}", notificationDTO.getReservationId());
            } else {
                LogUtils.warn("通知处理失败 - 预约ID:{}", notificationDTO.getReservationId());
                // 抛出异常触发消息重新入队或进入死信队列
                throw new RuntimeException("通知发送失败");
            }
            
        } catch (Exception e) {
            LogUtils.error("处理通知消息异常: {}, 错误: {}", message, e.getMessage(), e);
            // 重新抛出异常，让RabbitMQ处理重试逻辑
            throw new RuntimeException("处理通知消息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 监听死信队列（可选：用于处理多次失败的消息）
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_DLQ)
    public void handleDeadLetter(String message) {
        LogUtils.error("通知消息进入死信队列（多次重试失败）: {}", message);
        
        try {
            ReservationNotificationDTO notificationDTO = jsonSerialization.deserializeFromJson(
                message, ReservationNotificationDTO.class
            );
            
            if (notificationDTO != null) {
                LogUtils.error("通知最终失败 - 预约ID:{}, 用户:{}, 电影:{}", 
                    notificationDTO.getReservationId(),
                    notificationDTO.getUsername(),
                    notificationDTO.getTitle());
                
                // 记录失败日志到notification_log表，便于排查和后续人工重试
                try {
                    notificationLogService.logNotification(
                        notificationDTO.getReservationId(),
                        notificationDTO.getUserId(),
                        NotificationTypeEnum.EMAIL.getCode(),
                        NotificationStatusEnum.FAILED.getCode(),
                        "死信队列：邮件与站内消息多次重试后仍发送失败"
                    );
                } catch (Exception logEx) {
                    LogUtils.error("死信日志记录失败 - 预约ID:{}, 错误:{}", 
                        notificationDTO.getReservationId(), logEx.getMessage(), logEx);
                }
                
                // 预约状态保持PENDING，电影再次上线时可通过publishNewMovieOnline重新触发通知
                try {
                    LambdaUpdateWrapper<Reservation> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(Reservation::getId, notificationDTO.getReservationId())
                                .set(Reservation::getStatus, "PENDING");
                    reservationMapper.update(null, updateWrapper);
                } catch (Exception updateEx) {
                    LogUtils.error("死信处理回滚预约状态失败 - 预约ID:{}, 错误:{}", 
                        notificationDTO.getReservationId(), updateEx.getMessage(), updateEx);
                }
            }
        } catch (Exception e) {
            LogUtils.error("处理死信消息异常: {}", e.getMessage(), e);
        }
    }
}

