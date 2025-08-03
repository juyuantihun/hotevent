# 热点事件管理系统前端

[![构建状态](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/your-repo/hot-events-frontend)
[![测试覆盖率](https://img.shields.io/badge/coverage-80%25-green)](https://codecov.io/gh/your-repo/hot-events-frontend)
[![代码质量](https://img.shields.io/badge/code%20quality-A-brightgreen)](https://sonarcloud.io/dashboard?id=your-repo_hot-events-frontend)

## 项目简介

热点事件管理系统前端是一个基于Vue 3、TypeScript和Element Plus的现代化Web应用，用于管理和分析国际热点事件。

## 技术栈

- Vue 3
- TypeScript
- Pinia (状态管理)
- Vue Router
- Element Plus (UI组件库)
- Axios (HTTP客户端)
- Vite (构建工具)
- Vitest (单元测试)
- Playwright (端到端测试)

## 开发环境设置

### 前提条件

- Node.js 16.x 或更高版本
- npm 8.x 或更高版本

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

## 测试

### 运行单元测试

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

### 运行端到端测试

```bash
# 运行所有端到端测试
npm run test:e2e

# 使用UI界面运行端到端测试
npm run test:e2e:ui

# 查看端到端测试报告
npm run test:e2e:report
```

### 运行所有测试（CI模式）

```bash
npm run test:ci
```

## 代码质量

### 代码检查

```bash
npm run lint
```

### 类型检查

```bash
npm run type-check
```

## 持续集成

项目配置了以下CI/CD工具：

- GitHub Actions
- GitLab CI
- Jenkins

详细配置请参考各自的配置文件：
- `.github/workflows/test.yml`
- `.gitlab-ci.yml`
- `Jenkinsfile`

## 测试覆盖率

测试覆盖率目标：

- 语句覆盖率: 70%
- 分支覆盖率: 60%
- 函数覆盖率: 70%
- 行覆盖率: 70%

覆盖率报告可以在以下位置找到：
- HTML报告: `coverage/index.html`
- JSON报告: `coverage/coverage-final.json`
- LCOV报告: `coverage/lcov.info`

## 项目结构

```
src/
├── api/        # API请求封装
├── assets/     # 静态资源
├── components/ # 组件
├── directives/ # 自定义指令
├── router/     # 路由配置
├── services/   # 服务
├── store/      # 状态管理
├── style/      # 样式文件
├── types/      # TypeScript类型定义
├── utils/      # 工具函数
├── views/      # 页面组件
├── App.vue     # 根组件
└── main.ts     # 入口文件
```

## 测试结构

```
src/__tests__/
├── components/    # 组件测试
├── directives/    # 指令测试
├── e2e/           # 端到端测试
├── fixtures/      # 测试数据
├── integration/   # 集成测试
├── mocks/         # 模拟对象
├── services/      # 服务测试
├── store/         # 状态管理测试
├── utils/         # 工具函数测试
└── views/         # 页面组件测试
```

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

## 许可证

MIT