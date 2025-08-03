# 状态管理最佳实践指南

本文档提供了使用优化后的状态管理系统的最佳实践和指导原则，帮助开发人员更有效地管理应用状态。

## 目录

1. [状态管理架构](#状态管理架构)
2. [状态模块设计原则](#状态模块设计原则)
3. [状态更新流程规范](#状态更新流程规范)
4. [组件内状态管理](#组件内状态管理)
5. [状态持久化策略](#状态持久化策略)
6. [状态模块通信](#状态模块通信)
7. [性能优化技巧](#性能优化技巧)
8. [调试与故障排除](#调试与故障排除)

## 状态管理架构

我们的状态管理系统基于 Pinia，并进行了多项增强，包括：

- **状态模块通信**：允许不同状态模块之间进行通信
- **状态快照**：支持创建和恢复状态快照
- **状态锁定**：防止状态被意外修改
- **增强的持久化**：提供更灵活的状态持久化选项
- **状态调试工具**：在开发环境中提供强大的调试功能

### 核心模块

- `store/index.ts`：状态管理入口，提供全局状态管理功能
- `utils/storeHelpers.ts`：基础状态管理辅助工具
- `utils/storeEnhancer.ts`：状态管理增强工具
- `utils/storeDebugger.ts`：状态调试工具（仅开发环境）

### 状态模块

- `auth`：用户认证和权限管理
- `app`：应用全局配置和UI状态
- `dictionary`：系统字典数据
- `event`：事件数据管理

## 状态模块设计原则

### 单一职责原则

每个状态模块应该只负责管理一个特定领域的状态：

```typescript
// 好的做法：模块只负责一个领域
export const useAuthStore = defineStore('auth', () => {
  // 只包含认证相关的状态和方法
})

// 不好的做法：模块职责不清晰
export const useDataStore = defineStore('data', () => {
  // 混合了用户、产品、订单等多种状态
})
```

### 状态结构设计

- 使用接口明确定义状态结构
- 将相关状态分组
- 避免冗余和重复数据
- 使用计算属性派生状态

```typescript
// 好的做法：清晰的状态结构
interface AuthState {
  token: string
  userInfo: UserInfo | null
  roles: string[]
  permissions: string[]
  loading: Record<string, boolean>
}

// 不好的做法：扁平且混乱的状态
interface BadState {
  token: string
  userName: string
  userEmail: string
  isAdmin: boolean
  canEdit: boolean
  isLoading: boolean
  isLoadingUser: boolean
  // ...更多扁平化状态
}
```

### 可重置状态

所有状态模块应支持重置功能：

```typescript
// 使用辅助函数创建可重置的状态
const state = createResettableState<AuthState>(initialState)

// 在需要时重置状态
function logout() {
  resetState()
}
```

## 状态更新流程规范

### 状态更新原则

1. **单向数据流**：状态只能通过 actions 修改
2. **原子性更新**：相关状态应在一次更新中完成
3. **同步状态**：避免状态不一致
4. **错误处理**：所有状态更新操作应包含错误处理

### 标准更新流程

```typescript
// 1. 设置加载状态
setLoading('operation', true)

try {
  // 2. 调用API
  const response = await api.performOperation()
  
  // 3. 验证响应
  if (!response.success) {
    throw new Error(response.message)
  }
  
  // 4. 更新状态
  state.data = response.data
  
  // 5. 更新相关状态
  updateRelatedState()
  
  // 6. 返回结果
  return response.data
} catch (error) {
  // 7. 错误处理
  setError(error.message)
  throw error
} finally {
  // 8. 清理加载状态
  setLoading('operation', false)
}
```

### 批量更新

对于需要更新多个状态项的情况，使用 `$patch` 方法：

```typescript
// 好的做法：使用 $patch 批量更新
store.$patch({
  item1: newValue1,
  item2: newValue2,
  nested: {
    item3: newValue3
  }
})

// 不好的做法：多次单独更新
store.item1 = newValue1
store.item2 = newValue2
store.nested.item3 = newValue3
```

### 防抖更新

对于频繁变化的状态，使用防抖更新：

```typescript
// 使用防抖更新方法
store.$debouncedUpdate('searchQuery', newQuery, 300)
```

## 组件内状态管理

### 状态访问原则

- 组件应只访问其直接需要的状态
- 避免在多个组件中重复相同的状态访问逻辑
- 使用计算属性访问和转换状态

### 组合式API使用

```vue
<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/store'

// 获取状态存储
const authStore = useAuthStore()

// 使用计算属性访问状态
const isLoggedIn = computed(() => authStore.isLoggedIn)
const userName = computed(() => authStore.userInfo?.name || '')

// 调用操作方法
function handleLogout() {
  authStore.logoutAction()
}
</script>
```

### 避免状态泄漏

- 组件卸载时清理订阅和监听器
- 避免直接修改状态对象
- 使用深拷贝避免引用问题

## 状态持久化策略

### 持久化配置

```typescript
export const useAuthStore = defineStore('auth', () => {
  // 状态定义...
}, {
  persist: {
    key: 'auth-state',
    storage: localStorage,
    paths: ['token', 'userInfo', 'roles']
  }
})
```

### 持久化最佳实践

- 只持久化必要的状态
- 敏感数据使用会话存储（sessionStorage）
- 大型数据考虑使用 IndexedDB
- 定期清理过期数据

### 手动持久化

使用增强的持久化功能：

```typescript
// 手动触发持久化
authStore.$persistNow()

// 清除持久化数据
authStore.$clearPersisted()
```

## 状态模块通信

### 消息发送

```typescript
// 发送消息到特定模块
authStore.$sendMessage('event', 'user:login', { userId: 123 })

// 广播消息到所有模块
authStore.$broadcast('app:themeChanged', { theme: 'dark' })
```

### 消息监听

```typescript
// 在模块初始化时设置监听器
authStore.$onMessage('user:login', (payload) => {
  // 处理消息
  console.log('用户登录:', payload.userId)
})
```

### 通信最佳实践

- 定义清晰的消息类型
- 避免过度依赖模块间通信
- 使用类型化的消息负载
- 记录重要的通信事件

## 性能优化技巧

### 减少不必要的状态更新

- 使用计算属性缓存派生状态
- 避免在循环中更新状态
- 使用批量更新和防抖更新

### 优化大型集合

- 使用分页加载
- 实现虚拟滚动
- 考虑使用不可变数据结构

### 监控状态性能

使用状态调试器监控性能：

```typescript
// 获取性能统计
const stats = authStore.$debug.getPerformanceStats()
console.log('登录操作平均耗时:', stats.loginAction.avg)
```

## 调试与故障排除

### 使用状态调试器

在开发环境中，可以通过全局对象访问状态调试器：

```typescript
// 在浏览器控制台中
window.__STORE_DEBUGGER__.getLogs()
```

### 创建和应用快照

```typescript
// 创建快照
const snapshotId = authStore.$debug.createSnapshot()

// 应用快照
authStore.$debug.applySnapshot(snapshotId)
```

### 锁定和解锁状态

```typescript
// 锁定状态防止修改
authStore.$lock()

// 解锁状态
authStore.$unlock()
```

### 常见问题排查

1. **状态不更新**
   - 检查是否正确调用了更新方法
   - 确认状态未被锁定
   - 验证计算属性依赖是否正确

2. **状态丢失**
   - 检查持久化配置
   - 验证重置逻辑是否正确
   - 检查浏览器存储限制

3. **性能问题**
   - 使用调试器识别慢操作
   - 检查是否有不必要的状态更新
   - 优化大型集合的处理方式

---

通过遵循本指南中的最佳实践，开发人员可以更有效地使用优化后的状态管理系统，提高应用的性能和可维护性。