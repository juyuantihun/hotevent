# 时间线DeepSeek集成系统配置指南

## 概述

本文档详细说明了时间线DeepSeek集成系统的配置方法，包括应用程序配置、数据库配置、DeepSeek API配置、提示词模板配置等。

## 配置文件结构

```
hot_event/
├── src/main/resources/
│   ├── application.yml                 # 主配置文件
│   ├── application-dev.yml            # 开发环境配置
│   ├── application-prod.yml           # 生产环境配置
│   ├── application-test.yml           # 测试环境配置
│   └── config/
│       └── prompt-templates.yml       # 提示词模板配置
├── docker/
│   ├── docker-compose.yml            # Docker编排配置
│   └── .env                          # 环境变量文件
└── config/
    ├── logback-spring.xml            # 日志配置
    └── application.properties        # 外部配置文件
```

## 主配置文件 (application.yml)

### 基础配置

```yaml
# 服务器配置
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api
    encoding:
      charset: UTF-8
      enabled: true
      force: true

# Spring配置
spring:
  application:
    name: hot-events-timeline
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:hot_events}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    hikari:
      minimum-idle: ${DB_MIN_IDLE:5}
      maximum-pool-size: ${DB_MAX_POOL_SIZE:20}
      idle-timeout: ${DB_IDLE_TIMEOUT:300000}
      max-lifetime: ${DB_MAX_LIFETIME:1800000}
      connection-timeout: ${DB_CONNECTION_TIMEOUT:30000}
      validation-timeout: ${DB_VALIDATION_TIMEOUT:5000}
      leak-detection-threshold: ${DB_LEAK_DETECTION:60000}

  # Redis配置
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:0}
    timeout: ${REDIS_TIMEOUT:5000}
    lettuce:
      pool:
        max-active: ${REDIS_MAX_ACTIVE:8}
        max-idle: ${REDIS_MAX_IDLE:8}
        min-idle: ${REDIS_MIN_IDLE:0}
        max-wait: ${REDIS_MAX_WAIT:5000}

  # Neo4j配置
  neo4j:
    uri: ${NEO4J_URI:bolt://localhost:7687}
    authentication:
      username: ${NEO4J_USERNAME:neo4j}
      password: ${NEO4J_PASSWORD:password}
    pool:
      max-connection-pool-size: ${NEO4J_MAX_POOL_SIZE:50}
      idle-time-before-connection-test: ${NEO4J_IDLE_TIME:30s}
      max-connection-lifetime: ${NEO4J_MAX_LIFETIME:1h}

# MyBatis Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    lazy-loading-enabled: true
    multiple-result-sets-enabled: true
    use-column-label: true
    use-generated-keys: true
    auto-mapping-behavior: partial
    auto-mapping-unknown-column-behavior: warning
    default-executor-type: reuse
    default-statement-timeout: 25000
    default-fetch-size: 100
    safe-row-bounds-enabled: false
    map-underscore-to-camel-case: true
    local-cache-scope: session
    jdbc-type-for-null: other
    lazy-load-trigger-methods: equals,clone,hashCode,toString
  global-config:
    db-config:
      id-type: auto
      table-underline: true
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:mapper/*.xml
  type-aliases-package: com.hotech.events.entity

# 日志配置
logging:
  level:
    root: ${LOG_LEVEL_ROOT:INFO}
    com.hotech.events: ${LOG_LEVEL_APP:DEBUG}
    org.springframework.web: ${LOG_LEVEL_WEB:INFO}
    org.mybatis: ${LOG_LEVEL_MYBATIS:DEBUG}
    org.neo4j: ${LOG_LEVEL_NEO4J:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE_PATH:./logs/hot-events.log}
    max-size: ${LOG_FILE_MAX_SIZE:100MB}
    max-history: ${LOG_FILE_MAX_HISTORY:30}
```

### DeepSeek API配置

