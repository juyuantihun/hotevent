# 启动服务并测试
Write-Host "正在启动Spring Boot服务..." -ForegroundColor Green

# 启动服务（后台运行）
$job = Start-Job -ScriptBlock {
    Set-Location "D:\chengqing\hot&event\hot_event"
    mvn spring-boot:run
}

Write-Host "等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 45

# 检查端口是否开放
$portOpen = $false
for ($i = 1; $i -le 10; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -TimeoutSec 5 -ErrorAction Stop
        $portOpen = $true
        break
    } catch {
        Write-Host "尝试 $i/10: 服务还未就绪，继续等待..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
    }
}

if ($portOpen) {
    Write-Host "服务已启动，开始测试API..." -ForegroundColor Green
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/timelines/91/events?page=1&pageSize=4&includeDetails=false" -Method GET -ContentType "application/json" -TimeoutSec 30
        
        Write-Host "API测试成功!" -ForegroundColor Green
        Write-Host "响应数据:" -ForegroundColor Yellow
        $response | ConvertTo-Json -Depth 3
        
    } catch {
        Write-Host "API测试失败: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "服务启动失败或超时" -ForegroundColor Red
}

# 清理后台任务
Stop-Job $job -ErrorAction SilentlyContinue
Remove-Job $job -ErrorAction SilentlyContinue