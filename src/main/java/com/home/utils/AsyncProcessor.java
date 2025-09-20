package com.home.utils;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 异步处理工具类
 * 负责将耗时操作异步化，提升接口响应速度
 */
@Component
public class AsyncProcessor {

    /**
     * 异步处理预约消息
     * @param movieTitle 电影标题
     * @param processor 处理函数
     * @return Future结果
     */
    @Async("taskExecutor")
    public CompletableFuture<Integer> processReservationAsync(String movieTitle, ReservationProcessor processor) {
        try {
            LogUtils.info("开始异步处理预约消息: {}", movieTitle);
            int result = processor.process(movieTitle);
            LogUtils.info("异步处理完成: {}, 结果: {}", movieTitle, result);
            return CompletableFuture.completedFuture(result);
        } catch (Exception ex) {
            LogUtils.error("异步处理失败: {}, 错误: {}", movieTitle, ex.getMessage());
            return CompletableFuture.completedFuture(0);
        }
    }

    /**
     * 预约处理接口
     */
    @FunctionalInterface
    public interface ReservationProcessor {
        int process(String movieTitle) throws Exception;
    }
}
