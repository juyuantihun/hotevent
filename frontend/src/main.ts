import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import router from './router'
import { registerDirectives } from './directives'
import App from './App.vue'
import './style/fixed-index.css'
import './style/auth-responsive.css'
import { initPerformanceMonitoring, recordMetric } from './utils/performance'
import { startMemoryMonitoring } from './services/memoryMonitor'
import lazyLoadDirective from './directives/lazyLoad'
import { initializeStoreSystem, useAppStore, useDictionaryStore } from './store'
import { installGlobalErrorHandler } from './services/errorHandler'

// 开发环境下使用模拟数据
if (import.meta.env.DEV) {
  console.log('[开发模式] 已启用模拟数据功能，当后端API不可用时将使用模拟数据')
}

// 记录应用启动时间
const appStartTime = performance.now()

// 初始化性能监控
initPerformanceMonitoring()

// 创建应用实例
const app = createApp(App)

// 初始化状态管理系统
initializeStoreSystem(app)

// 安装全局错误处理器
installGlobalErrorHandler(app)

// 使用核心插件
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 注册自定义指令
registerDirectives(app)

// 注册图片懒加载指令
app.use(lazyLoadDirective)

// 延迟注册非关键组件
const registerNonCriticalComponents = () => {
  // 动态导入 Element Plus 图标
  import('@element-plus/icons-vue').then((module) => {
    const ElementPlusIconsVue = module
    // 注册 Element Plus 图标
    for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
      app.component(key, component)
    }
  })
}

// 初始化应用状态
const appStore = useAppStore()
const dictionaryStore = useDictionaryStore()

// 设置设备类型
const setDeviceType = () => {
  const width = window.innerWidth
  if (width < 768) {
    appStore.setDevice('mobile')
  } else if (width < 1200) {
    appStore.setDevice('tablet')
  } else {
    appStore.setDevice('desktop')
  }
}

// 监听窗口大小变化
window.addEventListener('resize', setDeviceType)
setDeviceType()

// 应用主题
const applyTheme = () => {
  const theme = appStore.currentTheme
  document.documentElement.setAttribute('data-theme', theme)
}
applyTheme()

// 监听系统主题变化
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', applyTheme)

// 挂载应用
app.mount('#app')

// 记录应用挂载时间
recordMetric('appMount', appStartTime)

// 预加载字典数据
dictionaryStore.initAllDictionaries().catch(error => {
  console.error('初始化字典数据失败:', error)
})

// 使用 requestIdleCallback 在浏览器空闲时注册非关键组件
if ('requestIdleCallback' in window) {
  window.requestIdleCallback(registerNonCriticalComponents, { timeout: 2000 })
} else {
  // 降级处理
  setTimeout(registerNonCriticalComponents, 100)
}

// 在应用完全加载后记录
window.addEventListener('load', () => {
  recordMetric('appFullyLoaded', appStartTime)
  
  // 启动内存监控（在应用完全加载后）
  setTimeout(() => {
    startMemoryMonitoring()
  }, 2000)
})