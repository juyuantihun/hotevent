# 时间线DeepSeek集成系统部署和运维指南

## 概述

本文档详细说明了时间线DeepSeek集成系统的部署方法、运维流程和最佳实践，包括本地开发环境、测试环境和生产环境的部署指南。

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │   后端API服务   │    │   DeepSeek API  │
│   (Vue.js)      │◄──►│   (Spring Boot) │◄──►│                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MySQL数据库   │◄──►│   Redis缓存     │    │   Neo4j图数据库 │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 环境要求

### 硬件要求

#### 最小配置
- **CPU**: 2核心
- **内存**: 4GB RAM
- **存储**: 50GB SSD
- **网络**: 100Mbps

#### 推荐配置
- **CPU**: 4核心以上
- **内存**: 8GB RAM以上
- **存储**: 200GB SSD以上
- **网络**: 1Gbps

#### 生产环境配置
- **CPU**: 8核心以上
- **内存**: 16GB RAM以上
- **存储**: 500GB SSD以上
- **网络**: 1Gbps以上

### 软件要求

#### 基础软件
- **操作系统**: Ubuntu 20.04+ / CentOS 8+ / Windows Server 2019+
- **Java**: OpenJDK 17+
- **Node.js**: 18.0+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

#### 数据库
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Neo4j**: 5.0+

## 部署方式

### 1. Docker部署（推荐）

#### 1.1 准备Docker环境

```bash
# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### 1.2 创建Docker Compose配置

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL数据库
  mysql:
    image: mysql:8.0
    container_name: hot-events-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "${MYSQL_PORT}:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password
    networks:
      - hot-events-network

  # Redis缓存
  redis:
    image: redis:7.0-alpine
    container_name: hot-events-redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASSWORD}
    ports:
      - "${REDIS_PORT}:6379"
    volumes:
      - redis_data:/data
    networks:
      - hot-events-network

  # Neo4j图数据库
  neo4j:
    image: neo4j:5.0
    container_name: hot-events-neo4j
    restart: unless-stopped
    environment:
      NEO4J_AUTH: ${NEO4J_AUTH}
      NEO4J_PLUGINS: '["apoc"]'
      NEO4J_dbms_security_procedures_unrestricted: apoc.*
    ports:
      - "${NEO4J_HTTP_PORT}:7474"
      - "${NEO4J_BOLT_PORT}:7687"
    volumes:
      - neo4j_data:/data
      - neo4j_logs:/logs
    networks:
      - hot-events-network

  # 后端应用
  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: hot-events-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: ${APP_PROFILE}
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: ${MYSQL_DATABASE}
      DB_USERNAME: ${MYSQL_USER}
      DB_PASSWORD: ${MYSQL_PASSWORD}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      NEO4J_URI: bolt://neo4j:7687
      NEO4J_USERNAME: neo4j
      NEO4J_PASSWORD: ${NEO4J_PASSWORD}
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
    ports:
      - "${APP_PORT}:8080"
    depends_on:
      - mysql
      - redis
      - neo4j
    volumes:
      - app_logs:/app/logs
      - ./config:/app/config
    networks:
      - hot-events-network

  # 前端应用
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: hot-events-frontend
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
    volumes:
      - ./frontend/nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    networks:
      - hot-events-network

volumes:
  mysql_data:
  redis_data:
  neo4j_data:
  neo4j_logs:
  app_logs:

networks:
  hot-events-network:
    driver: bridge
```

#### 1.3 创建环境配置文件

```bash
# .env
COMPOSE_PROJECT_NAME=hot-events

# 应用配置
APP_VERSION=1.0.0
APP_PORT=8080
APP_PROFILE=prod

# MySQL配置
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_DATABASE=hot_events
MYSQL_USER=app_user
MYSQL_PASSWORD=your_secure_app_password
MYSQL_PORT=3306

# Redis配置
REDIS_PASSWORD=your_secure_redis_password
REDIS_PORT=6379

# Neo4j配置
NEO4J_AUTH=neo4j/your_secure_neo4j_password
NEO4J_PASSWORD=your_secure_neo4j_password
NEO4J_HTTP_PORT=7474
NEO4J_BOLT_PORT=7687

# DeepSeek API配置
DEEPSEEK_API_KEY=your_deepseek_api_key
```

