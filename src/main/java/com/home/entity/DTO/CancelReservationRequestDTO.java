package com.home.entity.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author: zhazhaming
 * @Date: 2025/01/08
 */
@ApiModel(value = "CancelReservationRequestDTO", description = "取消电影预约请求DTO")
public class CancelReservationRequestDTO {

    @ApiModelProperty(value = "电影标题", required = true)
    private String title;

    @ApiModelProperty(value = "用户ID", required = true)
    private String userId;

    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
