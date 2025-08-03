# DeepSeek联网搜索功能配置指南

## 概述

本文档介绍如何配置和使用DeepSeek的联网搜索功能，该功能可以让AI模型访问最新的网络信息，提供更准确、更及时的事件数据。

## 功能特性

- **实时信息获取**: 通过联网搜索获取最新的新闻和事件信息
- **权威来源**: 优先搜索权威新闻源和官方发布的信息
- **智能缓存**: 自动缓存搜索结果，提高响应速度
- **配置管理**: 灵活的配置选项，支持动态调整
- **监控统计**: 详细的使用统计和性能监控

## 配置说明

### 1. 基础配置

在 `application.yml` 中配置基本参数：

```yaml
app:
  deepseek:
    api-url: https://api.deepseek.com/v1/chat/completions
    api-key: ${DEEPSEEK_API_KEY:your_api_key_here}
    model: deepseek-chat
    
    # 联网搜索配置
    web-search:
      enabled: true              # 是否启用联网搜索
      max-results: 10            # 最大搜索结果数
      search-timeout: 30000      # 搜索超时时间(毫秒)
```

### 2. 环境变量配置

推荐使用环境变量配置API密钥：

```bash
# Linux/Mac
export DEEPSEEK_API_KEY=your_actual_api_key

# Windows
set DEEPSEEK_API_KEY=your_actual_api_key
```

### 3. 高级配置

```yaml
app:
  deepseek:
    enhanced:
      # 缓存配置
      cache-ttl: 300000          # 缓存TTL，5分钟
      enable-cache: true
      
      # 限流配置
      rate-limit: 60             # 每分钟60次请求
      enable-rate-limit: true
      
      # 监控配置
      enable-monitoring: true
      health-check-interval: 60000  # 1分钟
```

## 使用方法

### 1. 通过Web界面管理

访问系统的"联网搜索管理"页面：
- 查看联网搜索状态
- 启用/禁用联网搜索功能
- 调整配置参数
- 测试搜索功能
- 查看使用统计

### 2. API接口

#### 获取联网搜索状态
```http
GET /api/web-search/status
```

#### 启用联网搜索
```http
POST /api/web-search/enable
```

#### 禁用联网搜索
```http
POST /api/web-search/disable
```

#### 测试联网搜索
```http
POST /api/web-search/test?query=最新科技新闻
```

#### 更新配置
```http
POST /api/web-search/config
Content-Type: application/json

{
  "enabled": true,
  "maxResults": 15,
  "searchTimeout": 25000
}
```

### 3. 程序化使用

```java
@Autowired
private WebSearchService webSearchService;

// 检查联网搜索是否可用
boolean available = webSearchService.isWebSearchAvailable();

// 测试联网搜索
Map<String, Object> testResult = webSearchService.testWebSearch("最新国际新闻");

// 获取使用统计
Map<String, Object> stats = webSearchService.getWebSearchStats();
```

## 提示词优化

联网搜索功能会自动优化提示词，包含以下指令：

1. **明确搜索要求**: 指示AI使用联网搜索功能
2. **权威来源**: 优先搜索权威新闻源
3. **关键词建议**: 提供相关的搜索关键词
4. **时效性要求**: 强调获取最新信息

示例提示词结构：
```
请使用联网搜索功能，根据以下条件查找并生成相关的国际事件数据：

【联网搜索要求】
- 请务必使用联网搜索功能获取最新、最准确的事件信息
- 优先搜索权威新闻源（如BBC、CNN、路透社、新华社等）
- 确保事件信息的时效性和真实性

【搜索条件】
时间线名称：国际政治事件
目标地区：全球
时间范围：2024-01-01 至 2024-12-31

【建议搜索关键词】
- "国际政治事件" + "最新新闻" + "重大事件"
- "international politics" + "breaking news"
```

## 监控和统计

系统提供详细的监控和统计功能：

### 请求统计
- 总请求数
- 成功请求数
- 失败请求数
- 成功率

### 性能统计
- 总搜索时间
- 平均响应时间
- 缓存命中率
- 缓存大小

### 健康检查
- API连接状态
- 响应时间监控
- 错误率监控

## 故障排除

### 常见问题

1. **联网搜索不可用**
   - 检查API密钥是否正确配置
   - 确认网络连接正常
   - 查看DeepSeek API服务状态

2. **搜索结果为空**
   - 检查搜索关键词是否合适
   - 确认时间范围设置合理
   - 查看API调用日志

3. **响应时间过长**
   - 调整搜索超时时间
   - 减少最大搜索结果数
   - 检查网络延迟

### 日志查看

查看相关日志：
```bash
# 查看DeepSeek服务日志
tail -f logs/hot-events.log | grep "DeepSeek\|WebSearch"

# 查看API调用日志
tail -f logs/hot-events.log | grep "联网搜索\|web.search"
```

## 最佳实践

1. **合理设置超时时间**: 建议设置为30-60秒
2. **控制搜索结果数量**: 建议设置为10-20个结果
3. **使用缓存**: 启用缓存可以显著提高响应速度
4. **监控使用情况**: 定期查看统计信息，优化配置
5. **关键词优化**: 使用具体、相关的关键词提高搜索质量

## 安全注意事项

1. **API密钥保护**: 不要在代码中硬编码API密钥
2. **访问控制**: 限制联网搜索功能的访问权限
3. **内容过滤**: 对搜索结果进行适当的内容过滤
4. **日志脱敏**: 避免在日志中记录敏感信息

## 更新日志

- **v1.0.0**: 初始版本，支持基本的联网搜索功能
- **v1.1.0**: 添加缓存和限流功能
- **v1.2.0**: 增加监控和统计功能
- **v1.3.0**: 优化提示词模板，提高搜索质量

## 技术支持

如果遇到问题，请：
1. 查看本文档的故障排除部分
2. 检查系统日志
3. 联系技术支持团队