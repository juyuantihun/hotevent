/**
 * 应用全局状态管理模块
 * 管理全局UI状态、应用配置和主题设置
 */
import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'
import { createResettableState } from '@/utils/storeHelpers'

/**
 * 应用主题类型
 */
export type ThemeType = 'light' | 'dark' | 'system'

/**
 * 设备类型
 */
export type DeviceType = 'desktop' | 'tablet' | 'mobile'

/**
 * 应用配置接口
 */
export interface AppConfig {
  /** 页面大小选项 */
  pageSizes: number[]
  /** 默认页面大小 */
  defaultPageSize: number
  /** 是否启用动画 */
  enableAnimation: boolean
  /** 是否启用性能监控 */
  enablePerformanceMonitoring: boolean
  /** 是否启用调试模式 */
  debugMode: boolean
}

/**
 * 应用状态接口
 */
interface AppState {
  /** 侧边栏折叠状态 */
  sidebarCollapsed: boolean
  /** 应用主题 */
  theme: ThemeType
  /** 全局加载状态 */
  globalLoading: boolean
  /** 全局错误信息 */
  globalError: string | null
  /** 设备类型 */
  device: DeviceType
  /** 应用配置 */
  config: AppConfig
  /** 上次更新时间 */
  lastUpdated: number | null
}

/**
 * 应用状态管理存储
 * 使用组合式API风格
 */
export const useAppStore = defineStore('app', () => {
  /**
   * 初始状态
   */
  const initialState = (): AppState => ({
    sidebarCollapsed: false,
    theme: 'system',
    globalLoading: false,
    globalError: null,
    device: 'desktop',
    config: {
      pageSizes: [10, 20, 50, 100],
      defaultPageSize: 10,
      enableAnimation: true,
      enablePerformanceMonitoring: true,
      debugMode: import.meta.env.DEV
    },
    lastUpdated: null
  })
  
  // 创建可重置的状态
  const state = createResettableState<AppState>(initialState)
  
  // 系统主题偏好
  const systemPrefersDark = ref(window.matchMedia('(prefers-color-scheme: dark)').matches)
  
  // 监听系统主题变化
  if (typeof window !== 'undefined') {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    // 添加主题变化监听器
    const themeChangeHandler = (e: MediaQueryListEvent) => {
      systemPrefersDark.value = e.matches
      // 如果当前主题设置为跟随系统，则更新文档主题
      if (state.theme === 'system') {
        applyThemeToDocument(e.matches ? 'dark' : 'light')
      }
    }
    
    // 添加监听器
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', themeChangeHandler)
    } else if ((mediaQuery as any).addListener) {
      // 兼容旧版浏览器
      (mediaQuery as any).addListener(themeChangeHandler)
    }
  }
  
  /**
   * 计算属性
   */
  // 获取当前实际主题
  const currentTheme = computed((): 'light' | 'dark' => {
    if (state.theme !== 'system') {
      return state.theme
    }
    
    // 根据系统偏好返回主题
    return systemPrefersDark.value ? 'dark' : 'light'
  })
  
  // 是否为移动设备
  const isMobile = computed(() => state.device === 'mobile')
  
  // 是否为平板设备
  const isTablet = computed(() => state.device === 'tablet')
  
  // 是否为桌面设备
  const isDesktop = computed(() => state.device === 'desktop')
  
  // 是否处于调试模式
  const isDebugMode = computed(() => state.config.debugMode)
  
  /**
   * 将主题应用到文档
   * @param theme 主题类型
   */
  function applyThemeToDocument(theme: 'light' | 'dark' | 'system') {
    if (theme === 'system') {
      // 如果是系统主题，则根据系统偏好设置
      const prefersDark = systemPrefersDark.value
      document.documentElement.setAttribute('data-theme', prefersDark ? 'dark' : 'light')
    } else {
      document.documentElement.setAttribute('data-theme', theme)
    }
    
    // 添加类名，便于CSS选择器使用
    document.documentElement.classList.remove('theme-light', 'theme-dark')
    document.documentElement.classList.add(`theme-${theme === 'system' ? (systemPrefersDark.value ? 'dark' : 'light') : theme}`)
  }
  
  /**
   * 操作方法
   */
  // 切换侧边栏折叠状态
  function toggleSidebar() {
    state.sidebarCollapsed = !state.sidebarCollapsed
    state.lastUpdated = Date.now()
  }
  
  // 设置主题
  function setTheme(theme: ThemeType) {
    state.theme = theme
    applyThemeToDocument(theme)
    state.lastUpdated = Date.now()
  }
  
  // 设置全局加载状态
  function setGlobalLoading(status: boolean) {
    state.globalLoading = status
  }
  
  // 设置全局错误信息
  function setGlobalError(error: string | null) {
    state.globalError = error
  }
  
  // 设置设备类型
  function setDevice(device: DeviceType) {
    state.device = device
    state.lastUpdated = Date.now()
    
    // 在移动设备上自动折叠侧边栏
    if (device === 'mobile' && !state.sidebarCollapsed) {
      state.sidebarCollapsed = true
    }
  }
  
  // 更新应用配置
  function updateConfig(config: Partial<AppConfig>) {
    state.config = { ...state.config, ...config }
    state.lastUpdated = Date.now()
  }
  
  // 检测设备类型
  function detectDevice() {
    const width = window.innerWidth
    
    if (width < 768) {
      setDevice('mobile')
    } else if (width < 1200) {
      setDevice('tablet')
    } else {
      setDevice('desktop')
    }
  }
  
  // 初始化应用
  function initApp() {
    // 检测设备类型
    detectDevice()
    
    // 应用主题
    applyThemeToDocument(state.theme)
    
    // 添加窗口大小变化监听器
    window.addEventListener('resize', detectDevice)
    
    state.lastUpdated = Date.now()
  }
  
  // 监听主题变化
  watch(() => state.theme, (newTheme) => {
    applyThemeToDocument(newTheme)
  })
  
  // 初始化应用
  if (typeof window !== 'undefined') {
    initApp()
  }
  
  return {
    // 状态
    ...state,
    
    // 计算属性
    currentTheme,
    isMobile,
    isTablet,
    isDesktop,
    isDebugMode,
    
    // 方法
    toggleSidebar,
    setTheme,
    setGlobalLoading,
    setGlobalError,
    setDevice,
    updateConfig,
    detectDevice,
    initApp
  }
}, {
  /**
   * 持久化配置
   */
  persist: {
    key: 'app-state',
    storage: localStorage,
    paths: ['theme', 'sidebarCollapsed', 'config']
  }
})