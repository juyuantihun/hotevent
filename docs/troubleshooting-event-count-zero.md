# 解决事件数量为0的问题

## 问题描述

当使用DeepSeek联网搜索功能生成时间线时，虽然DeepSeek能够搜索到相关事件（如伊以战争），但最终生成的时间线显示`event_count=0`。

## 可能的原因

### 1. JSON解析失败
DeepSeek返回的响应格式可能与系统期望的JSON格式不匹配。

**常见情况：**
- DeepSeek返回markdown格式的响应（包含```json代码块）
- 返回纯文本而非JSON格式
- JSON格式不完整或有语法错误

### 2. 事件字段不匹配
DeepSeek返回的事件对象字段名称与系统期望的不一致。

**期望的JSON格式：**
```json
{
  "events": [
    {
      "title": "事件标题",
      "description": "事件描述",
      "eventTime": "2024-01-01T12:00:00",
      "location": "事件地点",
      "subject": "事件主体",
      "object": "事件客体",
      "eventType": "事件类型"
    }
  ]
}
```

### 3. API调用失败
- API密钥无效或过期
- 网络连接问题
- API限流或配额用完

## 诊断步骤

### 1. 使用调试工具

访问调试接口来检查具体问题：

```bash
# 测试事件检索
curl -X POST "http://localhost:8080/api/debug/deepseek/test-event-fetch?name=伊以战争时间线&description=伊以战争相关事件"

# 检查API状态
curl -X GET "http://localhost:8080/api/debug/deepseek/api-status"

# 获取使用统计
curl -X GET "http://localhost:8080/api/debug/deepseek/usage-stats"
```

### 2. 查看应用日志

查看详细的调试日志：

```bash
# 查看DeepSeek相关日志
tail -f logs/hot-events.log | grep -i "deepseek\|事件\|解析"

# 查看响应调试信息
tail -f logs/hot-events.log | grep -i "响应调试\|JSON"
```

### 3. 检查配置

确认配置文件中的设置：

```yaml
app:
  deepseek:
    api-key: ${DEEPSEEK_API_KEY:your_api_key}  # 确保API密钥正确
    web-search:
      enabled: true                            # 确保联网搜索已启用
      max-results: 10
      search-timeout: 30000
```

## 解决方案

### 方案1: 检查API密钥

1. 确认DeepSeek API密钥是否有效
2. 检查API密钥是否有足够的配额
3. 尝试在DeepSeek官网测试API密钥

```bash
# 设置正确的API密钥
export DEEPSEEK_API_KEY=your_actual_api_key
```

### 方案2: 改进提示词

如果DeepSeek返回的不是标准JSON格式，可以改进提示词：

1. 访问 http://localhost:8080/#/websearch
2. 测试联网搜索功能
3. 查看返回的响应格式

### 方案3: 使用调试模式

启用详细的调试日志：

```yaml
logging:
  level:
    com.hotech.events: debug
    com.hotech.events.service.impl.EnhancedDeepSeekServiceImpl: debug
    com.hotech.events.debug: debug
```

### 方案4: 手动测试响应解析

使用调试接口测试不同类型的响应：

```bash
# 测试markdown格式响应
curl -X POST "http://localhost:8080/api/debug/deepseek/simulate-issue?issueType=markdown"

# 测试纯文本响应
curl -X POST "http://localhost:8080/api/debug/deepseek/simulate-issue?issueType=text_only"

# 测试格式错误的JSON
curl -X POST "http://localhost:8080/api/debug/deepseek/simulate-issue?issueType=malformed_json"
```

## 临时解决方案

如果问题持续存在，可以使用以下临时解决方案：

### 1. 使用数据库备用数据

系统会自动降级到数据库中的事件数据：

```java
// 在EnhancedDeepSeekServiceImpl中
if (events.isEmpty()) {
    log.warn("DeepSeek返回空事件列表，使用数据库备用数据");
    return fetchEventsFromDatabase(request);
}
```

### 2. 手动添加事件

通过事件管理界面手动添加相关事件：

1. 访问 http://localhost:8080/#/event/create
2. 手动录入伊以战争相关事件
3. 将事件关联到时间线

### 3. 调整搜索参数

尝试调整搜索参数：

```yaml
app:
  deepseek:
    web-search:
      max-results: 5          # 减少结果数量
      search-timeout: 60000   # 增加超时时间
```

## 预防措施

### 1. 定期检查API状态

设置定期检查API连接状态：

```bash
# 添加到crontab
*/30 * * * * curl -s http://localhost:8080/api/debug/deepseek/api-status
```

### 2. 监控使用统计

定期查看API使用统计，避免超出配额：

```bash
curl -X GET "http://localhost:8080/api/debug/deepseek/usage-stats"
```

### 3. 备份重要配置

定期备份API密钥和配置文件。

## 联系支持

如果问题仍然存在，请提供以下信息：

1. 应用日志（特别是DeepSeek相关的日志）
2. API使用统计信息
3. 具体的错误信息
4. 使用的API密钥（脱敏后的前几位）
5. 网络环境信息

---

**注意：** 这个问题通常是由于DeepSeek API返回的响应格式与系统期望的JSON格式不匹配导致的。通过使用调试工具和改进响应解析逻辑，大多数情况下都能解决这个问题。