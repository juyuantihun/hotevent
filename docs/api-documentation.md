# 时间线DeepSeek集成系统 API文档

## 概述

本文档描述了时间线DeepSeek集成系统的REST API接口，包括时间线生成、事件管理、配置管理等核心功能。

## 基础信息

- **基础URL**: `http://localhost:8080/api`
- **API版本**: v1.0
- **认证方式**: JWT Token
- **数据格式**: JSON
- **字符编码**: UTF-8

## 认证

所有API请求都需要在请求头中包含JWT Token：

```http
Authorization: Bearer <your-jwt-token>
```

## 核心API接口

### 1. 时间线管理 API

#### 1.1 生成时间线

**接口地址**: `POST /timeline/generate`

**功能描述**: 根据指定参数生成时间线，集成DeepSeek AI进行智能事件检索和验证。

**请求参数**:
```json
{
  "name": "中美贸易战时间线",
  "description": "2018-2024年中美贸易争端发展历程",
  "regionIds": [1, 2],
  "startTime": "2018-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "enableDeepSeekFetch": true,
  "maxEvents": 100,
  "credibilityThreshold": 0.8
}
```

**参数说明**:
- `name`: 时间线名称（必填，最大200字符）
- `description`: 时间线描述（可选，最大1000字符）
- `regionIds`: 地区ID列表（可选，为空时使用全球范围）
- `startTime`: 开始时间（必填，ISO 8601格式）
- `endTime`: 结束时间（必填，ISO 8601格式）
- `enableDeepSeekFetch`: 是否启用DeepSeek事件检索（默认true）
- `maxEvents`: 最大事件数量（默认100，最大500）
- `credibilityThreshold`: 可信度阈值（默认0.7，范围0.0-1.0）

**响应示例**:
```json
{
  "code": 200,
  "message": "时间线生成成功",
  "data": {
    "timelineId": 12345,
    "name": "中美贸易战时间线",
    "status": "COMPLETED",
    "eventCount": 85,
    "relationCount": 42,
    "generatedAt": "2024-01-15T10:30:00",
    "processingTime": 45000
  }
}
```

#### 1.2 获取时间线详情

**接口地址**: `GET /timeline/{id}`

**功能描述**: 获取指定时间线的详细信息和事件列表。

**路径参数**:
- `id`: 时间线ID

**查询参数**:
- `includeEvents`: 是否包含事件列表（默认true）
- `includeRelations`: 是否包含关系信息（默认false）
- `page`: 页码（默认1）
- `size`: 每页大小（默认20，最大100）

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "timeline": {
      "id": 12345,
      "name": "中美贸易战时间线",
      "description": "2018-2024年中美贸易争端发展历程",
      "status": "COMPLETED",
      "eventCount": 85,
      "relationCount": 42,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:35:00"
    },
    "events": [
      {
        "id": 67890,
        "title": "美国对中国商品加征关税",
        "description": "美国宣布对价值500亿美元的中国商品加征25%关税",
        "eventTime": "2018-03-22T14:00:00",
        "location": "华盛顿",
        "subject": "美国政府",
        "object": "中国商品",
        "eventType": "贸易政策",
        "credibilityScore": 0.95,
        "validationStatus": "VALIDATED",
        "sources": ["Reuters", "Bloomberg", "新华社"]
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 85,
      "totalPages": 5
    }
  }
}
```

#### 1.3 获取时间线列表

**接口地址**: `GET /timeline/list`

**功能描述**: 获取时间线列表，支持分页和筛选。

**查询参数**:
- `keyword`: 关键词搜索（可选）
- `status`: 状态筛选（可选，GENERATING/COMPLETED/FAILED）
- `startDate`: 创建开始日期（可选）
- `endDate`: 创建结束日期（可选）
- `page`: 页码（默认1）
- `size`: 每页大小（默认20，最大100）

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "timelines": [
      {
        "id": 12345,
        "name": "中美贸易战时间线",
        "description": "2018-2024年中美贸易争端发展历程",
        "status": "COMPLETED",
        "eventCount": 85,
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 156,
      "totalPages": 8
    }
  }
}
```

### 2. 事件管理 API

#### 2.1 获取事件详情

**接口地址**: `GET /event/{id}`

**功能描述**: 获取指定事件的详细信息。

