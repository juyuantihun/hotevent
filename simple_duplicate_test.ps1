# 简单的重复请求测试
$baseUrl = "http://localhost:8080"

$body = @'
{
    "name": "重复测试时间线",
    "description": "测试防重复功能",
    "regionIds": [42],
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-12-31T23:59:59"
}
'@

Write-Host "=== 测试防重复创建功能 ===" -ForegroundColor Green

# 第一次请求
Write-Host "`n1. 第一次请求..." -ForegroundColor Yellow
try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl/api/timelines/generate/async" -Method POST -Body $body -ContentType "application/json"
    $id1 = $response1.data.id
    Write-Host "第一次请求成功，ID: $id1" -ForegroundColor Green
} catch {
    Write-Host "第一次请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 等待1秒
Start-Sleep -Seconds 1

# 第二次请求
Write-Host "`n2. 第二次请求..." -ForegroundColor Yellow
try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/api/timelines/generate/async" -Method POST -Body $body -ContentType "application/json"
    $id2 = $response2.data.id
    Write-Host "第二次请求成功，ID: $id2" -ForegroundColor Green
} catch {
    Write-Host "第二次请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 验证结果
Write-Host "`n=== 结果验证 ===" -ForegroundColor Green
if ($id1 -eq $id2) {
    Write-Host "✅ 防重复机制工作正常！两次请求返回相同ID: $id1" -ForegroundColor Green
} else {
    Write-Host "❌ 防重复机制失效！创建了不同的时间线:" -ForegroundColor Red
    Write-Host "第一次ID: $id1" -ForegroundColor Red
    Write-Host "第二次ID: $id2" -ForegroundColor Red
}

Write-Host "`n测试完成！" -ForegroundColor Green