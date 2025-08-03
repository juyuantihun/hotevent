/**
 * 状态管理入口文件
 * 统一导出所有状态模块，提供全局状态管理功能
 */
import { createPinia } from 'pinia'

// 导入状态模块
import { useAuthStore } from './modules/auth'
import { useDictionaryStore } from './modules/dictionary'
import { useEventStore } from './modules/event'
import { useAppStore } from './modules/app'

// 创建 Pinia 实例
const pinia = createPinia()

/**
 * 初始化状态管理系统
 * @param app Vue应用实例
 */
export function initializeStoreSystem(app: any) {
  // 使用Pinia
  app.use(pinia)
  
  // 预加载常用状态
  if (import.meta.env.PROD) {
    // 在生产环境中预加载关键状态
    const appStore = useAppStore()
    appStore.initApp()
    
    // 检查认证状态
    const authStore = useAuthStore()
    if (authStore.isLoggedIn) {
      authStore.checkSession().catch(() => {
        // 会话无效时静默处理
        console.warn('用户会话已过期')
      })
    }
  } else {
    // 在开发环境中也预加载应用状态，但不检查认证
    const appStore = useAppStore()
    appStore.initApp()
  }
}

// 导出 Pinia 实例
export default pinia

// 导出状态模块
export {
  useAuthStore,
  useDictionaryStore,
  useEventStore,
  useAppStore
}

/**
 * 重置所有状态
 * 用于用户登出或需要清空所有状态的场景
 * @param options 重置选项
 */
export function resetAllStores(options = { preservePersisted: false }) {
  const authStore = useAuthStore()
  const dictionaryStore = useDictionaryStore()
  const eventStore = useEventStore()
  const appStore = useAppStore()

  // 重置所有状态
  authStore.resetState && authStore.resetState(options)
  dictionaryStore.resetState && dictionaryStore.resetState(options)
  eventStore.resetState && eventStore.resetState(options)
  appStore.resetState && appStore.resetState(options)
}

/**
 * 获取所有状态的快照
 * 用于调试和状态恢复
 * @returns 状态快照对象
 */
export function getStoresSnapshot() {
  const authStore = useAuthStore()
  const dictionaryStore = useDictionaryStore()
  const eventStore = useEventStore()
  const appStore = useAppStore()

  return {
    auth: { ...authStore.$state },
    dictionary: { ...dictionaryStore.$state },
    event: { ...eventStore.$state },
    app: { ...appStore.$state }
  }
}