```yaml
# DeepSeek配置
deepseek:
  api:
    # API基础配置
    url: ${DEEPSEEK_API_URL:https://api.deepseek.com/v1/chat/completions}
    key: ${DEEPSEEK_API_KEY:your-api-key-here}
    model: ${DEEPSEEK_MODEL:deepseek-chat}
    
    # 请求配置
    timeout: ${DEEPSEEK_TIMEOUT:60000}
    connect-timeout: ${DEEPSEEK_CONNECT_TIMEOUT:10000}
    read-timeout: ${DEEPSEEK_READ_TIMEOUT:60000}
    max-retries: ${DEEPSEEK_MAX_RETRIES:3}
    retry-delay: ${DEEPSEEK_RETRY_DELAY:1000}
    
    # 限流配置
    rate-limit:
      requests-per-minute: ${DEEPSEEK_RATE_LIMIT:60}
      requests-per-hour: ${DEEPSEEK_RATE_LIMIT_HOUR:1000}
      requests-per-day: ${DEEPSEEK_RATE_LIMIT_DAY:10000}
    
    # 缓存配置
    cache:
      enabled: ${DEEPSEEK_CACHE_ENABLED:true}
      ttl: ${DEEPSEEK_CACHE_TTL:3600}
      max-size: ${DEEPSEEK_CACHE_MAX_SIZE:1000}
    
    # 监控配置
    monitoring:
      enabled: ${DEEPSEEK_MONITORING_ENABLED:true}
      metrics-interval: ${DEEPSEEK_METRICS_INTERVAL:60}
      health-check-interval: ${DEEPSEEK_HEALTH_CHECK_INTERVAL:300}
```

### 时间线生成配置

```yaml
# 时间线配置
timeline:
  generation:
    # 默认配置
    default-max-events: ${TIMELINE_DEFAULT_MAX_EVENTS:100}
    default-credibility-threshold: ${TIMELINE_DEFAULT_CREDIBILITY:0.7}
    default-timeout: ${TIMELINE_DEFAULT_TIMEOUT:300000}
    
    # 批处理配置
    batch:
      size: ${TIMELINE_BATCH_SIZE:50}
      parallel-threads: ${TIMELINE_PARALLEL_THREADS:4}
      queue-capacity: ${TIMELINE_QUEUE_CAPACITY:1000}
    
    # 进度跟踪配置
    progress:
      update-interval: ${TIMELINE_PROGRESS_INTERVAL:5000}
      enable-websocket: ${TIMELINE_WEBSOCKET_ENABLED:true}
    
    # 存储配置
    storage:
      enable-deduplication: ${TIMELINE_DEDUP_ENABLED:true}
      dedup-similarity-threshold: ${TIMELINE_DEDUP_THRESHOLD:0.9}
      auto-update-dictionaries: ${TIMELINE_AUTO_UPDATE_DICT:true}
```

### 事件验证配置

```yaml
# 事件验证配置
validation:
  # 基础配置
  enabled: ${VALIDATION_ENABLED:true}
  strict-mode: ${VALIDATION_STRICT_MODE:false}
  credibility-threshold: ${VALIDATION_CREDIBILITY_THRESHOLD:0.7}
  
  # 验证规则配置
  rules:
    time-consistency:
      enabled: ${VALIDATION_TIME_ENABLED:true}
      weight: ${VALIDATION_TIME_WEIGHT:0.25}
    location-accuracy:
      enabled: ${VALIDATION_LOCATION_ENABLED:true}
      weight: ${VALIDATION_LOCATION_WEIGHT:0.25}
    logical-consistency:
      enabled: ${VALIDATION_LOGICAL_ENABLED:true}
      weight: ${VALIDATION_LOGICAL_WEIGHT:0.25}
    source-credibility:
      enabled: ${VALIDATION_SOURCE_ENABLED:true}
      weight: ${VALIDATION_SOURCE_WEIGHT:0.25}
  
  # 批处理配置
  batch:
    size: ${VALIDATION_BATCH_SIZE:100}
    parallel-threads: ${VALIDATION_PARALLEL_THREADS:2}
    timeout: ${VALIDATION_TIMEOUT:30000}
```

### 监控和告警配置

