# 时间线增强功能用户使用指南

## 概述

时间线增强功能是对原有事件时间线生成系统的重大升级，主要解决了以下问题：
- 大时间跨度查询时的事件缺失问题
- 事件地理信息的智能处理和显示
- 时间线UI的优化和简化

## 主要功能特性

### 1. 智能时间段分割
- **自动分割**：系统自动检测大时间跨度查询，将其分割为多个子时间段
- **并发处理**：多个时间段并发调用API，提高处理效率
- **智能合并**：自动合并和去重多个时间段的事件结果

### 2. 地理信息智能处理
- **坐标解析**：自动解析事件发生地的经纬度坐标
- **默认坐标**：国家级事件使用首都坐标，地区级事件使用首府坐标
- **缓存机制**：地理信息缓存提高响应速度

### 3. 优化的时间线UI
- **简洁显示**：隐藏中间时间线的时间标签
- **保留重要信息**：事件卡片仍显示详细时间信息
- **地理信息展示**：支持显示事件的地理位置信息

## 使用方法

### 基本查询

#### 1. 标准时间线查询
```javascript
// 前端调用示例
const request = {
    keyword: "中美贸易战",
    startTime: "2018-01-01T00:00:00",
    endTime: "2020-12-31T23:59:59",
    maxEvents: 100
};

fetch('/api/timeline/enhanced/generate', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
})
.then(response => response.json())
.then(data => {
    console.log('时间线事件:', data);
});
```

#### 2. 大时间跨度查询
对于跨度超过7天的查询，系统会自动启用时间段分割功能：

```javascript
const longTermRequest = {
    keyword: "新冠疫情",
    startTime: "2019-12-01T00:00:00",
    endTime: "2023-06-30T23:59:59",
    maxEvents: 200
};

// 系统会自动分割时间段并并发处理
fetch('/api/timeline/enhanced/generate', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(longTermRequest)
});
```

### 高级功能

#### 1. 地理信息查询
```javascript
// 查询特定地区的事件
const geoRequest = {
    keyword: "经济发展",
    startTime: "2020-01-01T00:00:00",
    endTime: "2023-12-31T23:59:59",
    maxEvents: 150,
    includeGeographicInfo: true  // 启用地理信息处理
};
```

#### 2. 性能监控查询
```javascript
// 获取性能指标
fetch('/api/timeline/performance/metrics')
.then(response => response.json())
.then(metrics => {
    console.log('处理时间:', metrics.processingTime);
    console.log('API调用次数:', metrics.totalApiCalls);
    console.log('缓存命中率:', metrics.cacheHitRate);
});
```

## 配置选项

### 1. 时间段分割配置

在 `timeline-enhancement-config.yml` 中配置：

```yaml
timeline:
  segmentation:
    max-span-days: 7          # 最大时间跨度（天）
    min-events-per-segment: 5  # 每段最少事件数
    max-segments: 10          # 最大分割段数
    parallel-processing: true  # 启用并发处理
```

### 2. 地理信息配置

```yaml
geographic:
  coordinate-cache-ttl: 3600    # 缓存过期时间（秒）
  default-coordinates:
    enabled: true               # 启用默认坐标
    fallback-to-capital: true   # 国家级回退到首都
    fallback-to-region-center: true  # 地区级回退到首府
```

### 3. API调用配置

```yaml
api:
  volcengine:
    max-tokens: 4000           # 增加到4000 tokens
    timeout: 30000             # 超时时间（毫秒）
    retry-count: 3             # 重试次数
    batch-size: 5              # 批量处理大小
```

## 响应格式

### 标准响应格式

```json
{
  "success": true,
  "data": [
    {
      "id": "event_001",
      "title": "中美第一阶段贸易协议签署",
      "description": "中美两国在华盛顿签署第一阶段贸易协议...",
      "eventTime": "2020-01-15T10:30:00",
      "location": "华盛顿",
      "latitude": 38.9072,
      "longitude": -77.0369,
      "source": "官方新闻",
      "credibility": 0.95,
      "timeSegmentId": "segment_001"
    }
  ],
  "metadata": {
    "totalEvents": 45,
    "timeSegments": 3,
    "processingTime": 12500,
    "geographicCoverage": 0.78
  }
}
```

