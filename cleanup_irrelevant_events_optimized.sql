-- 优化版清理不相关事件的SQL脚本
-- 执行前请备份数据库

USE hot_events_db;

-- 1. 查看当前事件总数
SELECT 
    COUNT(*) as total_events,
    COUNT(CASE WHEN fetch_method = 'FALLBACK_GENERATOR' THEN 1 END) as fallback_events,
    COUNT(CASE WHEN fetch_method = 'SIMPLE_FALLBACK' THEN 1 END) as simple_fallback_events,
    COUNT(CASE WHEN event_title LIKE '%测试%' THEN 1 END) as test_events
FROM event;

-- 2. 查看最近7天的事件分布
SELECT 
    DATE(created_at) as create_date,
    COUNT(*) as event_count,
    COUNT(CASE WHEN fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK') THEN 1 END) as fallback_count
FROM event 
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(created_at)
ORDER BY create_date DESC;

-- 3. 查找明显的测试事件和备用数据
SELECT 
    id,
    event_title,
    event_description,
    fetch_method,
    created_at
FROM event 
WHERE 
    -- 测试事件
    (event_title LIKE '%测试%' 
     OR event_title LIKE '%test%'
     OR event_title LIKE '%这是一个关于%的测试事件%'
     OR event_description LIKE '%测试事件%'
     OR event_description LIKE '%用于测试%')
    
    -- 备用数据生成的事件
    OR fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
    
    -- 明显不相关的通用事件
    OR (event_title LIKE '%关于%的相关事件%')
    OR (event_description LIKE '%这是一个与%相关的历史事件%')
    
    -- 重复的模板事件
    OR (event_title = '国际历史上的重大项目启动' AND event_description LIKE '%这一事件对当时的发展产生了重要影响%')
    OR (event_title LIKE '%历史上的%' AND event_description LIKE '%这一事件对当时的发展产生了重要影响%')
    
ORDER BY created_at DESC
LIMIT 50;

-- 4. 查找可能的重复事件（相同标题）
SELECT 
    event_title,
    COUNT(*) as duplicate_count,
    GROUP_CONCAT(id ORDER BY created_at DESC) as event_ids
FROM event 
GROUP BY event_title 
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC
LIMIT 20;

-- 5. 删除明显的测试事件和备用数据（请谨慎执行）
-- 注意：执行前请确认这些事件确实需要删除

-- 删除测试事件
DELETE FROM event 
WHERE 
    event_title LIKE '%测试%' 
    OR event_title LIKE '%test%'
    OR event_title LIKE '%这是一个关于%的测试事件%'
    OR event_description LIKE '%测试事件%'
    OR event_description LIKE '%用于测试%';

-- 删除备用数据生成的事件
DELETE FROM event 
WHERE fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK');

-- 删除明显的模板事件
DELETE FROM event 
WHERE 
    (event_title LIKE '%关于%的相关事件%')
    OR (event_description LIKE '%这是一个与%相关的历史事件%')
    OR (event_title = '国际历史上的重大项目启动' AND event_description LIKE '%这一事件对当时的发展产生了重要影响%')
    OR (event_title LIKE '%历史上的%' AND event_description LIKE '%这一事件对当时的发展产生了重要影响%');

-- 删除重复事件（保留最新的一个）
DELETE e1 FROM event e1
INNER JOIN event e2 
WHERE 
    e1.event_title = e2.event_title 
    AND e1.id < e2.id;

-- 6. 验证清理结果
SELECT 
    COUNT(*) as remaining_events,
    COUNT(CASE WHEN fetch_method = 'DEEPSEEK' THEN 1 END) as deepseek_events,
    COUNT(CASE WHEN fetch_method = 'manual_fetch' THEN 1 END) as manual_events,
    COUNT(CASE WHEN fetch_method = 'deepseek_task' THEN 1 END) as deepseek_task_events
FROM event;

-- 7. 显示清理后的最新事件
SELECT 
    id,
    event_title,
    event_time,
    event_location,
    fetch_method,
    created_at
FROM event 
ORDER BY event_time DESC, created_at DESC
LIMIT 20;

-- 8. 重置自增ID（可选）
-- ALTER TABLE event AUTO_INCREMENT = 1;

-- 清理完成提示
SELECT '清理完成！请检查剩余事件是否符合预期。' as message;