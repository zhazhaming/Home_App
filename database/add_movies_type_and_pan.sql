-- ============================================================
-- 为 movies 表扩展分类字段，支持 连续剧/动漫/综艺 等类型入库
-- 配套爬虫：D:\python\pycharm project\test\Spider\video_spider.py
--
-- 变更内容（纯增量，不影响现有电影数据）：
--   1. type     : 资源类型 movie=电影(默认) tv=连续剧 anime=动漫 variety=综艺
--   2. pan_url  : 云盘链接（迅雷/夸克/百度，多条用换行分隔），部分综艺无磁力只有网盘
--   3. magnet_url 扩为 text：连续剧一页可能有多条磁力（按集），多条用换行分隔
--   4. 为 type 加索引，方便网站按类型分页查询
--
-- 回滚：
--   ALTER TABLE `movies` DROP INDEX `idx_type`, DROP COLUMN `type`, DROP COLUMN `pan_url`;
--   （magnet_url 为 text 不影响原有功能，可不回滚）
-- ============================================================

ALTER TABLE `movies`
    ADD COLUMN `type` VARCHAR(16) NOT NULL DEFAULT 'movie' COMMENT '资源类型: movie/tv/anime/variety',
    ADD COLUMN `pan_url` TEXT NULL COMMENT '云盘链接，多条换行分隔';

ALTER TABLE `movies`
    MODIFY COLUMN `magnet_url` TEXT NULL COMMENT '磁力链接，多条换行分隔';

ALTER TABLE `movies`
    ADD INDEX `idx_type` (`type`);

-- 网站后端按类型分页查询示例：
--   SELECT id, name, url, magnet_url, img_url, date, type, pan_url
--   FROM movies WHERE type = 'tv' ORDER BY date DESC LIMIT ?, ?;
