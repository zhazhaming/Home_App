-- =====================================================
-- 电影预约通知系统 - 数据库迁移脚本
-- 创建时间: 2025-10-04
-- 说明: 包含reservation表修改、新增通知消息表和日志表
-- =====================================================

-- =====================================================
-- 1. 修改 reservation 表，新增通知相关字段
-- =====================================================
ALTER TABLE `reservation` 
ADD COLUMN `notified_at` DATETIME NULL COMMENT '通知发送时间' AFTER `created_at`,
ADD COLUMN `email_sent` TINYINT(1) DEFAULT 0 COMMENT '邮件是否发送成功: 0-未发送/1-已发送' AFTER `notified_at`,
ADD COLUMN `message_sent` TINYINT(1) DEFAULT 0 COMMENT '站内消息是否发送成功: 0-未发送/1-已发送' AFTER `email_sent`,
ADD COLUMN `movie_id` INT NULL COMMENT '关联的电影ID(电影上线后)' AFTER `title`;

-- 添加索引优化查询性能
ALTER TABLE `reservation`
ADD INDEX `idx_status` (`status`),
ADD INDEX `idx_user_id` (`user_id`),
ADD INDEX `idx_title` (`title`),
ADD INDEX `idx_created_at` (`created_at`);

-- =====================================================
-- 2. 创建 notification_message 表（站内消息表）
-- =====================================================
CREATE TABLE IF NOT EXISTS `notification_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `message_type` VARCHAR(32) NOT NULL DEFAULT 'MOVIE_ONLINE' COMMENT '消息类型: MOVIE_ONLINE-电影上线/SYSTEM_NOTICE-系统通知',
    `title` VARCHAR(255) NOT NULL COMMENT '消息标题',
    `content` TEXT NOT NULL COMMENT '消息内容(支持HTML)',
    `movie_id` INT NULL COMMENT '关联的电影ID',
    `movie_title` VARCHAR(255) NULL COMMENT '电影标题',
    `movie_poster_url` VARCHAR(512) NULL COMMENT '电影海报URL',
    `action_url` VARCHAR(512) NULL COMMENT '操作跳转链接',
    `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读/1-已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_at` DATETIME NULL COMMENT '阅读时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-未删除/1-已删除',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_message_type` (`message_type`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_deleted` (`deleted`),
    INDEX `idx_user_unread` (`user_id`, `is_read`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息通知表';

-- =====================================================
-- 3. 创建 notification_log 表（通知日志表）
-- =====================================================
CREATE TABLE IF NOT EXISTS `notification_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `reservation_id` BIGINT NOT NULL COMMENT '预约记录ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `notification_type` VARCHAR(32) NOT NULL COMMENT '通知类型: EMAIL-邮件/SITE_MESSAGE-站内消息',
    `status` VARCHAR(32) NOT NULL COMMENT '通知状态: SUCCESS-成功/FAILED-失败/RETRY-重试中',
    `error_message` TEXT NULL COMMENT '失败原因/错误信息',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `receiver_email` VARCHAR(255) NULL COMMENT '接收邮箱(邮件通知时记录)',
    `movie_title` VARCHAR(255) NULL COMMENT '电影标题',
    PRIMARY KEY (`id`),
    INDEX `idx_reservation_id` (`reservation_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_notification_type` (`notification_type`),
    INDEX `idx_sent_at` (`sent_at`),
    INDEX `idx_reservation_type` (`reservation_id`, `notification_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送日志表';

-- =====================================================
-- 4. 创建 notification_template 表（消息模板表-可选）
-- =====================================================
CREATE TABLE IF NOT EXISTS `notification_template` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码: MOVIE_ONLINE_EMAIL/MOVIE_ONLINE_MESSAGE',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(32) NOT NULL COMMENT '模板类型: EMAIL/SITE_MESSAGE',
    `subject` VARCHAR(255) NULL COMMENT '邮件主题(邮件模板专用)',
    `content` TEXT NOT NULL COMMENT '模板内容(支持占位符: {username}, {movieTitle}, {actionUrl}等)',
    `variables` VARCHAR(512) NULL COMMENT '变量说明JSON格式',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用/1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知消息模板表';

-- =====================================================
-- 5. 插入默认消息模板数据
-- =====================================================
INSERT INTO `notification_template` (`template_code`, `template_name`, `template_type`, `subject`, `content`, `variables`, `status`) VALUES
('MOVIE_ONLINE_EMAIL', '电影上线邮件通知', 'EMAIL', '您预约的电影《{movieTitle}》已上线！', 
'<div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
    <h2>尊敬的 {username}，您好！</h2>
    <p>您在 <strong>{reservationDate}</strong> 预约的电影已经上线啦！</p>
    <div style="background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
        <h3 style="margin-top: 0;">《{movieTitle}》</h3>
        <p>现在就可以前往观看或下载了！</p>
    </div>
    <a href="{actionUrl}" style="display: inline-block; background: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 10px 0;">立即观看</a>
    <p style="color: #666; font-size: 12px; margin-top: 30px;">此邮件由系统自动发送，请勿回复。</p>
</div>',
'{"username":"用户名","movieTitle":"电影标题","reservationDate":"预约日期","actionUrl":"跳转链接"}',
1),
('MOVIE_ONLINE_MESSAGE', '电影上线站内消息', 'SITE_MESSAGE', NULL,
'您在 {reservationDate} 预约的电影《{movieTitle}》已上线，点击查看详情！',
'{"username":"用户名","movieTitle":"电影标题","reservationDate":"预约日期"}',
1);

-- =====================================================
-- 6. 创建用户通知偏好设置表（可选-高级功能）
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_notification_preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `email_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用邮件通知: 0-禁用/1-启用',
    `site_message_enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用站内消息: 0-禁用/1-启用',
    `do_not_disturb_start` TIME NULL COMMENT '免打扰开始时间',
    `do_not_disturb_end` TIME NULL COMMENT '免打扰结束时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户通知偏好设置表';

-- =====================================================
-- 7. 数据初始化：为现有预约记录设置默认值
-- =====================================================
UPDATE `reservation` 
SET `email_sent` = 0, `message_sent` = 0 
WHERE `email_sent` IS NULL OR `message_sent` IS NULL;

-- =====================================================
-- 8. 创建视图：方便查询通知统计数据（可选）
-- =====================================================
CREATE OR REPLACE VIEW `v_notification_statistics` AS
SELECT 
    DATE(nl.sent_at) as stat_date,
    nl.notification_type,
    nl.status,
    COUNT(*) as total_count,
    COUNT(DISTINCT nl.user_id) as unique_users
FROM notification_log nl
GROUP BY DATE(nl.sent_at), nl.notification_type, nl.status;

-- =====================================================
-- 9. 数据库权限检查（可选-提示）
-- =====================================================
-- 请确保应用数据库用户具有以下权限：
-- GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.* TO 'your_user'@'localhost';
-- FLUSH PRIVILEGES;

-- =====================================================
-- 10. 回滚脚本（谨慎使用！仅在测试环境）
-- =====================================================
/*
-- 如果需要回滚，取消下面注释并执行

-- 删除新增的表
DROP TABLE IF EXISTS `user_notification_preference`;
DROP TABLE IF EXISTS `notification_template`;
DROP TABLE IF EXISTS `notification_log`;
DROP TABLE IF EXISTS `notification_message`;

-- 删除视图
DROP VIEW IF EXISTS `v_notification_statistics`;

-- 删除reservation表新增的字段
ALTER TABLE `reservation` 
DROP COLUMN `movie_id`,
DROP COLUMN `message_sent`,
DROP COLUMN `email_sent`,
DROP COLUMN `notified_at`;

-- 删除索引
ALTER TABLE `reservation`
DROP INDEX `idx_status`,
DROP INDEX `idx_user_id`,
DROP INDEX `idx_title`,
DROP INDEX `idx_created_at`;
*/

-- =====================================================
-- 执行完成提示
-- =====================================================
SELECT '===== 数据库迁移脚本执行完成 =====' AS message;
SELECT '请检查以下表是否创建成功:' AS message;
SELECT 'notification_message, notification_log, notification_template' AS tables;
SELECT '请检查 reservation 表是否新增字段成功' AS message;

