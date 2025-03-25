package com.home.Enum;

/**
 * @Author: zhazhaming
 * @Date: 2024/10/06/11:57
 */
public enum ParameterEnum {

    PIC_PUBLIC(0, "用户图片发布"),
    PIC_NOT_PUBLIC(1,"用户图片不发布"),
    USER_LOGIN_EXPIRE_TIME_SHOT(60*60*24,"用户短效token过期时间"),  // 1day
    USER_LOGIN_EXPIRE_TIME_LONG(60*60*24*7,"用户长效token过期时间");   // 7day



    private Integer parameter;

    private String msg;

    public Integer getParameter() {
        return parameter;
    }

    public void setParameter(Integer parameter) {
        this.parameter = parameter;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    ParameterEnum(Integer parameter, String msg) {
        this.parameter = parameter;
        this.msg = msg;
    }
}