```yaml
# 监控配置
monitoring:
  # 基础监控
  enabled: ${MONITORING_ENABLED:true}
  metrics-interval: ${MONITORING_METRICS_INTERVAL:60}
  
  # 健康检查
  health:
    database:
      enabled: ${MONITORING_DB_ENABLED:true}
      timeout: ${MONITORING_DB_TIMEOUT:5000}
    deepseek-api:
      enabled: ${MONITORING_DEEPSEEK_ENABLED:true}
      timeout: ${MONITORING_DEEPSEEK_TIMEOUT:10000}
    neo4j:
      enabled: ${MONITORING_NEO4J_ENABLED:true}
      timeout: ${MONITORING_NEO4J_TIMEOUT:5000}
  
  # 告警配置
  alerts:
    enabled: ${ALERTS_ENABLED:true}
    email:
      enabled: ${ALERTS_EMAIL_ENABLED:false}
      smtp-host: ${ALERTS_SMTP_HOST:smtp.gmail.com}
      smtp-port: ${ALERTS_SMTP_PORT:587}
      username: ${ALERTS_EMAIL_USERNAME:}
      password: ${ALERTS_EMAIL_PASSWORD:}
      from: ${ALERTS_EMAIL_FROM:noreply@hotech.com}
      to: ${ALERTS_EMAIL_TO:admin@hotech.com}
    
    # 告警阈值
    thresholds:
      api-failure-rate: ${ALERTS_API_FAILURE_RATE:0.1}
      response-time: ${ALERTS_RESPONSE_TIME:5000}
      memory-usage: ${ALERTS_MEMORY_USAGE:0.8}
      cpu-usage: ${ALERTS_CPU_USAGE:0.8}
      disk-usage: ${ALERTS_DISK_USAGE:0.9}
```

## 环境特定配置

### 开发环境 (application-dev.yml)

```yaml
# 开发环境配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hot_events_dev?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: dev_user
    password: dev_password
  
  redis:
    host: localhost
    port: 6379
    database: 1
  
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: dev_password

# 开发环境DeepSeek配置
deepseek:
  api:
    url: https://api.deepseek.com/v1/chat/completions
    key: ${DEEPSEEK_DEV_API_KEY:dev-api-key}
    rate-limit:
      requests-per-minute: 30
      requests-per-hour: 500

# 开发环境日志配置
logging:
  level:
    root: INFO
    com.hotech.events: DEBUG
    org.springframework.web: DEBUG
  file:
    name: ./logs/hot-events-dev.log

# 开发环境监控配置
monitoring:
  enabled: true
  alerts:
    enabled: false
```

### 生产环境 (application-prod.yml)

```yaml
# 生产环境配置
spring:
  datasource:
    url: jdbc:mysql://${PROD_DB_HOST}:${PROD_DB_PORT}/${PROD_DB_NAME}?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: ${PROD_DB_USERNAME}
    password: ${PROD_DB_PASSWORD}
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 600000
      max-lifetime: 1800000
  
  redis:
    host: ${PROD_REDIS_HOST}
    port: ${PROD_REDIS_PORT}
    password: ${PROD_REDIS_PASSWORD}
    database: 0
  
  neo4j:
    uri: ${PROD_NEO4J_URI}
    authentication:
      username: ${PROD_NEO4J_USERNAME}
      password: ${PROD_NEO4J_PASSWORD}

# 生产环境DeepSeek配置
deepseek:
  api:
    url: ${PROD_DEEPSEEK_API_URL}
    key: ${PROD_DEEPSEEK_API_KEY}
    rate-limit:
      requests-per-minute: 100
      requests-per-hour: 2000
      requests-per-day: 20000

# 生产环境日志配置
logging:
  level:
    root: WARN
    com.hotech.events: INFO
  file:
    name: /var/log/hot-events/hot-events.log
    max-size: 500MB
    max-history: 90

# 生产环境监控配置
monitoring:
  enabled: true
  alerts:
    enabled: true
    email:
      enabled: true
      smtp-host: ${PROD_SMTP_HOST}
      smtp-port: ${PROD_SMTP_PORT}
      username: ${PROD_SMTP_USERNAME}
      password: ${PROD_SMTP_PASSWORD}
```

## 提示词模板配置 (prompt-templates.yml)

