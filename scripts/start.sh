#!/bin/bash

# AI Agent Demo 启动脚本

set -e

echo "🚀 AI Agent Demo 启动脚本"
echo "================================"

# 检查环境变量
if [ -z "$OPENAI_API_KEY" ]; then
    echo "❌ 错误: 未设置 OPENAI_API_KEY 环境变量"
    echo "请设置环境变量: export OPENAI_API_KEY=你的API密钥"
    exit 1
fi

echo "✅ OpenAI API Key 已配置"

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java"
    echo "请安装Java 8或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
echo "✅ Java版本: $JAVA_VERSION"

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven"
    echo "请安装Maven 3.6或更高版本"
    exit 1
fi

echo "✅ Maven已安装"

# 编译项目
echo "🔨 编译项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "✅ 编译成功"

# 创建workspace目录
mkdir -p workspace

# 启动应用
echo "🌐 启动应用..."
echo "📱 访问地址: http://localhost:8080"
echo "🔧 API文档: http://localhost:8080/swagger-ui.html"
echo "📊 健康检查: http://localhost:8080/actuator/health"
echo ""
echo "按 Ctrl+C 停止应用"
echo "================================"

java -jar target/ai-agent-demo-langchain4j-1.0-SNAPSHOT.jar 