**路径参数**:
- `id`: 事件ID

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 67890,
    "title": "美国对中国商品加征关税",
    "description": "美国宣布对价值500亿美元的中国商品加征25%关税",
    "eventTime": "2018-03-22T14:00:00",
    "location": "华盛顿",
    "subject": "美国政府",
    "object": "中国商品",
    "eventType": "贸易政策",
    "keywords": ["贸易战", "关税", "中美关系"],
    "credibilityScore": 0.95,
    "validationStatus": "VALIDATED",
    "fetchMethod": "DEEPSEEK",
    "sources": ["Reuters", "Bloomberg", "新华社"],
    "validationDetails": {
      "timeConsistency": true,
      "locationAccuracy": true,
      "logicalConsistency": true,
      "sourceCredibility": 0.95
    },
    "createdAt": "2024-01-15T10:32:00",
    "lastValidatedAt": "2024-01-15T10:33:00"
  }
}
```

#### 2.2 事件搜索

**接口地址**: `GET /event/search`

**功能描述**: 根据条件搜索事件。

**查询参数**:
- `keyword`: 关键词（可选）
- `subject`: 事件主体（可选）
- `object`: 事件客体（可选）
- `eventType`: 事件类型（可选）
- `location`: 地点（可选）
- `startTime`: 开始时间（可选）
- `endTime`: 结束时间（可选）
- `minCredibility`: 最小可信度（可选，默认0.0）
- `validationStatus`: 验证状态（可选）
- `page`: 页码（默认1）
- `size`: 每页大小（默认20，最大100）

### 3. 配置管理 API

#### 3.1 获取提示词模板

**接口地址**: `GET /config/prompt-templates`

**功能描述**: 获取所有提示词模板配置。

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "templateName": "event_fetch",
      "templateType": "EVENT_RETRIEVAL",
      "templateContent": "你是一个专业的国际事件分析师...",
      "version": "1.0",
      "isActive": true,
      "createdAt": "2024-01-15T09:00:00",
      "updatedAt": "2024-01-15T09:00:00"
    }
  ]
}
```

#### 3.2 更新提示词模板

**接口地址**: `PUT /config/prompt-templates/{id}`

**功能描述**: 更新指定的提示词模板。

**路径参数**:
- `id`: 模板ID

**请求参数**:
```json
{
  "templateContent": "更新后的模板内容...",
  "version": "1.1",
  "isActive": true
}
```

#### 3.3 获取系统配置

**接口地址**: `GET /config/system`

**功能描述**: 获取系统配置信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "deepseek": {
      "apiUrl": "https://api.deepseek.com/v1/chat/completions",
      "timeout": 60000,
      "maxRetries": 3,
      "rateLimitPerMinute": 60
    },
    "validation": {
      "credibilityThreshold": 0.7,
      "enableStrictMode": false,
      "autoValidation": true
    },
    "storage": {
      "batchSize": 100,
      "enableDeduplication": true,
      "maxEventAge": 365
    }
  }
}
```

### 4. 监控和统计 API

#### 4.1 获取API使用统计

**接口地址**: `GET /monitoring/api-usage`

**功能描述**: 获取DeepSeek API使用统计信息。

**查询参数**:
- `startDate`: 开始日期（可选）
- `endDate`: 结束日期（可选）
- `requestType`: 请求类型（可选）

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalRequests": 1250,
    "successfulRequests": 1180,
    "failedRequests": 70,
    "successRate": 0.944,
    "averageResponseTime": 2500,
    "totalTokenUsage": 125000,
    "dailyStats": [
      {
        "date": "2024-01-15",
        "requests": 85,
        "successRate": 0.95,
        "tokenUsage": 8500
      }
    ]
  }
}
```

#### 4.2 获取事件验证统计

**接口地址**: `GET /monitoring/validation-stats`

