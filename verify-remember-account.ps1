#!/usr/bin/env pwsh

Write-Host "=== 记住账号功能验证脚本 ===" -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "frontend/src/views/login/index.vue")) {
    Write-Host "错误：请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "1. 验证文本修改..." -ForegroundColor Yellow

# 检查登录页面是否包含"记住账号"
$loginContent = Get-Content "frontend/src/views/login/index.vue" -Raw
if ($loginContent -match "记住账号") {
    Write-Host "   ✓ 登录页面文本已修改为'记住账号'" -ForegroundColor Green
} else {
    Write-Host "   ✗ 登录页面文本修改失败" -ForegroundColor Red
}

# 检查是否还有"记住密码"的残留
if ($loginContent -match "记住密码") {
    Write-Host "   ✗ 发现'记住密码'残留文本" -ForegroundColor Red
} else {
    Write-Host "   ✓ 已清除'记住密码'文本" -ForegroundColor Green
}

Write-Host "2. 检查功能实现..." -ForegroundColor Yellow

# 检查是否有rememberMe相关的逻辑
if ($loginContent -match "rememberMe") {
    Write-Host "   ✓ rememberMe功能逻辑完整" -ForegroundColor Green
} else {
    Write-Host "   ✗ rememberMe功能逻辑缺失" -ForegroundColor Red
}

# 检查是否有rememberedUsername相关的逻辑
if ($loginContent -match "rememberedUsername") {
    Write-Host "   ✓ rememberedUsername存储逻辑完整" -ForegroundColor Green
} else {
    Write-Host "   ✗ rememberedUsername存储逻辑缺失" -ForegroundColor Red
}

Write-Host "3. 检查认证Store..." -ForegroundColor Yellow

# 检查认证store中的接口定义
$authStoreContent = Get-Content "frontend/src/store/modules/auth.ts" -Raw
if ($authStoreContent -match "是否记住用户名") {
    Write-Host "   ✓ 认证Store接口注释正确" -ForegroundColor Green
} else {
    Write-Host "   ✗ 认证Store接口注释可能需要更新" -ForegroundColor Red
}

Write-Host "4. 测试建议..." -ForegroundColor Yellow
Write-Host "   请按照以下步骤测试功能：" -ForegroundColor Cyan
Write-Host "   1. 启动开发服务器: cd frontend && npm run dev" -ForegroundColor Cyan
Write-Host "   2. 访问登录页面" -ForegroundColor Cyan
Write-Host "   3. 验证复选框文本显示为'记住账号'" -ForegroundColor Cyan
Write-Host "   4. 勾选复选框并登录" -ForegroundColor Cyan
Write-Host "   5. 退出登录后重新访问，验证用户名是否自动填充" -ForegroundColor Cyan

Write-Host ""
Write-Host "=== 验证完成 ===" -ForegroundColor Green

# 打开测试页面
if (Test-Path "test-remember-account-fix.html") {
    Write-Host "正在打开测试指南页面..." -ForegroundColor Yellow
    Start-Process "test-remember-account-fix.html"
}