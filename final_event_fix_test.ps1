# 事件详情修复最终验证脚本
Write-Host "🔧 开始验证事件详情修复效果..." -ForegroundColor Green

# 1. 检查后端API数据
Write-Host "`n📊 测试后端API数据..." -ForegroundColor Yellow
try {
    $apiResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/timelines/91/events" -Method GET
    Write-Host "✅ API返回 $($apiResponse.Count) 个事件" -ForegroundColor Green
    
    # 显示前3个事件的关键字段
    for ($i = 0; $i -lt [Math]::Min(3, $apiResponse.Count); $i++) {
        $event = $apiResponse[$i]
        Write-Host "  事件 $($i+1):" -ForegroundColor Cyan
        Write-Host "    ID: $($event.id)" -ForegroundColor White
        Write-Host "    event_description: $($event.event_description.Substring(0, [Math]::Min(80, $event.event_description.Length)))..." -ForegroundColor White
        Write-Host "    event_time: $($event.event_time)" -ForegroundColor White
        Write-Host "    event_location: $($event.event_location)" -ForegroundColor White
    }
} catch {
    Write-Host "❌ API测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 检查前端服务状态
Write-Host "`n🌐 检查前端服务状态..." -ForegroundColor Yellow
$frontendPort = netstat -an | findstr ":5173"
if ($frontendPort) {
    Write-Host "✅ 前端服务运行在端口 5173" -ForegroundColor Green
} else {
    Write-Host "❌ 前端服务未运行" -ForegroundColor Red
}

# 3. 检查后端服务状态
Write-Host "`n🔧 检查后端服务状态..." -ForegroundColor Yellow
$backendPort = netstat -an | findstr ":8080"
if ($backendPort) {
    Write-Host "✅ 后端服务运行在端口 8080" -ForegroundColor Green
} else {
    Write-Host "❌ 后端服务未运行" -ForegroundColor Red
}

# 4. 打开测试页面
Write-Host "`n🚀 打开测试页面..." -ForegroundColor Yellow
Write-Host "正在打开以下页面进行验证:" -ForegroundColor Cyan
Write-Host "  1. 时间线详情页面: http://localhost:5173/timeline/detail/91" -ForegroundColor White
Write-Host "  2. 事件修复验证页面: http://localhost:8080/test_event_fix_verification.html" -ForegroundColor White
Write-Host "  3. 调试数据页面: http://localhost:8080/debug_timeline_data.html" -ForegroundColor White

Start-Process "http://localhost:5173/timeline/detail/91"
Start-Sleep -Seconds 2
Start-Process "http://localhost:8080/test_event_fix_verification.html"
Start-Sleep -Seconds 2
Start-Process "http://localhost:8080/debug_timeline_data.html"

# 5. 修复总结
Write-Host "`n📋 修复总结:" -ForegroundColor Green
Write-Host "✅ 修复了 SimpleTimelineDetailView.vue 中的事件标题提取逻辑" -ForegroundColor White
Write-Host "✅ 修复了 EnhancedTimelineDetailView.vue 中的事件标题提取逻辑" -ForegroundColor White
Write-Host "✅ 更新了数据字段映射，使用 event_description 作为标题" -ForegroundColor White
Write-Host "✅ 添加了标题截取逻辑（前50字符）" -ForegroundColor White
Write-Host "✅ 创建了多个验证和调试页面" -ForegroundColor White

Write-Host "`n🎯 预期效果:" -ForegroundColor Green
Write-Host "- 时间线详情页面应该显示正确的事件标题，而不是'未知事件'" -ForegroundColor White
Write-Host "- 事件标题应该是从 event_description 字段提取的内容" -ForegroundColor White
Write-Host "- 所有13个事件都应该有有意义的标题显示" -ForegroundColor White

Write-Host "`n✨ 验证完成！请检查打开的页面确认修复效果。" -ForegroundColor Green