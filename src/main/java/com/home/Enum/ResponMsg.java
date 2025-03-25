package com.home.Enum;

public enum ResponMsg {

    Success(200,true,"请求成功"),
    USER_LOGIN_SUCCESS(200,true,"登录成功"),
    USER_REGISTER_SUCCESS(200,true,"注册成功"),
    USER_LOGIN_NULL(200,true,"未查询到用户"),
    USER_TOKEN_EXPIRED(401, false, "登录已过期"),
    USER_PASSWORD_FAIL(402, false, "用户密码错误"),
    Error(500,false,"请求失败，请重试！！"),
    PARAMETER_ERROR(501,false,"参数异常，请正确填写后重试！！"),
    USER_NOT_EXIST(502,false,"用户不存在或用户名密码错误，请检查后重试！！"),
    USER_IS_EXIST(503,false,"用户已存在，不允许再次注册！！"),
    USER_REGISTER_FAIL(504,false, "注册失败，请联系管理员！！"),
    SYS_ERROR(505,false,"系统异常，请联系管理员！！"),
    FILE_FORMAT_ERROR(506, false,"文件格式错误，请更换文件格式"),
    FLIE_EXCEED_LIMIT(507,false,"文件过大,超过规定大小"),
    FILE_NAME_EMPTY(508, false, "文件名不存在，请联系管理员"),
    FAIL_USER_LOGIN_EXPIRED(509, false, "用户未登录/登录信息过期，请重新登录");




    // 响应业务状态
    private Integer status;
    // 调用是否成功
    private Boolean success;
    // 响应消息，可以为成功或者失败的消息
    private String msg;

    ResponMsg(Integer status,Boolean success,String msg){
        this.status = status;
        this.success = success;
        this.msg = msg;
    }
    public Integer status(){
        return status;
    }
    public Boolean success(){
        return success;
    }
    public String msg(){
        return msg;
    }
}
