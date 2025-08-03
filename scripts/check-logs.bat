@echo off
chcp 65001 >nul

echo ==========================================
echo   检查DeepSeek调试日志
echo ==========================================

echo 正在查找最新的日志文件...

REM 检查常见的日志位置
if exist "logs\hot-events.log" (
    echo 找到日志文件: logs\hot-events.log
    echo.
    echo === 最近的DeepSeek相关日志 ===
    powershell -Command "Get-Content 'logs\hot-events.log' | Select-String -Pattern '响应调试|DeepSeek|事件|解析|JSON' | Select-Object -Last 20"
) else if exist "target\logs\hot-events.log" (
    echo 找到日志文件: target\logs\hot-events.log
    echo.
    echo === 最近的DeepSeek相关日志 ===
    powershell -Command "Get-Content 'target\logs\hot-events.log' | Select-String -Pattern '响应调试|DeepSeek|事件|解析|JSON' | Select-Object -Last 20"
) else (
    echo 未找到日志文件，检查控制台输出...
    echo.
    echo 请查看应用启动的控制台窗口中的日志输出
    echo 寻找包含以下关键词的日志：
    echo - "响应调试"
    echo - "DeepSeek"
    echo - "事件解析"
    echo - "JSON"
)

echo.
echo ==========================================
echo   手动检查建议
echo ==========================================
echo 1. 查看应用控制台输出
echo 2. 寻找 "=== DeepSeek响应调试开始 ===" 的日志
echo 3. 查看原始响应内容
echo 4. 检查JSON解析错误信息

pause