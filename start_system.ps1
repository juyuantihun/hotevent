#!/usr/bin/env pwsh

# 热点事件系统启动脚本
Write-Host "=== 启动热点事件系统 ===" -ForegroundColor Green

# 检查Java环境
Write-Host "`n检查Java环境..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✓ Java环境正常: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java环境检查失败，请确保已安装Java 17+" -ForegroundColor Red
    exit 1
}

# 检查Node.js环境
Write-Host "`n检查Node.js环境..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✓ Node.js环境正常: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Node.js环境检查失败，请确保已安装Node.js" -ForegroundColor Red
    exit 1
}

# 启动后端服务
Write-Host "`n启动后端服务..." -ForegroundColor Yellow
Write-Host "正在编译和启动Spring Boot应用..." -ForegroundColor Cyan

# 在新窗口中启动后端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; mvn clean spring-boot:run"

# 等待后端启动
Write-Host "等待后端服务启动..." -ForegroundColor Yellow
$timeout = 60
$elapsed = 0
do {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $backendStatus = Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($backendStatus) {
        Write-Host "✓ 后端服务启动成功" -ForegroundColor Green
        break
    }
    Write-Host "." -NoNewline
} while ($elapsed -lt $timeout)

if (-not $backendStatus) {
    Write-Host "`n✗ 后端服务启动超时" -ForegroundColor Red
    Write-Host "请检查控制台输出或手动启动：mvn spring-boot:run" -ForegroundColor Yellow
}

# 启动前端服务
Write-Host "`n启动前端服务..." -ForegroundColor Yellow
Write-Host "正在启动Vite开发服务器..." -ForegroundColor Cyan

# 在新窗口中启动前端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD/frontend'; npm run dev"

# 等待前端启动
Write-Host "等待前端服务启动..." -ForegroundColor Yellow
$timeout = 30
$elapsed = 0
do {
    Start-Sleep -Seconds 2
    $elapsed += 2
    $frontendStatus = Test-NetConnection -ComputerName localhost -Port 5173 -InformationLevel Quiet -WarningAction SilentlyContinue
    if ($frontendStatus) {
        Write-Host "✓ 前端服务启动成功" -ForegroundColor Green
        break
    }
    Write-Host "." -NoNewline
} while ($elapsed -lt $timeout)

if (-not $frontendStatus) {
    Write-Host "`n✗ 前端服务启动超时" -ForegroundColor Red
    Write-Host "请检查控制台输出或手动启动：cd frontend && npm run dev" -ForegroundColor Yellow
}

# 显示访问信息
Write-Host "`n=== 系统启动完成 ===" -ForegroundColor Green
if ($backendStatus -and $frontendStatus) {
    Write-Host "✓ 系统已成功启动" -ForegroundColor Green
    Write-Host "`n访问地址：" -ForegroundColor Cyan
    Write-Host "  前端应用：http://localhost:5173" -ForegroundColor White
    Write-Host "  后端API：http://localhost:8080" -ForegroundColor White
    Write-Host "  API文档：http://localhost:8080/swagger-ui.html" -ForegroundColor White
    
    Write-Host "`n测试账号：" -ForegroundColor Cyan
    Write-Host "  用户名：admin" -ForegroundColor White
    Write-Host "  密码：123456" -ForegroundColor White
    
    # 自动打开浏览器
    Write-Host "`n正在打开浏览器..." -ForegroundColor Yellow
    Start-Process "http://localhost:5173"
} else {
    Write-Host "✗ 系统启动不完整，请检查控制台输出" -ForegroundColor Red
}

Write-Host "`n按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")