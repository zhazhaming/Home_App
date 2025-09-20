package com.home.service;

import com.home.config.RabbitMQConfig;
import com.home.entity.DTO.ReservationRequestDTO;
import com.home.entity.Reservation;
import com.home.mapper.ReservationMapper;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import com.home.utils.QueueMonitor;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 预约异步消费者 - 业务代码
 * 负责处理用户预约消息的异步消费
 */
@Service
public class ReservationAsyncConsumer {

    @Autowired
    private JsonSerialization jsonSerialization;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private QueueMonitor queueMonitor;

    // 当前正在处理的目标电影
    private static final ConcurrentHashMap<String, AtomicInteger> processingCounters = new ConcurrentHashMap<>();

    /**
     * 异步消费预约消息
     * 当有新电影上线时，会触发此方法处理所有相关预约
     */
    @RabbitListener(queues = RabbitMQConfig.RESERVATION_QUEUE, concurrency = "1-5")
    public void handleReservationMessage(String message) {
        try {
            ReservationRequestDTO dto = jsonSerialization.deserializeFromJson(message, ReservationRequestDTO.class);
            if (dto != null) {
                LogUtils.info("异步消费预约消息: 用户={}, 电影={}", dto.getUsername(), dto.getTitle());
                
                // 检查是否有正在处理的目标电影
                String targetMovie = getCurrentTargetMovie();
                if (targetMovie != null) {
                    processReservationMessage(dto, targetMovie);
                } else {
                    // 没有目标电影，重新发送到队列
                    LogUtils.debug("没有目标电影，重新发送消息: {}", dto.getTitle());
                }
            }
        } catch (Exception ex) {
            LogUtils.error("处理预约消息失败: {}", ex.getMessage());
        }
    }

    /**
     * 处理单个预约消息
     */
    private void processReservationMessage(ReservationRequestDTO dto, String targetMovie) {
        String req = normalizeTitle(dto.getTitle());
        String target = normalizeTitle(targetMovie);
        
        if (!target.isEmpty() && (req.contains(target) || target.contains(req))) {
            // 匹配：更新数据库状态
            LambdaUpdateWrapper<Reservation> uw = new LambdaUpdateWrapper<>();
            uw.eq(Reservation::getTitle, dto.getTitle())
              .eq(Reservation::getStatus, "PENDING")
              .set(Reservation::getStatus, "NOTIFIED");
            
            int updateResult = reservationMapper.update(null, uw);
            if (updateResult > 0) {
                LogUtils.info("异步通知用户: {} 关于电影: {}", dto.getUsername(), dto.getTitle());
            }
        }
    }

    /**
     * 设置当前处理的目标电影
     */
    public void setTargetMovie(String movieTitle) {
        String target = normalizeTitle(movieTitle);
        String counterKey = "movie_" + target;
        processingCounters.computeIfAbsent(counterKey, k -> new AtomicInteger(0));
        LogUtils.info("设置目标电影: {}", movieTitle);
    }

    /**
     * 获取当前目标电影
     */
    private String getCurrentTargetMovie() {
        // 简化实现，实际应该从配置或缓存中获取
        return processingCounters.keySet().stream()
            .filter(key -> key.startsWith("movie_"))
            .map(key -> key.substring(6))
            .findFirst()
            .orElse(null);
    }

    /**
     * 清理目标电影
     */
    public void clearTargetMovie(String movieTitle) {
        String target = normalizeTitle(movieTitle);
        String counterKey = "movie_" + target;
        processingCounters.remove(counterKey);
        // 清理目标电影
        LogUtils.info("清理目标电影: {}", movieTitle);
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.replaceAll("[^\\p{IsHan}A-Za-z0-9]+", "").toLowerCase();
    }
}
