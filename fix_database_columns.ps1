#!/usr/bin/env pwsh

# 数据库字段修复脚本
Write-Host "=== 修复数据库缺失字段 ===" -ForegroundColor Green

# 数据库连接参数
$dbHost = "localhost"
$dbPort = "3306"
$dbName = "hot_events_db"
$dbUser = "root"
$dbPassword = "123456"

# SQL文件路径
$sqlFile = "add_missing_event_columns.sql"

Write-Host "`n检查SQL文件..." -ForegroundColor Yellow
if (-not (Test-Path $sqlFile)) {
    Write-Host "✗ SQL文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}
Write-Host "✓ SQL文件存在" -ForegroundColor Green

# 检查MySQL客户端
Write-Host "`n检查MySQL客户端..." -ForegroundColor Yellow
try {
    $mysqlVersion = mysql --version 2>&1
    Write-Host "✓ MySQL客户端可用: $mysqlVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ MySQL客户端不可用，请确保已安装MySQL客户端" -ForegroundColor Red
    Write-Host "或者手动执行SQL文件中的语句" -ForegroundColor Yellow
    exit 1
}

# 执行SQL脚本
Write-Host "`n执行SQL修复脚本..." -ForegroundColor Yellow
try {
    # 构建MySQL命令
    $mysqlCmd = "mysql -h$dbHost -P$dbPort -u$dbUser -p$dbPassword $dbName < $sqlFile"
    
    Write-Host "正在执行: $mysqlCmd" -ForegroundColor Cyan
    
    # 执行命令
    $result = cmd /c "mysql -h$dbHost -P$dbPort -u$dbUser -p$dbPassword $dbName < $sqlFile 2>&1"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ SQL脚本执行成功" -ForegroundColor Green
        Write-Host "已添加以下字段到event表:" -ForegroundColor Cyan
        Write-Host "  - event_title (事件标题)" -ForegroundColor White
        Write-Host "  - credibility_score (可信度评分)" -ForegroundColor White
        Write-Host "  - validation_status (验证状态)" -ForegroundColor White
        Write-Host "  - source_urls (来源URL)" -ForegroundColor White
        Write-Host "  - fetch_method (获取方法)" -ForegroundColor White
        Write-Host "  - last_validated_at (最后验证时间)" -ForegroundColor White
        Write-Host "  - subject_coordinate_id (主体坐标ID)" -ForegroundColor White
        Write-Host "  - object_coordinate_id (客体坐标ID)" -ForegroundColor White
        Write-Host "  - event_coordinate_id (事件坐标ID)" -ForegroundColor White
        Write-Host "  - geographic_status (地理状态)" -ForegroundColor White
        Write-Host "  - geographic_updated_at (地理信息更新时间)" -ForegroundColor White
    } else {
        Write-Host "✗ SQL脚本执行失败" -ForegroundColor Red
        Write-Host "错误信息: $result" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ 执行SQL脚本时发生错误: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 验证字段是否添加成功
Write-Host "`n验证字段添加结果..." -ForegroundColor Yellow
try {
    $verifyCmd = "mysql -h$dbHost -P$dbPort -u$dbUser -p$dbPassword -e `"DESCRIBE $dbName.event;`""
    $tableStructure = cmd /c $verifyCmd
    
    if ($tableStructure -match "subject_coordinate_id" -and 
        $tableStructure -match "object_coordinate_id" -and 
        $tableStructure -match "event_coordinate_id") {
        Write-Host "✓ 关键字段验证成功" -ForegroundColor Green
    } else {
        Write-Host "✗ 字段验证失败，请检查数据库" -ForegroundColor Red
    }
} catch {
    Write-Host "⚠ 无法验证字段，请手动检查数据库" -ForegroundColor Yellow
}

Write-Host "`n=== 修复完成 ===" -ForegroundColor Green
Write-Host "现在可以重启后端服务来测试修复结果" -ForegroundColor Cyan
Write-Host "重启命令: mvn spring-boot:run" -ForegroundColor White

Write-Host "`n按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")