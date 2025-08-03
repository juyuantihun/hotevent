# 清理不相关事件并重启系统的脚本
Write-Host "=== 清理不相关事件并重启系统 ===" -ForegroundColor Green

# 数据库连接信息
$server = "localhost"
$database = "hot_events_db"
$username = "root"
$password = "123456"

# 1. 执行数据库清理
Write-Host "`n1. 执行数据库清理..." -ForegroundColor Yellow
try {
    # 执行清理SQL脚本
    mysql -h$server -u$username -p$password $database < "cleanup_irrelevant_events_optimized.sql"
    Write-Host "数据库清理完成" -ForegroundColor Green
} catch {
    Write-Host "数据库清理失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 检查应用程序状态
Write-Host "`n2. 检查应用程序状态..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "发现Java进程，正在停止..." -ForegroundColor Yellow
    $javaProcesses | Stop-Process -Force
    Start-Sleep -Seconds 3
    Write-Host "Java进程已停止" -ForegroundColor Green
} else {
    Write-Host "未发现运行中的Java进程" -ForegroundColor Cyan
}

# 3. 重新编译项目
Write-Host "`n3. 重新编译项目..." -ForegroundColor Yellow
try {
    mvn clean compile -q
    Write-Host "项目编译完成" -ForegroundColor Green
} catch {
    Write-Host "项目编译失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "尝试跳过测试重新编译..." -ForegroundColor Yellow
    mvn clean compile -DskipTests -q
}

# 4. 启动应用程序
Write-Host "`n4. 启动应用程序..." -ForegroundColor Yellow
try {
    # 在后台启动应用程序
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden
    Write-Host "应用程序启动中..." -ForegroundColor Green
    
    # 等待应用程序启动
    Write-Host "等待应用程序完全启动..." -ForegroundColor Cyan
    $maxWait = 60
    $waited = 0
    
    do {
        Start-Sleep -Seconds 2
        $waited += 2
        
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host "应用程序启动成功！" -ForegroundColor Green
                break
            }
        } catch {
            # 继续等待
        }
        
        Write-Host "等待中... ($waited/$maxWait 秒)" -ForegroundColor Gray
        
    } while ($waited -lt $maxWait)
    
    if ($waited -ge $maxWait) {
        Write-Host "应用程序启动超时，请手动检查" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "启动应用程序失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. 验证系统状态
Write-Host "`n5. 验证系统状态..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10
    Write-Host "系统健康状态: $($healthResponse.status)" -ForegroundColor Green
    
    # 检查数据库连接
    try {
        $dbTestResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/regions" -TimeoutSec 10
        Write-Host "数据库连接正常" -ForegroundColor Green
    } catch {
        Write-Host "数据库连接可能有问题" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "无法连接到应用程序，请检查启动状态" -ForegroundColor Red
}

# 6. 显示清理统计
Write-Host "`n6. 显示清理统计..." -ForegroundColor Yellow
try {
    $statsQuery = "SELECT COUNT(*) as total_events FROM event;"
    $totalEvents = mysql -h$server -u$username -p$password $database -e $statsQuery -s -N
    Write-Host "当前事件总数: $totalEvents" -ForegroundColor Cyan
    
    $recentQuery = "SELECT COUNT(*) FROM event WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 DAY);"
    $recentEvents = mysql -h$server -u$username -p$password $database -e $recentQuery -s -N
    Write-Host "最近24小时事件数: $recentEvents" -ForegroundColor Cyan
    
} catch {
    Write-Host "获取统计信息失败" -ForegroundColor Yellow
}

Write-Host "`n=== 清理和重启完成 ===" -ForegroundColor Green
Write-Host "系统已优化，现在应该生成更相关的事件了。" -ForegroundColor Cyan
Write-Host "建议测试创建一个新的时间线来验证效果。" -ForegroundColor Cyan