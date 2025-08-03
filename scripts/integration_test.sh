#!/bin/bash

# 系统集成测试脚本
# 用于验证事件时间线功能增强系统的各个模块是否正常工作

set -e

# 配置变量
BASE_URL="http://localhost:8080"
API_BASE_URL="${BASE_URL}/api"
FRONTEND_URL="http://localhost:3000"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查服务是否运行
check_service() {
    local url=$1
    local service_name=$2
    
    log_info "检查 ${service_name} 服务状态..."
    
    if curl -s --connect-timeout 5 "${url}" > /dev/null; then
        log_info "${service_name} 服务正常运行"
        return 0
    else
        log_error "${service_name} 服务未运行或无法访问"
        return 1
    fi
}

# 测试API接口
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    local description=$5
    
    log_info "测试: ${description}"
    
    local response
    local status_code
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "${method}" \
            -H "Content-Type: application/json" \
            -d "${data}" \
            "${API_BASE_URL}${endpoint}")
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "${method}" \
            "${API_BASE_URL}${endpoint}")
    fi
    
    status_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [ "$status_code" -eq "$expected_status" ]; then
        log_info "✓ ${description} - 状态码: ${status_code}"
        echo "$body"
        return 0
    else
        log_error "✗ ${description} - 期望状态码: ${expected_status}, 实际状态码: ${status_code}"
        echo "响应内容: $body"
        return 1
    fi
}

# 测试地区管理功能
test_region_management() {
    log_info "开始测试地区管理功能..."
    
    # 测试获取地区列表
    test_api "GET" "/regions?page=1&size=10" "" 200 "获取地区列表"
    
    # 测试创建地区
    local region_data='{
        "name": "测试地区",
        "type": "CUSTOM",
        "description": "集成测试创建的地区",
        "dictionaryIds": []
    }'
    
    local create_response
    create_response=$(test_api "POST" "/regions" "$region_data" 200 "创建地区")
    
    # 提取创建的地区ID
    local region_id
    region_id=$(echo "$create_response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
    
    if [ -n "$region_id" ]; then
        log_info "创建的地区ID: $region_id"
        
        # 测试获取地区详情
        test_api "GET" "/regions/${region_id}" "" 200 "获取地区详情"
        
        # 测试更新地区
        local update_data='{
            "id": '${region_id}',
            "name": "更新的测试地区",
            "type": "CUSTOM",
            "description": "集成测试更新的地区",
            "dictionaryIds": []
        }'
        
        test_api "PUT" "/regions/${region_id}" "$update_data" 200 "更新地区"
        
        # 测试删除地区
        test_api "DELETE" "/regions/${region_id}" "" 200 "删除地区"
    else
        log_warn "无法提取地区ID，跳过后续地区测试"
    fi
    
    log_info "地区管理功能测试完成"
}

# 测试时间线管理功能
test_timeline_management() {
    log_info "开始测试时间线管理功能..."
    
    # 测试获取时间线列表
    test_api "GET" "/timelines?page=1&size=10" "" 200 "获取时间线列表"
    
    # 测试异步生成时间线
    local timeline_data='{
        "name": "测试时间线",
        "description": "集成测试生成的时间线",
        "regionIds": [1],
        "startTime": "'$(date -d '30 days ago' -Iseconds)'",
        "endTime": "'$(date -Iseconds)'"
    }'
    
    local generate_response
    generate_response=$(test_api "POST" "/timelines/generate/async" "$timeline_data" 200 "异步生成时间线")
    
    # 提取生成的时间线ID
    local timeline_id
    timeline_id=$(echo "$generate_response" | grep -o '"id":[0-9]*' | cut -d':' -f2 | head -1)
    
    if [ -n "$timeline_id" ]; then
        log_info "生成的时间线ID: $timeline_id"
        
        # 等待一段时间让时间线生成
        log_info "等待时间线生成..."
        sleep 5
        
        # 测试获取时间线详情
        test_api "GET" "/timelines/${timeline_id}" "" 200 "获取时间线详情"
        
        # 测试获取时间线生成进度
        test_api "GET" "/timelines/${timeline_id}/generation-progress" "" 200 "获取时间线生成进度"
        
        # 测试获取时间线包含的地区
        test_api "GET" "/timelines/${timeline_id}/regions" "" 200 "获取时间线包含的地区"
        
        # 测试获取时间线包含的事件
        test_api "GET" "/timelines/${timeline_id}/events" "" 200 "获取时间线包含的事件"
        
        # 测试获取时间线图形数据
        test_api "GET" "/timelines/${timeline_id}/graph" "" 200 "获取时间线图形数据"
        
        # 测试取消时间线生成（如果还在生成中）
        test_api "POST" "/timelines/${timeline_id}/cancel-generation" "" 200 "取消时间线生成"
        
        # 测试删除时间线
        test_api "DELETE" "/timelines/${timeline_id}" "" 200 "删除时间线"
    else
        log_warn "无法提取时间线ID，跳过后续时间线测试"
    fi
    
    log_info "时间线管理功能测试完成"
}

