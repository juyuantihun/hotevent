-- 添加关键缺失字段到 event 表
USE hot_events_db;

-- 检查并添加 subject_coordinate_id 字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'subject_coordinate_id') = 0,
    'ALTER TABLE event ADD COLUMN subject_coordinate_id BIGINT NULL DEFAULT NULL COMMENT "事件主体地理坐标ID"',
    'SELECT "subject_coordinate_id already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 object_coordinate_id 字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'object_coordinate_id') = 0,
    'ALTER TABLE event ADD COLUMN object_coordinate_id BIGINT NULL DEFAULT NULL COMMENT "事件客体地理坐标ID"',
    'SELECT "object_coordinate_id already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 event_coordinate_id 字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'event_coordinate_id') = 0,
    'ALTER TABLE event ADD COLUMN event_coordinate_id BIGINT NULL DEFAULT NULL COMMENT "事件发生地坐标ID"',
    'SELECT "event_coordinate_id already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加其他必要字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'event_title') = 0,
    'ALTER TABLE event ADD COLUMN event_title VARCHAR(500) NULL DEFAULT NULL COMMENT "事件标题"',
    'SELECT "event_title already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'credibility_score') = 0,
    'ALTER TABLE event ADD COLUMN credibility_score DECIMAL(3,2) NULL DEFAULT NULL COMMENT "可信度评分"',
    'SELECT "credibility_score already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'validation_status') = 0,
    'ALTER TABLE event ADD COLUMN validation_status TINYINT(1) NULL DEFAULT 0 COMMENT "验证状态"',
    'SELECT "validation_status already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'source_urls') = 0,
    'ALTER TABLE event ADD COLUMN source_urls TEXT NULL COMMENT "来源URL列表"',
    'SELECT "source_urls already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'fetch_method') = 0,
    'ALTER TABLE event ADD COLUMN fetch_method VARCHAR(50) NULL DEFAULT NULL COMMENT "获取方法"',
    'SELECT "fetch_method already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'last_validated_at') = 0,
    'ALTER TABLE event ADD COLUMN last_validated_at DATETIME NULL DEFAULT NULL COMMENT "最后验证时间"',
    'SELECT "last_validated_at already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'geographic_status') = 0,
    'ALTER TABLE event ADD COLUMN geographic_status TINYINT(1) NULL DEFAULT 0 COMMENT "地理信息状态"',
    'SELECT "geographic_status already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_name = 'event' 
    AND table_schema = 'hot_events_db' 
    AND column_name = 'geographic_updated_at') = 0,
    'ALTER TABLE event ADD COLUMN geographic_updated_at DATETIME NULL DEFAULT NULL COMMENT "地理信息更新时间"',
    'SELECT "geographic_updated_at already exists" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示最终的表结构
SELECT 'Table structure after adding missing columns:' as message;
DESCRIBE event;