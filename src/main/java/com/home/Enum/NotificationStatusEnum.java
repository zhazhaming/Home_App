package com.home.Enum;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知状态枚举
 */
public enum NotificationStatusEnum {
    
    SUCCESS("SUCCESS", "发送成功"),
    FAILED("FAILED", "发送失败"),
    RETRY("RETRY", "重试中");

    private final String code;
    private final String description;

    NotificationStatusEnum(String code, String description) {
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
    public static NotificationStatusEnum fromCode(String code) {
        for (NotificationStatusEnum status : NotificationStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的通知状态: " + code);
    }
}

