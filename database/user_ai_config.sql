-- =============================================
-- 用户AI聊天配置表（个人API Key）
-- 配套前端：home-web /user/center 「AI聊天设置」页
-- API Key 使用 AES-GCM 加密后存储，库中不保存明文
-- 执行库：本地为 movie 库（application-test.yml），线上为 home 库
-- =============================================

USE movie;

CREATE TABLE IF NOT EXISTS user_ai_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  provider VARCHAR(64) NOT NULL DEFAULT 'custom' COMMENT 'AI服务提供商: deepseek/zhipu/qwen/openai/custom',
  base_url VARCHAR(255) DEFAULT NULL COMMENT 'OpenAI兼容API基础地址',
  model VARCHAR(128) DEFAULT NULL COMMENT '模型名称',
  api_key_cipher VARCHAR(1024) NOT NULL COMMENT 'AES-GCM加密后的API Key(Base64: iv+密文)',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户AI聊天配置表(存储加密后的个人API Key)';
