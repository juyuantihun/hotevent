# DeepSeek API 火山方舟版本迁移说明

## 概述

本文档说明了如何将hot_event项目中的DeepSeek API调用从官方版本迁移到火山方舟版本。

## 主要变更

### 1. 配置文件变更 (application.yml)

```yaml
# 原配置（官方版本）
deepseek:
  api-url: https://api.deepseek.com/v1/chat/completions
  api-key: sk-1b5b172c7d1248808ccfb059ead0acde
  model: deepseek-chat

# 新配置（火山方舟版本）
deepseek:
  api-url: https://ark.cn-beijing.volces.com/api/v3/chat/completions
  api-key: 314de2f8-ecd5-4311-825b-65e0233e350e
  model: deepseek-r1-250120
```

### 2. 代码变更 (DeepSeekServiceImpl.java)

#### 2.1 联网搜索功能
- **变更原因**: 火山方舟版本暂不支持联网搜索功能
- **变更内容**: 注释掉联网搜索相关配置代码

```java
// 火山方舟版本暂不支持联网搜索功能，注释掉相关配置
// if (webSearchEnabled) {
//     requestBody.put("web_search", true);
//     // ... 其他联网搜索配置
// }
```

#### 2.2 消息格式优化
- **变更原因**: 火山方舟版本支持system role，可以提供更好的对话体验
- **变更内容**: 添加system message

```java
// 添加系统消息（火山方舟版本支持）
Map<String, String> systemMessage = new HashMap<>();
systemMessage.put("role", "system");
systemMessage.put("content", "你是一个专业的事件分析助手，能够准确分析和处理各种国际事件数据。");
messages.add(systemMessage);

// 添加用户消息
Map<String, String> userMessage = new HashMap<>();
userMessage.put("role", "user");
userMessage.put("content", prompt);
messages.add(userMessage);
```

### 3. 新增测试工具

#### 3.1 测试控制器
- **文件**: `VolcengineTestController.java`
- **功能**: 提供HTTP接口测试火山方舟API连接状态
- **接口**:
  - `GET /api/volcengine/test/connection` - 测试API连接
  - `GET /api/volcengine/test/chat` - 测试简单对话
  - `GET /api/volcengine/test/config` - 获取当前配置

#### 3.2 命令行测试脚本
- **Windows批处理**: `scripts/test-volcengine-deepseek.bat`
- **PowerShell脚本**: `scripts/test-volcengine-deepseek.ps1`

## API差异对比

| 特性 | 官方版本 | 火山方舟版本 |
|------|----------|--------------|
| API URL | https://api.deepseek.com/v1/chat/completions | https://ark.cn-beijing.volces.com/api/v3/chat/completions |
| 模型名称 | deepseek-chat | deepseek-r1-250120 |
| 联网搜索 | 支持 | 暂不支持 |
| System Role | 支持 | 支持 |
| 请求格式 | 标准OpenAI格式 | 标准OpenAI格式 |
| 响应格式 | 标准OpenAI格式 | 标准OpenAI格式 |

## 测试方法

### 1. 使用PowerShell脚本测试
```powershell
cd hot_event/scripts
./test-volcengine-deepseek.ps1
```

### 2. 使用HTTP接口测试
启动应用后访问：
```
GET http://localhost:8080/api/volcengine/test/connection
GET http://localhost:8080/api/volcengine/test/config
```

### 3. 使用Swagger UI测试
访问：`http://localhost:8080/doc.html`
找到"火山方舟DeepSeek测试"分组进行测试

## 注意事项

1. **API密钥安全**: 生产环境中应使用环境变量设置API密钥
   ```bash
   export DEEPSEEK_API_KEY=your-volcengine-api-key
   ```

2. **联网搜索功能**: 火山方舟版本暂不支持联网搜索，相关功能已禁用

3. **模型版本**: 使用的是`deepseek-r1-250120`模型，可能与官方版本在响应格式上有细微差异

4. **错误处理**: 保留了原有的错误处理和备用数据机制

## 回滚方案

如需回滚到官方版本，只需修改配置文件：

```yaml
deepseek:
  api-url: https://api.deepseek.com/v1/chat/completions
  api-key: ${DEEPSEEK_API_KEY:your-official-api-key}
  model: deepseek-chat
  web-search:
    enabled: true  # 重新启用联网搜索
```

并取消DeepSeekServiceImpl.java中联网搜索相关代码的注释。

## 验证清单

- [x] 配置文件已更新为火山方舟版本
- [x] 代码中联网搜索功能已禁用
- [x] 添加了system message支持
- [x] 测试脚本可以正常运行
- [x] HTTP测试接口已创建
- [x] 应用启动无错误
- [x] 编译问题已修复（文本块语法、var关键字、Swagger注解）
- [ ] API连接测试通过（需要有效的API密钥）

## 迁移完成状态

✅ **配置迁移完成**
- API URL已更新为火山方舟版本：`https://ark.cn-beijing.volces.com/api/v3/chat/completions`
- 模型名称已更新为：`deepseek-r1-250120`
- API密钥已配置为火山方舟提供的token

✅ **代码适配完成**
- 联网搜索功能已禁用（火山方舟版本暂不支持）
- 添加了system message支持
- 修复了Java 8兼容性问题（文本块语法、var关键字）
- 移除了缺失的Swagger注解依赖

✅ **应用启动成功**
- 应用已成功启动在端口8080
- 所有组件正常初始化
- 数据库连接正常
- 系统监控服务运行正常

⚠️ **待验证项目**
- API连接测试需要有效的API密钥才能完成
- 建议在生产环境中使用环境变量设置API密钥

## 联系信息

如有问题，请联系开发团队或查看相关文档。