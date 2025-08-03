@echo off
chcp 65001 >nul

REM DeepSeek联网搜索功能启动脚本 (Windows版本)
REM 使用方法: start-with-websearch.bat [your_deepseek_api_key]

echo ==========================================
echo   DeepSeek联网搜索功能启动脚本
echo ==========================================

REM 检查是否提供了API密钥参数
if "%1"=="" (
    echo 请提供DeepSeek API密钥作为参数
    echo 使用方法: start-with-websearch.bat your_deepseek_api_key
    echo.
    echo 或者设置环境变量:
    echo set DEEPSEEK_API_KEY=your_deepseek_api_key
    echo start-with-websearch.bat
    pause
    exit /b 1
)

REM 设置API密钥
set DEEPSEEK_API_KEY=%1

echo ✓ 设置DeepSeek API密钥: %DEEPSEEK_API_KEY:~0,10%...

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Java环境，请先安装Java 17或更高版本
    pause
    exit /b 1
)

echo ✓ Java环境检查通过

REM 检查Maven环境
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Maven环境，请先安装Maven
    pause
    exit /b 1
)

echo ✓ Maven环境检查通过

REM 构建项目
echo.
echo 开始构建项目...
mvn clean compile -q
if errorlevel 1 (
    echo ❌ 项目构建失败
    pause
    exit /b 1
)

echo ✓ 项目构建成功

REM 设置JVM参数
set JAVA_OPTS=-Xms512m -Xmx2g -Dspring.profiles.active=dev

REM 设置联网搜索相关的环境变量
set APP_DEEPSEEK_WEB_SEARCH_ENABLED=true
set APP_DEEPSEEK_WEB_SEARCH_MAX_RESULTS=10
set APP_DEEPSEEK_WEB_SEARCH_SEARCH_TIMEOUT=30000

echo.
echo 启动参数:
echo   - DeepSeek API密钥: %DEEPSEEK_API_KEY:~0,10%...
echo   - 联网搜索: 启用
echo   - 最大搜索结果: 10
echo   - 搜索超时: 30秒
echo   - JVM参数: %JAVA_OPTS%

echo.
echo 正在启动应用...
echo 应用启动后可访问:
echo   - 主页: http://localhost:8080
echo   - 联网搜索管理: http://localhost:8080/#/websearch
echo   - API文档: http://localhost:8080/doc.html

echo.
echo 按 Ctrl+C 停止应用
echo ==========================================

REM 启动应用
mvn spring-boot:run ^
    -Dspring-boot.run.jvmArguments="%JAVA_OPTS%" ^
    -Dspring-boot.run.arguments="--app.deepseek.api-key=%DEEPSEEK_API_KEY% --app.deepseek.web-search.enabled=true"

pause