#### 1.4 创建Dockerfile

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 复制应用文件
COPY target/hot-events-*.jar app.jar
COPY src/main/resources/application.yml application.yml

# 创建日志目录
RUN mkdir -p /app/logs

# 设置JVM参数
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/monitoring/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 1.5 前端Dockerfile

```dockerfile
# frontend/Dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 安装依赖
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build

# 生产阶段
FROM nginx:alpine

# 复制构建结果
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 暴露端口
EXPOSE 80 443

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
```

#### 1.6 部署命令

```bash
# 克隆项目
git clone https://github.com/your-org/hot-events.git
cd hot-events

# 配置环境变量
cp .env.example .env
# 编辑.env文件，设置正确的配置

# 构建并启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down

# 重启服务
docker-compose restart backend
```

### 2. Kubernetes部署

#### 2.1 创建命名空间

```yaml
# namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: hot-events
  labels:
    name: hot-events
```

#### 2.2 创建ConfigMap

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: hot-events-config
  namespace: hot-events
data:
  application.yml: |
    spring:
      profiles:
        active: k8s
      datasource:
        url: jdbc:mysql://mysql-service:3306/hot_events
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
      redis:
        host: redis-service
        port: 6379
        password: ${REDIS_PASSWORD}
      neo4j:
        uri: bolt://neo4j-service:7687
        authentication:
          username: neo4j
          password: ${NEO4J_PASSWORD}
    deepseek:
      api:
        key: ${DEEPSEEK_API_KEY}
        url: https://api.deepseek.com/v1/chat/completions
```

#### 2.3 创建Secret

```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: hot-events-secret
  namespace: hot-events
type: Opaque
data:
  db-username: YXBwX3VzZXI=  # base64编码的app_user
  db-password: eW91cl9wYXNzd29yZA==  # base64编码的密码
  redis-password: cmVkaXNfcGFzc3dvcmQ=  # base64编码的Redis密码
  neo4j-password: bmVvNGpfcGFzc3dvcmQ=  # base64编码的Neo4j密码
  deepseek-api-key: ZGVlcHNlZWtfYXBpX2tleQ==  # base64编码的API密钥
```

#### 2.4 创建MySQL部署

```yaml
# mysql-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysql
  namespace: hot-events
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: db-password
        - name: MYSQL_DATABASE
          value: hot_events
        - name: MYSQL_USER
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: db-username
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: db-password
        ports:
        - containerPort: 3306
        volumeMounts:
        - name: mysql-storage
          mountPath: /var/lib/mysql
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: mysql-storage
        persistentVolumeClaim:
          claimName: mysql-pvc

---
apiVersion: v1
kind: Service
metadata:
  name: mysql-service
  namespace: hot-events
spec:
  selector:
    app: mysql
  ports:
  - port: 3306
    targetPort: 3306
  type: ClusterIP

---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
  namespace: hot-events
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 20Gi
```

#### 2.5 创建应用部署

```yaml
# app-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hot-events-backend
  namespace: hot-events
spec:
  replicas: 3
  selector:
    matchLabels:
      app: hot-events-backend
  template:
    metadata:
      labels:
        app: hot-events-backend
    spec:
      containers:
      - name: backend
        image: hot-events:latest
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: db-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: redis-password
        - name: NEO4J_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: neo4j-password
        - name: DEEPSEEK_API_KEY
          valueFrom:
            secretKeyRef:
              name: hot-events-secret
              key: deepseek-api-key
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        livenessProbe:
          httpGet:
            path: /api/monitoring/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /api/monitoring/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
      volumes:
      - name: config-volume
        configMap:
          name: hot-events-config
      - name: logs-volume
        emptyDir: {}

