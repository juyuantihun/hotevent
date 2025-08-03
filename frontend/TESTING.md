# 前端测试和代码质量指南

本文档提供了关于如何使用项目中配置的测试和代码质量工具的指南。

## 代码质量工具

### ESLint

ESLint 已配置为检查代码质量和风格问题。

运行 ESLint 检查：

```bash
npm run lint
```

### SonarQube

SonarQube 配置已添加，用于更深入的代码质量分析。

要使用 SonarQube 进行分析，请确保已安装 SonarQube Scanner，然后运行：

```bash
sonar-scanner
```

## 测试框架

### 单元测试

项目使用 Vitest 作为测试框架，结合 Vue Test Utils 和 Testing Library 进行组件测试。

运行测试：

```bash
# 运行所有测试
npm run test

# 以监视模式运行测试
npm run test:watch

# 运行测试并生成覆盖率报告
npm run test:coverage

# 使用UI界面运行测试
npm run test:ui
```

### 测试文件结构

测试文件应放在 `src/__tests__` 目录中，并遵循以下结构：

```
src/
├── __tests__/
│   ├── components/    # 组件测试
│   ├── views/         # 页面视图测试
│   ├── store/         # 状态管理测试
│   ├── utils/         # 工具函数测试
│   └── integration/   # 集成测试
```

### 编写测试

测试文件应使用 `.test.ts` 或 `.spec.ts` 扩展名。

示例测试文件：

```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/vue';
import MyComponent from '@/components/MyComponent.vue';

describe('MyComponent', () => {
  it('renders correctly', () => {
    render(MyComponent, {
      props: {
        title: '测试标题',
      },
    });
    
    expect(screen.getByText('测试标题')).toBeDefined();
  });
});
```

## 测试数据

测试数据应放在 `src/__tests__/fixtures` 目录中，按功能或组件分类。

## 持续集成

在提交代码前，请确保运行以下命令：

```bash
# 类型检查
npm run type-check

# 代码风格检查
npm run lint

# 运行测试
npm run test
```

## 最佳实践

1. 为每个组件和工具函数编写单元测试
2. 使用 Testing Library 的查询方法，优先使用用户可见的文本和标签
3. 测试组件的行为而不是实现细节
4. 使用 `describe` 和 `it` 清晰描述测试内容
5. 保持测试简单，每个测试只测试一个行为
6. 使用 `beforeEach` 和 `afterEach` 设置和清理测试环境