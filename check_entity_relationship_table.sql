-- 检查entity_relationship表是否存在
SHOW TABLES LIKE 'entity_relationship';

-- 如果表存在，查看表结构
DESCRIBE entity_relationship;

-- 检查表中是否有数据
SELECT COUNT(*) as record_count FROM entity_relationship;