package com.home.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 通知发送日志实体类
 */
@ApiModel(value = "NotificationLog", description = "通知发送日志实体")
@TableName("notification_log")
public class NotificationLog {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("预约记录ID")
    @TableField("reservation_id")
    private Long reservationId;

    @ApiModelProperty("用户ID")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty("通知类型: EMAIL-邮件/SITE_MESSAGE-站内消息")
    @TableField("notification_type")
    private String notificationType;

    @ApiModelProperty("通知状态: SUCCESS-成功/FAILED-失败/RETRY-重试中")
    @TableField("status")
    private String status;

    @ApiModelProperty("失败原因/错误信息")
    @TableField("error_message")
    private String errorMessage;

    @ApiModelProperty("重试次数")
    @TableField("retry_count")
    private Integer retryCount;

    @ApiModelProperty("发送时间")
    @TableField("sent_at")
    private LocalDateTime sentAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}