```yaml
# 提示词模板配置
templates:
  # 事件检索模板
  event_fetch:
    name: "事件检索模板"
    type: "EVENT_RETRIEVAL"
    version: "1.0"
    active: true
    content: |
      你是一个专业的国际事件分析师。请根据以下条件，从可靠的新闻源中检索真实的国际事件：
      
      时间线名称：{timelineName}
      时间线描述：{timelineDescription}
      目标地区：{regions}
      时间范围：{startTime} 至 {endTime}
      
      请严格按照以下要求：
      1. 只返回真实发生的事件
      2. 事件必须与指定地区和时间范围相关
      3. 优先选择具有重大影响的事件
      4. 确保事件信息的准确性和完整性
      5. 每个事件必须包含可靠的信息来源
      
      返回格式必须是有效的JSON：
      {
        "events": [
          {
            "title": "事件标题",
            "description": "详细描述",
            "eventTime": "2024-01-01T12:00:00",
            "location": "具体地点",
            "subject": "事件主体",
            "object": "事件客体", 
            "eventType": "事件类型",
            "keywords": ["关键词1", "关键词2"],
            "sources": ["来源1", "来源2"],
            "credibilityScore": 0.95
          }
        ]
      }
    
    parameters:
      - name: "timelineName"
        type: "string"
        required: true
        description: "时间线名称"
      - name: "timelineDescription"
        type: "string"
        required: false
        description: "时间线描述"
      - name: "regions"
        type: "array"
        required: false
        description: "目标地区列表"
      - name: "startTime"
        type: "datetime"
        required: true
        description: "开始时间"
      - name: "endTime"
        type: "datetime"
        required: true
        description: "结束时间"

  # 事件验证模板
  event_validation:
    name: "事件验证模板"
    type: "EVENT_VALIDATION"
    version: "1.0"
    active: true
    content: |
      请验证以下事件的真实性和准确性：
      
      {eventsList}
      
      验证标准：
      1. 事件是否真实发生
      2. 时间、地点信息是否准确
      3. 事件描述是否客观
      4. 是否有可靠来源支持
      5. 事件逻辑是否一致
      
      请为每个事件提供验证结果，返回格式必须是有效的JSON：
      {
        "validationResults": [
          {
            "eventId": "事件ID",
            "isValid": true,
            "credibilityScore": 0.95,
            "issues": [],
            "suggestions": [],
            "validationDetails": {
              "timeConsistency": true,
              "locationAccuracy": true,
              "logicalConsistency": true,
              "sourceCredibility": 0.95
            }
          }
        ]
      }
    
    parameters:
      - name: "eventsList"
        type: "array"
        required: true
        description: "待验证的事件列表"

  # 时间线组织模板
  timeline_organize:
    name: "时间线组织模板"
    type: "TIMELINE_ORGANIZATION"
    version: "1.0"
    active: true
    content: |
      请将以下已验证的事件组织成连贯的时间线：
      
      时间线主题：{timelineName}
      时间线描述：{timelineDescription}
      事件列表：{eventsList}
      现有关系：{relationsList}
      
      组织原则：
      1. 按时间顺序排列事件
      2. 识别事件间的因果关系
      3. 突出关键节点和转折点
      4. 保持逻辑连贯性
      5. 标注重要的时间节点
      
      返回格式必须是有效的JSON：
      {
        "timeline": {
          "events": [
            {
              "eventId": 123,
              "order": 1,
              "importance": "HIGH",
              "category": "MILESTONE"
            }
          ],
          "relations": [
            {
              "fromEventId": 123,
              "toEventId": 124,
              "relationType": "CAUSE_EFFECT",
              "strength": 0.8,
              "description": "关系描述"
            }
          ],
          "milestones": [
            {
              "eventId": 123,
              "type": "TURNING_POINT",
              "description": "里程碑描述"
            }
          ]
        }
      }
    
    parameters:
      - name: "timelineName"
        type: "string"
        required: true
        description: "时间线名称"
      - name: "timelineDescription"
        type: "string"
        required: false
        description: "时间线描述"
      - name: "eventsList"
        type: "array"
        required: true
        description: "事件列表"
      - name: "relationsList"
        type: "array"
        required: false
        description: "现有关系列表"

# 模板管理配置
template_management:
  # 热更新配置
  hot_reload:
    enabled: ${TEMPLATE_HOT_RELOAD:true}
    check_interval: ${TEMPLATE_CHECK_INTERVAL:30}
    backup_enabled: ${TEMPLATE_BACKUP_ENABLED:true}
    backup_count: ${TEMPLATE_BACKUP_COUNT:5}
  
  # 验证配置
  validation:
    enabled: ${TEMPLATE_VALIDATION_ENABLED:true}
    strict_mode: ${TEMPLATE_VALIDATION_STRICT:false}
    required_fields: ["name", "type", "content"]
  
  # 缓存配置
  cache:
    enabled: ${TEMPLATE_CACHE_ENABLED:true}
    ttl: ${TEMPLATE_CACHE_TTL:3600}
    max_size: ${TEMPLATE_CACHE_MAX_SIZE:100}
```

