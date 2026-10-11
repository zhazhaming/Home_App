package com.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.home.entity.NotificationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知日志Mapper接口
 */
@Mapper
public interface NotificationLogMapper extends BaseMapper<NotificationLog> {

    /**
     * 根据预约ID查询通知日志
     * @param reservationId 预约ID
     * @return 日志列表
     */
    List<NotificationLog> selectByReservationId(@Param("reservationId") Long reservationId);

    /**
     * 根据用户ID查询通知日志
     * @param userId 用户ID
     * @param limit 查询数量限制
     * @return 日志列表
     */
    List<NotificationLog> selectByUserId(@Param("userId") String userId, @Param("limit") Integer limit);

    /**
     * 更新重试次数
     * @param id 日志ID
     * @param retryCount 重试次数
     * @return 影响行数
     */
    Integer updateRetryCount(@Param("id") Long id, @Param("retryCount") Integer retryCount);

    /**
     * 查询失败的通知日志（用于重试）
     * @param maxRetryCount 最大重试次数
     * @param limit 查询数量限制
     * @return 日志列表
     */
    List<NotificationLog> selectFailedLogs(@Param("maxRetryCount") Integer maxRetryCount, 
                                           @Param("limit") Integer limit);
}

