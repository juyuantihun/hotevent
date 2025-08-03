-- 创建时间线表
CREATE TABLE IF NOT EXISTS `timeline` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '时间线名称',
  `description` varchar(500) DEFAULT NULL COMMENT '时间线描述',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` varchar(20) NOT NULL COMMENT '状态：GENERATING(生成中), COMPLETED(已完成), FAILED(失败)',
  `event_count` int(11) DEFAULT '0' COMMENT '事件数量',
  `relation_count` int(11) DEFAULT '0' COMMENT '关系数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_timeline_name` (`name`),
  KEY `idx_timeline_status` (`status`),
  KEY `idx_timeline_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间线表';

-- 创建时间线地区关联表
CREATE TABLE IF NOT EXISTS `timeline_region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `timeline_id` bigint(20) NOT NULL COMMENT '时间线ID',
  `region_id` bigint(20) NOT NULL COMMENT '地区ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_timeline_region` (`timeline_id`, `region_id`),
  KEY `idx_timeline_region_timeline_id` (`timeline_id`),
  KEY `idx_timeline_region_region_id` (`region_id`),
  CONSTRAINT `fk_timeline_region_timeline` FOREIGN KEY (`timeline_id`) REFERENCES `timeline` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_timeline_region_region` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间线地区关联表';

-- 创建时间线事件关联表
CREATE TABLE IF NOT EXISTS `timeline_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `timeline_id` bigint(20) NOT NULL COMMENT '时间线ID',
  `event_id` bigint(20) NOT NULL COMMENT '事件ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_timeline_event` (`timeline_id`, `event_id`),
  KEY `idx_timeline_event_timeline_id` (`timeline_id`),
  KEY `idx_timeline_event_event_id` (`event_id`),
  CONSTRAINT `fk_timeline_event_timeline` FOREIGN KEY (`timeline_id`) REFERENCES `timeline` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间线事件关联表';