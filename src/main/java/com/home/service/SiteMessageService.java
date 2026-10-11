package com.home.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.home.entity.NotificationMessage;
import com.home.entity.DTO.MessageListResponseDTO;
import com.home.entity.DTO.SiteMessageDTO;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 站内消息服务接口
 */
public interface SiteMessageService extends IService<NotificationMessage> {

    /**
     * 创建电影上线通知消息
     * @param userId 用户ID
     * @param username 用户名
     * @param movieId 电影ID
     * @param movieTitle 电影标题
     * @return 是否创建成功
     */
    boolean createMovieOnlineMessage(String userId, String username, Integer movieId, String movieTitle);

    /**
     * 分页查询用户消息列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页数量
     * @param isRead 是否已读（null查询全部）
     * @return 消息列表响应
     */
    MessageListResponseDTO getUserMessages(String userId, Integer page, Integer pageSize, Integer isRead);

    /**
     * 获取用户未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(String userId);

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, String userId);

    /**
     * 标记所有消息为已读
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(String userId);

    /**
     * 删除消息
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, String userId);

    /**
     * 获取消息详情
     * @param messageId 消息ID
     * @param userId 用户ID
     * @return 消息DTO
     */
    SiteMessageDTO getMessageDetail(Long messageId, String userId);
}

