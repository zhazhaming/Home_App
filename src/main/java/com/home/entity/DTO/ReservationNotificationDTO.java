package com.home.entity.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 预约通知DTO，用于RabbitMQ消息传递
 */
@ApiModel(value = "ReservationNotificationDTO", description = "预约通知数据传输对象")
public class ReservationNotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("预约记录ID")
    private Long reservationId;

    @ApiModelProperty("电影标题")
    private String title;

    @ApiModelProperty("电影ID（上线后的真实ID）")
    private Integer movieId;

    @ApiModelProperty("通知邮箱")
    private String notifyEmail;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("电影海报URL")
    private String moviePosterUrl;

    @ApiModelProperty("下载/观看链接")
    private String actionUrl;

    @ApiModelProperty("预约时间")
    private LocalDateTime reservationDate;

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getNotifyEmail() {
        return notifyEmail;
    }

    public void setNotifyEmail(String notifyEmail) {
        this.notifyEmail = notifyEmail;
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

    public String getMoviePosterUrl() {
        return moviePosterUrl;
    }

    public void setMoviePosterUrl(String moviePosterUrl) {
        this.moviePosterUrl = moviePosterUrl;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    @Override
    public String toString() {
        return "ReservationNotificationDTO{" +
                "reservationId=" + reservationId +
                ", title='" + title + '\'' +
                ", movieId=" + movieId +
                ", notifyEmail='" + notifyEmail + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", moviePosterUrl='" + moviePosterUrl + '\'' +
                ", actionUrl='" + actionUrl + '\'' +
                ", reservationDate=" + reservationDate +
                '}';
    }
}

