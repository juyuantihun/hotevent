#!/bin/bash

# DeepSeek联网搜索功能启动脚本
# 使用方法: ./start-with-websearch.sh [your_deepseek_api_key]

echo "=========================================="
echo "  DeepSeek联网搜索功能启动脚本"
echo "=========================================="

# 检查是否提供了API密钥参数
if [ -z "$1" ]; then
    echo "请提供DeepSeek API密钥作为参数"
    echo "使用方法: ./start-with-websearch.sh your_deepseek_api_key"
    echo ""
    echo "或者设置环境变量:"
    echo "export DEEPSEEK_API_KEY=your_deepseek_api_key"
    echo "./start-with-websearch.sh"
    exit 1
fi

# 设置API密钥
export DEEPSEEK_API_KEY=$1

echo "✓ 设置DeepSeek API密钥: ${DEEPSEEK_API_KEY:0:10}..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境，请先安装Java 17或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ 错误: Java版本过低，需要Java 17或更高版本"
    exit 1
fi

echo "✓ Java环境检查通过: Java $JAVA_VERSION"

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

echo "✓ Maven环境检查通过"

# 检查MySQL连接
echo "检查MySQL数据库连接..."
if ! command -v mysql &> /dev/null; then
    echo "⚠️  警告: 未找到MySQL客户端，无法检查数据库连接"
else
    # 尝试连接数据库（使用配置文件中的默认设置）
    if mysql -h localhost -P 3307 -u root -pxj2022 -e "SELECT 1;" &> /dev/null; then
        echo "✓ MySQL数据库连接正常"
    else
        echo "⚠️  警告: 无法连接到MySQL数据库，请检查数据库配置"
        echo "   默认配置: localhost:3307, 用户名: root, 密码: xj2022"
    fi
fi

# 构建项目
echo ""
echo "开始构建项目..."
if mvn clean compile -q; then
    echo "✓ 项目构建成功"
else
    echo "❌ 项目构建失败"
    exit 1
fi

# 设置JVM参数
export JAVA_OPTS="-Xms512m -Xmx2g -Dspring.profiles.active=dev"

# 设置联网搜索相关的环境变量
export APP_DEEPSEEK_WEB_SEARCH_ENABLED=true
export APP_DEEPSEEK_WEB_SEARCH_MAX_RESULTS=10
export APP_DEEPSEEK_WEB_SEARCH_SEARCH_TIMEOUT=30000

echo ""
echo "启动参数:"
echo "  - DeepSeek API密钥: ${DEEPSEEK_API_KEY:0:10}..."
echo "  - 联网搜索: 启用"
echo "  - 最大搜索结果: 10"
echo "  - 搜索超时: 30秒"
echo "  - JVM参数: $JAVA_OPTS"

echo ""
echo "正在启动应用..."
echo "应用启动后可访问:"
echo "  - 主页: http://localhost:8080"
echo "  - 联网搜索管理: http://localhost:8080/#/websearch"
echo "  - API文档: http://localhost:8080/doc.html"

echo ""
echo "按 Ctrl+C 停止应用"
echo "=========================================="

# 启动应用
mvn spring-boot:run \
    -Dspring-boot.run.jvmArguments="$JAVA_OPTS" \
    -Dspring-boot.run.arguments="--app.deepseek.api-key=$DEEPSEEK_API_KEY --app.deepseek.web-search.enabled=true"