-- 检查最新的事件记录
USE hot_events_db;

-- 查看最新的事件记录
SELECT 
    id,
    event_code,
    event_title,
    event_description,
    event_time,
    event_location,
    subject,
    object,
    credibility_score,
    validation_status,
    created_at
FROM event 
ORDER BY created_at DESC 
LIMIT 10;

-- 查看时间线记录
SELECT 
    id,
    name,
    description,
    status,
    created_at
FROM timeline 
ORDER BY created_at DESC 
LIMIT 5;

-- 查看时间线事件关联
SELECT 
    te.timeline_id,
    te.event_id,
    t.name as timeline_name,
    e.event_title,
    e.event_code
FROM timeline_event te
JOIN timeline t ON te.timeline_id = t.id
JOIN event e ON te.event_id = e.id
ORDER BY te.created_at DESC
LIMIT 10;