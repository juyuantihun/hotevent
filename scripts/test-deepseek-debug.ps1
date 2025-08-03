# 测试DeepSeek调试接口
Write-Host "测试DeepSeek调试接口..." -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# 测试接口列表
$endpoints = @(
    "/api/debug/deepseek/connection",
    "/api/debug/deepseek/config",
    "/api/debug/deepseek/stats"
)

foreach ($endpoint in $endpoints) {
    $url = $baseUrl + $endpoint
    Write-Host "测试接口: $url" -ForegroundColor Yellow
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method GET -TimeoutSec 10
        Write-Host "成功!" -ForegroundColor Green
        Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor White
    } catch {
        Write-Host "失败: $($_.Exception.Message)" -ForegroundColor Red
    }
    
    Write-Host "---" -ForegroundColor Gray
}

Write-Host "测试完成。" -ForegroundColor Green
Read-Host "按任意键继续"