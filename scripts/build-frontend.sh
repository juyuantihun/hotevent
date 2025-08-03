#!/bin/bash

# 前端应用构建脚本

echo "=========================================="
echo "  前端应用构建脚本"
echo "=========================================="

# 检查Node.js环境
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js环境，请先安装Node.js"
    echo "下载地址: https://nodejs.org/"
    exit 1
fi

echo "✓ Node.js环境检查通过: $(node --version)"

# 检查npm环境
if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到npm环境"
    exit 1
fi

echo "✓ npm环境检查通过: $(npm --version)"

# 进入前端目录
cd "$(dirname "$0")/../frontend" || exit 1

# 检查package.json是否存在
if [ ! -f "package.json" ]; then
    echo "❌ 错误: 未找到package.json文件"
    echo "请确保在正确的前端项目目录中"
    exit 1
fi

echo "✓ 前端项目目录检查通过"

# 安装依赖
echo ""
echo "正在安装前端依赖..."
if ! npm install; then
    echo "❌ 依赖安装失败"
    exit 1
fi

echo "✓ 前端依赖安装成功"

# 构建前端应用
echo ""
echo "正在构建前端应用..."
if ! npm run build; then
    echo "❌ 前端构建失败"
    exit 1
fi

echo "✓ 前端构建成功"

# 检查构建产物
if [ ! -d "dist" ]; then
    echo "❌ 错误: 未找到构建产物目录 dist"
    exit 1
fi

echo "✓ 构建产物检查通过"

# 返回项目根目录
cd ..

# 清理目标目录
if [ -d "src/main/resources/static" ]; then
    echo "清理旧的静态资源..."
    rm -rf "src/main/resources/static"
fi

# 创建静态资源目录
mkdir -p "src/main/resources/static"

# 复制构建产物
echo ""
echo "正在复制构建产物到Spring Boot静态资源目录..."
if ! cp -r frontend/dist/* src/main/resources/static/; then
    echo "❌ 复制构建产物失败"
    exit 1
fi

echo "✓ 构建产物复制成功"

echo ""
echo "=========================================="
echo "  前端构建完成！"
echo "=========================================="
echo ""
echo "下一步："
echo "1. 重启Spring Boot应用"
echo "2. 访问: http://localhost:8080"
echo "3. 联网搜索页面: http://localhost:8080/#/websearch"
echo ""