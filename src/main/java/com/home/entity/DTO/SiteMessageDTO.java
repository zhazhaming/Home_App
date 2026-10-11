package com.home.entity.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 站内消息DTO
 */
@ApiModel(value = "SiteMessageDTO", description = "站内消息数据传输对象")
public class SiteMessageDTO {

    @ApiModelProperty("消息ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private String userId;

    @ApiModelProperty("消息类型")
    private String messageType;

    @ApiModelProperty("消息标题")
    private String title;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("关联的电影ID")
    private Integer movieId;

    @ApiModelProperty("电影标题")
    private String movieTitle;

    @ApiModelProperty("是否已读")
    private Boolean isRead;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("阅读时间")
    private LocalDateTime readAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    @Override
    public String toString() {
        return "SiteMessageDTO{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                ", readAt=" + readAt +
                '}';
    }
}

