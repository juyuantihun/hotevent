# 通过API清理无关事件
Write-Host "=== 通过API清理无关事件 ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# 检查应用是否运行
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -TimeoutSec 5
    Write-Host "应用运行正常" -ForegroundColor Green
} catch {
    Write-Host "应用未运行，请先启动应用: mvn spring-boot:run" -ForegroundColor Red
    exit 1
}

Write-Host "`n1. 获取当前所有事件..." -ForegroundColor Yellow

try {
    # 获取所有事件
    $allEvents = Invoke-RestMethod -Uri "$baseUrl/api/events?page=0&size=100" -Method GET
    
    Write-Host "总事件数: $($allEvents.totalElements)" -ForegroundColor Cyan
    
    # 查找无关事件
    $irrelevantEvents = $allEvents.content | Where-Object {
        $_.eventTitle -like "*以色列*" -or
        $_.eventTitle -like "*伊朗*" -or
        $_.eventTitle -like "*空袭*" -or
        $_.eventTitle -like "*核设施*" -or
        $_.eventTitle -like "*军事目标*" -or
        $_.eventTitle -like "*测试事件*" -or
        $_.eventTitle -like "*test_event*" -or
        $_.fetchMethod -eq "FALLBACK_GENERATOR" -or
        $_.fetchMethod -eq "SIMPLE_FALLBACK"
    }
    
    Write-Host "`n2. 发现的无关事件:" -ForegroundColor Yellow
    
    if ($irrelevantEvents.Count -gt 0) {
        $irrelevantEvents | ForEach-Object {
            Write-Host "ID: $($_.id) | 标题: $($_.eventTitle) | 时间: $($_.eventTime) | 方法: $($_.fetchMethod)" -ForegroundColor Gray
        }
        
        Write-Host "`n发现 $($irrelevantEvents.Count) 个无关事件" -ForegroundColor Cyan
        Write-Host "是否要删除这些事件？(y/N): " -ForegroundColor Yellow -NoNewline
        $confirm = Read-Host
        
        if ($confirm -eq 'y' -or $confirm -eq 'Y') {
            Write-Host "`n3. 删除无关事件..." -ForegroundColor Yellow
            
            $deletedCount = 0
            $failedCount = 0
            
            foreach ($event in $irrelevantEvents) {
                try {
                    Invoke-RestMethod -Uri "$baseUrl/api/events/$($event.id)" -Method DELETE
                    Write-Host "✓ 删除事件 ID: $($event.id)" -ForegroundColor Green
                    $deletedCount++
                } catch {
                    Write-Host "✗ 删除事件 ID: $($event.id) 失败: $($_.Exception.Message)" -ForegroundColor Red
                    $failedCount++
                }
            }
            
            Write-Host "`n删除结果: 成功 $deletedCount 个，失败 $failedCount 个" -ForegroundColor Cyan
            
            # 验证删除结果
            Write-Host "`n4. 验证删除结果..." -ForegroundColor Yellow
            
            $updatedEvents = Invoke-RestMethod -Uri "$baseUrl/api/events?page=0&size=20" -Method GET
            Write-Host "剩余事件数: $($updatedEvents.totalElements)" -ForegroundColor Cyan
            
            Write-Host "`n当前前10个事件:" -ForegroundColor Yellow
            $updatedEvents.content | Select-Object -First 10 | ForEach-Object {
                Write-Host "ID: $($_.id) | 标题: $($_.eventTitle) | 时间: $($_.eventTime)" -ForegroundColor Gray
            }
            
        } else {
            Write-Host "取消删除操作" -ForegroundColor Yellow
        }
    } else {
        Write-Host "没有发现无关事件" -ForegroundColor Green
    }
    
} catch {
    Write-Host "操作失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 清理完成 ===" -ForegroundColor Green