## 环境变量配置

### 基础环境变量

```bash
# 服务器配置
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=hot_events
DB_USERNAME=root
DB_PASSWORD=your_password
DB_MIN_IDLE=5
DB_MAX_POOL_SIZE=20

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
REDIS_DATABASE=0

# Neo4j配置
NEO4J_URI=bolt://localhost:7687
NEO4J_USERNAME=neo4j
NEO4J_PASSWORD=your_neo4j_password

# DeepSeek API配置
DEEPSEEK_API_URL=https://api.deepseek.com/v1/chat/completions
DEEPSEEK_API_KEY=your_deepseek_api_key
DEEPSEEK_MODEL=deepseek-chat
DEEPSEEK_TIMEOUT=60000
DEEPSEEK_MAX_RETRIES=3

# 日志配置
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
LOG_FILE_PATH=./logs/hot-events.log

# 监控配置
MONITORING_ENABLED=true
ALERTS_ENABLED=true
ALERTS_EMAIL_ENABLED=false
```

### Docker环境变量 (.env)

```bash
# Docker Compose环境变量
COMPOSE_PROJECT_NAME=hot-events

# 应用配置
APP_VERSION=1.0.0
APP_PORT=8080
APP_PROFILE=prod

# MySQL配置
MYSQL_VERSION=8.0
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=hot_events
MYSQL_USER=app_user
MYSQL_PASSWORD=app_password
MYSQL_PORT=3306

# Redis配置
REDIS_VERSION=7.0
REDIS_PASSWORD=redis_password
REDIS_PORT=6379

# Neo4j配置
NEO4J_VERSION=5.0
NEO4J_AUTH=neo4j/neo4j_password
NEO4J_HTTP_PORT=7474
NEO4J_BOLT_PORT=7687

# 网络配置
NETWORK_NAME=hot-events-network

# 存储配置
MYSQL_DATA_PATH=./data/mysql
REDIS_DATA_PATH=./data/redis
NEO4J_DATA_PATH=./data/neo4j
LOG_PATH=./logs
```

## 配置验证和测试

### 配置验证脚本

```bash
#!/bin/bash
# config-validation.sh

echo "开始验证配置..."

# 检查必需的环境变量
required_vars=(
    "DEEPSEEK_API_KEY"
    "DB_PASSWORD"
    "REDIS_PASSWORD"
    "NEO4J_PASSWORD"
)

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "错误: 环境变量 $var 未设置"
        exit 1
    fi
done

# 检查数据库连接
echo "检查数据库连接..."
mysql -h${DB_HOST:-localhost} -P${DB_PORT:-3306} -u${DB_USERNAME:-root} -p${DB_PASSWORD} -e "SELECT 1" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ 数据库连接正常"
else
    echo "✗ 数据库连接失败"
    exit 1
fi

# 检查Redis连接
echo "检查Redis连接..."
redis-cli -h ${REDIS_HOST:-localhost} -p ${REDIS_PORT:-6379} -a ${REDIS_PASSWORD} ping > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Redis连接正常"
else
    echo "✗ Redis连接失败"
    exit 1
fi

# 检查Neo4j连接
echo "检查Neo4j连接..."
cypher-shell -a ${NEO4J_URI:-bolt://localhost:7687} -u ${NEO4J_USERNAME:-neo4j} -p ${NEO4J_PASSWORD} "RETURN 1" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Neo4j连接正常"
else
    echo "✗ Neo4j连接失败"
    exit 1
fi

# 检查DeepSeek API
echo "检查DeepSeek API..."
curl -s -H "Authorization: Bearer ${DEEPSEEK_API_KEY}" \
     -H "Content-Type: application/json" \
     -d '{"model":"deepseek-chat","messages":[{"role":"user","content":"test"}],"max_tokens":1}' \
     ${DEEPSEEK_API_URL} > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ DeepSeek API连接正常"
else
    echo "✗ DeepSeek API连接失败"
    exit 1
fi

echo "所有配置验证通过！"
```

