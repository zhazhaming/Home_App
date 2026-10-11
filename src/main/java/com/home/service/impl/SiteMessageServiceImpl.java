package com.home.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.home.Enum.MessageTypeEnum;
import com.home.config.NotificationConfig;
import com.home.entity.NotificationMessage;
import com.home.entity.DTO.MessageListResponseDTO;
import com.home.entity.DTO.SiteMessageDTO;
import com.home.mapper.NotificationMessageMapper;
import com.home.service.SiteMessageService;
import com.home.utils.LogUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 站内消息服务实现类
 */
@Service
public class SiteMessageServiceImpl extends ServiceImpl<NotificationMessageMapper, NotificationMessage> 
        implements SiteMessageService {

    @Autowired
    private NotificationMessageMapper notificationMessageMapper;

    @Autowired
    private NotificationConfig notificationConfig;

    @Override
    public boolean createMovieOnlineMessage(String userId, String username, Integer movieId, String movieTitle) {
        try {
            NotificationMessage message = new NotificationMessage();
            message.setUserId(userId);
            message.setMessageType(MessageTypeEnum.MOVIE_ONLINE.getCode());
            message.setTitle("您预约的电影《" + movieTitle + "》已上线");
            message.setContent(String.format("尊敬的 %s，您预约的电影《%s》现已上线，快去观看吧！", username, movieTitle));
            message.setMovieId(movieId);
            message.setMovieTitle(movieTitle);
            message.setIsRead(0);
            message.setCreatedAt(LocalDateTime.now());
            
            boolean success = this.save(message);
            if (success) {
                LogUtils.info("站内消息创建成功 - 用户:{}, 电影:{}", userId, movieTitle);
            }
            return success;
        } catch (Exception e) {
            LogUtils.error("创建站内消息失败 - 用户:{}, 电影:{}, 错误:{}", userId, movieTitle, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public MessageListResponseDTO getUserMessages(String userId, Integer page, Integer pageSize, Integer isRead) {
        try {
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 查询消息列表
            List<NotificationMessage> messages = notificationMessageMapper.selectUserMessages(
                userId, isRead, offset, pageSize
            );
            
            // 查询总数
            Integer total = notificationMessageMapper.countUserMessages(userId, isRead);
            
            // 查询未读数量
            Integer unreadCount = notificationMessageMapper.countUnreadMessages(userId);
            
            // 转换为DTO
            List<SiteMessageDTO> messageDTOs = messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            // 构建响应
            MessageListResponseDTO response = new MessageListResponseDTO();
            response.setMessages(messageDTOs);
            response.setTotal(total);
            response.setPage(page);
            response.setPageSize(pageSize);
            response.setUnreadCount(unreadCount);
            
            return response;
        } catch (Exception e) {
            LogUtils.error("查询用户消息列表失败 - 用户:{}, 错误:{}", userId, e.getMessage(), e);
            return new MessageListResponseDTO();
        }
    }

    @Override
    public Integer getUnreadCount(String userId) {
        try {
            return notificationMessageMapper.countUnreadMessages(userId);
        } catch (Exception e) {
            LogUtils.error("查询未读消息数量失败 - 用户:{}, 错误:{}", userId, e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean markAsRead(Long messageId, String userId) {
        try {
            Integer rows = notificationMessageMapper.markAsRead(messageId, userId);
            boolean success = rows > 0;
            if (success) {
                LogUtils.info("消息已标记为已读 - 消息ID:{}, 用户:{}", messageId, userId);
            }
            return success;
        } catch (Exception e) {
            LogUtils.error("标记消息已读失败 - 消息ID:{}, 用户:{}, 错误:{}", messageId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean markAllAsRead(String userId) {
        try {
            Integer rows = notificationMessageMapper.markAllAsRead(userId);
            LogUtils.info("所有消息已标记为已读 - 用户:{}, 影响行数:{}", userId, rows);
            return rows > 0;
        } catch (Exception e) {
            LogUtils.error("标记所有消息已读失败 - 用户:{}, 错误:{}", userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteMessage(Long messageId, String userId) {
        try {
            Integer rows = notificationMessageMapper.deleteMessage(messageId, userId);
            boolean success = rows > 0;
            if (success) {
                LogUtils.info("消息已删除 - 消息ID:{}, 用户:{}", messageId, userId);
            }
            return success;
        } catch (Exception e) {
            LogUtils.error("删除消息失败 - 消息ID:{}, 用户:{}, 错误:{}", messageId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public SiteMessageDTO getMessageDetail(Long messageId, String userId) {
        try {
            NotificationMessage message = this.getById(messageId);
            if (message != null && message.getUserId().equals(userId)) {
                return convertToDTO(message);
            }
            return null;
        } catch (Exception e) {
            LogUtils.error("获取消息详情失败 - 消息ID:{}, 用户:{}, 错误:{}", messageId, userId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 实体转DTO
     */
    private SiteMessageDTO convertToDTO(NotificationMessage message) {
        SiteMessageDTO dto = new SiteMessageDTO();
        BeanUtils.copyProperties(message, dto);
        dto.setIsRead(message.getIsRead() == 1);
        return dto;
    }
}

