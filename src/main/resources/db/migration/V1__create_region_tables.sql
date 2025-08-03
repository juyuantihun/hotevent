-- 创建地区表
CREATE TABLE IF NOT EXISTS `region` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '地区名称',
  `type` varchar(20) NOT NULL COMMENT '地区类型：CUSTOM(自定义), CONTINENT(洲), COUNTRY(国家), PROVINCE(省份), CITY(城市)',
  `description` varchar(500) DEFAULT NULL COMMENT '地区描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_region_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区表';

-- 创建地区项目表
CREATE TABLE IF NOT EXISTS `region_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `region_id` bigint(20) NOT NULL COMMENT '地区ID',
  `dictionary_id` bigint(20) NOT NULL COMMENT '字典项ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_dictionary` (`region_id`, `dictionary_id`),
  KEY `idx_region_item_region_id` (`region_id`),
  KEY `idx_region_item_dictionary_id` (`dictionary_id`),
  CONSTRAINT `fk_region_item_region` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区项目表';