### 配置测试命令

```bash
# 测试数据库连接
java -jar hot-events.jar --spring.profiles.active=test --test.database=true

# 测试Redis连接
java -jar hot-events.jar --spring.profiles.active=test --test.redis=true

# 测试Neo4j连接
java -jar hot-events.jar --spring.profiles.active=test --test.neo4j=true

# 测试DeepSeek API
java -jar hot-events.jar --spring.profiles.active=test --test.deepseek=true

# 完整配置测试
java -jar hot-events.jar --spring.profiles.active=test --test.all=true
```

## 常见配置问题和解决方案

### 1. 数据库连接问题

**问题**: `Communications link failure`
**解决方案**:
- 检查数据库服务是否启动
- 验证连接参数（主机、端口、用户名、密码）
- 检查防火墙设置
- 增加连接超时时间

```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 60000
      validation-timeout: 10000
```

### 2. DeepSeek API配置问题

**问题**: `API key invalid`
**解决方案**:
- 验证API密钥是否正确
- 检查API密钥是否过期
- 确认API配额是否充足

```yaml
deepseek:
  api:
    key: ${DEEPSEEK_API_KEY}
    timeout: 60000
    max-retries: 3
```

### 3. 内存配置问题

**问题**: `OutOfMemoryError`
**解决方案**:
- 增加JVM堆内存大小
- 优化数据库连接池配置
- 调整缓存大小

```bash
# JVM参数
java -Xms2g -Xmx4g -XX:+UseG1GC -jar hot-events.jar
```

### 4. 日志配置问题

**问题**: 日志文件过大或日志级别不当
**解决方案**:
- 配置日志轮转
- 调整日志级别
- 使用异步日志

```yaml
logging:
  file:
    max-size: 100MB
    max-history: 30
  level:
    root: WARN
    com.hotech.events: INFO
```

## 配置最佳实践

### 1. 安全配置

- 使用环境变量存储敏感信息
- 定期轮换API密钥和数据库密码
- 启用SSL/TLS加密
- 限制网络访问权限

### 2. 性能配置

- 合理设置数据库连接池大小
- 启用适当的缓存机制
- 配置合理的超时时间
- 使用连接池监控

### 3. 监控配置

- 启用健康检查端点
- 配置关键指标监控
- 设置合理的告警阈值
- 记录详细的操作日志

### 4. 备份配置

- 定期备份配置文件
- 使用版本控制管理配置
- 建立配置变更审批流程
- 测试配置恢复流程

## 配置管理工具

### 1. Spring Boot Admin

```yaml
# 启用Spring Boot Admin
spring:
  boot:
    admin:
      client:
        url: http://admin-server:8080
        instance:
          name: hot-events-timeline
```

### 2. Consul配置中心

```yaml
# Consul配置
spring:
  cloud:
    consul:
      host: localhost
      port: 8500
      config:
        enabled: true
        format: yaml
        data-key: configuration
```

### 3. Kubernetes ConfigMap

```yaml
# ConfigMap示例
apiVersion: v1
kind: ConfigMap
metadata:
  name: hot-events-config
data:
  application.yml: |
    spring:
      profiles:
        active: k8s
    deepseek:
      api:
        url: https://api.deepseek.com/v1/chat/completions
```

通过以上配置指南，您可以根据不同的环境和需求灵活配置时间线DeepSeek集成系统，确保系统的稳定运行和最佳性能。