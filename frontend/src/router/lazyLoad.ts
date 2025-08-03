import { defineAsyncComponent } from 'vue'
import LoadingComponent from '@/components/common/LoadingIndicator.vue'
import ErrorComponent from '@/components/common/ErrorState.vue'

/**
 * 高级懒加载组件工厂函数
 * 提供加载状态、错误处理和预加载功能
 * 
 * @param componentImport 组件导入函数
 * @param delay 延迟显示加载组件的时间（毫秒）
 * @param timeout 加载超时时间（毫秒）
 */
export function lazyLoad(
  componentImport: () => Promise<any>,
  delay: number = 200,
  timeout: number = 10000
) {
  return defineAsyncComponent({
    loader: componentImport,
    loadingComponent: LoadingComponent,
    errorComponent: ErrorComponent,
    delay,
    timeout,
    // 加载失败时的处理
    onError(error, retry, fail, attempts) {
      if (error.message.includes('Failed to fetch') && attempts <= 3) {
        // 网络错误时自动重试，最多3次
        console.log(`组件加载失败，正在重试 (${attempts}/3)...`)
        retry()
      } else {
        // 其他错误或超过重试次数
        console.error('组件加载失败:', error)
        fail()
      }
    },
  })
}

/**
 * 预加载路由组件
 * 在后台预加载组件但不立即渲染
 * 
 * @param componentImport 组件导入函数
 */
export function preloadRouteComponent(componentImport: () => Promise<any>) {
  // 触发组件加载但不渲染
  componentImport()
  // 返回正常的懒加载组件
  return lazyLoad(componentImport)
}