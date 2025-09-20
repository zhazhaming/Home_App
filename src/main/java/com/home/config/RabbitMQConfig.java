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

    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }
}


