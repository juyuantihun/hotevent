# 事件时间线功能增强系统部署指南

本文档提供了事件时间线功能增强系统的部署和运行指南。

## 系统架构

系统采用前后端分离的架构：
- 后端：Spring Boot应用
- 前端：Vue 3应用
- 数据库：MySQL关系型数据库和Neo4j图数据库

## 环境要求

### 后端环境要求
- JDK 11或更高版本
- Maven 3.6或更高版本
- MySQL 8.0或更高版本
- Neo4j 4.4或更高版本

### 前端环境要求
- Node.js 16或更高版本
- npm 8或更高版本

## 数据库配置

### MySQL配置

1. 创建MySQL数据库：

```sql
CREATE DATABASE hot_events CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 创建数据库用户并授权：

```sql
CREATE USER 'hot_events_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON hot_events.* TO 'hot_events_user'@'%';
FLUSH PRIVILEGES;
```

3. 导入初始数据：

```bash
mysql -u hot_events_user -p hot_events < hot_event/sql/hot_events_db.sql
```

### Neo4j配置

1. 安装Neo4j数据库（社区版或企业版）

2. 创建Neo4j数据库：

```bash
# 启动Neo4j服务
neo4j start

# 创建数据库（使用Neo4j Browser或cypher-shell）
CREATE DATABASE hot_events;
```

3. 设置Neo4j用户和密码：

```bash
# 使用cypher-shell或Neo4j Browser
:use system
CREATE USER hot_events_user SET PASSWORD 'your_password' CHANGE NOT REQUIRED;
GRANT ALL ON DATABASE hot_events TO hot_events_user;
```

## 后端部署

### 配置文件

1. 修改`hot_event/src/main/resources/application.properties`文件：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/hot_events?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
spring.datasource.username=hot_events_user
spring.datasource.password=your_password

# Neo4j配置
spring.neo4j.uri=bolt://localhost:7687
spring.neo4j.authentication.username=hot_events_user
spring.neo4j.authentication.password=your_password

# DeepSeek API配置
deepseek.api.url=https://api.deepseek.com
deepseek.api.key=your_api_key

# 服务器配置
server.port=8080
```

2. 根据实际环境修改配置文件中的数据库连接信息和API密钥。

### 构建和运行

1. 构建后端应用：

```bash
cd hot_event
mvn clean package -DskipTests
```

2. 运行后端应用：

```bash
java -jar target/hot-event-1.0.0.jar
```

或者使用Spring Boot Maven插件：

```bash
mvn spring-boot:run
```

## 前端部署

### 配置文件

1. 修改`hot_event/frontend/.env`文件：

```
VUE_APP_API_BASE_URL=http://localhost:8080/api
```

2. 根据实际环境修改API基础URL。

### 构建和运行

1. 安装依赖：

```bash
cd hot_event/frontend
npm install
```

2. 开发模式运行：

```bash
npm run dev
```

3. 构建生产版本：

```bash
npm run build
```

4. 部署生产版本：

将`hot_event/frontend/dist`目录下的文件部署到Web服务器（如Nginx、Apache）中。

## Nginx配置示例

```nginx
server {
    listen 80;
    server_name your_domain.com;

    # 前端静态文件
    location / {
        root /path/to/hot_event/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## Docker部署

### 后端Docker部署

1. 创建Dockerfile：

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/hot-event-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

2. 构建Docker镜像：

```bash
cd hot_event
docker build -t hot-event-backend:1.0.0 .
```

3. 运行Docker容器：

```bash
docker run -d -p 8080:8080 --name hot-event-backend \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/hot_events \
  -e SPRING_DATASOURCE_USERNAME=hot_events_user \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e SPRING_NEO4J_URI=bolt://neo4j:7687 \
  -e SPRING_NEO4J_AUTHENTICATION_USERNAME=hot_events_user \
  -e SPRING_NEO4J_AUTHENTICATION_PASSWORD=your_password \
  hot-event-backend:1.0.0
```

### 前端Docker部署

1. 创建Dockerfile：

```dockerfile
FROM node:16-alpine as build-stage

WORKDIR /app

COPY frontend/package*.json ./

RUN npm install

COPY frontend/ .

RUN npm run build

FROM nginx:stable-alpine as production-stage

COPY --from=build-stage /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

2. 创建nginx.conf：

```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

3. 构建Docker镜像：

```bash
cd hot_event
docker build -t hot-event-frontend:1.0.0 .
```

4. 运行Docker容器：

```bash
docker run -d -p 80:80 --name hot-event-frontend hot-event-frontend:1.0.0
```

### Docker Compose部署

创建`docker-compose.yml`文件：

```yaml
version: '3'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: hot_events
      MYSQL_USER: hot_events_user
      MYSQL_PASSWORD: your_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
    ports:
      - "3306:3306"

  neo4j:
    image: neo4j:4.4
    environment:
      NEO4J_AUTH: hot_events_user/your_password
    volumes:
      - neo4j_data:/data
    ports:
      - "7474:7474"
      - "7687:7687"

  backend:
    build:
      context: .
      dockerfile: Dockerfile-backend
    depends_on:
      - mysql
      - neo4j
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/hot_events
      SPRING_DATASOURCE_USERNAME: hot_events_user
      SPRING_DATASOURCE_PASSWORD: your_password
      SPRING_NEO4J_URI: bolt://neo4j:7687
      SPRING_NEO4J_AUTHENTICATION_USERNAME: hot_events_user
      SPRING_NEO4J_AUTHENTICATION_PASSWORD: your_password
    ports:
      - "8080:8080"

  frontend:
    build:
      context: .
      dockerfile: Dockerfile-frontend
    depends_on:
      - backend
    ports:
      - "80:80"

volumes:
  mysql_data:
  neo4j_data:
```

运行Docker Compose：

```bash
docker-compose up -d
```

## 系统验证

部署完成后，可以通过以下步骤验证系统是否正常运行：

1. 访问前端应用：http://localhost/ 或 http://your_domain.com/
2. 登录系统（如果有登录功能）
3. 创建地区并验证地区管理功能
4. 生成时间线并验证时间线功能

## 常见问题

### 数据库连接问题

- 检查数据库服务是否正常运行
- 验证数据库连接信息是否正确
- 确认数据库用户权限是否正确

### Neo4j连接问题

- 检查Neo4j服务是否正常运行
- 验证Neo4j连接信息是否正确
- 确认Neo4j用户权限是否正确

### API调用问题

- 检查DeepSeek API密钥是否正确
- 验证网络连接是否正常
- 检查API调用日志

## 系统监控

### 日志监控

系统日志位于以下位置：

- 后端日志：`logs/hot-event.log`
- 前端日志：浏览器控制台

### 性能监控

可以使用以下工具监控系统性能：

- Spring Boot Actuator：http://localhost:8080/actuator
- MySQL性能监控：使用MySQL Workbench或其他监控工具
- Neo4j性能监控：使用Neo4j Browser或其他监控工具

## 备份与恢复

### MySQL备份

```bash
# 备份
mysqldump -u hot_events_user -p hot_events > hot_events_backup.sql

# 恢复
mysql -u hot_events_user -p hot_events < hot_events_backup.sql
```

### Neo4j备份

```bash
# 备份
neo4j-admin dump --database=hot_events --to=/path/to/hot_events_backup.dump

# 恢复
neo4j-admin load --from=/path/to/hot_events_backup.dump --database=hot_events --force
```

## 联系方式

如有问题，请联系系统管理员或开发团队。