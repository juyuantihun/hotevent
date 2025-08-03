# 最终修复验证脚本
Write-Host "🎯 开始验证事件详情修复效果..." -ForegroundColor Green

# 1. 测试API数据
Write-Host "`n📊 测试API数据结构..." -ForegroundColor Yellow
try {
    $apiResponse = Invoke-RestMethod -Uri "http://localhost:5173/api/timelines/91/events" -Method GET
    if ($apiResponse.code -eq 200 -and $apiResponse.data.events) {
        $events = $apiResponse.data.events
        Write-Host "✅ API返回 $($events.Count) 个事件" -ForegroundColor Green
        
        # 显示第一个事件的详细信息
        $firstEvent = $events[0]
        Write-Host "  第一个事件详情:" -ForegroundColor Cyan
        Write-Host "    ID: $($firstEvent.id)" -ForegroundColor White
        Write-Host "    event_description: $($firstEvent.event_description.Substring(0, [Math]::Min(100, $firstEvent.event_description.Length)))..." -ForegroundColor White
        Write-Host "    event_time: $($firstEvent.event_time)" -ForegroundColor White
        Write-Host "    event_location: $($firstEvent.event_location)" -ForegroundColor White
    } else {
        Write-Host "❌ API数据格式异常" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ API测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 检查服务状态
Write-Host "`n🌐 检查服务状态..." -ForegroundColor Yellow
$frontendPort = netstat -an | findstr ":5173"
$backendPort = netstat -an | findstr ":8080"

if ($frontendPort) {
    Write-Host "✅ 前端服务运行正常 (端口 5173)" -ForegroundColor Green
} else {
    Write-Host "❌ 前端服务未运行" -ForegroundColor Red
}

if ($backendPort) {
    Write-Host "✅ 后端服务运行正常 (端口 8080)" -ForegroundColor Green
} else {
    Write-Host "❌ 后端服务未运行" -ForegroundColor Red
}

# 3. 打开验证页面
Write-Host "`n🚀 打开验证页面..." -ForegroundColor Yellow
Write-Host "正在打开以下页面进行最终验证:" -ForegroundColor Cyan
Write-Host "  1. 时间线详情页面 (主要验证页面)" -ForegroundColor White
Write-Host "  2. 组件测试页面" -ForegroundColor White

Start-Process "http://localhost:5173/timeline/detail/91"
Start-Sleep -Seconds 2
Start-Process "http://localhost:8080/test_enhanced_timeline_component.html"

# 4. 修复总结
Write-Host "`n📋 修复总结:" -ForegroundColor Green
Write-Host "🔧 问题根源: getOriginalEvents函数中的数据结构不匹配" -ForegroundColor White
Write-Host "✅ 修复内容: 更新了数据提取逻辑，优先使用已处理的event.title" -ForegroundColor White
Write-Host "✅ 数据流程: API数据 -> loadTimelineDetail处理 -> getOriginalEvents提取 -> 页面显示" -ForegroundColor White

Write-Host "`n🎯 预期效果:" -ForegroundColor Green
Write-Host "- 时间线详情页面应该显示正确的事件标题" -ForegroundColor White
Write-Host "- 不再显示'未知事件'，而是显示实际的事件描述内容" -ForegroundColor White
Write-Host "- 所有13个事件都应该有有意义的标题" -ForegroundColor White

Write-Host "`n🔍 验证方法:" -ForegroundColor Green
Write-Host "1. 查看时间线详情页面，确认事件标题不再是'未知事件'" -ForegroundColor White
Write-Host "2. 打开浏览器开发者工具，查看控制台日志" -ForegroundColor White
Write-Host "3. 检查'处理后的事件'日志，确认title字段有正确的值" -ForegroundColor White

Write-Host "`n✨ 修复完成！请检查打开的页面确认效果。" -ForegroundColor Green