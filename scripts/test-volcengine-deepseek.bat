@echo off
echo 测试火山方舟DeepSeek API连接...

REM 设置变量
set API_URL=https://ark.cn-beijing.volces.com/api/v3/chat/completions
set API_KEY=314de2f8-ecd5-4311-825b-65e0233e350e
set MODEL=deepseek-r1-250120

REM 使用curl测试API连接
curl -X POST %API_URL% ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %API_KEY%" ^
  -d "{\"model\": \"%MODEL%\", \"messages\": [{\"role\": \"system\", \"content\": \"你是一个专业的事件分析助手。\"}, {\"role\": \"user\", \"content\": \"请简单介绍一下你的功能。\"}], \"max_tokens\": 100, \"temperature\": 0.7}"

echo.
echo 测试完成。
pause