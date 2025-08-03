# 测试DeepSeek联网搜索功能
Write-Host "测试DeepSeek联网搜索功能..." -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# 测试接口列表
$tests = @(
    @{
        name = "连接测试"
        url = "/api/deepseek/online-search/test/connection"
        method = "GET"
    },
    @{
        name = "配置信息"
        url = "/api/deepseek/online-search/config"
        method = "GET"
    },
    @{
        name = "获取最新事件"
        url = "/api/deepseek/online-search/test/latest-events?limit=3"
        method = "POST"
    },
    @{
        name = "根据关键词搜索"
        url = "/api/deepseek/online-search/test/events-by-keywords?keywords=以色列,巴勒斯坦&limit=3"
        method = "POST"
    },
    @{
        name = "根据日期范围搜索"
        url = "/api/deepseek/online-search/test/events-by-date-range?startDate=2025-01-20&endDate=2025-01-25&limit=3"
        method = "POST"
    },
    @{
        name = "简单对话测试"
        url = "/api/deepseek/online-search/test/chat?query=请介绍一下最近的国际事件"
        method = "POST"
    }
)

foreach ($test in $tests) {
    $url = $baseUrl + $test.url
    Write-Host "`n=== $($test.name) ===" -ForegroundColor Yellow
    Write-Host "URL: $url" -ForegroundColor Cyan
    Write-Host "方法: $($test.method)" -ForegroundColor Cyan
    
    try {
        $startTime = Get-Date
        
        if ($test.method -eq "GET") {
            $response = Invoke-RestMethod -Uri $url -Method GET -TimeoutSec 30
        } else {
            $response = Invoke-RestMethod -Uri $url -Method POST -TimeoutSec 30
        }
        
        $endTime = Get-Date
        $duration = ($endTime - $startTime).TotalMilliseconds
        
        Write-Host "✅ 成功! 耗时: $([math]::Round($duration))ms" -ForegroundColor Green
        
        # 显示关键信息
        if ($response.connected -ne $null) {
            Write-Host "连接状态: $($response.connected)" -ForegroundColor $(if($response.connected) {"Green"} else {"Red"})
        }
        
        if ($response.success -ne $null) {
            Write-Host "执行状态: $($response.success)" -ForegroundColor $(if($response.success) {"Green"} else {"Red"})
        }
        
        if ($response.message) {
            Write-Host "消息: $($response.message)" -ForegroundColor White
        }
        
        if ($response.eventCount -ne $null) {
            Write-Host "事件数量: $($response.eventCount)" -ForegroundColor White
        }
        
        if ($response.responseTime) {
            Write-Host "API响应时间: $($response.responseTime)" -ForegroundColor White
        }
        
        if ($response.searchType) {
            Write-Host "搜索类型: $($response.searchType)" -ForegroundColor White
        }
        
        if ($response.model) {
            Write-Host "模型: $($response.model)" -ForegroundColor White
        }
        
        if ($response.webSearchEnabled -ne $null) {
            Write-Host "联网搜索: $($response.webSearchEnabled)" -ForegroundColor $(if($response.webSearchEnabled) {"Green"} else {"Red"})
        }
        
        # 如果有事件数据，显示第一个事件的标题
        if ($response.events -and $response.events.Count -gt 0) {
            Write-Host "第一个事件: $($response.events[0].title)" -ForegroundColor Cyan
        }
        
    } catch {
        Write-Host "❌ 失败: $($_.Exception.Message)" -ForegroundColor Red
        
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode
            Write-Host "状态码: $statusCode" -ForegroundColor Red
        }
    }
    
    Write-Host "---" -ForegroundColor Gray
}

Write-Host "`n🎉 测试完成！" -ForegroundColor Green
Write-Host "如果连接测试成功，说明联网搜索功能已正常配置。" -ForegroundColor Yellow
Write-Host "如果API调用失败，请检查API密钥是否有效。" -ForegroundColor Yellow

Read-Host "`n按任意键继续"