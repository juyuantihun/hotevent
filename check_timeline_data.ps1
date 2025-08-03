# 检查时间线数据
Write-Host "=== 检查时间线数据 ===" -ForegroundColor Green

# 数据库连接信息
$server = "localhost"
$database = "hot_events_db"
$username = "root"
$password = "123456"

try {
    # 检查时间线表
    Write-Host "`n1. 检查时间线表..." -ForegroundColor Yellow
    $query1 = "SELECT id, name, status, event_count, created_at FROM timeline ORDER BY created_at DESC LIMIT 5"
    $result1 = mysql -h$server -u$username -p$password -D$database -e $query1 2>$null
    if ($result1) {
        Write-Host "最近的时间线:" -ForegroundColor Cyan
        Write-Host $result1
    } else {
        Write-Host "❌ 无法查询时间线数据" -ForegroundColor Red
    }

    # 检查事件表
    Write-Host "`n2. 检查事件表..." -ForegroundColor Yellow
    $query2 = "SELECT COUNT(*) as total_events FROM event"
    $result2 = mysql -h$server -u$username -p$password -D$database -e $query2 2>$null
    if ($result2) {
        Write-Host "事件总数:" -ForegroundColor Cyan
        Write-Host $result2
    }

    # 检查时间线事件关联表
    Write-Host "`n3. 检查时间线事件关联..." -ForegroundColor Yellow
    $query3 = "SELECT timeline_id, COUNT(*) as event_count FROM timeline_event GROUP BY timeline_id ORDER BY timeline_id DESC LIMIT 5"
    $result3 = mysql -h$server -u$username -p$password -D$database -e $query3 2>$null
    if ($result3) {
        Write-Host "时间线事件关联:" -ForegroundColor Cyan
        Write-Host $result3
    }

    # 检查最新时间线的事件
    Write-Host "`n4. 检查最新时间线的事件..." -ForegroundColor Yellow
    $query4 = @"
SELECT t.id as timeline_id, t.name, t.status, t.event_count, 
       COUNT(te.event_id) as actual_event_count
FROM timeline t 
LEFT JOIN timeline_event te ON t.id = te.timeline_id 
WHERE t.id = (SELECT MAX(id) FROM timeline)
GROUP BY t.id, t.name, t.status, t.event_count
"@
    $result4 = mysql -h$server -u$username -p$password -D$database -e $query4 2>$null
    if ($result4) {
        Write-Host "最新时间线详情:" -ForegroundColor Cyan
        Write-Host $result4
    }

} catch {
    Write-Host "❌ 数据库查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🏁 检查完成" -ForegroundColor Green