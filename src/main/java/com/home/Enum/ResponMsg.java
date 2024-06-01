package com.home.Enum;

public enum ResponMsg {

    Success(200,true,"请求成功"),
    USER_LOGIN_SUCCESS(200,true,"登录成功"),
    USER_REGISTER_SUCCESS(200,true,"注册成功"),
    USER_LOGIN_NULL(200,true,"未查询到用户"),
    Error(500,false,"请求失败，请重试！！"),
    PARAMETER_ERROR(501,false,"参数异常，请正确填写后重试！！"),
    ARTICLE_PAGESIZE_ERROR(505,false,"文章分页设置错误，请重试！！"),
    ARTICLE_ID_ERROR(506,false,"文章id错误，请重试！！"),
    VOLUNTEER_PAGESIZE_ERROR(507,false,"志愿者分页设置错误，请重试！！"),
    VOLUNTEER_ID_ERROR(508,false,"志愿者id错误，请重试！！"),
    VOLUNTEER_REGION_ERROR(509,false,"志愿者所在地区错误，请重试！！"),
    VOLUNTEER_REQUSET_ERROR(512,false,"志愿者远程请求错误，请联系管理员"),
    ADMIN_LOGIN_PARAMERROR(510,false,"管理员名称和密码不能为空，请重试！！"),
    ADMIN_NOT_EXIT_ERROR(511,false,"管理员不存在或密码错误"),
    USER_LOGIN_ERROR(512,false,"用户名或者密码错误"),
    USER_REGISTER_ERROR(513,false,"注册失败,请联系管理员"),
    USER_UPDATA_SCORE_ERROR(514,false,"加积分失败"),;




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