---
apiVersion: v1
kind: Service
metadata:
  name: hot-events-backend-service
  namespace: hot-events
spec:
  selector:
    app: hot-events-backend
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP

---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: hot-events-ingress
  namespace: hot-events
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - api.hot-events.com
    secretName: hot-events-tls
  rules:
  - host: api.hot-events.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: hot-events-backend-service
            port:
              number: 8080
```

#### 2.6 部署到Kubernetes

```bash
# 应用配置
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml

# 部署数据库
kubectl apply -f mysql-deployment.yaml
kubectl apply -f redis-deployment.yaml
kubectl apply -f neo4j-deployment.yaml

# 部署应用
kubectl apply -f app-deployment.yaml

# 查看部署状态
kubectl get pods -n hot-events
kubectl get services -n hot-events

# 查看日志
kubectl logs -f deployment/hot-events-backend -n hot-events

# 扩容
kubectl scale deployment hot-events-backend --replicas=5 -n hot-events
```

### 3. 传统部署

#### 3.1 环境准备

```bash
# 安装Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# 安装MySQL
sudo apt install mysql-server
sudo mysql_secure_installation

# 安装Redis
sudo apt install redis-server

# 安装Neo4j
wget -O - https://debian.neo4j.com/neotechnology.gpg.key | sudo apt-key add -
echo 'deb https://debian.neo4j.com stable 4.4' | sudo tee -a /etc/apt/sources.list.d/neo4j.list
sudo apt update
sudo apt install neo4j

# 安装Nginx
sudo apt install nginx
```

#### 3.2 数据库初始化

```bash
# MySQL初始化
mysql -u root -p
CREATE DATABASE hot_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON hot_events.* TO 'app_user'@'localhost';
FLUSH PRIVILEGES;

# 导入数据库结构
mysql -u app_user -p hot_events < hot_events_db.sql

# Neo4j配置
sudo systemctl enable neo4j
sudo systemctl start neo4j
# 访问 http://localhost:7474 设置密码
```

#### 3.3 应用部署

```bash
# 创建应用目录
sudo mkdir -p /opt/hot-events
sudo chown $USER:$USER /opt/hot-events

# 复制应用文件
cp target/hot-events-*.jar /opt/hot-events/app.jar
cp src/main/resources/application-prod.yml /opt/hot-events/

# 创建启动脚本
cat > /opt/hot-events/start.sh << 'EOF'
#!/bin/bash
cd /opt/hot-events
java -Xms2g -Xmx4g -XX:+UseG1GC \
     -Dspring.profiles.active=prod \
     -Dspring.config.location=application-prod.yml \
     -jar app.jar > logs/app.log 2>&1 &
echo $! > app.pid
EOF

chmod +x /opt/hot-events/start.sh

# 创建systemd服务
sudo cat > /etc/systemd/system/hot-events.service << 'EOF'
[Unit]
Description=Hot Events Timeline Service
After=network.target mysql.service redis.service neo4j.service

[Service]
Type=forking
User=app_user
Group=app_user
WorkingDirectory=/opt/hot-events
ExecStart=/opt/hot-events/start.sh
ExecStop=/bin/kill -TERM $MAINPID
PIDFile=/opt/hot-events/app.pid
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable hot-events
sudo systemctl start hot-events
```

#### 3.4 前端部署

```bash
# 构建前端
cd frontend
npm install
npm run build

