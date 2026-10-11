package com.home.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.home.Enum.ResponMsg;
import com.home.entity.DTO.MessageListResponseDTO;
import com.home.entity.DTO.SiteMessageDTO;
import com.home.service.SiteMessageService;
import com.home.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: zhazhaming
 * @Date: 2025/10/04
 * @Description: 站内消息Controller
 */
@RestController
@Api(description = "站内消息相关接口")
@CrossOrigin
@RequestMapping("message")
public class MessageController {

    @Autowired
    private SiteMessageService siteMessageService;

    /**
     * 获取用户未读消息数量
     */
    @GetMapping("/unreadCount")
    @ApiOperation("获取未读消息数量")
    public R<Integer> getUnreadCount(@ApiParam("用户ID") @RequestParam String userId) {
        LogUtils.info("查询未读消息数量 - 用户ID:{}", userId);
        try {
            Integer count = siteMessageService.getUnreadCount(userId);
            return R.ok(count).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("查询未读消息数量失败 - 用户ID:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("查询失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户消息列表
     */
    @GetMapping("/list")
    @ApiOperation("获取消息列表")
    public R<MessageListResponseDTO> getMessageList(
            @ApiParam("用户ID") @RequestParam String userId,
            @ApiParam("页码（默认1）") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小（默认20）") @RequestParam(defaultValue = "20") Integer pageSize,
            @ApiParam("是否已读（0-未读/1-已读/null-全部）") @RequestParam(required = false) Integer isRead) {
        
        LogUtils.info("查询消息列表 - 用户ID:{}, 页码:{}, 每页大小:{}, 是否已读:{}", 
            userId, page, pageSize, isRead);
        
        try {
            // 参数校验
            if (page <= 0) page = 1;
            if (pageSize <= 0 || pageSize > 100) pageSize = 20;
            
            MessageListResponseDTO response = siteMessageService.getUserMessages(userId, page, pageSize, isRead);
            return R.ok(response).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("查询消息列表失败 - 用户ID:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取消息详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取消息详情")
    public R<SiteMessageDTO> getMessageDetail(
            @ApiParam("消息ID") @PathVariable Long id,
            @ApiParam("用户ID") @RequestParam String userId) {
        
        LogUtils.info("查询消息详情 - 消息ID:{}, 用户ID:{}", id, userId);
        
        try {
            SiteMessageDTO message = siteMessageService.getMessageDetail(id, userId);
            if (message == null) {
                return R.failed("消息不存在或无权访问");
            }
            return R.ok(message).setCode(ResponMsg.Success.status());
        } catch (Exception e) {
            LogUtils.error("查询消息详情失败 - 消息ID:{}, 用户ID:{}, 错误:{}", 
                id, userId, e.getMessage(), e);
            return R.failed("查询失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{id}")
    @ApiOperation("标记消息已读")
    public R<String> markAsRead(
            @ApiParam("消息ID") @PathVariable Long id,
            @ApiParam("用户ID") @RequestParam String userId) {
        
        LogUtils.info("标记消息已读 - 消息ID:{}, 用户ID:{}", id, userId);
        
        try {
            boolean success = siteMessageService.markAsRead(id, userId);
            if (success) {
                return R.ok("标记成功").setCode(ResponMsg.Success.status());
            } else {
                return R.failed("标记失败，消息不存在或已读");
            }
        } catch (Exception e) {
            LogUtils.error("标记消息已读失败 - 消息ID:{}, 用户ID:{}, 错误:{}", 
                id, userId, e.getMessage(), e);
            return R.failed("操作失败: " + e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/readAll")
    @ApiOperation("全部标记已读")
    public R<String> markAllAsRead(@ApiParam("用户ID") @RequestParam String userId) {
        LogUtils.info("标记所有消息已读 - 用户ID:{}", userId);
        
        try {
            boolean success = siteMessageService.markAllAsRead(userId);
            if (success) {
                return R.ok("全部标记成功").setCode(ResponMsg.Success.status());
            } else {
                return R.ok("没有未读消息").setCode(ResponMsg.Success.status());
            }
        } catch (Exception e) {
            LogUtils.error("标记所有消息已读失败 - 用户ID:{}, 错误:{}", userId, e.getMessage(), e);
            return R.failed("操作失败: " + e.getMessage());
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除消息")
    public R<String> deleteMessage(
            @ApiParam("消息ID") @PathVariable Long id,
            @ApiParam("用户ID") @RequestParam String userId) {
        
        LogUtils.info("删除消息 - 消息ID:{}, 用户ID:{}", id, userId);
        
        try {
            boolean success = siteMessageService.deleteMessage(id, userId);
            if (success) {
                return R.ok("删除成功").setCode(ResponMsg.Success.status());
            } else {
                return R.failed("删除失败，消息不存在");
            }
        } catch (Exception e) {
            LogUtils.error("删除消息失败 - 消息ID:{}, 用户ID:{}, 错误:{}", 
                id, userId, e.getMessage(), e);
            return R.failed("操作失败: " + e.getMessage());
        }
    }
}

