# 测试火山方舟DeepSeek API连接
Write-Host "测试火山方舟DeepSeek API连接..." -ForegroundColor Green

# 设置变量
$apiUrl = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
$apiKey = "314de2f8-ecd5-4311-825b-65e0233e350e"
$model = "deepseek-r1-250120"

# 构建请求体
$requestBody = @{
    model = $model
    messages = @(
        @{
            role = "system"
            content = "你是一个专业的事件分析助手。"
        },
        @{
            role = "user"
            content = "请简单介绍一下你的功能，用中文回答。"
        }
    )
    max_tokens = 100
    temperature = 0.7
} | ConvertTo-Json -Depth 3

# 设置请求头
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $apiKey"
}

Write-Host "API URL: $apiUrl" -ForegroundColor Yellow
Write-Host "模型: $model" -ForegroundColor Yellow
Write-Host "API Key: $($apiKey.Substring(0,4))****$($apiKey.Substring($apiKey.Length-4))" -ForegroundColor Yellow

try {
    Write-Host "发送请求..." -ForegroundColor Blue
    
    # 发送请求
    $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $requestBody -Headers $headers -TimeoutSec 30
    
    Write-Host "请求成功！" -ForegroundColor Green
    Write-Host "响应内容:" -ForegroundColor Cyan
    
    if ($response.choices -and $response.choices.Count -gt 0) {
        $content = $response.choices[0].message.content
        Write-Host $content -ForegroundColor White
    } else {
        Write-Host "响应格式异常" -ForegroundColor Red
        Write-Host ($response | ConvertTo-Json -Depth 5) -ForegroundColor Gray
    }
    
} catch {
    Write-Host "请求失败！" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "状态码: $statusCode" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "错误详情: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "无法读取错误详情" -ForegroundColor Red
        }
    }
}

Write-Host "`n测试完成。" -ForegroundColor Green
Read-Host "按任意键继续"