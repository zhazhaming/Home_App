package com.home.entity.DTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 消息列表分页响应DTO
 */
@ApiModel(value = "MessageListResponseDTO", description = "消息列表分页响应")
public class MessageListResponseDTO {

    @ApiModelProperty("消息列表")
    private List<SiteMessageDTO> messages;

    @ApiModelProperty("总记录数")
    private Integer total;

    @ApiModelProperty("当前页码")
    private Integer page;

    @ApiModelProperty("每页大小")
    private Integer pageSize;

    @ApiModelProperty("未读消息数量")
    private Integer unreadCount;

    public List<SiteMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<SiteMessageDTO> messages) {
        this.messages = messages;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public String toString() {
        return "MessageListResponseDTO{" +
                "messages=" + messages +
                ", total=" + total +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", unreadCount=" + unreadCount +
                '}';
    }
}

