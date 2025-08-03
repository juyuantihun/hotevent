# 清理无关事件的脚本
Write-Host "=== 清理无关事件 ===" -ForegroundColor Green

# 数据库连接信息（请根据实际情况调整）
$server = "localhost"
$database = "hot_events"
$username = "root"
$password = "123456"

# 检查MySQL命令是否可用
try {
    $mysqlVersion = mysql --version 2>$null
    Write-Host "MySQL版本: $mysqlVersion" -ForegroundColor Cyan
} catch {
    Write-Host "错误: 未找到MySQL命令，请确保MySQL已安装并在PATH中" -ForegroundColor Red
    exit 1
}

Write-Host "`n1. 查看当前所有事件（按时间排序）..." -ForegroundColor Yellow
$query1 = @"
SELECT 
    id,
    event_title,
    event_time,
    fetch_method
FROM events 
ORDER BY event_time ASC, created_at ASC
LIMIT 20;
"@

try {
    $result1 = mysql -h$server -u$username -p$password $database -e $query1 2>$null
    Write-Host "当前事件列表:" -ForegroundColor Cyan
    Write-Host $result1 -ForegroundColor Gray
} catch {
    Write-Host "查询事件失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. 查找可能的无关事件..." -ForegroundColor Yellow
$query2 = @"
SELECT 
    id,
    event_title,
    event_description,
    event_time,
    fetch_method
FROM events 
WHERE 
    event_title LIKE '%以色列%' 
    OR event_title LIKE '%伊朗%'
    OR event_title LIKE '%空袭%'
    OR event_title LIKE '%核设施%'
    OR event_title LIKE '%军事目标%'
    OR event_description LIKE '%以色列%'
    OR event_description LIKE '%伊朗%'
    OR fetch_method = 'FALLBACK_GENERATOR'
    OR fetch_method = 'SIMPLE_FALLBACK'
    OR event_title LIKE '%测试%'
    OR event_title LIKE '%test%'
ORDER BY event_time ASC;
"@

try {
    $result2 = mysql -h$server -u$username -p$password $database -e $query2 2>$null
    Write-Host "发现的无关事件:" -ForegroundColor Cyan
    Write-Host $result2 -ForegroundColor Gray
    
    if ($result2 -and $result2.Trim() -ne "") {
        Write-Host "`n是否要删除这些无关事件？(y/N): " -ForegroundColor Yellow -NoNewline
        $confirm = Read-Host
        
        if ($confirm -eq 'y' -or $confirm -eq 'Y') {
            Write-Host "`n3. 删除无关事件..." -ForegroundColor Yellow
            
            # 删除以色列-伊朗相关事件
            $deleteQuery1 = @"
DELETE FROM events 
WHERE 
    (event_title LIKE '%以色列%' AND event_title LIKE '%伊朗%')
    OR (event_title LIKE '%空袭%' AND event_title LIKE '%核设施%')
    OR (event_description LIKE '%以色列%' AND event_description LIKE '%伊朗%');
"@
            
            # 删除测试事件
            $deleteQuery2 = @"
DELETE FROM events 
WHERE 
    fetch_method IN ('FALLBACK_GENERATOR', 'SIMPLE_FALLBACK')
    OR event_title LIKE '%测试事件%'
    OR event_title LIKE '%test_event%';
"@
            
            try {
                $deleteResult1 = mysql -h$server -u$username -p$password $database -e $deleteQuery1 2>$null
                Write-Host "删除以色列-伊朗相关事件完成" -ForegroundColor Green
                
                $deleteResult2 = mysql -h$server -u$username -p$password $database -e $deleteQuery2 2>$null
                Write-Host "删除测试事件完成" -ForegroundColor Green
                
                # 验证删除结果
                Write-Host "`n4. 验证删除结果..." -ForegroundColor Yellow
                $verifyQuery = @"
SELECT 
    COUNT(*) as remaining_events 
FROM events;
"@
                
                $remainingCount = mysql -h$server -u$username -p$password $database -e $verifyQuery 2>$null
                Write-Host "剩余事件数量: $remainingCount" -ForegroundColor Cyan
                
                # 显示清理后的前10个事件
                $finalQuery = @"
SELECT 
    id,
    event_title,
    event_time
FROM events 
ORDER BY event_time ASC
LIMIT 10;
"@
                
                $finalResult = mysql -h$server -u$username -p$password $database -e $finalQuery 2>$null
                Write-Host "`n清理后的事件列表（前10个）:" -ForegroundColor Cyan
                Write-Host $finalResult -ForegroundColor Gray
                
            } catch {
                Write-Host "删除事件失败: $($_.Exception.Message)" -ForegroundColor Red
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