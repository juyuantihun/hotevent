# 执行API调用记录表创建脚本
# 使用方法: .\execute_api_call_record_sql.ps1

Write-Host "正在执行API调用记录表创建脚本..." -ForegroundColor Green

# 检查SQL文件是否存在
$sqlFile = "src\main\resources\sql\api_call_record.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "错误: SQL文件不存在: $sqlFile" -ForegroundColor Red
    exit 1
}

# 读取SQL文件内容
$sqlContent = Get-Content $sqlFile -Raw

Write-Host "SQL文件内容:" -ForegroundColor Yellow
Write-Host $sqlContent

Write-Host "`n请手动执行以下步骤:" -ForegroundColor Cyan
Write-Host "1. 连接到MySQL数据库"
Write-Host "2. 选择hot_events_db数据库: USE hot_events_db;"
Write-Host "3. 执行上述SQL语句"

Write-Host "`n或者使用以下命令（如果MySQL在PATH中）:" -ForegroundColor Cyan
Write-Host "mysql -u root -p hot_events_db < $sqlFile"

Write-Host "`n脚本执行完成。" -ForegroundColor Green