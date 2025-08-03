# 时间线增强功能 API 文档

## 概述

时间线增强功能提供了一套完整的RESTful API，支持智能时间段分割、地理信息处理和性能监控等功能。

## 基础信息

- **基础URL**: `http://localhost:8080/api`
- **API版本**: v2.0
- **内容类型**: `application/json`
- **字符编码**: UTF-8

## 认证

目前API暂不需要认证，后续版本将支持API Key认证。

## 核心API接口

### 1. 增强时间线生成

#### POST /timeline/enhanced/generate

生成增强版时间线，支持大时间跨度自动分割和地理信息处理。

**请求参数：**

```json
{
  "keyword": "string",           // 必填，搜索关键词
  "startTime": "datetime",       // 必填，开始时间 (ISO 8601格式)
  "endTime": "datetime",         // 必填，结束时间 (ISO 8601格式)
  "maxEvents": "integer",        // 可选，最大事件数量，默认100
  "includeGeographicInfo": "boolean"  // 可选，是否包含地理信息，默认true
}
```

**请求示例：**

```bash
curl -X POST "http://localhost:8080/api/timeline/enhanced/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "中美贸易战",
    "startTime": "2018-01-01T00:00:00",
    "endTime": "2020-12-31T23:59:59",
    "maxEvents": 100,
    "includeGeographicInfo": true
  }'
```

**响应格式：**

```json
{
  "success": true,
  "data": [
    {
      "id": "event_001",
      "title": "中美第一阶段贸易协议签署",
      "description": "中美两国在华盛顿签署第一阶段贸易协议，标志着贸易战阶段性缓解",
      "eventTime": "2020-01-15T10:30:00",
      "location": "华盛顿",
      "latitude": 38.9072,
      "longitude": -77.0369,
      "source": "官方新闻",
      "credibility": 0.95,
      "timeSegmentId": "segment_001",
      "tags": ["贸易", "协议", "中美关系"]
    }
  ],
  "metadata": {
    "totalEvents": 45,
    "timeSegments": 3,
    "processingTime": 12500,
    "geographicCoverage": 0.78,
    "apiCallsUsed": 8
  }
}
```

**错误响应：**

```json
{
  "success": false,
  "error": {
    "code": "INVALID_TIME_RANGE",
    "message": "结束时间不能早于开始时间",
    "timestamp": "2024-01-15T10:30:00Z",
    "details": {
      "startTime": "2023-01-01T00:00:00",
      "endTime": "2022-01-01T00:00:00"
    }
  }
}
```

### 2. 时间线配置管理

#### GET /timeline/configuration

获取当前时间线配置信息。

**响应示例：**

```json
{
  "success": true,
  "data": {
    "maxTokens": 4000,
    "segmentMaxDays": 7,
    "maxSegments": 10,
    "parallelProcessing": true,
    "geographicProcessing": {
      "enabled": true,
      "cacheEnabled": true,
      "cacheTtl": 3600
    }
  }
}
```

#### PUT /timeline/configuration

更新时间线配置。

**请求参数：**

```json
{
  "maxTokens": 4000,
  "segmentMaxDays": 7,
  "maxSegments": 10,
  "parallelProcessing": true
}
```

### 3. 地理信息处理

#### GET /geographic/coordinate/{location}

获取指定地点的坐标信息。

**路径参数：**
- `location`: 地点名称（URL编码）

**请求示例：**

```bash
curl -X GET "http://localhost:8080/api/geographic/coordinate/北京"
```

**响应示例：**

```json
{
  "success": true,
  "data": {
    "location": "北京",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "source": "cache",
    "accuracy": "city"
  }
}
```

#### POST /geographic/batch-coordinates

批量获取多个地点的坐标信息。

**请求参数：**

```json
{
  "locations": ["北京", "上海", "广州", "深圳"]
}
```

**响应示例：**

```json
{
  "success": true,
  "data": [
    {
      "location": "北京",
      "latitude": 39.9042,
      "longitude": 116.4074,
      "source": "cache"
    },
    {
      "location": "上海",
      "latitude": 31.2304,
      "longitude": 121.4737,
      "source": "api"
    }
  ]
}
```

#### POST /geographic/cache/clear

清理地理信息缓存。

**响应示例：**

```json
{
  "success": true,
  "message": "地理信息缓存已清理",
  "clearedItems": 156
}
```

#### GET /geographic/cache/status

获取地理信息缓存状态。

**响应示例：**

```json
{
  "success": true,
  "data": {
    "totalItems": 1024,
    "hitRate": 0.85,
    "missRate": 0.15,
    "evictionCount": 23,
    "averageLoadTime": 245
  }
}
```

