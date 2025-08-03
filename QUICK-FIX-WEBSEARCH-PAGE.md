# 联网搜索页面快速修复指南

## 🚨 问题描述

访问 `http://localhost:8080/#/websearch` 时出现404错误，显示"Whitelabel Error Page"。

## 🔧 快速解决方案

### 方案1: 使用临时HTML页面（立即可用）

我已经为您创建了临时的联网搜索管理页面，可以立即使用：

**访问地址：**
```
http://localhost:8080/websearch
```

**功能包括：**
- ✅ 查看联网搜索状态
- ✅ 启用/禁用联网搜索
- ✅ 测试搜索功能
- ✅ 查看配置信息
- ✅ 清除缓存

### 方案2: 构建完整前端应用

如果您想使用完整的Vue.js前端应用：

#### Windows用户：
```cmd
# 构建前端应用
.\scripts\build-frontend.bat

# 重启应用
.\scripts\start-with-websearch.bat your_api_key
```

#### Linux/Mac用户：
```bash
# 给脚本执行权限
chmod +x scripts/build-frontend.sh

# 构建前端应用
./scripts/build-frontend.sh

# 重启应用
./scripts/start-with-websearch.sh your_api_key
```

## 🎯 立即测试联网搜索功能

### 1. 访问临时管理页面
```
http://localhost:8080/websearch
```

### 2. 使用API接口直接测试
```bash
# PowerShell用户
Invoke-WebRequest -Uri "http://localhost:8080/api/debug/deepseek/test-event-fetch?name=伊以战争时间线" -Method POST

# 或者直接在浏览器访问
http://localhost:8080/api/debug/deepseek/test-event-fetch?name=伊以战争时间线&description=伊以战争相关事件
```

### 3. 运行调试脚本
```powershell
.\scripts\test-deepseek-debug.ps1
```

## 📊 可用的管理页面

1. **联网搜索管理**：http://localhost:8080/websearch
2. **API文档**：http://localhost:8080/doc.html
3. **系统健康检查**：http://localhost:8080/actuator/health
4. **主页**：http://localhost:8080

## 🔍 诊断"事件数量为0"问题

使用临时管理页面的测试功能：

1. 访问：http://localhost:8080/websearch
2. 在"测试功能"区域输入：`伊以战争时间线`
3. 点击"测试联网搜索"
4. 查看测试结果和响应内容

## 📝 重要说明

- **临时页面**：`/websearch` 是服务器端渲染的HTML页面，功能完整
- **前端路由**：`/#/websearch` 需要Vue.js应用构建后才能使用
- **API接口**：所有调试和管理API都正常工作
- **数据持久化**：所有配置和统计数据都会正确保存

## 🚀 下一步

1. **立即使用**：访问 http://localhost:8080/websearch 开始测试
2. **查看日志**：观察应用日志中的调试信息
3. **分析问题**：使用调试工具找出event_count=0的具体原因
4. **优化配置**：根据测试结果调整DeepSeek配置

---

**现在您可以立即使用联网搜索功能了！** 🎉