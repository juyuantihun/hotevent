@echo off
chcp 65001 >nul

echo ==========================================
echo   前端应用构建脚本
echo ==========================================

REM 检查Node.js环境
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Node.js环境，请先安装Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

echo ✓ Node.js环境检查通过

REM 检查npm环境
npm --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到npm环境
    pause
    exit /b 1
)

echo ✓ npm环境检查通过

REM 进入前端目录
cd /d "%~dp0..\frontend"

REM 检查package.json是否存在
if not exist "package.json" (
    echo ❌ 错误: 未找到package.json文件
    echo 请确保在正确的前端项目目录中
    pause
    exit /b 1
)

echo ✓ 前端项目目录检查通过

REM 安装依赖
echo.
echo 正在安装前端依赖...
npm install
if errorlevel 1 (
    echo ❌ 依赖安装失败
    pause
    exit /b 1
)

echo ✓ 前端依赖安装成功

REM 构建前端应用
echo.
echo 正在构建前端应用...
npm run build
if errorlevel 1 (
    echo ❌ 前端构建失败
    pause
    exit /b 1
)

echo ✓ 前端构建成功

REM 检查构建产物
if not exist "dist" (
    echo ❌ 错误: 未找到构建产物目录 dist
    pause
    exit /b 1
)

echo ✓ 构建产物检查通过

REM 清理目标目录
cd /d "%~dp0.."
if exist "src\main\resources\static" (
    echo 清理旧的静态资源...
    rmdir /s /q "src\main\resources\static"
)

REM 创建静态资源目录
mkdir "src\main\resources\static"

REM 复制构建产物
echo.
echo 正在复制构建产物到Spring Boot静态资源目录...
xcopy /e /i /y "frontend\dist\*" "src\main\resources\static\"
if errorlevel 1 (
    echo ❌ 复制构建产物失败
    pause
    exit /b 1
)

echo ✓ 构建产物复制成功

echo.
echo ==========================================
echo   前端构建完成！
echo ==========================================
echo.
echo 下一步：
echo 1. 重启Spring Boot应用
echo 2. 访问: http://localhost:8080
echo 3. 联网搜索页面: http://localhost:8080/#/websearch
echo.

pause