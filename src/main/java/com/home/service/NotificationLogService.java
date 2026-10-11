package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.NotificationLog;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知日志服务接口
 */
public interface NotificationLogService extends IService<NotificationLog> {

    /**
     * 记录通知日志
     * @param reservationId 预约ID
     * @param userId 用户ID
     * @param notificationType 通知类型
     * @param status 通知状态
     * @param errorMessage 错误信息（可选）
     * @return 是否成功
     */
    boolean logNotification(Long reservationId, String userId, String notificationType, 
                           String status, String errorMessage);

    /**
     * 根据预约ID查询通知日志
     * @param reservationId 预约ID
     * @return 日志列表
     */
    List<NotificationLog> getLogsByReservationId(Long reservationId);

    /**
     * 根据用户ID查询最近的通知日志
     * @param userId 用户ID
     * @param limit 查询数量限制
     * @return 日志列表
     */
    List<NotificationLog> getRecentLogsByUserId(String userId, Integer limit);

    /**
     * 更新重试次数
     * @param logId 日志ID
     * @param retryCount 重试次数
     * @return 是否成功
     */
    boolean updateRetryCount(Long logId, Integer retryCount);

    /**
     * 查询失败的通知日志（用于重试）
     * @param maxRetryCount 最大重试次数
     * @param limit 查询数量限制
     * @return 日志列表
     */
    List<NotificationLog> getFailedLogs(Integer maxRetryCount, Integer limit);
}

