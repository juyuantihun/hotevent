#!/usr/bin/env pwsh

Write-Host "=== 火山引擎API经纬度要求修复验证脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "src/main/java/com/hotech/events/service/impl/DeepSeekServiceImpl.java")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 验证DeepSeekServiceImpl修复..." -ForegroundColor Yellow

# 检查DeepSeek服务实现是否包含经纬度要求
$deepSeekContent = Get-Content "src/main/java/com/hotech/events/service/impl/DeepSeekServiceImpl.java" -Raw

$requiredElements = @(
    "latitude.*纬度",
    "longitude.*经度", 
    "数值格式",
    "必须提供准确的经纬度坐标",
    "重要要求"
)

$foundElements = 0
foreach ($element in $requiredElements) {
    if ($deepSeekContent -match $element) {
        Write-Host "   ✓ 找到要求: $element" -ForegroundColor Green
        $foundElements++
    } else {
        Write-Host "   ✗ 缺少要求: $element" -ForegroundColor Red
    }
}

if ($foundElements -eq $requiredElements.Count) {
    Write-Host "   ✓ DeepSeekServiceImpl 经纬度要求完整 ($foundElements/$($requiredElements.Count))" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  DeepSeekServiceImpl 经纬度要求不完整 ($foundElements/$($requiredElements.Count))" -ForegroundColor Yellow
}

Write-Host "2. 验证PromptTemplateServiceImpl修复..." -ForegroundColor Yellow

# 检查提示词模板服务是否包含经纬度要求
$promptTemplateContent = Get-Content "src/main/java/com/hotech/events/service/impl/PromptTemplateServiceImpl.java" -Raw

$templateRequiredElements = @(
    "latitude.*纬度",
    "longitude.*经度",
    "输出格式要求",
    "必须提供准确的经纬度坐标",
    "数值格式"
)

$foundTemplateElements = 0
foreach ($element in $templateRequiredElements) {
    if ($promptTemplateContent -match $element) {
        Write-Host "   ✓ 找到要求: $element" -ForegroundColor Green
        $foundTemplateElements++
    } else {
        Write-Host "   ✗ 缺少要求: $element" -ForegroundColor Red
    }
}

if ($foundTemplateElements -eq $templateRequiredElements.Count) {
    Write-Host "   ✓ PromptTemplateServiceImpl 经纬度要求完整 ($foundTemplateElements/$($templateRequiredElements.Count))" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  PromptTemplateServiceImpl 经纬度要求不完整 ($foundTemplateElements/$($templateRequiredElements.Count))" -ForegroundColor Yellow
}

Write-Host "3. 验证JSON格式示例..." -ForegroundColor Yellow

# 检查是否包含完整的JSON示例
$jsonElements = @(
    '"latitude":\s*39\.9042',
    '"longitude":\s*116\.4074',
    '"id":\s*"event_001"',
    '"eventTime":\s*".*T.*Z"'
)

$foundJsonElements = 0
foreach ($element in $jsonElements) {
    if ($deepSeekContent -match $element -or $promptTemplateContent -match $element) {
        Write-Host "   ✓ 找到JSON示例元素: $element" -ForegroundColor Green
        $foundJsonElements++
    } else {
        Write-Host "   ✗ 缺少JSON示例元素: $element" -ForegroundColor Red
    }
}

if ($foundJsonElements -eq $jsonElements.Count) {
    Write-Host "   ✓ JSON格式示例完整 ($foundJsonElements/$($jsonElements.Count))" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  JSON格式示例不完整 ($foundJsonElements/$($jsonElements.Count))" -ForegroundColor Yellow
}

Write-Host "4. 检查字段完整性..." -ForegroundColor Yellow

# 检查是否包含所有必需的字段
$requiredFields = @(
    "id.*事件唯一标识",
    "title.*事件标题",
    "description.*事件详细描述",
    "eventTime.*事件发生时间",
    "location.*事件发生地点",
    "latitude.*纬度",
    "longitude.*经度",
    "subject.*事件主体",
    "object.*事件客体",
    "eventType.*事件类型",
    "keywords.*关键词列表",
    "sources.*信息来源列表",
    "credibilityScore.*可信度评分"
)

