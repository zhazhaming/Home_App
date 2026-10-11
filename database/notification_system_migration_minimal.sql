-- =====================================================
-- 电影预约通知系统 - 最小化迁移脚本（核心功能）
-- 创建时间: 2025-10-04
-- 说明: 仅包含核心必需的表和字段
-- =====================================================

-- 1. 修改 reservation 表
ALTER TABLE `reservation` 
ADD COLUMN `notified_at` DATETIME NULL COMMENT '通知发送时间' AFTER `created_at`,
ADD COLUMN `email_sent` TINYINT(1) DEFAULT 0 COMMENT '邮件是否发送成功' AFTER `notified_at`,
ADD COLUMN `message_sent` TINYINT(1) DEFAULT 0 COMMENT '站内消息是否发送成功' AFTER `email_sent`,
ADD COLUMN `movie_id` INT NULL COMMENT '关联的电影ID' AFTER `title`;

-- 2. 添加索引
ALTER TABLE `reservation`
ADD INDEX `idx_status` (`status`),
ADD INDEX `idx_user_id` (`user_id`);

-- 3. 创建站内消息表
CREATE TABLE `notification_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` VARCHAR(64) NOT NULL,
    `message_type` VARCHAR(32) NOT NULL DEFAULT 'MOVIE_ONLINE',
    `title` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `movie_id` INT NULL,
    `movie_title` VARCHAR(255) NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_at` DATETIME NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 4. 创建通知日志表
CREATE TABLE `notification_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `reservation_id` BIGINT NOT NULL,
    `user_id` VARCHAR(64) NOT NULL,
    `notification_type` VARCHAR(32) NOT NULL,
    `status` VARCHAR(32) NOT NULL,
    `error_message` TEXT NULL,
    `retry_count` INT NOT NULL DEFAULT 0,
    `sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_reservation_id` (`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知日志表';

-- 5. 初始化现有数据
UPDATE `reservation` SET `email_sent` = 0, `message_sent` = 0;

SELECT '===== 最小化迁移脚本执行完成 =====' AS message;

