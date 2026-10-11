package com.home.Enum;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知类型枚举
 */
public enum NotificationTypeEnum {
    
    EMAIL("EMAIL", "邮件通知"),
    SITE_MESSAGE("SITE_MESSAGE", "站内消息");

    private final String code;
    private final String description;

    NotificationTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static NotificationTypeEnum fromCode(String code) {
        for (NotificationTypeEnum type : NotificationTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的通知类型: " + code);
    }
}