$foundFields = 0
foreach ($field in $requiredFields) {
    if ($deepSeekContent -match $field -or $promptTemplateContent -match $field) {
        $foundFields++
    }
}

Write-Host "   找到必需字段: $foundFields/$($requiredFields.Count)" -ForegroundColor $(if ($foundFields -eq $requiredFields.Count) { "Green" } else { "Yellow" })

Write-Host "5. 编译检查..." -ForegroundColor Yellow

# 尝试编译项目（如果Maven可用）
if (Get-Command "mvn" -ErrorAction SilentlyContinue) {
    Write-Host "   正在检查编译..." -ForegroundColor Cyan
    $compileResult = & mvn compile -q 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ 项目编译成功" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 项目编译失败" -ForegroundColor Red
        Write-Host "   编译错误信息: $compileResult" -ForegroundColor Red
    }
} else {
    Write-Host "   ⚠️  Maven不可用，跳过编译检查" -ForegroundColor Yellow
}

Write-Host "6. 生成测试建议..." -ForegroundColor Yellow

Write-Host "   请按照以下步骤测试修复效果：" -ForegroundColor Cyan
Write-Host "   1. 重启Spring Boot应用" -ForegroundColor Cyan
Write-Host "   2. 创建新的时间线（通过前端或API）" -ForegroundColor Cyan
Write-Host "   3. 检查应用日志，确认提示词包含经纬度要求" -ForegroundColor Cyan
Write-Host "   4. 验证API响应包含latitude和longitude字段" -ForegroundColor Cyan
Write-Host "   5. 检查数据库中事件的经纬度字段是否有值" -ForegroundColor Cyan
Write-Host "   6. 测试时间线的地图可视化功能" -ForegroundColor Cyan

Write-Host "7. 日志检查命令..." -ForegroundColor Yellow
Write-Host "   # 查看DeepSeek API调用日志" -ForegroundColor Cyan
Write-Host "   grep 'buildEventFetchPrompt\|生成的提示词' logs/application.log" -ForegroundColor Gray
Write-Host "   # 查看API响应解析日志" -ForegroundColor Cyan
Write-Host "   grep 'API响应解析结果\|解析事件数据' logs/application.log" -ForegroundColor Gray

Write-Host "8. 数据库验证SQL..." -ForegroundColor Yellow
Write-Host "   -- 查询最近创建的事件及其坐标信息" -ForegroundColor Cyan
Write-Host "   SELECT id, event_location, latitude, longitude, source_type, created_at" -ForegroundColor Gray
Write-Host "   FROM event WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)" -ForegroundColor Gray
Write-Host "   ORDER BY created_at DESC LIMIT 10;" -ForegroundColor Gray

Write-Host ""
Write-Host "=== 验证完成 ===" -ForegroundColor Green

# 显示修复摘要
Write-Host "修复摘要：" -ForegroundColor Yellow
Write-Host "- 在DeepSeekServiceImpl中添加了经纬度要求" -ForegroundColor White
Write-Host "- 在PromptTemplateServiceImpl中更新了响应格式" -ForegroundColor White
Write-Host "- 明确要求API返回数值格式的经纬度坐标" -ForegroundColor White
Write-Host "- 提供了完整的JSON格式示例" -ForegroundColor White
Write-Host "- 强调了每个事件都必须包含坐标信息" -ForegroundColor White

Write-Host ""
Write-Host "预期效果：" -ForegroundColor Yellow
Write-Host "- 🌍 从火山引擎获取的事件将包含准确的经纬度坐标" -ForegroundColor White
Write-Host "- 📍 坐标格式为数值类型，便于地图可视化" -ForegroundColor White
Write-Host "- 🗺️  支持基于地理位置的事件分析和展示" -ForegroundColor White
Write-Host "- 📊 提高时间线生成功能的地理信息完整性" -ForegroundColor White
Write-Host "- 🔄 与地理信息增强服务形成双重保障" -ForegroundColor White

# 打开测试页面
if (Test-Path "test-coordinate-requirement-fix.html") {
    Write-Host "正在打开测试指南页面..." -ForegroundColor Yellow
    Start-Process "test-coordinate-requirement-fix.html"
}