# 测试数据库连接
test_database_connection() {
    log_info "测试数据库连接..."
    
    # 测试MySQL连接（通过健康检查接口）
    if curl -s "${BASE_URL}/actuator/health" | grep -q "UP"; then
        log_info "✓ 应用健康检查通过"
    else
        log_error "✗ 应用健康检查失败"
        return 1
    fi
    
    log_info "数据库连接测试完成"
}

# 测试前端应用
test_frontend() {
    log_info "测试前端应用..."
    
    if check_service "$FRONTEND_URL" "前端应用"; then
        # 检查前端页面是否包含预期内容
        local page_content
        page_content=$(curl -s "$FRONTEND_URL")
        
        if echo "$page_content" | grep -q "时间线"; then
            log_info "✓ 前端页面内容正常"
        else
            log_warn "前端页面内容可能异常"
        fi
    else
        log_warn "前端应用测试跳过（服务未运行）"
    fi
    
    log_info "前端应用测试完成"
}

# 性能测试
performance_test() {
    log_info "开始性能测试..."
    
    # 测试API响应时间
    local start_time
    local end_time
    local duration
    
    start_time=$(date +%s%N)
    test_api "GET" "/regions?page=1&size=10" "" 200 "性能测试 - 获取地区列表" > /dev/null
    end_time=$(date +%s%N)
    
    duration=$(( (end_time - start_time) / 1000000 ))
    log_info "地区列表API响应时间: ${duration}ms"
    
    if [ "$duration" -lt 1000 ]; then
        log_info "✓ API响应时间正常"
    else
        log_warn "API响应时间较慢: ${duration}ms"
    fi
    
    log_info "性能测试完成"
}

# 主测试函数
main() {
    log_info "开始系统集成测试..."
    log_info "测试目标: ${BASE_URL}"
    
    # 检查后端服务
    if ! check_service "${BASE_URL}/actuator/health" "后端应用"; then
        log_error "后端服务未运行，测试终止"
        exit 1
    fi
    
    # 执行各项测试
    local test_results=()
    
    # 数据库连接测试
    if test_database_connection; then
        test_results+=("数据库连接: ✓")
    else
        test_results+=("数据库连接: ✗")
    fi
    
    # 地区管理功能测试
    if test_region_management; then
        test_results+=("地区管理: ✓")
    else
        test_results+=("地区管理: ✗")
    fi
    
    # 时间线管理功能测试
    if test_timeline_management; then
        test_results+=("时间线管理: ✓")
    else
        test_results+=("时间线管理: ✗")
    fi
    
    # 前端应用测试
    if test_frontend; then
        test_results+=("前端应用: ✓")
    else
        test_results+=("前端应用: ✗")
    fi
    
    # 性能测试
    if performance_test; then
        test_results+=("性能测试: ✓")
    else
        test_results+=("性能测试: ✗")
    fi
    
    # 输出测试结果摘要
    log_info "========== 测试结果摘要 =========="
    for result in "${test_results[@]}"; do
        echo "$result"
    done
    
    # 检查是否有失败的测试
    local failed_count
    failed_count=$(printf '%s\n' "${test_results[@]}" | grep -c "✗" || true)
    
    if [ "$failed_count" -eq 0 ]; then
        log_info "所有测试通过！系统集成成功。"
        exit 0
    else
        log_error "有 ${failed_count} 项测试失败。请检查系统配置。"
        exit 1
    fi
}

# 显示帮助信息
show_help() {
    echo "系统集成测试脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示帮助信息"
    echo "  -u, --url      指定后端服务URL (默认: http://localhost:8080)"
    echo "  -f, --frontend 指定前端服务URL (默认: http://localhost:3000)"
    echo ""
    echo "示例:"
    echo "  $0                                    # 使用默认URL运行测试"
    echo "  $0 -u http://localhost:8080          # 指定后端URL"
    echo "  $0 -f http://localhost:3000          # 指定前端URL"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -u|--url)
            BASE_URL="$2"
            API_BASE_URL="${BASE_URL}/api"
            shift 2
            ;;
        -f|--frontend)
            FRONTEND_URL="$2"
            shift 2
            ;;
        *)
            log_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
done

# 运行主测试函数
main