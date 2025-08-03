-- 为timeline表添加唯一约束，防止创建重复的时间线
USE hot_events_db;

-- 1. 首先清理现有的重复数据
-- 查看重复的时间线
SELECT name, COUNT(*) as count 
FROM timeline 
GROUP BY name 
HAVING COUNT(*) > 1;

-- 2. 删除重复的时间线（保留最新的一个）
DELETE t1 FROM timeline t1
INNER JOIN timeline t2 
WHERE t1.name = t2.name 
AND t1.id < t2.id;

-- 3. 添加唯一约束（防止同名时间线在短时间内重复创建）
-- 注意：这个约束可能需要根据业务需求调整
-- 如果允许同名但不同时间范围的时间线，可以考虑复合唯一约束

-- 方案1：简单的名称唯一约束
-- ALTER TABLE timeline ADD CONSTRAINT uk_timeline_name UNIQUE (name);

-- 方案2：名称+状态唯一约束（允许同名但不同状态的时间线）
-- ALTER TABLE timeline ADD CONSTRAINT uk_timeline_name_status UNIQUE (name, status);

-- 方案3：名称+时间范围唯一约束（推荐）
ALTER TABLE timeline ADD CONSTRAINT uk_timeline_name_time 
UNIQUE (name, start_time, end_time);

-- 4. 查看约束是否添加成功
SHOW INDEX FROM timeline WHERE Key_name LIKE 'uk_%';

-- 5. 验证约束工作
-- 以下语句应该会失败（如果已存在相同名称和时间范围的时间线）
-- INSERT INTO timeline (name, start_time, end_time, status, event_count, relation_count, created_at, updated_at) 
-- VALUES ('测试时间线', '2024-01-01 00:00:00', '2024-12-31 23:59:59', 'COMPLETED', 0, 0, NOW(), NOW());