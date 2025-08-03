/**
 * 资源预加载配置
 * 用于管理应用中需要预加载的资源
 */

// 预加载的路由路径
export const preloadRoutes = [
  '/login',
  '/dashboard',
  '/event/list',
  '/timeline/list'
]

// 预加载的图片资源
export const preloadImages = [
  // 添加关键图片资源路径
]

// 预加载的组件
export const preloadComponents = [
  () => import('@/components/common/LoadingIndicator.vue'),
  () => import('@/components/common/ErrorState.vue'),
  () => import('@/components/common/EmptyState.vue')
]

// 预连接的外部资源
export const preconnectResources = [
  // API服务器地址会自动添加
]

// 预加载优先级
export const preloadPriority = {
  HIGH: 'high',
  MEDIUM: 'medium',
  LOW: 'low'
}