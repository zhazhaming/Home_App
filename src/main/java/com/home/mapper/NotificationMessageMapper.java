package com.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.home.entity.NotificationMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 站内消息Mapper接口
 */
@Mapper
public interface NotificationMessageMapper extends BaseMapper<NotificationMessage> {

    /**
     * 分页查询用户消息列表
     * @param userId 用户ID
     * @param isRead 是否已读（null查询全部）
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 消息列表
     */
    List<NotificationMessage> selectUserMessages(@Param("userId") String userId,
                                                  @Param("isRead") Integer isRead,
                                                  @Param("offset") Integer offset,
                                                  @Param("pageSize") Integer pageSize);

    /**
     * 查询用户未读消息数量
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer countUnreadMessages(@Param("userId") String userId);

    /**
     * 标记消息为已读
     * @param id 消息ID
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer markAsRead(@Param("id") Long id, @Param("userId") String userId);

    /**
     * 标记用户所有消息为已读
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer markAllAsRead(@Param("userId") String userId);

    /**
     * 删除消息（逻辑删除）
     * @param id 消息ID
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer deleteMessage(@Param("id") Long id, @Param("userId") String userId);

    /**
     * 查询用户消息总数
     * @param userId 用户ID
     * @param isRead 是否已读（null查询全部）
     * @return 总数
     */
    Integer countUserMessages(@Param("userId") String userId, @Param("isRead") Integer isRead);
}

