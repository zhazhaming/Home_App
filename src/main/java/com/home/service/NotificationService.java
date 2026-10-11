package com.home.service;

import com.home.entity.DTO.ReservationNotificationDTO;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知服务接口（门面模式，整合邮件和站内消息）
 */
public interface NotificationService {

    /**
     * 发送完整通知（邮件+站内消息）
     * @param notificationDTO 通知信息
     * @return 是否成功（至少一种方式成功即返回true）
     */
    boolean sendNotification(ReservationNotificationDTO notificationDTO);

    /**
     * 仅发送邮件通知
     * @param notificationDTO 通知信息
     * @return 是否成功
     */
    boolean sendEmailNotification(ReservationNotificationDTO notificationDTO);

    /**
     * 仅发送站内消息
     * @param notificationDTO 通知信息
     * @return 是否成功
     */
    boolean sendSiteMessageNotification(ReservationNotificationDTO notificationDTO);
}

