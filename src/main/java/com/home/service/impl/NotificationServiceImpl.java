package com.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.home.Enum.NotificationStatusEnum;
import com.home.Enum.NotificationTypeEnum;
import com.home.entity.DTO.ReservationNotificationDTO;
import com.home.entity.Reservation;
import com.home.service.EmailService;
import com.home.service.NotificationLogService;
import com.home.service.NotificationService;
import com.home.service.SiteMessageService;
import com.home.utils.LogUtils;
import com.home.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知服务实现类（门面模式）
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SiteMessageService siteMessageService;

    @Autowired
    private NotificationLogService notificationLogService;

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendNotification(ReservationNotificationDTO notificationDTO) {
        LogUtils.info("开始发送完整通知 - 预约ID:{}, 用户:{}, 电影:{}", 
            notificationDTO.getReservationId(), 
            notificationDTO.getUsername(), 
            notificationDTO.getTitle());

        boolean emailSuccess = false;
        boolean messageSuccess = false;

        try {
            // 1. 发送邮件通知
            emailSuccess = sendEmailNotification(notificationDTO);

            // 2. 发送站内消息
            messageSuccess = sendSiteMessageNotification(notificationDTO);

            // 3. 更新预约记录状态
            updateReservationStatus(notificationDTO.getReservationId(), emailSuccess, messageSuccess);

            // 4. 至少一种方式成功即认为通知成功
            boolean overallSuccess = emailSuccess || messageSuccess;
            
            if (overallSuccess) {
                LogUtils.info("通知发送完成 - 预约ID:{}, 邮件:{}, 站内:{}", 
                    notificationDTO.getReservationId(), 
                    emailSuccess ? "成功" : "失败", 
                    messageSuccess ? "成功" : "失败");
            } else {
                LogUtils.warn("通知发送全部失败 - 预约ID:{}", notificationDTO.getReservationId());
            }

            return overallSuccess;
        } catch (Exception e) {
            LogUtils.error("发送通知异常 - 预约ID:{}, 错误:{}", 
                notificationDTO.getReservationId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendEmailNotification(ReservationNotificationDTO notificationDTO) {
        try {
            // 发送邮件
            boolean success = emailService.sendMovieOnlineEmail(notificationDTO);
            
            // 记录日志
            String status = success ? NotificationStatusEnum.SUCCESS.getCode() : NotificationStatusEnum.FAILED.getCode();
            String errorMessage = success ? null : "邮件发送失败";
            
            notificationLogService.logNotification(
                notificationDTO.getReservationId(),
                notificationDTO.getUserId(),
                NotificationTypeEnum.EMAIL.getCode(),
                status,
                errorMessage
            );
            
            return success;
        } catch (Exception e) {
            LogUtils.error("邮件通知发送异常 - 预约ID:{}, 错误:{}", 
                notificationDTO.getReservationId(), e.getMessage(), e);
            
            // 记录失败日志
            notificationLogService.logNotification(
                notificationDTO.getReservationId(),
                notificationDTO.getUserId(),
                NotificationTypeEnum.EMAIL.getCode(),
                NotificationStatusEnum.FAILED.getCode(),
                e.getMessage()
            );
            
            return false;
        }
    }

    @Override
    public boolean sendSiteMessageNotification(ReservationNotificationDTO notificationDTO) {
        try {
            // 创建站内消息
            boolean success = siteMessageService.createMovieOnlineMessage(
                notificationDTO.getUserId(),
                notificationDTO.getUsername(),
                notificationDTO.getMovieId(),
                notificationDTO.getTitle()
            );
            
            // 记录日志
            String status = success ? NotificationStatusEnum.SUCCESS.getCode() : NotificationStatusEnum.FAILED.getCode();
            String errorMessage = success ? null : "站内消息创建失败";
            
            notificationLogService.logNotification(
                notificationDTO.getReservationId(),
                notificationDTO.getUserId(),
                NotificationTypeEnum.SITE_MESSAGE.getCode(),
                status,
                errorMessage
            );
            
            return success;
        } catch (Exception e) {
            LogUtils.error("站内消息通知发送异常 - 预约ID:{}, 错误:{}", 
                notificationDTO.getReservationId(), e.getMessage(), e);
            
            // 记录失败日志
            notificationLogService.logNotification(
                notificationDTO.getReservationId(),
                notificationDTO.getUserId(),
                NotificationTypeEnum.SITE_MESSAGE.getCode(),
                NotificationStatusEnum.FAILED.getCode(),
                e.getMessage()
            );
            
            return false;
        }
    }

    /**
     * 更新预约记录的通知状态
     */
    private void updateReservationStatus(Long reservationId, boolean emailSuccess, boolean messageSuccess) {
        try {
            LambdaUpdateWrapper<Reservation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Reservation::getId, reservationId)
                        .set(Reservation::getNotifiedAt, LocalDateTime.now())
                        .set(Reservation::getEmailSent, emailSuccess ? 1 : 0)
                        .set(Reservation::getMessageSent, messageSuccess ? 1 : 0)
                        .set(Reservation::getStatus, "NOTIFIED");
            
            int rows = reservationMapper.update(null, updateWrapper);
            
            if (rows > 0) {
                LogUtils.info("预约记录状态更新成功 - ID:{}, 邮件:{}, 站内:{}", 
                    reservationId, emailSuccess, messageSuccess);
            } else {
                LogUtils.warn("预约记录状态更新失败 - ID:{}", reservationId);
            }
        } catch (Exception e) {
            LogUtils.error("更新预约记录状态异常 - ID:{}, 错误:{}", reservationId, e.getMessage(), e);
        }
    }
}

