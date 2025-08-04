#!/usr/bin/env pwsh

Write-Host "=== 登录跳转功能测试脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "frontend/src/views/login/index.vue")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 验证修复是否生效..." -ForegroundColor Yellow

# 检查登录页面是否已修复
$loginContent = Get-Content "frontend/src/views/login/index.vue" -Raw
if ($loginContent -match "window\.location\.href = redirectPath") {
    Write-Host "   ✓ 登录页面跳转已修复" -ForegroundColor Green
} else {
    Write-Host "   ✗ 登录页面跳转修复可能未生效" -ForegroundColor Red
}

# 检查路由守卫是否正常
$routerContent = Get-Content "frontend/src/router/index.ts" -Raw
if ($routerContent -match "next\(\{ path: '/dashboard' \}\)") {
    Write-Host "   ✓ 路由守卫逻辑正常" -ForegroundColor Green
} else {
    Write-Host "   ✗ 路由守卫可能有问题" -ForegroundColor Red
}

Write-Host "2. 检查开发服务器状态..." -ForegroundColor Yellow

# 检查是否有运行中的开发服务器
$nodeProcesses = Get-Process -Name "node" -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -eq "node" }
if ($nodeProcesses) {
    Write-Host "   发现运行中的Node.js进程" -ForegroundColor Yellow
    Write-Host "   建议重启开发服务器以应用修复" -ForegroundColor Yellow
    
    $restart = Read-Host "是否要重启开发服务器？(y/n)"
    if ($restart -eq "y" -or $restart -eq "Y") {
        Write-Host "   正在停止现有进程..." -ForegroundColor Yellow
        $nodeProcesses | Stop-Process -Force
        Start-Sleep -Seconds 2
        
        Write-Host "   正在启动开发服务器..." -ForegroundColor Yellow
        Set-Location "frontend"
        Start-Process -FilePath "npm" -ArgumentList "run", "dev" -NoNewWindow
        Set-Location ".."
        Write-Host "   开发服务器已启动" -ForegroundColor Green
    }
} else {
    Write-Host "   未发现运行中的开发服务器" -ForegroundColor Yellow
    $start = Read-Host "是否要启动开发服务器？(y/n)"
    if ($start -eq "y" -or $start -eq "Y") {
        Write-Host "   正在启动开发服务器..." -ForegroundColor Yellow
        Set-Location "frontend"
        Start-Process -FilePath "npm" -ArgumentList "run", "dev"
        Set-Location ".."
        Write-Host "   开发服务器已启动" -ForegroundColor Green
    }
}

Write-Host "3. 测试指南..." -ForegroundColor Yellow
Write-Host "   请按照以下步骤测试登录跳转功能：" -ForegroundColor Cyan
Write-Host "   1. 打开浏览器访问 http://localhost:3000" -ForegroundColor Cyan
Write-Host "   2. 如果已登录，先退出登录" -ForegroundColor Cyan
Write-Host "   3. 在登录页面输入用户名和密码" -ForegroundColor Cyan
Write-Host "   4. 点击登录按钮" -ForegroundColor Cyan
Write-Host "   5. 观察是否显示'登录成功'消息" -ForegroundColor Cyan
Write-Host "   6. 验证是否在500ms后自动跳转到dashboard" -ForegroundColor Cyan

Write-Host "4. 调试信息..." -ForegroundColor Yellow
Write-Host "   如果问题仍然存在，请检查浏览器控制台：" -ForegroundColor Cyan
Write-Host "   - 查看是否有JavaScript错误" -ForegroundColor Cyan
Write-Host "   - 检查'登录成功，当前认证状态'日志" -ForegroundColor Cyan
Write-Host "   - 检查'准备跳转到'日志" -ForegroundColor Cyan
Write-Host "   - 检查网络请求是否正常" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== 测试准备完成 ===" -ForegroundColor Green

# 打开测试页面
if (Test-Path "test-login-redirect-fix.html") {
    Write-Host "正在打开测试指南页面..." -ForegroundColor Yellow
    Start-Process "test-login-redirect-fix.html"
}

Write-Host "祝测试顺利！" -ForegroundColor Green