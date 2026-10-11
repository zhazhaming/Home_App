# 数据库迁移说明文档

## 📋 概述

本目录包含电影预约通知系统的数据库迁移脚本，用于支持邮件+站内消息双通道通知功能。

---

## 📁 文件说明

### 1. `notification_system_migration.sql` （完整版）
- **说明**: 包含所有功能的完整迁移脚本
- **包含内容**:
  - ✅ reservation 表修改（新增通知字段）
  - ✅ notification_message 表（站内消息）
  - ✅ notification_log 表（通知日志）
  - ✅ notification_template 表（消息模板-可选）
  - ✅ user_notification_preference 表（用户偏好-可选）
  - ✅ 默认模板数据插入
  - ✅ 统计视图创建
  - ✅ 回滚脚本

### 2. `notification_system_migration_minimal.sql` （最小化版本）⭐ 推荐
- **说明**: 仅包含核心必需功能
- **包含内容**:
  - ✅ reservation 表修改
  - ✅ notification_message 表
  - ✅ notification_log 表
  - ✅ 必要索引

---

## 🚀 执行步骤

### 方式一：MySQL Workbench 执行

1. 打开 MySQL Workbench
2. 连接到目标数据库
3. 打开 `notification_system_migration_minimal.sql` 文件
4. **重要**: 先备份数据库！
   ```sql
   mysqldump -u username -p database_name > backup_$(date +%Y%m%d).sql
   ```
5. 点击 "Execute" 执行脚本
6. 检查执行结果，确认无错误

### 方式二：命令行执行

```bash
# 1. 备份数据库（重要！）
mysqldump -u root -p movies_db > backup_before_migration.sql

# 2. 执行迁移脚本
mysql -u root -p movies_db < notification_system_migration_minimal.sql

# 3. 验证表是否创建成功
mysql -u root -p movies_db -e "SHOW TABLES LIKE 'notification%';"

# 4. 检查 reservation 表字段
mysql -u root -p movies_db -e "DESC reservation;"
```

### 方式三：使用数据库管理工具（如 Navicat）

1. 连接到数据库
2. 选择目标数据库
3. 右键 -> "执行SQL文件"
4. 选择 `notification_system_migration_minimal.sql`
5. 执行并检查结果

---

## ✅ 验证检查清单

执行完成后，请逐项检查：

### 1. reservation 表检查
```sql
DESC reservation;
```
**应包含的新字段**:
- ✅ `notified_at` (datetime)
- ✅ `email_sent` (tinyint)
- ✅ `message_sent` (tinyint)
- ✅ `movie_id` (int)

### 2. notification_message 表检查
```sql
SELECT COUNT(*) FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'your_database_name' 
AND TABLE_NAME = 'notification_message';
```
**应返回**: 1

### 3. notification_log 表检查
```sql
SELECT COUNT(*) FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'your_database_name' 
AND TABLE_NAME = 'notification_log';
```
**应返回**: 1

### 4. 索引检查
```sql
SHOW INDEX FROM reservation;
SHOW INDEX FROM notification_message;
```
**确认索引已创建**

---

## 📊 数据库表结构说明

### reservation 表（修改后）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(255) | 电影标题 |
| movie_id | INT | 电影ID（新增） |
| notify_email | VARCHAR(255) | 通知邮箱 |
| user_id | VARCHAR(64) | 用户ID |
| username | VARCHAR(128) | 用户名 |
| status | VARCHAR(32) | 状态 |
| created_at | DATETIME | 创建时间 |
| notified_at | DATETIME | 通知时间（新增） |
| email_sent | TINYINT(1) | 邮件发送状态（新增） |
| message_sent | TINYINT(1) | 站内消息状态（新增） |

### notification_message 表（新建）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| user_id | VARCHAR(64) | 用户ID |
| message_type | VARCHAR(32) | 消息类型 |
| title | VARCHAR(255) | 消息标题 |
| content | TEXT | 消息内容 |
| movie_id | INT | 电影ID |
| movie_title | VARCHAR(255) | 电影标题 |
| is_read | TINYINT(1) | 是否已读 |
| created_at | DATETIME | 创建时间 |
| read_at | DATETIME | 阅读时间 |

### notification_log 表（新建）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| reservation_id | BIGINT | 预约ID |
| user_id | VARCHAR(64) | 用户ID |
| notification_type | VARCHAR(32) | 通知类型 |
| status | VARCHAR(32) | 发送状态 |
| error_message | TEXT | 错误信息 |
| retry_count | INT | 重试次数 |
| sent_at | DATETIME | 发送时间 |

---

## ⚠️ 注意事项

1. **务必先备份数据库**，迁移操作不可逆！
2. **确认数据库名称**: 请将脚本中的 `your_database_name` 替换为实际数据库名
3. **字符集**: 新表使用 `utf8mb4` 字符集，支持emoji等特殊字符
4. **权限要求**: 执行用户需要 ALTER、CREATE、INSERT 权限
5. **生产环境**: 建议在低峰期执行，避免影响业务
6. **索引创建**: 如果表数据量大，索引创建可能耗时较长

---

## 🔄 回滚方案

如果执行后需要回滚，请执行以下SQL：

```sql
-- 删除新建的表
DROP TABLE IF EXISTS `notification_log`;
DROP TABLE IF EXISTS `notification_message`;

-- 删除新增的字段
ALTER TABLE `reservation` 
DROP COLUMN `movie_id`,
DROP COLUMN `message_sent`,
DROP COLUMN `email_sent`,
DROP COLUMN `notified_at`;

-- 删除索引
ALTER TABLE `reservation`
DROP INDEX IF EXISTS `idx_status`,
DROP INDEX IF EXISTS `idx_user_id`;
```

---

## 📞 问题排查

### 问题1: "Duplicate column name 'notified_at'"
**原因**: 字段已存在  
**解决**: 检查是否已执行过脚本，或手动删除后重新执行

### 问题2: "Table 'notification_message' already exists"
**原因**: 表已存在  
**解决**: 使用 `DROP TABLE IF EXISTS` 删除后重新创建

### 问题3: 索引创建失败
**原因**: 可能索引名冲突  
**解决**: 检查现有索引，必要时修改索引名称

### 问题4: 字符集不兼容
**原因**: 原表使用 utf8 字符集  
**解决**: 将脚本中的 `utf8mb4` 改为 `utf8`，或统一升级到 utf8mb4

---

## 📈 性能优化建议

1. **索引优化**: 根据实际查询模式，可能需要添加复合索引
2. **分区表**: 如果消息量巨大（百万级），考虑对 notification_message 按时间分区
3. **归档策略**: 建议定期归档或删除过期消息（如30天前的已读消息）
4. **定期分析**: 执行 `ANALYZE TABLE` 更新表统计信息

---

## 📝 更新日志

| 日期 | 版本 | 说明 |
|------|------|------|
| 2025-10-04 | v1.0 | 初始版本，包含核心通知功能 |

---

## 🎯 后续计划

- [ ] 添加消息模板管理功能
- [ ] 添加用户通知偏好设置
- [ ] 添加通知统计分析视图
- [ ] 实现消息定时清理任务

---

如有疑问，请联系开发团队或查看项目文档。

