# 前端编码风格指南

本文档定义了项目的编码风格规范，旨在保持代码的一致性和可维护性。所有开发人员应遵循这些规范。

## 命名规范

### 文件命名

- **组件文件**：使用 PascalCase（大驼峰）命名，例如：`UserProfile.vue`
- **工具/服务文件**：使用 camelCase（小驼峰）命名，例如：`dateFormatter.ts`
- **样式文件**：使用 kebab-case（短横线）命名，例如：`main-layout.css`
- **测试文件**：使用与被测试文件相同的命名方式，并添加 `.test` 或 `.spec` 后缀，例如：`UserProfile.test.ts`

### 变量命名

- **普通变量**：使用 camelCase（小驼峰）命名，例如：`userName`
- **常量**：使用全大写的 SNAKE_CASE，例如：`MAX_RETRY_COUNT`
- **私有变量**：使用下划线前缀，例如：`_privateVar`
- **布尔变量**：使用 `is`、`has`、`can` 等前缀，例如：`isLoading`、`hasPermission`

### 函数命名

- **普通函数**：使用动词开头的 camelCase，例如：`getUserData()`
- **事件处理函数**：使用 `handle` 或 `on` 前缀，例如：`handleSubmit()`、`onUserClick()`
- **Getter 函数**：使用 `get` 前缀，例如：`getFullName()`
- **Setter 函数**：使用 `set` 前缀，例如：`setUserPreferences()`
- **布尔返回函数**：使用 `is`、`has`、`can` 等前缀，例如：`isValidEmail()`

### 组件命名

- **组件名**：使用多词组合的 PascalCase，例如：`UserProfileCard`
- **基础组件**：使用 `Base`、`App` 或 `V` 前缀，例如：`BaseButton`、`AppHeader`
- **单例组件**：使用 `The` 前缀，例如：`TheNavbar`
- **紧密耦合的组件**：使用父组件名作为前缀，例如：`UserProfileAvatar`

## 代码格式

### 缩进和空格

- 使用 2 个空格进行缩进
- 在操作符前后添加空格
- 在逗号、冒号、分号后添加空格
- 在函数参数的逗号后添加空格
- 在对象字面量的冒号后添加空格

### 括号和换行

- 控制语句的左花括号与语句在同一行
- 函数声明的左花括号与函数声明在同一行
- 多行数组和对象字面量的最后一项后添加逗号
- 链式调用超过两个时，每个方法调用单独一行

### 引号

- 使用单引号 `'` 作为字符串的默认引号
- 在 JSX/TSX 中使用双引号 `"`
- 在字符串中包含单引号时使用双引号，反之亦然

## 注释规范

### 文件头注释

```typescript
/**
 * 文件名称
 * 文件描述
 */
```

### 函数注释

```typescript
/**
 * 函数描述
 * @param paramName 参数描述
 * @returns 返回值描述
 */
```

### 类/接口注释

```typescript
/**
 * 类/接口描述
 */
```

### 代码块注释

```typescript
// 代码块描述
```

### TODO 注释

```typescript
// TODO: 需要完成的任务描述
```

## Vue 组件规范

### 组件结构

组件应按以下顺序组织：

1. `<template>`
2. `<script>`
3. `<style>`

### Script 内部结构

在 `<script setup>` 中，按以下顺序组织代码：

1. 导入语句
2. 组件注册
3. Props 定义
4. Emits 定义
5. 响应式数据（ref, reactive）
6. 计算属性
7. 方法
8. 生命周期钩子
9. 监听器

### Props 定义

- 始终使用详细的类型定义
- 提供默认值和验证器
- 添加描述性注释

### 样式规范

- 优先使用 scoped 样式
- 使用 BEM 命名约定
- 避免使用 !important
- 使用 CSS 变量管理主题和颜色

## TypeScript 规范

### 类型定义

- 为函数参数和返回值添加类型注解
- 使用接口定义对象结构
- 使用类型别名简化复杂类型
- 避免使用 `any` 类型，优先使用 `unknown`
- 使用联合类型代替枚举

### 导入导出

- 按字母顺序组织导入语句
- 将第三方库的导入与本地导入分开
- 优先使用命名导出而非默认导出

## 最佳实践

### 代码质量

- 避免重复代码，提取共用逻辑
- 保持函数简短，单一职责
- 避免深层嵌套，优先使用提前返回
- 使用有意义的变量名和函数名
- 避免魔法数字和字符串，使用常量

### 性能优化

- 使用计算属性缓存计算结果
- 避免不必要的组件渲染
- 使用防抖和节流优化频繁事件
- 懒加载非关键组件和资源
- 优化大型列表渲染

### 安全性

- 避免使用 `v-html` 以防止 XSS 攻击
- 验证所有用户输入
- 不在前端存储敏感信息
- 使用 HTTPS 进行 API 通信
- 实施适当的 CORS 策略

## 工具配置

### ESLint 配置

项目使用 ESLint 进行代码质量检查，配置文件位于 `.eslintrc.js`。

### Prettier 配置

项目使用 Prettier 进行代码格式化，配置文件位于 `.prettierrc`。

### VS Code 设置

推荐在 VS Code 中使用以下设置：

```json
{
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "editor.defaultFormatter": "esbenp.prettier-vscode"
}
```

## 提交规范

使用 Angular 提交规范：

- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更改
- `style`: 不影响代码含义的更改（空格、格式化等）
- `refactor`: 既不修复 bug 也不添加功能的代码更改
- `perf`: 性能改进
- `test`: 添加或修正测试
- `chore`: 构建过程或辅助工具的变动