-- =============================================
-- 电影在线观看功能：movies表新增play_url字段
-- play_url 存可直接播放的片源地址（mp4/m3u8），多行=多线路/多集
-- 每行格式：名称|地址  或  仅地址
-- 示例：第1集|https://example.com/e1.m3u8
-- 执行库：本地为 movie 库，线上为 home 库（把 USE movie 改成 USE home）
-- =============================================

USE movie;

ALTER TABLE movies ADD COLUMN play_url text DEFAULT NULL COMMENT '在线播放片源(mp4/m3u8),多行=多线路,格式:名称|URL' AFTER pan_url;
