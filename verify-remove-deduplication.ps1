#!/usr/bin/env pwsh

Write-Host "=== 移除事件去重按钮验证脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "frontend/src/views/event/EventList.vue")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 验证按钮移除..." -ForegroundColor Yellow

# 检查事件列表页面是否还包含"事件去重"按钮
$eventListContent = Get-Content "frontend/src/views/event/EventList.vue" -Raw
if ($eventListContent -match "事件去重") {
    Write-Host "   ✗ 发现'事件去重'按钮残留" -ForegroundColor Red
} else {
    Write-Host "   ✓ '事件去重'按钮已成功移除" -ForegroundColor Green
}

Write-Host "2. 验证函数移除..." -ForegroundColor Yellow

# 检查是否还有handleDeduplication函数
if ($eventListContent -match "handleDeduplication") {
    Write-Host "   ✗ 发现handleDeduplication函数残留" -ForegroundColor Red
} else {
    Write-Host "   ✓ handleDeduplication函数已成功移除" -ForegroundColor Green
}

Write-Host "3. 验证图标导入清理..." -ForegroundColor Yellow

# 检查是否还有Operation图标导入
if ($eventListContent -match "Operation") {
    Write-Host "   ✗ 发现Operation图标导入残留" -ForegroundColor Red
} else {
    Write-Host "   ✓ Operation图标导入已成功移除" -ForegroundColor Green
}

Write-Host "4. 验证其他按钮完整性..." -ForegroundColor Yellow

# 检查其他重要按钮是否仍然存在
$buttonsToCheck = @("新增事件", "导出数据", "批量操作")
$allButtonsPresent = $true

foreach ($button in $buttonsToCheck) {
    if ($eventListContent -match $button) {
        Write-Host "   ✓ '$button'按钮存在" -ForegroundColor Green
    } else {
        Write-Host "   ✗ '$button'按钮缺失" -ForegroundColor Red
        $allButtonsPresent = $false
    }
}

if ($allButtonsPresent) {
    Write-Host "   ✓ 所有重要按钮完整" -ForegroundColor Green
} else {
    Write-Host "   ✗ 部分重要按钮缺失" -ForegroundColor Red
}

Write-Host "5. 检查语法完整性..." -ForegroundColor Yellow

# 检查是否有明显的语法错误（简单检查）
$syntaxIssues = @()

# 检查是否有未闭合的标签或括号
$openTags = ($eventListContent | Select-String -Pattern "<el-button" -AllMatches).Matches.Count
$closeTags = ($eventListContent | Select-String -Pattern "</el-button>" -AllMatches).Matches.Count

if ($openTags -eq $closeTags) {
    Write-Host "   ✓ HTML标签匹配" -ForegroundColor Green
} else {
    Write-Host "   ✗ HTML标签不匹配 (开始:$openTags, 结束:$closeTags)" -ForegroundColor Red
    $syntaxIssues += "HTML标签不匹配"
}

# 检查导入语句的完整性
if ($eventListContent -match "} from '@element-plus/icons-vue'") {
    Write-Host "   ✓ 图标导入语句完整" -ForegroundColor Green
} else {
    Write-Host "   ✗ 图标导入语句可能有问题" -ForegroundColor Red
    $syntaxIssues += "图标导入语句问题"
}

if ($syntaxIssues.Count -eq 0) {
    Write-Host "   ✓ 未发现明显语法问题" -ForegroundColor Green
} else {
    Write-Host "   ✗ 发现潜在语法问题: $($syntaxIssues -join ', ')" -ForegroundColor Red
}

Write-Host "6. 测试建议..." -ForegroundColor Yellow
Write-Host "   请按照以下步骤测试修改效果：" -ForegroundColor Cyan
Write-Host "   1. 启动开发服务器: cd frontend && npm run dev" -ForegroundColor Cyan
Write-Host "   2. 登录系统" -ForegroundColor Cyan
Write-Host "   3. 导航到'事件管理' -> '事件列表'" -ForegroundColor Cyan
Write-Host "   4. 验证'事件去重'按钮不再显示" -ForegroundColor Cyan
Write-Host "   5. 验证其他按钮功能正常" -ForegroundColor Cyan
Write-Host "   6. 检查浏览器控制台是否有错误" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== 验证完成 ===" -ForegroundColor Green

# 显示修改摘要
Write-Host "修改摘要：" -ForegroundColor Yellow
Write-Host "- 移除了'事件去重'按钮" -ForegroundColor White
Write-Host "- 移除了handleDeduplication处理函数" -ForegroundColor White
Write-Host "- 移除了未使用的Operation图标导入" -ForegroundColor White
Write-Host "- 保留了其他所有功能按钮" -ForegroundColor White

# 打开测试页面
if (Test-Path "test-remove-deduplication-button.html") {
    Write-Host "正在打开测试指南页面..." -ForegroundColor Yellow
    Start-Process "test-remove-deduplication-button.html"
}