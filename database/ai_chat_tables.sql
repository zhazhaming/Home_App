-- =============================================
-- AI聊天功能表结构
-- 配套前端：home-web /ai-chat 页面
-- 执行库：本地为 movie 库（application-test.yml），线上为 home 库
-- =============================================

USE movie;

CREATE TABLE IF NOT EXISTS ai_conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL DEFAULT '0' COMMENT '用户ID(0-未登录游客)',
  title VARCHAR(255) DEFAULT NULL COMMENT '对话标题(NULL时前端显示为: 新对话)',
  preview VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息预览',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话表';

CREATE TABLE IF NOT EXISTS ai_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT NOT NULL COMMENT '所属对话ID',
  role VARCHAR(16) NOT NULL COMMENT '角色: user-用户/ai-助手',
  content TEXT NOT NULL COMMENT '消息内容',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';
