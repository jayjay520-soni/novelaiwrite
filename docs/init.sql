-- =============================================
-- AI 小说创作平台 - 数据库初始化脚本
-- MySQL 8.0+
-- =============================================

CREATE DATABASE IF NOT EXISTS novel_ai_writer
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE novel_ai_writer;

-- ---------------------------------------------
-- 用户表
-- ---------------------------------------------
DROP TABLE IF EXISTS `user`;  
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
  `password`    VARCHAR(64)  NOT NULL COMMENT '密码(MD5)',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------
-- 小说作品表
-- ---------------------------------------------
DROP TABLE IF EXISTS `novel`;
CREATE TABLE `novel` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '作品ID',
  `user_id`     BIGINT       NOT NULL COMMENT '所属用户ID',
  `title`       VARCHAR(200) NOT NULL COMMENT '作品标题',
  `content`     LONGTEXT     DEFAULT NULL COMMENT '作品内容',
  `genre`       VARCHAR(50)  DEFAULT NULL COMMENT '小说类型',
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已完成 2-生成中',
  `word_count`  INT          NOT NULL DEFAULT 0 COMMENT '字数',
  `summary`     VARCHAR(500) DEFAULT NULL COMMENT '简介/提示词',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小说作品表';

-- ---------------------------------------------
-- 系统配置表（大模型参数等）
-- ---------------------------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT         DEFAULT NULL COMMENT '配置值',
  `description`  VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ---------------------------------------------
-- 初始配置数据
-- ---------------------------------------------
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('llm.model',        'gpt-4o-mini', '默认大模型名称'),
('llm.max_tokens',   '4096',        '最大生成Token数'),
('llm.temperature',  '0.8',         '生成温度参数'),
('llm.api_url',      'https://api.openai.com/v1/chat/completions', '大模型API地址'),
('rate_limit.max',   '60',          '每分钟最大请求数'),
('rate_limit.window','60',          '限流窗口(秒)');

-- ---------------------------------------------
-- 测试用户 (密码: 123456 的 MD5)
-- ---------------------------------------------
INSERT INTO `user` (`username`, `password`, `nickname`, `email`) VALUES
('demo', 'e10adc3949ba59abbe56e057f20f883e', '演示用户', 'demo@example.com');
