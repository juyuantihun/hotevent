#!/usr/bin/env pwsh

# 启动调试脚本
Write-Host "开始启动Hot Events应用..." -ForegroundColor Green

# 设置Java选项
$env:JAVA_OPTS = "-Xmx2g -Xms1g -Dspring.profiles.active=dev -Dlogging.level.com.hotech.events=DEBUG"

# 启动应用
try {
    Write-Host "正在启动应用，请等待..." -ForegroundColor Yellow
    
    # 使用Maven启动
    mvn spring-boot:run -f pom.xml -Dspring-boot.run.jvmArguments="-Xmx2g -Xms1g -Dspring.profiles.active=dev"
    
} catch {
    Write-Host "启动失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "错误详情: $($_.Exception.StackTrace)" -ForegroundColor Red
}

Write-Host "脚本执行完成" -ForegroundColor Green