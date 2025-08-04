#!/usr/bin/env pwsh

Write-Host "=== 地理信息增强功能验证脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "src/main/java/com/hotech/events/service/EventGeographicEnhancementService.java")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 验证服务接口文件..." -ForegroundColor Yellow

# 检查服务接口是否存在
if (Test-Path "src/main/java/com/hotech/events/service/EventGeographicEnhancementService.java") {
    Write-Host "   ✓ EventGeographicEnhancementService.java 已创建" -ForegroundColor Green
} else {
    Write-Host "   ✗ EventGeographicEnhancementService.java 缺失" -ForegroundColor Red
}

# 检查服务实现是否存在
if (Test-Path "src/main/java/com/hotech/events/service/impl/EventGeographicEnhancementServiceImpl.java") {
    Write-Host "   ✓ EventGeographicEnhancementServiceImpl.java 已创建" -ForegroundColor Green
} else {
    Write-Host "   ✗ EventGeographicEnhancementServiceImpl.java 缺失" -ForegroundColor Red
}

# 检查控制器是否存在
if (Test-Path "src/main/java/com/hotech/events/controller/GeographicEnhancementController.java") {
    Write-Host "   ✓ GeographicEnhancementController.java 已创建" -ForegroundColor Green
} else {
    Write-Host "   ✗ GeographicEnhancementController.java 缺失" -ForegroundColor Red
}

Write-Host "2. 验证服务集成..." -ForegroundColor Yellow

# 检查事件存储服务是否集成了地理信息增强
$eventStorageContent = Get-Content "src/main/java/com/hotech/events/service/impl/EventStorageServiceImpl.java" -Raw
if ($eventStorageContent -match "EventGeographicEnhancementService") {
    Write-Host "   ✓ EventStorageServiceImpl 已集成地理信息增强服务" -ForegroundColor Green
} else {
    Write-Host "   ✗ EventStorageServiceImpl 未集成地理信息增强服务" -ForegroundColor Red
}

# 检查时间线生成服务是否集成了地理信息增强
$timelineServiceContent = Get-Content "src/main/java/com/hotech/events/service/impl/EnhancedTimelineGenerationServiceImpl.java" -Raw
if ($timelineServiceContent -match "EventGeographicEnhancementService") {
    Write-Host "   ✓ EnhancedTimelineGenerationServiceImpl 已集成地理信息增强服务" -ForegroundColor Green
} else {
    Write-Host "   ✗ EnhancedTimelineGenerationServiceImpl 未集成地理信息增强服务" -ForegroundColor Red
}

Write-Host "3. 验证核心功能..." -ForegroundColor Yellow

# 检查服务实现中的关键方法
$serviceImplContent = Get-Content "src/main/java/com/hotech/events/service/impl/EventGeographicEnhancementServiceImpl.java" -Raw

$methodsToCheck = @(
    "enhanceEventGeographicInfo",
    "enhanceEventDataGeographicInfo", 
    "getCoordinatesByLocation",
    "extractLocationFromDescription",
    "needsGeographicEnhancement"
)

foreach ($method in $methodsToCheck) {
    if ($serviceImplContent -match $method) {
        Write-Host "   ✓ 方法 '$method' 已实现" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 方法 '$method' 缺失" -ForegroundColor Red
    }
}

Write-Host "4. 验证坐标数据..." -ForegroundColor Yellow

# 检查是否包含坐标数据初始化
if ($serviceImplContent -match "initializeLocationCoordinates") {
    Write-Host "   ✓ 坐标数据初始化方法已实现" -ForegroundColor Green
} else {
    Write-Host "   ✗ 坐标数据初始化方法缺失" -ForegroundColor Red
}

# 检查是否包含主要城市坐标
$citiesToCheck = @("北京", "上海", "华盛顿", "伦敦", "东京")
$foundCities = 0

foreach ($city in $citiesToCheck) {
    if ($serviceImplContent -match $city) {
        $foundCities++
    }
}

