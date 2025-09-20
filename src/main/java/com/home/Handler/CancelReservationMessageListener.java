package com.home.Handler;

import com.home.config.RabbitMQConfig;
import com.home.entity.DTO.CancelReservationRequestDTO;
import com.home.entity.Reservation;
import com.home.mapper.ReservationMapper;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: zhazhaming
 * @Date: 2025/01/08
 */
@Component
@org.springframework.context.annotation.Lazy
public class CancelReservationMessageListener {

    @Autowired
    private JsonSerialization jsonSerialization;

    @Autowired
    private ReservationMapper reservationMapper;

    @RabbitListener(queues = RabbitMQConfig.CANCEL_RESERVATION_QUEUE)
    public void onCancelReservationMessage(String message) throws Exception {
        LogUtils.info("接收取消预约消息: {}", message);
        
        try {
            CancelReservationRequestDTO dto = jsonSerialization.deserializeFromJson(message, CancelReservationRequestDTO.class);
            
            if (dto == null) {
                LogUtils.error("取消预约消息解析失败，消息内容: {}", message);
                return;
            }
            
            LogUtils.info("处理用户取消预约: userId:{}, username:{}, title:{}", 
                dto.getUserId(), dto.getUsername(), dto.getTitle());
            
            // 更新数据库状态为CANCELLED
            LambdaUpdateWrapper<Reservation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Reservation::getUserId, dto.getUserId())
                        .eq(Reservation::getTitle, dto.getTitle())
                        .eq(Reservation::getStatus, "PENDING")
                        .set(Reservation::getStatus, "CANCELLED");
            
            int updateResult = reservationMapper.update(null, updateWrapper);
            
            if (updateResult > 0) {
                LogUtils.info("取消预约成功: userId:{}, title:{}, 影响行数:{}", 
                    dto.getUserId(), dto.getTitle(), updateResult);
            } else {
                LogUtils.warn("取消预约失败，可能预约不存在或已取消: userId:{}, title:{}", 
                    dto.getUserId(), dto.getTitle());
            }
            
        } catch (Exception e) {
            LogUtils.error("处理取消预约消息失败: {}, 错误: {}", message, e.getMessage(), e);
        }
    }
}
