# DeepSeek联网搜索功能实现说明

## 概述

基于deepseek_r1_online_search-main项目的实现方式，为hot_event项目添加了DeepSeek联网搜索功能。

## 主要变更

### 1. 配置更新

#### API端点变更
```yaml
# 原配置
api-url: https://ark.cn-beijing.volces.com/api/v3/chat/completions

# 新配置（支持联网搜索）
api-url: https://ark.cn-beijing.volces.com/api/v3/bots
```

#### 模型变更
```yaml
# 原配置
model: deepseek-r1-250120

# 新配置（支持联网搜索）
model: bot-20250329163710-8zcqm
```

#### 联网搜索配置
```yaml
web-search:
  enabled: true
  max-results: 10
  search-timeout: 30000
```

### 2. 新增服务实现

#### DeepSeekOnlineSearchServiceImpl
- **位置**: `src/main/java/com/hotech/events/service/impl/DeepSeekOnlineSearchServiceImpl.java`
- **功能**: 专门的联网搜索服务实现
- **特点**: 
  - 使用火山方舟bots API端点
  - 支持联网搜索功能
  - 包含详细的提示词模板
  - 支持事件解析和去重

#### 核心功能
1. **连接测试**: `checkConnection()`
2. **获取最新事件**: `fetchLatestEvents()`
3. **关键词搜索**: `fetchEventsByKeywords()`
4. **日期范围搜索**: `fetchEventsByDateRange()`
5. **事件关系分析**: `analyzeEventRelations()`
6. **时间线组织**: `organizeTimelines()`

### 3. 新增测试控制器

#### DeepSeekOnlineSearchController
- **位置**: `src/main/java/com/hotech/events/controller/DeepSeekOnlineSearchController.java`
- **功能**: 提供HTTP接口测试联网搜索功能

#### 测试接口
- `GET /api/deepseek/online-search/test/connection` - 连接测试
- `GET /api/deepseek/online-search/config` - 配置信息
- `POST /api/deepseek/online-search/test/latest-events` - 获取最新事件
- `POST /api/deepseek/online-search/test/events-by-keywords` - 关键词搜索
- `POST /api/deepseek/online-search/test/events-by-date-range` - 日期范围搜索
- `POST /api/deepseek/online-search/test/chat` - 简单对话测试

### 4. 测试脚本

#### PowerShell测试脚本
- **位置**: `scripts/test-online-search.ps1`
- **功能**: 自动化测试所有联网搜索接口
- **特点**: 
  - 完整的接口测试覆盖
  - 详细的结果展示
  - 错误处理和状态显示

## API差异对比

| 特性 | 原版本 | 联网搜索版本 |
|------|--------|-------------|
| API端点 | `/api/v3/chat/completions` | `/api/v3/bots` |
| 模型名称 | `deepseek-r1-250120` | `bot-20250329163710-8zcqm` |
| 联网搜索 | 不支持 | 默认支持 |
| 请求格式 | 标准chat格式 | 标准chat格式 |
| 响应格式 | 标准OpenAI格式 | 标准OpenAI格式 |
| 系统消息 | 支持 | 支持 |

## 联网搜索特性

### 1. 自动联网搜索
- 使用bots端点的模型默认启用联网搜索
- 无需额外的API参数配置
- 自动获取最新的网络信息

### 2. 智能提示词设计
- 明确要求使用联网搜索功能
- 指定权威新闻源优先级
- 强调信息的时效性和真实性
- 规范化的JSON返回格式

### 3. 事件数据结构
```json
{
  "events": [
    {
      "title": "事件标题",
      "description": "详细描述",
      "eventTime": "2025-01-25T12:00:00",
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
```

## 使用方法

### 1. 启动应用
```bash
mvn spring-boot:run
```

### 2. 测试连接
```bash
curl -X GET "http://localhost:8080/api/deepseek/online-search/test/connection"
```

### 3. 获取最新事件
```bash
curl -X POST "http://localhost:8080/api/deepseek/online-search/test/latest-events?limit=5"
```

### 4. 关键词搜索
```bash
curl -X POST "http://localhost:8080/api/deepseek/online-search/test/events-by-keywords?keywords=以色列,巴勒斯坦&limit=5"
```

### 5. 运行完整测试
```powershell
cd hot_event/scripts
./test-online-search.ps1
```

## 错误处理和备用机制

### 1. 连接失败处理
- API调用失败时自动回退到数据库数据
- 详细的错误日志记录
- 优雅的错误响应

### 2. 数据解析容错
- JSON解析失败时的备用处理
- 数据格式验证和修复
- 事件去重机制

### 3. 性能优化
- 批量处理大量事件
- 缓存机制减少重复调用
- 超时控制和重试机制

## 配置说明

### 环境变量
```bash
# 设置API密钥
export DEEPSEEK_API_KEY=your_volcengine_api_key
```

### 应用配置
```yaml
app:
  deepseek:
    api-url: https://ark.cn-beijing.volces.com/api/v3/bots
    api-key: ${DEEPSEEK_API_KEY:your_api_key}
    model: bot-20250329163710-8zcqm
    web-search:
      enabled: true
      max-results: 10
      search-timeout: 30000
```

## 注意事项

1. **API密钥**: 确保使用有效的火山方舟API密钥
2. **网络连接**: 联网搜索需要稳定的网络连接
3. **响应时间**: 联网搜索可能比普通对话响应时间更长
4. **配额限制**: 注意API调用频率和配额限制
5. **数据质量**: 联网搜索结果的质量取决于网络信息的可用性

## 验证清单

- [x] 配置文件已更新为bots端点
- [x] 模型名称已更新为bot版本
- [x] 联网搜索功能已启用
- [x] 新增联网搜索服务实现
- [x] 新增测试控制器和接口
- [x] 创建测试脚本
- [x] 错误处理和备用机制完善
- [ ] API连接测试通过（需要有效API密钥）
- [ ] 联网搜索功能验证通过

## 后续优化建议

1. **流式输出**: 实现流式响应以提升用户体验
2. **缓存优化**: 增加智能缓存机制减少API调用
3. **并发处理**: 支持并发请求处理提升性能
4. **监控告警**: 添加API调用监控和告警机制
5. **数据验证**: 增强联网搜索结果的验证和过滤