package com.home.Handler;

import com.home.config.RabbitMQConfig;
import com.home.entity.DTO.ReservationRequestDTO;
import com.home.utils.JsonSerialization;
import com.home.utils.LogUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@org.springframework.context.annotation.Lazy
public class ReservationMessageListener {

    @Autowired
    private JsonSerialization jsonSerialization;

    // 当有新电影上线时，可调用业务方法触发消息消费（此处直接监听队列）
    @RabbitListener(queues = RabbitMQConfig.RESERVATION_QUEUE)
    public void onReservationMessage(String message) throws Exception {
        LogUtils.info("接收预约消息: {}", message);
        ReservationRequestDTO dto = jsonSerialization.deserializeFromJson(message, ReservationRequestDTO.class);
        // TODO: 在这里实现实际的通知逻辑，例如发送邮件或站内信
        LogUtils.info("为用户:{}(id:{}) 订阅电影标题:{} 邮箱:{}", dto.getUsername(), dto.getUserId(), dto.getTitle(), dto.getNotifyEmail());
    }
}