### 错误响应格式

```json
{
  "success": false,
  "error": {
    "code": "INVALID_TIME_RANGE",
    "message": "结束时间不能早于开始时间",
    "details": {
      "startTime": "2023-01-01T00:00:00",
      "endTime": "2022-01-01T00:00:00"
    }
  }
}
```

## 最佳实践

### 1. 时间范围选择
- **短期查询**（< 7天）：直接查询，响应最快
- **中期查询**（7-30天）：自动分割，平衡效率和完整性
- **长期查询**（> 30天）：建议分批查询或使用更具体的关键词

### 2. 关键词优化
- 使用具体、明确的关键词
- 避免过于宽泛的词汇
- 可以组合多个相关关键词

### 3. 性能优化
- 合理设置 `maxEvents` 参数
- 利用地理信息缓存
- 监控API调用频率

## 故障排除

### 常见问题

#### 1. 查询结果为空
**可能原因：**
- 时间范围内确实没有相关事件
- 关键词过于具体或拼写错误
- API服务暂时不可用

**解决方案：**
- 扩大时间范围
- 尝试相关关键词
- 检查系统状态

#### 2. 地理信息缺失
**可能原因：**
- 事件描述中缺少地理位置信息
- 地理信息解析失败
- 缓存服务异常

**解决方案：**
- 检查地理信息缓存状态
- 重启地理信息服务
- 查看错误日志

#### 3. 响应时间过长
**可能原因：**
- 时间跨度过大
- 并发处理配置不当
- API服务响应慢

**解决方案：**
- 减小时间跨度
- 调整并发配置
- 检查API服务状态

### 错误代码说明

| 错误代码 | 说明 | 解决方案 |
|---------|------|----------|
| INVALID_TIME_RANGE | 无效时间范围 | 检查开始和结束时间 |
| KEYWORD_REQUIRED | 关键词必填 | 提供有效关键词 |
| API_CALL_FAILED | API调用失败 | 检查网络和API状态 |
| GEOGRAPHIC_PROCESSING_ERROR | 地理信息处理错误 | 检查地理服务状态 |
| TIMEOUT_ERROR | 请求超时 | 减小查询范围或重试 |

## 监控和维护

### 1. 性能监控
```bash
# 查看系统性能指标
curl -X GET "http://localhost:8080/api/timeline/performance/metrics"

# 查看缓存状态
curl -X GET "http://localhost:8080/api/geographic/cache/status"
```

### 2. 日志监控
重要日志文件位置：
- 应用日志：`logs/application.log`
- 时间线处理日志：`logs/timeline-processing.log`
- 地理信息处理日志：`logs/geographic-processing.log`
- API调用日志：`logs/api-calls.log`

### 3. 缓存管理
```bash
# 清理地理信息缓存
curl -X POST "http://localhost:8080/api/geographic/cache/clear"

# 预热常用地理信息
curl -X POST "http://localhost:8080/api/geographic/cache/warmup"
```

## 版本更新说明

### v2.0.0 (当前版本)
- 新增时间段分割功能
- 新增地理信息智能处理
- 优化时间线UI显示
- 提升大时间跨度查询性能

### 升级注意事项
1. 数据库需要执行迁移脚本
2. 配置文件需要更新
3. 前端组件需要更新到新版本
4. 建议清理旧版本缓存数据

## 技术支持

如遇到问题，请按以下顺序排查：
1. 查看本用户指南
2. 检查系统日志
3. 查看API文档
4. 联系技术支持团队

**联系方式：**
- 技术支持邮箱：support@hotech.com
- 问题反馈：https://github.com/hotech/hot-events/issues
- 文档更新：https://docs.hotech.com/timeline-enhancement