### 4. 性能监控

#### GET /timeline/performance/metrics

获取时间线处理性能指标。

**响应示例：**

```json
{
  "success": true,
  "data": {
    "totalRequests": 1250,
    "averageProcessingTime": 8500,
    "maxProcessingTime": 45000,
    "minProcessingTime": 1200,
    "successRate": 0.96,
    "errorRate": 0.04,
    "apiCallsTotal": 5680,
    "cacheHitRate": 0.78,
    "geographicProcessingRate": 0.82,
    "timeSegmentationUsage": 0.35
  }
}
```

#### POST /timeline/performance/reset

重置性能监控指标。

**响应示例：**

```json
{
  "success": true,
  "message": "性能指标已重置"
}
```

### 5. 系统状态监控

#### GET /system/health

获取系统健康状态。

**响应示例：**

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "connectionPool": "healthy",
          "activeConnections": 5,
          "maxConnections": 20
        }
      },
      "volcengineApi": {
        "status": "UP",
        "details": {
          "lastCallTime": "2024-01-15T10:25:00Z",
          "responseTime": 1250,
          "errorRate": 0.02
        }
      },
      "geographicCache": {
        "status": "UP",
        "details": {
          "cacheSize": 1024,
          "hitRate": 0.85
        }
      }
    }
  }
}
```

#### GET /system/status

获取详细系统状态信息。

**响应示例：**

```json
{
  "success": true,
  "data": {
    "version": "2.0.0",
    "uptime": 86400000,
    "memory": {
      "used": 512,
      "free": 1536,
      "total": 2048,
      "max": 4096
    },
    "threads": {
      "active": 25,
      "peak": 45,
      "total": 120
    },
    "features": {
      "timeSegmentation": true,
      "geographicProcessing": true,
      "performanceMonitoring": true,
      "errorHandling": true
    }
  }
}
```

## 错误处理

### 错误代码说明

| 错误代码 | HTTP状态码 | 说明 | 解决方案 |
|---------|-----------|------|----------|
| INVALID_TIME_RANGE | 400 | 无效时间范围 | 检查开始和结束时间格式和逻辑 |
| KEYWORD_REQUIRED | 400 | 关键词必填 | 提供有效的搜索关键词 |
| MAX_EVENTS_EXCEEDED | 400 | 超过最大事件数限制 | 减少maxEvents参数值 |
| API_CALL_FAILED | 500 | 外部API调用失败 | 检查网络连接和API服务状态 |
| GEOGRAPHIC_PROCESSING_ERROR | 500 | 地理信息处理错误 | 检查地理信息服务状态 |
| TIMEOUT_ERROR | 504 | 请求超时 | 减小查询范围或重试 |
| RATE_LIMIT_EXCEEDED | 429 | 请求频率超限 | 降低请求频率 |
| INTERNAL_SERVER_ERROR | 500 | 内部服务器错误 | 查看服务器日志或联系技术支持 |

### 标准错误响应格式

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述信息",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req_123456789",
    "details": {
      "field": "具体错误字段",
      "value": "错误值",
      "constraint": "约束条件"
    }
  }
}
```

## 请求限制

### 频率限制
- 每分钟最多100次请求
- 每小时最多1000次请求
- 超出限制将返回429状态码

### 参数限制
- `keyword`: 最长100字符
- `maxEvents`: 1-500之间
- 时间跨度: 最长10年
- 批量坐标查询: 最多50个地点

## SDK和示例代码

### JavaScript/Node.js

```javascript
class TimelineEnhancementClient {
  constructor(baseUrl = 'http://localhost:8080/api') {
    this.baseUrl = baseUrl;
  }

  async generateTimeline(request) {
    const response = await fetch(`${this.baseUrl}/timeline/enhanced/generate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(request)
    });
    
    return await response.json();
  }

  async getCoordinate(location) {
    const response = await fetch(
      `${this.baseUrl}/geographic/coordinate/${encodeURIComponent(location)}`
    );
    
    return await response.json();
  }

  async getPerformanceMetrics() {
    const response = await fetch(`${this.baseUrl}/timeline/performance/metrics`);
    return await response.json();
  }
}

// 使用示例
const client = new TimelineEnhancementClient();

const timelineRequest = {
  keyword: "人工智能发展",
  startTime: "2020-01-01T00:00:00",
  endTime: "2023-12-31T23:59:59",
  maxEvents: 150
};

client.generateTimeline(timelineRequest)
  .then(result => {
    if (result.success) {
      console.log('生成的事件数量:', result.data.length);
      console.log('处理时间:', result.metadata.processingTime, 'ms');
    } else {
      console.error('错误:', result.error.message);
    }
  });
