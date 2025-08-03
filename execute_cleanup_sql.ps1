# 执行清理无关事件的SQL脚本
Write-Host "=== 执行数据库清理 ===" -ForegroundColor Green

# 数据库连接参数
$server = "localhost"
$database = "hot_events"
$username = "root"
$password = "123456"

Write-Host "连接数据库: $server/$database" -ForegroundColor Cyan

# 1. 查看当前最早的事件
Write-Host "`n1. 查看当前最早的事件..." -ForegroundColor Yellow
$query1 = "SELECT id, event_title, event_time, fetch_method FROM events ORDER BY event_time ASC, created_at ASC LIMIT 10;"

try {
    Write-Host "执行查询..." -ForegroundColor Gray
    $result = mysql -h$server -u$username -p$password $database -e $query1 2>$null
    if ($result) {
        Write-Host $result -ForegroundColor Cyan
    } else {
        Write-Host "没有查询到数据" -ForegroundColor Yellow
    }
} catch {
    Write-Host "查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 查找无关事件
Write-Host "`n2. 查找无关事件..." -ForegroundColor Yellow
$query2 = @"
SELECT id, event_title, event_time 
FROM events 
WHERE event_title LIKE '%以色列%' 
   OR event_title LIKE '%伊朗%'
   OR event_title LIKE '%空袭%'
   OR event_title LIKE '%核设施%'
   OR fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
   OR event_title LIKE '%测试%'
ORDER BY event_time ASC;
"@

try {
    $irrelevantEvents = mysql -h$server -u$username -p$password $database -e $query2 2>$null
    if ($irrelevantEvents -and $irrelevantEvents.Trim() -ne "") {
        Write-Host "发现的无关事件:" -ForegroundColor Cyan
        Write-Host $irrelevantEvents -ForegroundColor Gray
        
        Write-Host "`n是否要删除这些无关事件？(y/N): " -ForegroundColor Yellow -NoNewline
        $confirm = Read-Host
        
        if ($confirm -eq 'y' -or $confirm -eq 'Y') {
            # 3. 删除无关事件
            Write-Host "`n3. 删除无关事件..." -ForegroundColor Yellow
            
            $deleteQuery = @"
DELETE FROM events 
WHERE (event_title LIKE '%以色列%' AND (event_title LIKE '%伊朗%' OR event_title LIKE '%核设施%'))
   OR (event_title LIKE '%空袭%' AND event_title LIKE '%核设施%')
   OR fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
   OR event_title LIKE '%测试事件%'
   OR event_title LIKE '%test_event%';
"@
            
            try {
                mysql -h$server -u$username -p$password $database -e $deleteQuery 2>$null
                Write-Host "删除完成" -ForegroundColor Green
                
                # 4. 验证结果
                Write-Host "`n4. 验证删除结果..." -ForegroundColor Yellow
                $countQuery = "SELECT COUNT(*) as total_events FROM events;"
                $totalEvents = mysql -h$server -u$username -p$password $database -e $countQuery 2>$null
                Write-Host "剩余事件总数: $totalEvents" -ForegroundColor Cyan
                
                # 5. 显示清理后的前10个事件
                Write-Host "`n5. 清理后的前10个事件:" -ForegroundColor Yellow
                $finalQuery = "SELECT id, event_title, event_time FROM events ORDER BY event_time ASC LIMIT 10;"
                $finalResult = mysql -h$server -u$username -p$password $database -e $finalQuery 2>$null
                Write-Host $finalResult -ForegroundColor Cyan
                
            } catch {
                Write-Host "删除失败: $($_.Exception.Message)" -ForegroundColor Red
            }
        } else {
            Write-Host "取消删除操作" -ForegroundColor Yellow
        }
    } else {
        Write-Host "没有发现无关事件" -ForegroundColor Green
    }
} catch {
    Write-Host "查找无关事件失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 清理完成 ===" -ForegroundColor Green
Write-Host "提示: 如果需要重新生成时间线，请重启应用或调用时间线生成API" -ForegroundColor Yellow