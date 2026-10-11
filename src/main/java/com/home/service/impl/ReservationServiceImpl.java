package com.home.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.config.NotificationConfig;
import com.home.config.RabbitMQConfig;
import com.home.entity.DTO.ReservationRequestDTO;
import com.home.entity.DTO.CancelReservationRequestDTO;
import com.home.entity.Movies;
import com.home.entity.Reservation;
import com.home.service.MovieService;
import com.home.service.ReservationService;
import com.home.service.ReservationAsyncConsumer;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import com.home.utils.QueueMonitor;
import com.home.utils.AsyncProcessor;
import com.home.utils.ResponseCache;
import com.home.mapper.ReservationMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.List;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.core.Message;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JsonSerialization jsonSerialization;

    @Autowired
    private MovieService movieService;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private ReservationAsyncConsumer asyncConsumer;

    @Autowired
    private QueueMonitor queueMonitor;

    @Autowired
    private AsyncProcessor asyncProcessor;

    @Autowired
    private ResponseCache responseCache;

    @Autowired
    private NotificationConfig notificationConfig;

    @Value("${feature.mq.enabled:true}")
    private boolean mqEnabled;

    @Value("${reservation.cache.ttl:300000}")
    private long cacheTtlMs; // 缓存5分钟

    @Override
    public boolean sendReservationMessage(ReservationRequestDTO request) throws Exception {
        // 校验：电影是否已存在（标题标准化 + 包含匹配）
        List<Movies> candidates = movieService.getMoviesByName(request.getTitle());
        boolean exists = false;
        String req = normalizeTitle(request.getTitle());
        if (candidates != null) {
            for (Movies m : candidates) {
                if (m != null && m.getName () != null) {
                    String db = normalizeTitle(m.getName());
                    if (!req.isEmpty() && (db.contains(req) || req.contains(db))) {
                        exists = true;
                        break;
                    }
                }
            }
        }
        if (exists) {
            LogUtils.info("电影已存在，跳过预约: {}", request.getTitle());
            return false;
        }
        // 获取用户id 和预约的电影名称
        String user_id = request.getUserId ();
        String movie_name = request.getTitle ();
        List<Reservation> reservationList = reservationMapper.selectList (new LambdaQueryWrapper<Reservation> (  ).eq (Reservation::getUserId, user_id));
        for (Reservation reservation : reservationList) {
            String movie_name_reservation = reservation.getTitle ();
            if (movie_name.equals (movie_name_reservation)){
                LogUtils.info("电影已预约，跳过预约: {}", request.getTitle());
                return false;
            }
        }
        // 数据库不存在|| 用户没有预约过，持久化预约记录
        Reservation reservation = new Reservation ();
        reservation.setTitle(request.getTitle());
        reservation.setNotifyEmail(request.getNotifyEmail());
        reservation.setUserId(request.getUserId());
        reservation.setUsername(request.getUsername());
        reservation.setStatus("PENDING");
        reservation.setCreatedAt(java.time.LocalDateTime.now());
        reservationMapper.insert(reservation);

        String body = jsonSerialization.serializeToJson(request);
        LogUtils.info("发送预约消息: {}", body);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVATION_EXCHANGE, RabbitMQConfig.RESERVATION_ROUTING_KEY, body);
        } catch (Exception ex) {
            LogUtils.error("MQ 发送失败，将仅持久化记录: {}", ex.getMessage());
        }
        return true;
    }

    @Override
    public boolean sendCancelReservationMessage(CancelReservationRequestDTO request) throws Exception {
        // 校验：预约是否存在且状态为PENDING
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, request.getUserId())
                   .eq(Reservation::getTitle, request.getTitle())
                   .eq(Reservation::getStatus, "PENDING");
        
        Reservation existingReservation = reservationMapper.selectOne(queryWrapper);
        if (existingReservation == null) {
            LogUtils.info("预约不存在或已取消，跳过取消操作: userId:{}, title:{}", request.getUserId(), request.getTitle());
            return false;
        }

        // 发送取消预约消息到MQ
        String body = jsonSerialization.serializeToJson(request);
        LogUtils.info("发送取消预约消息: {}", body);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.CANCEL_RESERVATION_EXCHANGE, RabbitMQConfig.CANCEL_RESERVATION_ROUTING_KEY, body);
        } catch (Exception ex) {
            LogUtils.error("MQ 发送失败，将直接更新数据库: {}", ex.getMessage());
            // MQ发送失败时，直接更新数据库
            LambdaUpdateWrapper<Reservation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Reservation::getId, existingReservation.getId())
                        .set(Reservation::getStatus, "CANCELLED");
            reservationMapper.update(null, updateWrapper);
            LogUtils.info("直接更新数据库，取消预约成功: userId:{}, title:{}", request.getUserId(), request.getTitle());
        }
        return true;
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        // 保留中文、字母、数字，去掉其他符号与空白，转小写
        String cleaned = title.replaceAll("[^\\p{IsHan}A-Za-z0-9]+", "");
        return cleaned.toLowerCase();
    }


    @Override
    public Integer consumeReservationForTitle(String onlineTitle) throws Exception {
        LogUtils.info("开始处理电影上线通知: {}", onlineTitle);

        // 1. 检查缓存
        String cacheKey = "reservation_" + normalizeTitle(onlineTitle);
        Integer cachedResult = responseCache.get(cacheKey);
        if (cachedResult != null) {
            LogUtils.info("缓存命中，直接返回结果: {}", cachedResult);
            // 即使缓存命中，也要确保MQ数据被消费
            ensureMQConsumption(onlineTitle);
            return cachedResult;
        }

        // 2. 快速响应：立即返回0，异步处理
        LogUtils.info("启动异步处理，立即返回响应");
        
        // 异步处理预约消息
        asyncProcessor.processReservationAsync(onlineTitle, this::processReservationSync)
            .thenAccept(result -> {
                // 缓存结果
                responseCache.put(cacheKey, result, cacheTtlMs);
                LogUtils.info("异步处理完成，结果已缓存: {}", result);
            })
            .exceptionally(throwable -> {
                LogUtils.error("异步处理失败: {}", throwable.getMessage());
                return null;
            });
        // 立即返回0，表示已开始处理
        return 0;
    }


    /**
     * 获取用户预约记录（带分页信息）
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public List<Reservation> getReservationPageById(Integer userId, Integer page, Integer pageSize) throws Exception {
        // 使用MyBatis-Plus分页插件
        Page<Reservation> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId)
                   .orderByDesc(Reservation::getCreatedAt); // 按创建时间倒序
        
        Page<Reservation> result = (Page<Reservation>) reservationMapper.selectPage(pageParam, queryWrapper);
        
        LogUtils.info("查询用户预约记录，用户ID: {}, 页码: {}, 页大小: {}, 总记录数: {}, 总页数: {}", 
            userId, page, pageSize, result.getTotal(), result.getPages());
        
        return result.getRecords();
    }

    @Override
    public Integer getTotalReservation(Integer userId) {
        List<Reservation> reservationList = reservationMapper.selectList (new LambdaQueryWrapper<Reservation> ( ).eq (Reservation::getUserId, userId));
        return reservationList.size ();
    }

    @Override
    public Integer sendMovieOnlineNotifications(String title, Integer movieId) throws Exception {
        LogUtils.info("开始发送电影上线通知 - 标题:{}, 电影ID:{}", title, movieId);
        
        try {
            // 1. 查询所有待通知的预约记录（status='PENDING'且标题匹配）
            String normalizedTitle = normalizeTitle(title);
            LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Reservation::getStatus, "PENDING");
            List<Reservation> pendingReservations = reservationMapper.selectList(queryWrapper);
            
            int sent = 0;
            
            // 2. 遍历每个预约，构建通知DTO并发送到MQ
            for (Reservation reservation : pendingReservations) {
                String reservationTitle = normalizeTitle(reservation.getTitle());
                
                // 标题匹配检查
                if (reservationTitle.equals(normalizedTitle) || 
                    reservationTitle.contains(normalizedTitle) || 
                    normalizedTitle.contains(reservationTitle)) {
                    
                    try {
                        // 构建通知DTO
                        com.home.entity.DTO.ReservationNotificationDTO notificationDTO = 
                            new com.home.entity.DTO.ReservationNotificationDTO();
                        
                        notificationDTO.setReservationId(reservation.getId());
                        notificationDTO.setTitle(title); // 使用上线电影的真实标题
                        notificationDTO.setMovieId(movieId);
                        notificationDTO.setNotifyEmail(reservation.getNotifyEmail());
                        notificationDTO.setUserId(reservation.getUserId());
                        notificationDTO.setUsername(reservation.getUsername());
                        notificationDTO.setReservationDate(reservation.getCreatedAt());
                        
                        // 生成跳转链接（需要注入NotificationConfig）
                        String actionUrl = buildMovieDetailUrl(movieId);
                        notificationDTO.setActionUrl(actionUrl);
                        
                        // 更新预约记录的movieId
                        LambdaUpdateWrapper<Reservation> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(Reservation::getId, reservation.getId())
                                    .set(Reservation::getMovieId, movieId);
                        reservationMapper.update(null, updateWrapper);
                        
                        // 发送到RabbitMQ通知队列
                        String message = jsonSerialization.serializeToJson(notificationDTO);
                        rabbitTemplate.convertAndSend(
                            RabbitMQConfig.NOTIFICATION_EXCHANGE,
                            RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                            message
                        );
                        
                        sent++;
                        LogUtils.info("已发送通知到队列 - 预约ID:{}, 用户:{}, 电影:{}", 
                            reservation.getId(), reservation.getUsername(), title);
                            
                    } catch (Exception e) {
                        LogUtils.error("发送通知失败 - 预约ID:{}, 错误:{}", 
                            reservation.getId(), e.getMessage(), e);
                    }
                }
            }
            
            LogUtils.info("电影上线通知发送完成 - 标题:{}, 发送数量:{}", title, sent);
            return sent;
            
        } catch (Exception e) {
            LogUtils.error("发送电影上线通知异常 - 标题:{}, 错误:{}", title, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 构建电影详情页URL
     */
    private String buildMovieDetailUrl(Integer movieId) {
        // 使用notification.frontend配置生成跳转链接
        return notificationConfig.getFrontend().buildMovieDetailUrl(movieId);
    }

    /**
     * 同步处理预约消息（供异步调用）
     */
    private int processReservationSync(String onlineTitle) throws Exception {
        if (!mqEnabled) {
            LogUtils.warn("MQ未启用，使用数据库直接查询");
            return processFromDatabase(onlineTitle);
        }

        // 检查队列健康状态
        if (!queueMonitor.isQueueHealthy()) {
            LogUtils.warn("队列不健康，使用数据库直接查询");
            return processFromDatabase(onlineTitle);
        }

        // 设置异步消费者目标电影
        asyncConsumer.setTargetMovie(onlineTitle);

        try {
            // 真正消费MQ数据
            int processed = consumeMQMessages(onlineTitle);
            LogUtils.info("异步处理完成，共处理 {} 条匹配消息", processed);
            return processed;
            
        } finally {
            // 清理目标电影设置
            asyncConsumer.clearTargetMovie(onlineTitle);
        }
    }

    /**
     * 消费MQ消息
     */
    private int consumeMQMessages(String onlineTitle) throws Exception {
        String target = normalizeTitle(onlineTitle);
        int processed = 0;
        int consumed = 0;
        
        LogUtils.info("开始消费MQ消息，目标电影: {}", onlineTitle);
        
        try {
            // 限制消费数量，避免无限循环
            final int MAX_CONSUME_COUNT = 500; // 最多消费500条消息
            final long MAX_CONSUME_TIME = 30000; // 最多消费30秒
            
            long startTime = System.currentTimeMillis();
            Message msg;
            
            while ((msg = rabbitTemplate.receive(RabbitMQConfig.RESERVATION_QUEUE, 1000)) != null 
                   && consumed < MAX_CONSUME_COUNT
                   && (System.currentTimeMillis() - startTime) < MAX_CONSUME_TIME) {
                
                consumed++;
                String body = new String(msg.getBody(), StandardCharsets.UTF_8);
                ReservationRequestDTO dto = jsonSerialization.deserializeFromJson(body, ReservationRequestDTO.class);
                
                if (dto != null) {
                    String req = normalizeTitle(dto.getTitle());
                    if (!target.isEmpty() && (req.contains(target) || target.contains(req))) {
                        // 匹配：更新数据库状态
                        LambdaUpdateWrapper<Reservation> uw = new LambdaUpdateWrapper<>();
                        uw.eq(Reservation::getTitle, dto.getTitle())
                          .eq(Reservation::getStatus, "PENDING")
                          .set(Reservation::getStatus, "NOTIFIED");
                        processed += reservationMapper.update(null, uw);
                        LogUtils.info("处理匹配的预约消息: {}", dto.getTitle());
                    } else {
                        // 不匹配：重新发送到队列
                        rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVATION_EXCHANGE, 
                            RabbitMQConfig.RESERVATION_ROUTING_KEY, body);
                    }
                }
            }
            
            LogUtils.info("MQ消费完成，消费了 {} 条消息，处理了 {} 条匹配消息", consumed, processed);
            
        } catch (Exception ex) {
            LogUtils.error("消费MQ消息失败: {}", ex.getMessage());
        }
        
        return processed;
    }

    /**
     * 确保MQ数据被消费（即使缓存命中）
     */
    private void ensureMQConsumption(String onlineTitle) {
        try {
            // 检查是否有正在处理的任务
            String processingKey = "processing_" + normalizeTitle(onlineTitle);
            if (responseCache.get(processingKey) != null) {
                LogUtils.info("已有任务正在处理: {}", onlineTitle);
                return;
            }
            
            // 标记正在处理
            responseCache.put(processingKey, 1, 60000); // 1分钟过期
            
            // 异步处理MQ数据
            asyncProcessor.processReservationAsync(onlineTitle, this::processReservationSync)
                .thenAccept(result -> {
                    LogUtils.info("确保MQ消费完成: {}, 结果: {}", onlineTitle, result);
                })
                .exceptionally(throwable -> {
                    LogUtils.error("确保MQ消费失败: {}", throwable.getMessage());
                    return null;
                })
                .whenComplete((result, throwable) -> {
                    // 清理处理标记
                    responseCache.cleanExpired();
                });
                
        } catch (Exception ex) {
            LogUtils.error("确保MQ消费异常: {}", ex.getMessage());
        }
    }

    /**
     * 从数据库直接处理（备用方案）
     */
    private int processFromDatabase(String onlineTitle) {
        String target = normalizeTitle(onlineTitle);
        int processed = 0;

        LambdaQueryWrapper<Reservation> qw = new LambdaQueryWrapper<>();
        qw.eq(Reservation::getStatus, "PENDING");
        List<Reservation> pendingReservations = reservationMapper.selectList(qw);
        
        if (pendingReservations != null && !pendingReservations.isEmpty()) {
            LogUtils.info("从数据库找到 {} 条待处理预约", pendingReservations.size());
            
            for (Reservation reservation : pendingReservations) {
                String req = normalizeTitle(reservation.getTitle());
                if (!target.isEmpty() && (req.contains(target) || target.contains(req))) {
                    LambdaUpdateWrapper<Reservation> uw = new LambdaUpdateWrapper<>();
                    uw.eq(Reservation::getId, reservation.getId())
                      .set(Reservation::getStatus, "NOTIFIED");
                    
                    int updateResult = reservationMapper.update(null, uw);
                    if (updateResult > 0) {
                        processed++;
                        LogUtils.info("通知用户: {} 关于电影: {}", 
                            reservation.getUsername(), reservation.getTitle());
                    }
                }
            }
        }

        LogUtils.info("数据库处理完成，共通知 {} 个用户", processed);
        return processed;
    }
}



