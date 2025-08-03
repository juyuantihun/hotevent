# 执行region数据插入SQL
Write-Host "开始执行region数据插入..."

# 读取SQL文件内容
$sqlContent = Get-Content -Path "simple_region_insert.sql" -Raw

# 分割SQL语句（按分号分割）
$sqlStatements = $sqlContent -split ';' | Where-Object { $_.Trim() -ne '' -and -not $_.Trim().StartsWith('--') }

foreach ($sql in $sqlStatements) {
    $sql = $sql.Trim()
    if ($sql -eq '') { continue }
    
    Write-Host "执行SQL: $($sql.Substring(0, [Math]::Min(50, $sql.Length)))..."
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/test/execute-sql" -Method POST -Body $sql -ContentType "text/plain" -TimeoutSec 30
        
        if ($response.code -eq 200) {
            Write-Host "✓ 执行成功"
        } else {
            Write-Host "✗ 执行失败: $($response.msg)"
        }
    } catch {
        Write-Host "✗ 请求失败: $($_.Exception.Message)"
    }
    
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "测试region API..."

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/regions/tree" -Method GET -TimeoutSec 10
    
    if ($response.code -eq 200) {
        Write-Host "✓ Region API调用成功"
        Write-Host "地区数量: $($response.data.Count)"
        
        if ($response.data.Count -gt 0) {
            Write-Host "前5个地区:"
            $response.data | Select-Object -First 5 | ForEach-Object {
                Write-Host "  - ID: $($_.id), 名称: $($_.name), 类型: $($_.type), 父ID: $($_.parentId)"
            }
        }
    } else {
        Write-Host "✗ Region API调用失败: $($response.msg)"
    }
} catch {
    Write-Host "✗ Region API请求失败: $($_.Exception.Message)"
}