package com.home.Enum;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 站内消息类型枚举
 */
public enum MessageTypeEnum {
    
    MOVIE_ONLINE("MOVIE_ONLINE", "电影上线通知"),
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知"),
    RESERVATION_REMINDER("RESERVATION_REMINDER", "预约提醒");

    private final String code;
    private final String description;

    MessageTypeEnum(String code, String description) {
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
    public static MessageTypeEnum fromCode(String code) {
        for (MessageTypeEnum type : MessageTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的消息类型: " + code);
    }
}

