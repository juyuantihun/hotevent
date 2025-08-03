# 依赖更新计划

## 需要更新的依赖

根据`npm outdated`和`npm audit`的结果，以下依赖需要更新：

### 主要依赖

1. **xlsx** - 当前版本存在高危安全漏洞，需要更新到最新版本
2. **vue-echarts** - 可以更新到最新的7.x版本
3. **pinia** - 可以考虑更新到最新的3.x版本（需要评估兼容性）

### 开发依赖

1. **@vitejs/plugin-vue** - 更新到最新的6.0.0版本
2. **vite** - 更新到最新的5.x版本（避免7.x的破坏性更新）
3. **vue-tsc** - 更新到最新的3.0.3版本
4. **typescript** - 更新到5.3.3或更新版本
5. **@typescript-eslint/eslint-plugin** 和 **@typescript-eslint/parser** - 更新到兼容的版本
6. **unplugin-auto-import** 和 **unplugin-vue-components** - 更新到兼容的版本

## 安全漏洞修复

1. **xlsx** - 存在高危安全漏洞，需要更新或替换
2. **vue-template-compiler** - 存在中等安全漏洞，通过更新vue-tsc解决
3. **esbuild** - 存在中等安全漏洞，通过更新vite解决

## API使用更新

1. **Axios拦截器** - 优化错误处理和请求重试逻辑
2. **Vue 3 API** - 确保使用最新的Vue 3 Composition API最佳实践
3. **Pinia状态管理** - 优化状态管理模式，使用最新的Pinia API

## 实施步骤

1. 备份package.json
2. 更新关键依赖（优先修复安全漏洞）
3. 更新开发依赖
4. 更新API使用
5. 测试应用功能