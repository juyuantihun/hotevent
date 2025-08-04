#!/usr/bin/env pwsh

Write-Host "=== 退出登录功能修复脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "frontend/src/store/modules/auth.ts")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 备份原始文件..." -ForegroundColor Yellow

# 备份原始路由文件
if (Test-Path "frontend/src/router/index.ts") {
    Copy-Item "frontend/src/router/index.ts" "frontend/src/router/index.ts.backup" -Force
    Write-Host "   ✓ 已备份路由文件" -ForegroundColor Green
}

Write-Host "2. 应用修复..." -ForegroundColor Yellow

# 应用修复的路由文件
if (Test-Path "frontend/src/router/index.fixed.ts") {
    Copy-Item "frontend/src/router/index.fixed.ts" "frontend/src/router/index.ts" -Force
    Write-Host "   ✓ 已应用路由修复" -ForegroundColor Green
} else {
    Write-Host "   ✗ 修复文件不存在" -ForegroundColor Red
}

Write-Host "3. 验证修复..." -ForegroundColor Yellow

# 检查认证store是否已修复
$authStoreContent = Get-Content "frontend/src/store/modules/auth.ts" -Raw
if ($authStoreContent -match "window\.location\.href") {
    Write-Host "   ✓ 认证Store已修复" -ForegroundColor Green
} else {
    Write-Host "   ✗ 认证Store修复可能未生效" -ForegroundColor Red
}

# 检查路由文件是否已修复
$routerContent = Get-Content "frontend/src/router/index.ts" -Raw
if ($routerContent -match "检测到退出登录后的跳转") {
    Write-Host "   ✓ 路由守卫已修复" -ForegroundColor Green
} else {
    Write-Host "   ✗ 路由守卫修复可能未生效" -ForegroundColor Red
}

Write-Host "4. 重启开发服务器..." -ForegroundColor Yellow

# 检查是否有运行中的开发服务器
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcesses) {
    Write-Host "   发现运行中的Node.js进程，建议重启开发服务器" -ForegroundColor Yellow
    Write-Host "   请手动停止开发服务器并重新启动" -ForegroundColor Yellow
}

Write-Host "5. 测试指南..." -ForegroundColor Yellow
Write-Host "   1. 启动前端开发服务器: cd frontend && npm run dev" -ForegroundColor Cyan
Write-Host "   2. 打开浏览器访问应用" -ForegroundColor Cyan
Write-Host "   3. 登录系统" -ForegroundColor Cyan
Write-Host "   4. 点击右上角用户头像 -> 退出登录" -ForegroundColor Cyan
Write-Host "   5. 验证是否正确跳转到登录页面" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== 修复完成 ===" -ForegroundColor Green
Write-Host "如果问题仍然存在，请查看 test-logout-fix.html 获取更多调试信息" -ForegroundColor Yellow

# 打开测试页面
if (Test-Path "test-logout-fix.html") {
    Write-Host "正在打开测试页面..." -ForegroundColor Yellow
    Start-Process "test-logout-fix.html"
}