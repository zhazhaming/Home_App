package com.home.utils;

import com.home.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 队列监控工具类
 * 负责监控RabbitMQ队列状态和健康检查
 */
@Component
public class QueueMonitor {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final AtomicBoolean isHealthy = new AtomicBoolean(true);

    /**
     * 获取队列消息数量（估算）
     * @return 队列消息数量，获取失败返回-1
     */
    public int getQueueMessageCount() {
        try {
            // 通过尝试接收消息来估算队列大小
            int estimatedCount = 0;
            int maxCheck = 5; // 只检查5条消息来估算
            
            for (int i = 0; i < maxCheck; i++) {
                Message msg = rabbitTemplate.receive (RabbitMQConfig.RESERVATION_QUEUE, 100);
                if (msg != null) {
                    // 重新发送消息回队列
                    String body = new String(msg.getBody());
                    rabbitTemplate.convertAndSend(RabbitMQConfig.RESERVATION_EXCHANGE, 
                        RabbitMQConfig.RESERVATION_ROUTING_KEY, body);
                    estimatedCount++;
                } else {
                    break;
                }
            }
            
            if (estimatedCount > 0) {
                // 如果前5条都有消息，说明队列可能很大
                return estimatedCount >= maxCheck ? estimatedCount * 10 : estimatedCount;
            } else {
                return 0;
            }
            
        } catch (Exception ex) {
            LogUtils.warn("获取队列消息数量失败: {}", ex.getMessage());
            return -1;
        }
    }

    /**
     * 检查队列健康状态
     * @return true表示健康，false表示异常
     */
    public boolean isQueueHealthy() {
        try {
            int queueSize = getQueueMessageCount();
            if (queueSize > 10000) {
                LogUtils.warn("队列积压严重，消息数量: {}", queueSize);
                isHealthy.set(false);
                return false;
            }
            isHealthy.set(true);
            return true;
        } catch (Exception ex) {
            LogUtils.error("检查队列健康状态失败: {}", ex.getMessage());
            isHealthy.set(false);
            return false;
        }
    }

    /**
     * 获取队列统计信息
     */
    public QueueStats getQueueStats() {
        return new QueueStats(
            getQueueMessageCount(),
            isHealthy.get()
        );
    }

    /**
     * 队列统计信息
     */
    public static class QueueStats {
        private final int currentSize;
        private final boolean isHealthy;

        public QueueStats(int currentSize, boolean isHealthy) {
            this.currentSize = currentSize;
            this.isHealthy = isHealthy;
        }

        public int getCurrentSize() { return currentSize; }
        public boolean isHealthy() { return isHealthy; }

        @Override
        public String toString() {
            return String.format("QueueStats{currentSize=%d, isHealthy=%s}", currentSize, isHealthy);
        }
    }
}
