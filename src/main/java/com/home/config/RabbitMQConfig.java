package com.home.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zhazhaming
 * @Date: 2025/09/07
 */
@Configuration
public class RabbitMQConfig {

    public static final String RESERVATION_EXCHANGE = "movie.reservation.exchange";
    public static final String RESERVATION_QUEUE = "movie.reservation.queue";
    public static final String RESERVATION_ROUTING_KEY = "movie.reservation.key";

    public static final String CANCEL_RESERVATION_EXCHANGE = "movie.cancel.reservation.exchange";
    public static final String CANCEL_RESERVATION_QUEUE = "movie.cancel.reservation.queue";
    public static final String CANCEL_RESERVATION_ROUTING_KEY = "movie.cancel.reservation.key";

    // 通知队列配置
    public static final String NOTIFICATION_EXCHANGE = "movie.notification.exchange";
    public static final String NOTIFICATION_QUEUE = "movie.notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "movie.notification.key";
    
    // 通知死信队列（用于失败重试）
    public static final String NOTIFICATION_DLQ = "movie.notification.dlq";
    public static final String NOTIFICATION_DLX = "movie.notification.dlx";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "movie.notification.dlq.key";

    @Bean
    public DirectExchange reservationExchange() {
        // 延迟到实际使用时再初始化（由容器管理，不影响连接建立时机）
        return new DirectExchange(RESERVATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue reservationQueue() {
        return new Queue(RESERVATION_QUEUE, true);
    }

    @Bean
    public Binding reservationBinding(Queue reservationQueue, DirectExchange reservationExchange) {
        return BindingBuilder.bind(reservationQueue).to(reservationExchange).with(RESERVATION_ROUTING_KEY);
    }

    @Bean
    public DirectExchange cancelReservationExchange() {
        return new DirectExchange(CANCEL_RESERVATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue cancelReservationQueue() {
        return new Queue(CANCEL_RESERVATION_QUEUE, true);
    }

    @Bean
    public Binding cancelReservationBinding(Queue cancelReservationQueue, DirectExchange cancelReservationExchange) {
        return BindingBuilder.bind(cancelReservationQueue).to(cancelReservationExchange).with(CANCEL_RESERVATION_ROUTING_KEY);
    }

    // ==================== 通知队列配置 ====================
    
    /**
     * 通知交换机
     */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /**
     * 通知队列（带死信队列配置）
     */
    @Bean
    public Queue notificationQueue() {
        // 配置死信队列参数
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-dead-letter-exchange", NOTIFICATION_DLX);
        args.put("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY);
        return new Queue(NOTIFICATION_QUEUE, true, false, false, args);
    }

    /**
     * 通知队列绑定
     */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 通知死信交换机
     */
    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    /**
     * 通知死信队列
     */
    @Bean
    public Queue notificationDlq() {
        return new Queue(NOTIFICATION_DLQ, true);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding notificationDlqBinding(Queue notificationDlq, DirectExchange notificationDlx) {
        return BindingBuilder.bind(notificationDlq).to(notificationDlx).with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }
}