# 部署到Nginx
sudo cp -r dist/* /var/www/html/

# 配置Nginx
sudo cat > /etc/nginx/sites-available/hot-events << 'EOF'
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

sudo ln -s /etc/nginx/sites-available/hot-events /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 监控和日志

### 1. 应用监控

#### 1.1 Prometheus配置

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'hot-events'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/api/monitoring/prometheus'
    scrape_interval: 30s
```

#### 1.2 Grafana仪表板

```json
{
  "dashboard": {
    "title": "Hot Events Timeline Monitoring",
    "panels": [
      {
        "title": "API Response Time",
        "type": "graph",
        "targets": [
          {
            "expr": "http_request_duration_seconds_sum / http_request_duration_seconds_count"
          }
        ]
      },
      {
        "title": "DeepSeek API Usage",
        "type": "stat",
        "targets": [
          {
            "expr": "deepseek_api_requests_total"
          }
        ]
      }
    ]
  }
}
```

### 2. 日志管理

#### 2.1 ELK Stack配置

```yaml
# docker-compose-elk.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.0.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:8.0.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5044:5044"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.0.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
```

#### 2.2 Logstash配置

```ruby
# logstash.conf
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "hot-events" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:message}" }
    }
    
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "hot-events-%{+YYYY.MM.dd}"
  }
}
```

### 3. 健康检查

#### 3.1 健康检查脚本

```bash
#!/bin/bash
# health-check.sh

# 检查应用健康状态
check_app_health() {
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/monitoring/health)
    if [ "$response" = "200" ]; then
        echo "✓ 应用健康检查通过"
        return 0
    else
        echo "✗ 应用健康检查失败 (HTTP $response)"
        return 1
    fi
}

# 检查数据库连接
check_database() {
    mysql -h localhost -u app_user -p$DB_PASSWORD -e "SELECT 1" hot_events > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✓ 数据库连接正常"
        return 0
    else
        echo "✗ 数据库连接失败"
        return 1
    fi
}

# 检查Redis连接
check_redis() {
    redis-cli -a $REDIS_PASSWORD ping > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✓ Redis连接正常"
        return 0
    else
        echo "✗ Redis连接失败"
        return 1
    fi
}

# 检查Neo4j连接
check_neo4j() {
    cypher-shell -u neo4j -p $NEO4J_PASSWORD "RETURN 1" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✓ Neo4j连接正常"
        return 0
    else
        echo "✗ Neo4j连接失败"
        return 1
    fi
}

# 执行所有检查
main() {
    echo "开始系统健康检查..."
    
    failed=0
    check_app_health || failed=1
    check_database || failed=1
    check_redis || failed=1
    check_neo4j || failed=1
    
    if [ $failed -eq 0 ]; then
        echo "所有健康检查通过"
        exit 0
    else
        echo "健康检查失败"
        exit 1
    fi
}

main
```

## 备份和恢复

### 1. 数据库备份

#### 1.1 MySQL备份脚本

```bash
#!/bin/bash
# mysql-backup.sh

BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="hot_events"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
mysqldump -u app_user -p$DB_PASSWORD \
    --single-transaction \
    --routines \
    --triggers \
    $DB_NAME > $BACKUP_DIR/hot_events_$DATE.sql

# 压缩备份文件
gzip $BACKUP_DIR/hot_events_$DATE.sql

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "MySQL备份完成: hot_events_$DATE.sql.gz"
```

#### 1.2 Neo4j备份脚本

```bash
#!/bin/bash
# neo4j-backup.sh

BACKUP_DIR="/backup/neo4j"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 停止Neo4j服务
sudo systemctl stop neo4j

# 复制数据文件
cp -r /var/lib/neo4j/data $BACKUP_DIR/neo4j_data_$DATE

# 启动Neo4j服务
sudo systemctl start neo4j

# 压缩备份
tar -czf $BACKUP_DIR/neo4j_backup_$DATE.tar.gz -C $BACKUP_DIR neo4j_data_$DATE
rm -rf $BACKUP_DIR/neo4j_data_$DATE

echo "Neo4j备份完成: neo4j_backup_$DATE.tar.gz"
```

### 2. 应用备份

```bash
#!/bin/bash
# app-backup.sh

BACKUP_DIR="/backup/app"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份应用文件
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz \
    /opt/hot-events \
    /etc/nginx/sites-available/hot-events \
    /etc/systemd/system/hot-events.service

echo "应用备份完成: app_backup_$DATE.tar.gz"
```

### 3. 自动备份配置

```bash
# 添加到crontab
crontab -e

# 每天凌晨2点执行备份
0 2 * * * /opt/scripts/mysql-backup.sh
30 2 * * * /opt/scripts/neo4j-backup.sh
0 3 * * * /opt/scripts/app-backup.sh

# 每周日执行完整备份
0 1 * * 0 /opt/scripts/full-backup.sh
```

## 性能优化

### 1. JVM优化

```bash
# JVM参数优化
JAVA_OPTS="-Xms4g -Xmx8g \
           -XX:+UseG1GC \
           -XX:MaxGCPauseMillis=200 \
           -XX:+UseStringDeduplication \
           -XX:+OptimizeStringConcat \
           -XX:+UseCompressedOops \
           -XX:+UseCompressedClassPointers \
           -Djava.awt.headless=true \
           -Dfile.encoding=UTF-8 \
           -Duser.timezone=Asia/Shanghai"
```

### 2. 数据库优化

```sql
-- MySQL配置优化
[mysqld]
innodb_buffer_pool_size = 4G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT
query_cache_type = 1
query_cache_size = 256M
max_connections = 500
thread_cache_size = 50
table_open_cache = 2000
```

### 3. Redis优化

```bash
# Redis配置优化
maxmemory 2gb
maxmemory-policy allkeys-lru
save 900 1
save 300 10
save 60 10000
```

## 安全配置

### 1. 防火墙配置

```bash
# UFW防火墙配置
sudo ufw enable
sudo ufw default deny incoming
sudo ufw default allow outgoing

# 允许必要端口
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw allow 8080/tcp  # 应用端口（仅内网）

# 限制数据库端口访问
sudo ufw allow from 10.0.0.0/8 to any port 3306
sudo ufw allow from 10.0.0.0/8 to any port 6379
sudo ufw allow from 10.0.0.0/8 to any port 7687
```

### 2. SSL证书配置

```bash
# 使用Let's Encrypt
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
0 12 * * * /usr/bin/certbot renew --quiet
```

### 3. 应用安全配置

```yaml
# application-prod.yml安全配置
spring:
  security:
    require-ssl: true
  session:
    cookie:
      secure: true
      http-only: true
      same-site: strict

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

## 故障排除

### 1. 常见问题

#### 应用启动失败
```bash
# 检查日志
tail -f /opt/hot-events/logs/app.log

# 检查端口占用
netstat -tlnp | grep 8080

# 检查Java进程
jps -l
```

#### 数据库连接问题
```bash
# 检查MySQL状态
sudo systemctl status mysql

# 测试连接
mysql -h localhost -u app_user -p hot_events

# 检查连接数
mysql -e "SHOW PROCESSLIST;"
```

#### 内存不足
```bash
# 检查内存使用
free -h
top -p $(pgrep java)

# 生成堆转储
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc
jmap -dump:format=b,file=heap.hprof <pid>
```

### 2. 应急处理

```bash
#!/bin/bash
# emergency-restart.sh

echo "执行应急重启..."

# 停止应用
sudo systemctl stop hot-events

# 清理临时文件
rm -rf /tmp/hot-events-*

# 重启数据库服务
sudo systemctl restart mysql
sudo systemctl restart redis
sudo systemctl restart neo4j

# 等待数据库启动
sleep 30

# 启动应用
sudo systemctl start hot-events

# 检查状态
sleep 10
sudo systemctl status hot-events

echo "应急重启完成"
```

通过以上部署和运维指南，您可以在不同环境中成功部署和维护时间线DeepSeek集成系统，确保系统的稳定运行和高可用性。