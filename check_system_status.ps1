#!/usr/bin/env pwsh
# 系统状态检查脚本
# 用于快速检查热点事件系统的运行状态

Write-Host "=== 热点事件系统状态检查 ===" -ForegroundColor Green

# 检查后端服务状态
Write-Host "`n1. 检查后端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
    if ($response.status -eq "UP") {
        Write-Host "✅ 后端服务运行正常" -ForegroundColor Green
    } else {
        Write-Host "❌ 后端服务状态异常: $($response.status)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 后端服务无法访问 (可能未启动)" -ForegroundColor Red
}

# 检查前端服务状态
Write-Host "`n2. 检查前端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:3000" -Method GET -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ 前端服务运行正常" -ForegroundColor Green
    } else {
        Write-Host "❌ 前端服务状态异常: $($response.StatusCode)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 前端服务无法访问 (可能未启动)" -ForegroundColor Red
}

# 检查数据库连接
Write-Host "`n3. 检查数据库连接..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/regions?page=1&size=1" -Method GET -TimeoutSec 5
    if ($response.code -eq 200) {
        Write-Host "✅ 数据库连接正常" -ForegroundColor Green
    } else {
        Write-Host "❌ 数据库连接异常: $($response.msg)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 数据库连接测试失败" -ForegroundColor Red
}

# 检查时间线功能
Write-Host "`n4. 检查时间线功能..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/timelines?page=1&size=1" -Method GET -TimeoutSec 5
    if ($response.code -eq 200) {
        Write-Host "✅ 时间线功能正常" -ForegroundColor Green
        Write-Host "   当前时间线数量: $($response.data.total)" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 时间线功能异常: $($response.msg)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 时间线功能测试失败" -ForegroundColor Red
}

# 系统信息
Write-Host "`n=== 系统信息 ===" -ForegroundColor Green
Write-Host "后端地址: http://localhost:8080" -ForegroundColor Cyan
Write-Host "前端地址: http://localhost:3000" -ForegroundColor Cyan
Write-Host "API文档: http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan

Write-Host "`n=== 快速启动命令 ===" -ForegroundColor Green
Write-Host "启动后端: cd hot_event && mvn spring-boot:run" -ForegroundColor Cyan
Write-Host "启动前端: cd hot_event/frontend && npm run dev" -ForegroundColor Cyan

Write-Host "`n检查完成！" -ForegroundColor Green