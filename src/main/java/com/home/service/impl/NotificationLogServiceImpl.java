package com.home.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.entity.NotificationLog;
import com.home.mapper.NotificationLogMapper;
import com.home.service.NotificationLogService;
import com.home.utils.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知日志服务实现类
 */
@Service
public class NotificationLogServiceImpl extends ServiceImpl<NotificationLogMapper, NotificationLog> 
        implements NotificationLogService {

    @Autowired
    private NotificationLogMapper notificationLogMapper;

    @Override
    public boolean logNotification(Long reservationId, String userId, String notificationType, 
                                   String status, String errorMessage) {
        try {
            NotificationLog log = new NotificationLog();
            log.setReservationId(reservationId);
            log.setUserId(userId);
            log.setNotificationType(notificationType);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setRetryCount(0);
            log.setSentAt(LocalDateTime.now());
            
            boolean success = this.save(log);
            if (success) {
                LogUtils.info("通知日志记录成功 - 预约ID:{}, 类型:{}, 状态:{}", 
                    reservationId, notificationType, status);
            }
            return success;
        } catch (Exception e) {
            LogUtils.error("记录通知日志失败 - 预约ID:{}, 错误:{}", reservationId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<NotificationLog> getLogsByReservationId(Long reservationId) {
        try {
            return notificationLogMapper.selectByReservationId(reservationId);
        } catch (Exception e) {
            LogUtils.error("查询预约通知日志失败 - 预约ID:{}, 错误:{}", reservationId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<NotificationLog> getRecentLogsByUserId(String userId, Integer limit) {
        try {
            return notificationLogMapper.selectByUserId(userId, limit);
        } catch (Exception e) {
            LogUtils.error("查询用户通知日志失败 - 用户ID:{}, 错误:{}", userId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean updateRetryCount(Long logId, Integer retryCount) {
        try {
            Integer rows = notificationLogMapper.updateRetryCount(logId, retryCount);
            return rows > 0;
        } catch (Exception e) {
            LogUtils.error("更新重试次数失败 - 日志ID:{}, 错误:{}", logId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<NotificationLog> getFailedLogs(Integer maxRetryCount, Integer limit) {
        try {
            return notificationLogMapper.selectFailedLogs(maxRetryCount, limit);
        } catch (Exception e) {
            LogUtils.error("查询失败日志失败 - 错误:{}", e.getMessage(), e);
            return null;
        }
    }
}

