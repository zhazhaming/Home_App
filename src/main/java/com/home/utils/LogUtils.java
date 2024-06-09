package com.home.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @Author: zhazhaming
 * @Date: 2024/06/08/15:21
 */

@Component
public class LogUtils {

    private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);

    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void error(String message, Object... args) {
        logger.error(message, args);
    }

    public static void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public static void setRequestId(String requestId) {
        MDC.put("requestId", requestId);
    }

    public static void clearRequestId() {
        MDC.clear();
    }
}
