@echo off
chcp 65001 >nul

echo ==========================================
echo   测试联网搜索页面
echo ==========================================

echo 正在测试联网搜索页面访问...

REM 测试页面是否可访问
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/websearch' -Method GET -TimeoutSec 10; if ($response.StatusCode -eq 200) { Write-Host '✓ 联网搜索页面访问成功' -ForegroundColor Green; Write-Host '页面长度:' $response.Content.Length '字符' -ForegroundColor Cyan } else { Write-Host '❌ 页面访问失败，状态码:' $response.StatusCode -ForegroundColor Red } } catch { Write-Host '❌ 页面访问异常:' $_.Exception.Message -ForegroundColor Red }"

echo.
echo 正在测试API接口...

REM 测试API状态接口
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/web-search/status' -Method GET -TimeoutSec 10; $result = $response.Content | ConvertFrom-Json; Write-Host '✓ API状态接口正常' -ForegroundColor Green; Write-Host '联网搜索可用:' $result.available -ForegroundColor Cyan } catch { Write-Host '❌ API接口异常:' $_.Exception.Message -ForegroundColor Red }"

echo.
echo 正在测试原始API调试接口...

REM 测试原始API接口
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/api/debug/deepseek/test-raw-api?query=测试' -Method POST -TimeoutSec 30; $result = $response.Content | ConvertFrom-Json; Write-Host '✓ 原始API测试接口正常' -ForegroundColor Green; Write-Host '测试结果:' $result.success -ForegroundColor Cyan; if ($result.analysis) { Write-Host '响应类型:' $result.analysis.type -ForegroundColor Yellow } } catch { Write-Host '❌ 原始API测试异常:' $_.Exception.Message -ForegroundColor Red }"

echo.
echo ==========================================
echo   测试完成
echo ==========================================
echo.
echo 如果所有测试都通过，请访问:
echo http://localhost:8080/websearch
echo.
echo 然后点击 "测试原始API" 按钮进行详细诊断
echo.

pause