**功能描述**: 获取事件验证统计信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "totalValidations": 2500,
    "passedValidations": 2100,
    "failedValidations": 400,
    "passRate": 0.84,
    "averageCredibilityScore": 0.82,
    "validationsByType": {
      "TIME_CONSISTENCY": 2500,
      "LOCATION_ACCURACY": 2500,
      "LOGICAL_CONSISTENCY": 2500,
      "SOURCE_CREDIBILITY": 2500
    }
  }
}
```

#### 4.3 获取系统健康状态

**接口地址**: `GET /monitoring/health`

**功能描述**: 获取系统健康状态信息。

**响应示例**:
```json
{
  "code": 200,
  "message": "系统运行正常",
  "data": {
    "status": "HEALTHY",
    "uptime": 86400000,
    "services": {
      "database": {
        "status": "HEALTHY",
        "responseTime": 15,
        "connectionPool": {
          "active": 5,
          "idle": 15,
          "max": 20
        }
      },
      "deepseekApi": {
        "status": "HEALTHY",
        "responseTime": 2500,
        "rateLimitRemaining": 45
      },
      "neo4j": {
        "status": "HEALTHY",
        "responseTime": 25,
        "nodeCount": 15000,
        "relationshipCount": 8500
      }
    },
    "performance": {
      "cpuUsage": 0.35,
      "memoryUsage": 0.68,
      "diskUsage": 0.42
    }
  }
}
```

## 错误处理

### 错误响应格式

所有错误响应都遵循统一格式：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/timeline/generate",
  "details": {
    "field": "startTime",
    "error": "时间格式不正确，请使用ISO 8601格式"
  }
}
```

### 常见错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 400 | 请求参数错误 | 检查请求参数格式和必填字段 |
| 401 | 未授权访问 | 检查JWT Token是否有效 |
| 403 | 权限不足 | 联系管理员获取相应权限 |
| 404 | 资源不存在 | 检查资源ID是否正确 |
| 429 | 请求频率过高 | 降低请求频率，遵守限流规则 |
| 500 | 服务器内部错误 | 联系技术支持 |
| 502 | DeepSeek API不可用 | 检查网络连接和API配置 |
| 503 | 服务暂时不可用 | 稍后重试 |

### 限流规则

- **时间线生成**: 每用户每分钟最多5次请求
- **事件搜索**: 每用户每分钟最多60次请求
- **配置管理**: 每用户每分钟最多10次请求
- **监控接口**: 每用户每分钟最多30次请求

## 最佳实践

### 1. 时间线生成优化

- 合理设置时间范围，避免过长时间跨度
- 使用适当的可信度阈值，平衡质量和数量
- 启用缓存机制，避免重复生成相同时间线
- 监控生成进度，及时处理异常情况

### 2. 事件搜索优化

- 使用具体的搜索条件，提高查询效率
- 合理设置分页参数，避免一次性加载过多数据
- 利用索引字段进行筛选，如时间、地区、类型等
- 缓存常用搜索结果，提升用户体验

### 3. 配置管理建议

- 定期备份配置文件，防止意外丢失
- 在生产环境中谨慎修改配置，建议先在测试环境验证
- 监控配置变更日志，及时发现异常修改
- 使用版本控制管理配置变更历史

### 4. 错误处理建议

- 实现客户端重试机制，处理临时性错误
- 记录详细的错误日志，便于问题排查
- 为用户提供友好的错误提示信息
- 建立错误监控和告警机制

## SDK和示例代码

### JavaScript/TypeScript 示例

```typescript
// 时间线生成示例
const generateTimeline = async (request: TimelineGenerateRequest) => {
  try {
    const response = await fetch('/api/timeline/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(request)
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    return result.data;
  } catch (error) {
    console.error('时间线生成失败:', error);
    throw error;
  }
};

// 事件搜索示例
const searchEvents = async (params: EventSearchParams) => {
  const queryString = new URLSearchParams(params).toString();
  const response = await fetch(`/api/event/search?${queryString}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
};
```

### Java 示例

```java
// 使用RestTemplate调用API
@Service
public class TimelineApiClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public TimelineResponse generateTimeline(TimelineGenerateRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAuthToken());
        
        HttpEntity<TimelineGenerateRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(
            "/api/timeline/generate", 
            entity, 
            TimelineResponse.class
        );
    }
}
```

## 版本更新日志

### v1.0.0 (2024-01-15)
- 初始版本发布
- 支持基础时间线生成功能
- 集成DeepSeek AI事件检索
- 实现事件验证和存储机制

### 后续版本规划
- v1.1.0: 增加批量操作API
- v1.2.0: 支持GraphQL查询
- v1.3.0: 增加Webhook通知机制
- v2.0.0: 重构API架构，提升性能

## 联系方式

- **技术支持**: tech-support@hotech.com
- **API问题**: api-support@hotech.com
- **文档反馈**: docs@hotech.com
- **GitHub**: https://github.com/hotech/hot-events