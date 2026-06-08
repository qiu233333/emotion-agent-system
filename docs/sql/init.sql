SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS bishe_backend
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE bishe_backend;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS emotion_diary;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `user` (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码，实际项目中应保存加密后的密码',
  nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_username (username),
  KEY idx_user_role (role),
  KEY idx_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE emotion_diary (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日记ID',
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  title VARCHAR(100) NOT NULL COMMENT '日记标题',
  content TEXT NOT NULL COMMENT '日记内容',
  mood_tag VARCHAR(50) DEFAULT NULL COMMENT '用户自定义心情标签',
  emotion_polarity VARCHAR(20) DEFAULT NULL COMMENT '情绪极性：positive/negative/neutral',
  emotion_type VARCHAR(50) DEFAULT NULL COMMENT '情绪类型，如开心、焦虑、悲伤、愤怒、平静',
  emotion_score INT DEFAULT NULL COMMENT '情绪强度，建议范围1-5',
  keywords VARCHAR(255) DEFAULT NULL COMMENT '关键词，多个关键词用逗号分隔',
  ai_summary TEXT DEFAULT NULL COMMENT 'AI分析摘要',
  ai_suggestion TEXT DEFAULT NULL COMMENT 'AI生成的情绪建议',
  diary_date DATETIME NOT NULL COMMENT '日记日期',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_emotion_diary_user_id (user_id),
  KEY idx_emotion_diary_diary_date (diary_date),
  KEY idx_emotion_diary_emotion_type (emotion_type),
  KEY idx_emotion_diary_emotion_polarity (emotion_polarity),
  CONSTRAINT fk_emotion_diary_user
    FOREIGN KEY (user_id) REFERENCES `user` (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT chk_emotion_diary_score
    CHECK (emotion_score IS NULL OR emotion_score BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情绪日记表';

INSERT INTO `user` (
  username,
  password,
  nickname,
  avatar,
  email,
  role,
  status,
  create_time,
  update_time
) VALUES
('admin', '$2a$10$eXAxIgk5vxKKgIVTTDpud.sBLX4GcDyGjvVhAq0q7rz2ISYPNcLJm', '系统管理员', NULL, 'admin@example.com', 'admin', 1, '2026-05-30 09:00:00', '2026-05-30 09:00:00'),
('alice', '$2a$10$eXAxIgk5vxKKgIVTTDpud.sBLX4GcDyGjvVhAq0q7rz2ISYPNcLJm', '小爱', NULL, 'alice@example.com', 'user', 1, '2026-05-30 09:10:00', '2026-05-30 09:10:00'),
('bob', '$2a$10$eXAxIgk5vxKKgIVTTDpud.sBLX4GcDyGjvVhAq0q7rz2ISYPNcLJm', '小博', NULL, 'bob@example.com', 'user', 1, '2026-05-30 09:20:00', '2026-05-30 09:20:00');

INSERT INTO emotion_diary (
  user_id,
  title,
  content,
  mood_tag,
  emotion_polarity,
  emotion_type,
  emotion_score,
  keywords,
  ai_summary,
  ai_suggestion,
  diary_date,
  create_time,
  update_time
) VALUES
(1, '完成阶段任务', '今天完成了前后端联调，页面可以正常请求后端接口，感觉很有成就感。', '有成就感', 'positive', '开心', 4, '联调,进展,成就感', '用户因为项目推进顺利产生积极情绪。', '可以记录下今天有效的学习和开发方法，作为后续推进项目的参考。', '2026-05-30 00:00:00', '2026-05-30 10:00:00', '2026-05-30 10:00:00'),
(1, '准备论文材料', '今天整理毕设文档时有些焦虑，担心后面的功能来不及完成。', '焦虑', 'negative', '焦虑', 3, '毕设,论文,进度', '用户对毕业设计进度存在担忧，但情绪强度中等。', '建议拆分下一步任务，先完成最核心功能，再逐步补充展示和优化内容。', '2026-05-29 00:00:00', '2026-05-29 21:30:00', '2026-05-29 21:30:00'),
(1, '平静的一天', '今天按计划学习和休息，没有特别大的情绪波动，整体比较平稳。', '平静', 'neutral', '平静', 2, '学习,休息,平稳', '用户当天情绪平稳，生活节奏较规律。', '可以继续保持稳定作息，并适当加入运动或放松活动。', '2026-05-30 00:00:00', '2026-05-30 20:00:00', '2026-05-30 20:00:00');