```

### Python

```python
import requests
import json
from datetime import datetime

class TimelineEnhancementClient:
    def __init__(self, base_url='http://localhost:8080/api'):
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({'Content-Type': 'application/json'})

    def generate_timeline(self, keyword, start_time, end_time, max_events=100):
        request_data = {
            'keyword': keyword,
            'startTime': start_time.isoformat(),
            'endTime': end_time.isoformat(),
            'maxEvents': max_events,
            'includeGeographicInfo': True
        }
        
        response = self.session.post(
            f'{self.base_url}/timeline/enhanced/generate',
            data=json.dumps(request_data)
        )
        
        return response.json()

    def get_coordinate(self, location):
        response = self.session.get(
            f'{self.base_url}/geographic/coordinate/{location}'
        )
        return response.json()

    def get_performance_metrics(self):
        response = self.session.get(f'{self.base_url}/timeline/performance/metrics')
        return response.json()

# 使用示例
client = TimelineEnhancementClient()

start_time = datetime(2020, 1, 1)
end_time = datetime(2023, 12, 31)

result = client.generate_timeline(
    keyword="区块链技术",
    start_time=start_time,
    end_time=end_time,
    max_events=200
)

if result['success']:
    events = result['data']
    print(f"生成了 {len(events)} 个事件")
    print(f"处理时间: {result['metadata']['processingTime']} ms")
else:
    print(f"错误: {result['error']['message']}")
```

### Java

```java
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TimelineEnhancementClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TimelineEnhancementClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> generateTimeline(String keyword, 
                                              LocalDateTime startTime, 
                                              LocalDateTime endTime, 
                                              int maxEvents) {
        String url = baseUrl + "/timeline/enhanced/generate";
        
        Map<String, Object> request = new HashMap<>();
        request.put("keyword", keyword);
        request.put("startTime", startTime.toString());
        request.put("endTime", endTime.toString());
        request.put("maxEvents", maxEvents);
        request.put("includeGeographicInfo", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> getCoordinate(String location) {
        String url = baseUrl + "/geographic/coordinate/" + location;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }
}

// 使用示例
TimelineEnhancementClient client = new TimelineEnhancementClient("http://localhost:8080/api");

Map<String, Object> result = client.generateTimeline(
    "5G技术发展",
    LocalDateTime.of(2019, 1, 1, 0, 0),
    LocalDateTime.of(2023, 12, 31, 23, 59),
    100
);

if ((Boolean) result.get("success")) {
    List<Map<String, Object>> events = (List<Map<String, Object>>) result.get("data");
    System.out.println("生成事件数量: " + events.size());
} else {
    Map<String, Object> error = (Map<String, Object>) result.get("error");
    System.out.println("错误: " + error.get("message"));
}
```

## 测试环境

### 测试数据

系统提供了测试数据端点用于开发和测试：

#### POST /test/generate-sample-data

生成测试用的样本事件数据。

**请求参数：**

```json
{
  "eventCount": 50,
  "timeSpan": "2020-01-01T00:00:00/2023-12-31T23:59:59",
  "topics": ["科技", "经济", "政治", "社会"]
}
```

### 性能测试

建议的性能测试场景：

1. **并发测试**: 同时发起10个时间线生成请求
2. **大数据量测试**: 查询跨度5年，maxEvents=500
3. **地理信息测试**: 批量查询100个不同地点的坐标
4. **缓存测试**: 重复查询相同地点验证缓存效果

## 版本兼容性

### API版本控制

- 当前版本: v2.0
- 支持的版本: v1.0 (兼容模式), v2.0
- 版本指定: 通过Header `API-Version: v2.0` 或URL参数 `?version=v2.0`

### 向后兼容性

v2.0版本保持与v1.0的向后兼容：

- v1.0的所有端点仍然可用
- 响应格式保持兼容
- 新增字段不会影响现有客户端

## 更新日志

### v2.0.0 (2024-01-15)
- 新增时间段分割功能
- 新增地理信息处理API
- 新增性能监控端点
- 优化大时间跨度查询性能
- 增强错误处理和响应格式

### v1.0.0 (2023-12-01)
- 基础时间线生成功能
- 基本事件查询和过滤
- 简单的错误处理

## 技术支持

- **API文档**: https://docs.hotech.com/timeline-enhancement-api
- **问题反馈**: https://github.com/hotech/hot-events/issues
- **技术支持**: api-support@hotech.com
- **状态页面**: https://status.hotech.com