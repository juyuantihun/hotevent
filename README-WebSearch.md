# DeepSeek联网搜索功能快速开始

## 🚀 快速启动

### 方法一：使用启动脚本（推荐）

**Linux/Mac:**
```bash
chmod +x scripts/start-with-websearch.sh
./scripts/start-with-websearch.sh your_deepseek_api_key
```

**Windows:**
```cmd
scripts\start-with-websearch.bat your_deepseek_api_key
```

### 方法二：手动配置

1. **设置环境变量**
```bash
export DEEPSEEK_API_KEY=your_actual_api_key
```

2. **启动应用**
```bash
mvn spring-boot:run
```

## 🔧 配置说明

### 基础配置

在 `application.yml` 中已预配置联网搜索功能：

```yaml
app:
  deepseek:
    web-search:
      enabled: true              # 启用联网搜索
      max-results: 10            # 最大搜索结果数
      search-timeout: 30000      # 搜索超时30秒
```

### 获取DeepSeek API密钥

1. 访问 [DeepSeek官网](https://platform.deepseek.com/)
2. 注册账号并登录
3. 在控制台中创建API密钥
4. 复制密钥用于配置

## 🎯 功能特性

- ✅ **实时信息获取**: 通过联网搜索获取最新事件信息
- ✅ **权威来源**: 优先搜索BBC、CNN、路透社等权威媒体
- ✅ **智能缓存**: 自动缓存搜索结果，提高响应速度
- ✅ **配置管理**: 支持动态调整搜索参数
- ✅ **监控统计**: 详细的使用统计和性能监控
- ✅ **故障恢复**: 联网搜索失败时自动降级到数据库数据

## 📱 使用界面

启动应用后，访问以下页面：

- **主页**: http://localhost:8080
- **联网搜索管理**: http://localhost:8080/#/websearch
- **DeepSeek管理**: http://localhost:8080/#/deepseek
- **API文档**: http://localhost:8080/doc.html

## 🔍 测试联网搜索

### 通过Web界面测试

1. 访问 http://localhost:8080/#/websearch
2. 在"测试功能"区域输入查询内容
3. 点击"测试联网搜索"按钮
4. 查看测试结果

### 通过API测试

```bash
curl -X POST "http://localhost:8080/api/web-search/test?query=最新科技新闻"
```

### 通过事件检索测试

1. 访问 http://localhost:8080/#/deepseek
2. 创建新的时间线生成任务
3. 系统会自动使用联网搜索获取最新事件

## 📊 监控和统计

在联网搜索管理页面可以查看：

- **状态概览**: 功能可用性、请求统计、成功率、响应时间
- **详细统计**: 请求数量、性能指标、缓存状态
- **配置管理**: 动态调整搜索参数

## 🛠️ API接口

### 获取状态
```http
GET /api/web-search/status
```

### 启用/禁用功能
```http
POST /api/web-search/enable
POST /api/web-search/disable
```

### 测试搜索
```http
POST /api/web-search/test?query=测试内容
```

### 更新配置
```http
POST /api/web-search/config
Content-Type: application/json

{
  "enabled": true,
  "maxResults": 15,
  "searchTimeout": 25000
}
```

## 🔧 故障排除

### 常见问题

**1. 联网搜索不可用**
- 检查API密钥是否正确
- 确认网络连接正常
- 查看应用日志

**2. 搜索结果为空**
- 检查搜索关键词
- 确认时间范围设置
- 查看API调用日志

**3. 响应时间过长**
- 调整搜索超时时间
- 减少最大搜索结果数
- 检查网络延迟

### 查看日志

```bash
# 查看应用日志
tail -f logs/hot-events.log

# 过滤联网搜索相关日志
tail -f logs/hot-events.log | grep -i "websearch\|联网搜索"
```

## 📈 性能优化

### 推荐配置

```yaml
app:
  deepseek:
    web-search:
      enabled: true
      max-results: 10           # 建议10-20个结果
      search-timeout: 30000     # 建议30-60秒
    enhanced:
      cache-ttl: 300000         # 5分钟缓存
      rate-limit: 60            # 每分钟60次请求
```

### 最佳实践

1. **合理设置超时**: 30-60秒适中
2. **控制结果数量**: 10-20个结果平衡质量和速度
3. **启用缓存**: 显著提高响应速度
4. **监控使用**: 定期查看统计信息
5. **关键词优化**: 使用具体、相关的关键词

## 🔒 安全注意事项

- 🔐 不要在代码中硬编码API密钥
- 🔐 使用环境变量或配置文件管理密钥
- 🔐 定期轮换API密钥
- 🔐 监控API使用量，避免超额费用

## 📚 更多文档

- [详细配置指南](docs/websearch-configuration.md)
- [API文档](http://localhost:8080/doc.html)
- [DeepSeek官方文档](https://platform.deepseek.com/docs)

## 🆘 技术支持

如果遇到问题：
1. 查看本文档的故障排除部分
2. 检查应用日志
3. 访问联网搜索管理页面查看状态
4. 联系技术支持团队

---

**祝您使用愉快！** 🎉