if ($foundCities -eq $citiesToCheck.Count) {
    Write-Host "   ✓ 主要城市坐标数据完整 ($foundCities/$($citiesToCheck.Count))" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  部分城市坐标数据缺失 ($foundCities/$($citiesToCheck.Count))" -ForegroundColor Yellow
}

Write-Host "5. 验证API接口..." -ForegroundColor Yellow

# 检查控制器中的API端点
$controllerContent = Get-Content "src/main/java/com/hotech/events/controller/GeographicEnhancementController.java" -Raw

$endpointsToCheck = @(
    "/statistics",
    "/enhance-event",
    "/enhance-missing", 
    "/coordinates",
    "/extract-location"
)

foreach ($endpoint in $endpointsToCheck) {
    if ($controllerContent -match $endpoint) {
        Write-Host "   ✓ API端点 '$endpoint' 已实现" -ForegroundColor Green
    } else {
        Write-Host "   ✗ API端点 '$endpoint' 缺失" -ForegroundColor Red
    }
}

Write-Host "6. 检查数据库字段支持..." -ForegroundColor Yellow

# 检查Event实体是否支持经纬度字段
$eventEntityContent = Get-Content "src/main/java/com/hotech/events/entity/Event.java" -Raw
if ($eventEntityContent -match "longitude" -and $eventEntityContent -match "latitude") {
    Write-Host "   ✓ Event实体支持经纬度字段" -ForegroundColor Green
} else {
    Write-Host "   ✗ Event实体缺少经纬度字段支持" -ForegroundColor Red
}

Write-Host "7. 编译检查..." -ForegroundColor Yellow

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

Write-Host "8. 测试建议..." -ForegroundColor Yellow
Write-Host "   请按照以下步骤测试地理信息增强功能：" -ForegroundColor Cyan
Write-Host "   1. 启动Spring Boot应用" -ForegroundColor Cyan
Write-Host "   2. 访问 http://localhost:8080/api/geographic-enhancement/statistics" -ForegroundColor Cyan
Write-Host "   3. 测试单个事件增强: POST /api/geographic-enhancement/enhance-event/{eventId}" -ForegroundColor Cyan
Write-Host "   4. 测试批量增强: POST /api/geographic-enhancement/enhance-missing" -ForegroundColor Cyan
Write-Host "   5. 验证时间线生成功能是否包含完整地理信息" -ForegroundColor Cyan
Write-Host "   6. 检查数据库中事件的longitude和latitude字段" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== 验证完成 ===" -ForegroundColor Green

# 显示修复摘要
Write-Host "修复摘要：" -ForegroundColor Yellow
Write-Host "- 创建了EventGeographicEnhancementService服务接口" -ForegroundColor White
Write-Host "- 实现了EventGeographicEnhancementServiceImpl服务类" -ForegroundColor White
Write-Host "- 集成到EventStorageServiceImpl中" -ForegroundColor White
Write-Host "- 集成到EnhancedTimelineGenerationServiceImpl中" -ForegroundColor White
Write-Host "- 创建了GeographicEnhancementController API控制器" -ForegroundColor White
Write-Host "- 内置了全球主要城市和国家的坐标数据" -ForegroundColor White
Write-Host "- 支持智能地点提取和坐标映射" -ForegroundColor White
Write-Host "- 提供批量处理和统计监控功能" -ForegroundColor White

Write-Host ""
Write-Host "功能特点：" -ForegroundColor Yellow
Write-Host "- 🌍 自动检测缺少经纬度的事件" -ForegroundColor White
Write-Host "- 📍 智能从事件描述中提取地点信息" -ForegroundColor White
Write-Host "- 🗺️  内置全球主要城市坐标数据库" -ForegroundColor White
Write-Host "- 🔄 支持批量处理和单个事件处理" -ForegroundColor White
Write-Host "- 📊 提供详细的处理统计信息" -ForegroundColor White
Write-Host "- 🚀 无缝集成到时间线生成流程" -ForegroundColor White

# 打开测试页面
if (Test-Path "test-geographic-enhancement.html") {
    Write-Host "正在打开测试指南页面..." -ForegroundColor Yellow
    Start-Process "test-geographic-enhancement.html"
}