import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useAuthStore } from '@/store/modules/auth'
import { ElMessage } from 'element-plus'
import { lazyLoad } from './lazyLoad'

// 配置NProgress
NProgress.configure({ showSpinner: false })

// 白名单路由（不需要登录即可访问）
const whiteList = ['/login', '/register', '/forgot-password', '/reset-password/:token', '/404']

// 常用路由组件预加载
const Layout = lazyLoad(() => import('@/layout/index.vue'), 100)
const Login = lazyLoad(() => import('@/views/login/index.vue'), 100)
const Dashboard = lazyLoad(() => import('@/views/dashboard/index.vue'), 150)
const TimelineList = lazyLoad(() => import('@/views/timeline/index.vue'), 150)
const EventList = lazyLoad(() => import('@/views/event/EventList.vue'), 150)

// 次要路由组件使用标准懒加载
const Dictionary = lazyLoad(() => import('@/views/dictionary/index.vue'))
const DeepSeek = lazyLoad(() => import('@/views/deepseek/index.vue'))
const Relation = lazyLoad(() => import('@/views/relation/index.vue'))
const TimelineDetail = lazyLoad(() => import('@/views/timeline/components/TimelineDetailView.vue'))
const EventDetail = lazyLoad(() => import('@/views/event/detail.vue'))
const EventForm = lazyLoad(() => import('@/views/event/form.vue'))
const EventBatch = lazyLoad(() => import('@/views/event/batch.vue'))
const ErrorHandling = lazyLoad(() => import('@/views/error-handling/index.vue'))
const NotFound = lazyLoad(() => import('@/views/error/404.vue'))
const Register = lazyLoad(() => import('@/views/register/index.vue'))
const ForgotPassword = lazyLoad(() => import('@/views/forgot-password/index.vue'))
const ResetPassword = lazyLoad(() => import('@/views/reset-password/index.vue'))

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '仪表板',
          icon: 'Monitor',
          description: '系统概览和数据统计'
        }
      },
      {
        path: '/event',
        name: 'Event',
        meta: {
          title: '事件管理',
          icon: 'Document',
          description: '管理和查看事件数据'
        },
        children: [
          {
            path: '/event/list',
            name: 'EventList',
            component: EventList,
            meta: {
              title: '事件列表',
              icon: 'List',
              description: '查看和管理所有事件'
            }
          },
          {
            path: '/event/detail/:id',
            name: 'EventDetail',
            component: EventDetail,
            meta: {
              title: '事件详情',
              hidden: true,
              description: '查看事件详细信息'
            }
          },
          {
            path: '/event/create',
            name: 'EventCreate',
            component: EventForm,
            meta: {
              title: '录入事件',
              icon: 'Plus',
              description: '创建新的事件记录'
            }
          },
          {
            path: '/event/edit/:id',
            name: 'EventEdit',
            component: EventForm,
            meta: {
              title: '编辑事件',
              hidden: true,
              description: '修改事件信息'
            }
          },
          {
            path: '/event/batch',
            name: 'EventBatch',
            component: EventBatch,
            meta: {
              title: '批量录入',
              icon: 'Upload',
              description: '批量导入事件数据'
            }
          }
        ]
      },
      {
        path: '/dictionary',
        name: 'Dictionary',
        component: Dictionary,
        meta: {
          title: '字典管理',
          icon: 'Collection',
          description: '管理系统字典和配置项'
        }
      },
      {
        path: '/deepseek',
        name: 'DeepSeek',
        component: DeepSeek,
        meta: {
          title: 'DeepSeek管理',
          icon: 'Cpu',
          description: '管理DeepSeek智能搜索功能'
        }
      },
      {
        path: '/timeline',
        name: 'Timeline',
        meta: {
          title: '时间线管理',
          icon: 'Timer',
          description: '管理事件时间线'
        },
        children: [
          {
            path: '/timeline/list',
            name: 'TimelineList',
            component: TimelineList,
            meta: {
              title: '时间线列表',
              icon: 'List',
              description: '查看和管理所有时间线'
            }
          },
          {
            path: '/timeline/detail/:id',
            name: 'TimelineDetail',
            component: TimelineDetail,
            meta: {
              title: '时间线详情',
              hidden: true,
              description: '查看时间线详细信息'
            }
          }
        ]
      },
      {
        path: '/relation',
        name: 'Relation',
        component: Relation,
        meta: {
          title: '关联关系',
          icon: 'Share',
          description: '管理事件和实体之间的关联关系'
        }
      },
      {
        path: '/error-handling',
        name: 'ErrorHandling',
        component: ErrorHandling,
        meta: {
          title: '错误处理',
          icon: 'Warning',
          description: '错误处理机制演示'
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      hidden: true,
      description: '用户登录页面'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '注册',
      hidden: true,
      description: '用户注册页面'
    }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: {
      title: '忘记密码',
      hidden: true,
      description: '忘记密码页面'
    }
  },
  {
    path: '/reset-password/:token',
    name: 'ResetPassword',
    component: ResetPassword,
    meta: {
      title: '重置密码',
      hidden: true,
      description: '重置密码页面'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: '页面不存在',
      hidden: true,
      description: '404错误页面'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

/**
 * 检查路由是否在白名单中
 * @param path 路由路径
 * @returns 是否在白名单中
 */
const isInWhiteList = (path: string): boolean => {
  return whiteList.some(whitePath => {
    // 处理带参数的路由，如 '/reset-password/:token'
    if (whitePath.includes(':')) {
      const pathPattern = whitePath.replace(/:\w+/g, '[^/]+')
      const regex = new RegExp(`^${pathPattern}$`)
      return regex.test(path)
    }
    return whitePath === path
  })
}

/**
 * 验证认证状态
 * @param authStore 认证状态管理实例
 * @returns 认证状态验证结果
 */
const validateAuthState = async (authStore: any): Promise<{
  isValid: boolean
  needsRefresh: boolean
  error?: string
}> => {
  try {
    // 检查令牌是否存在
    if (!authStore.token) {
      return { isValid: false, needsRefresh: false, error: '未找到认证令牌' }
    }

    // 检查用户信息是否存在
    if (!authStore.userInfo) {
      try {
        // 尝试获取用户信息
        await authStore.getUserInfoAction()
      } catch (error) {
        return { isValid: false, needsRefresh: false, error: '获取用户信息失败' }
      }
    }

    // 检查令牌是否即将过期
    if (authStore.isTokenExpiringSoon) {
      return { isValid: true, needsRefresh: true }
    }

    return { isValid: true, needsRefresh: false }
  } catch (error) {
    return { isValid: false, needsRefresh: false, error: error instanceof Error ? error.message : '认证状态验证失败' }
  }
}

// 全局前置守卫
router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
  // 开始进度条
  NProgress.start()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - TimeFlow事件管理系统` : 'TimeFlow事件管理系统'
  
  // 获取认证状态
  const authStore = useAuthStore()
  const hasToken = authStore.token
  
  // 检查当前路由是否在白名单中
  const isWhiteListRoute = isInWhiteList(to.path)
  
  // 已登录状态
  if (hasToken) {
    // 如果已登录，不允许访问登录页面
    if (to.path === '/login') {
      ElMessage.info('您已登录，无需重复登录')
      next({ path: '/' })
      NProgress.done()
      return
    }
    
    // 验证认证状态
    const authValidation = await validateAuthState(authStore)
    
    if (!authValidation.isValid) {
      // 认证状态无效，清除状态并重定向到登录页面
      await authStore.logoutAction()
      ElMessage.error(authValidation.error || '会话已过期，请重新登录')
      next(`/login?redirect=${to.path}`)
      NProgress.done()
      return
    }
    
    // 如果需要刷新令牌
    if (authValidation.needsRefresh) {
      try {
        const refreshed = await authStore.refreshTokenAction()
        if (!refreshed) {
          // 令牌刷新失败，但仍然允许继续导航
          console.warn('令牌刷新失败，可能需要重新登录')
          ElMessage.warning('您的登录即将过期，建议重新登录')
        }
      } catch (refreshError) {
        console.warn('令牌刷新出错:', refreshError)
        ElMessage.warning('令牌刷新失败，建议重新登录')
      }
    }
    
    // 已登录且认证状态有效，允许访问任何页面
    next()
  } else {
    // 未登录状态
    if (isWhiteListRoute) {
      // 在白名单中，直接进入
      next()
    } else {
      // 不在白名单中，重定向到登录页面
      ElMessage.warning('请先登录')
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

// 全局后置钩子
router.afterEach(() => {
  // 结束进度条
  NProgress.done()
})

export default router