#!/bin/bash

# LangChain4j AI Agent Demo 运行脚本
# 使用方法: ./scripts/run.sh [模式]

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_message() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}  LangChain4j AI Agent Demo${NC}"
    echo -e "${BLUE}================================${NC}"
}

# 检查环境
check_environment() {
    print_message "检查运行环境..."
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        print_error "Java 未安装或不在 PATH 中"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_message "Java 版本: $java_version"
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装或不在 PATH 中"
        exit 1
    fi
    
    mvn_version=$(mvn -version 2>&1 | head -n 1)
    print_message "Maven: $mvn_version"
    
    # 检查 API Key
    if [ -z "$OPENAI_API_KEY" ]; then
        print_error "未设置 OPENAI_API_KEY 环境变量"
        print_message "请设置环境变量: export OPENAI_API_KEY=你的API密钥"
        exit 1
    fi
    
    print_message "环境检查通过 ✓"
}

# 编译项目
build_project() {
    print_message "编译项目..."
    mvn clean package -DskipTests
    print_message "编译完成 ✓"
}

# 运行项目
run_project() {
    local mode=${1:-interactive}
    
    print_message "启动项目 (模式: $mode)..."
    
    case $mode in
        "demo")
            java -jar target/ai-agent-demo-langchain4j-1.0-SNAPSHOT.jar demo
            ;;
        "interactive"|"chat")
            java -jar target/ai-agent-demo-langchain4j-1.0-SNAPSHOT.jar interactive
            ;;
        "help")
            java -jar target/ai-agent-demo-langchain4j-1.0-SNAPSHOT.jar help
            ;;
        *)
            print_error "未知模式: $mode"
            print_message "可用模式: demo, interactive, help"
            exit 1
            ;;
    esac
}

# 显示帮助信息
show_help() {
    echo "使用方法: $0 [选项] [模式]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -b, --build    仅编译项目"
    echo "  -c, --check    仅检查环境"
    echo ""
    echo "模式:"
    echo "  demo           运行演示模式"
    echo "  interactive    运行交互模式 (默认)"
    echo "  help           显示程序帮助"
    echo ""
    echo "示例:"
    echo "  $0                    # 交互模式"
    echo "  $0 demo               # 演示模式"
    echo "  $0 -b                 # 仅编译"
    echo "  $0 -c                 # 仅检查环境"
}

# 主函数
main() {
    print_header
    
    # 解析参数
    BUILD_ONLY=false
    CHECK_ONLY=false
    MODE="interactive"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -b|--build)
                BUILD_ONLY=true
                shift
                ;;
            -c|--check)
                CHECK_ONLY=true
                shift
                ;;
            demo|interactive|help)
                MODE=$1
                shift
                ;;
            *)
                print_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 检查环境
    check_environment
    
    if [ "$CHECK_ONLY" = true ]; then
        print_message "环境检查完成"
        exit 0
    fi
    
    # 编译项目
    build_project
    
    if [ "$BUILD_ONLY" = true ]; then
        print_message "编译完成"
        exit 0
    fi
    
    # 运行项目
    run_project "$MODE"
}

# 执行主函数
main "$@" 