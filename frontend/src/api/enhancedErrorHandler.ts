/**
 * 增强的API错误处理模块
 * 提供统一的API错误处理、智能重试和用户友好的反馈
 */
import axios from 'axios'
import type { AxiosInstance, AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import { useAppStore } from '@/store'
import { handleError, ErrorType, ErrorSeverity } from '@/services/errorHandler'
import { useAuthStore } from '@/store/modules/auth'
import router from '@/router'
import { networkMonitor } from '@/services/networkMonitor'

/**
 * 错误响应码映射
 */
interface ErrorCodeMap {
  [key: number]: {
    message: string
    severity: ErrorSeverity
    retryable: boolean
    action?: () => void
  }
}

/**
 * 错误响应码映射表
 */
const errorCodeMap: ErrorCodeMap = {
  400: {
    message: '请求参数错误',
    severity: ErrorSeverity.WARNING,
    retryable: false
  },
  401: {
    message: '用户未授权或会话已过期',
    severity: ErrorSeverity.ERROR,
    retryable: false,
    action: async () => {
      const authStore = useAuthStore()
      await authStore.logoutAction()
      router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
    }
  },
  403: {
    message: '没有权限访问该资源',
    severity: ErrorSeverity.ERROR,
    retryable: false
  },
  404: {
    message: '请求的资源不存在',
    severity: ErrorSeverity.WARNING,
    retryable: false
  },
  405: {
    message: '请求方法不允许',
    severity: ErrorSeverity.WARNING,
    retryable: false
  },
  408: {
    message: '请求超时',
    severity: ErrorSeverity.WARNING,
    retryable: true
  },
  409: {
    message: '资源冲突',
    severity: ErrorSeverity.WARNING,
    retryable: false
  },
  429: {
    message: '请求过于频繁，请稍后再试',
    severity: ErrorSeverity.WARNING,
    retryable: true
  },
  500: {
    message: '服务器内部错误',
    severity: ErrorSeverity.ERROR,
    retryable: true
  },
  501: {
    message: '服务未实现',
    severity: ErrorSeverity.ERROR,
    retryable: false
  },
  502: {
    message: '网关错误',
    severity: ErrorSeverity.ERROR,
    retryable: true
  },
  503: {
    message: '服务不可用',
    severity: ErrorSeverity.ERROR,
    retryable: true
  },
  504: {
    message: '网关超时',
    severity: ErrorSeverity.ERROR,
    retryable: true
  }
}

/**
 * 错误处理配置接口
 */
interface ErrorHandlerConfig {
  // 是否显示错误消息
  showErrorMessage: boolean
  // 显示错误消息的方式：'message'、'notification'、'both'
  messageType: 'message' | 'notification' | 'both'
  // 是否更新全局错误状态
  updateGlobalError: boolean
  // 是否启用重试
  enableRetry: boolean
  // 最大重试次数
  maxRetries: number
  // 重试延迟（毫秒）
  retryDelay: number
  // 是否启用离线请求队列
  offlineQueueing: boolean
  // 是否在控制台记录详细错误信息
  logDetails: boolean
  // 是否上报错误到服务器
  reportToServer: boolean
  // 自定义错误处理函数
  customHandler?: (error: AxiosError, config: ErrorHandlerConfig) => void
}

/**
 * 默认错误处理配置
 */
const defaultConfig: ErrorHandlerConfig = {
  showErrorMessage: true,
  messageType: 'message',
  updateGlobalError: false,
  enableRetry: true,
  maxRetries: 3,
  retryDelay: 1000,
  offlineQueueing: true,
  logDetails: true,
  reportToServer: import.meta.env.PROD
}

/**
 * 离线请求队列
 */
const offlineRequestQueue: Array<{
  config: InternalAxiosRequestConfig
  resolve: (value: any) => void
  reject: (reason?: any) => void
}> = []

/**
 * 创建增强的API错误处理拦截器
 * @param config 错误处理配置
 * @returns API错误处理拦截器
 */
export function createEnhancedApiErrorHandler(
  config: Partial<ErrorHandlerConfig> = {}
) {
  // 合并配置
  const mergedConfig: ErrorHandlerConfig = {
    ...defaultConfig,
    ...config
  }
  
  return (axiosInstance: AxiosInstance): AxiosInstance => {
    // 请求拦截器
    axiosInstance.interceptors.request.use(
      (config) => {
        // 检查网络连接
        if (!navigator.onLine && mergedConfig.offlineQueueing) {
          // 如果离线且启用了离线队列
          return new Promise((resolve, reject) => {
            // 将请求添加到队列
            offlineRequestQueue.push({ config, resolve, reject })
            
            // 显示离线提示
            if (mergedConfig.showErrorMessage) {
              showErrorMessage({
                message: '您当前处于离线状态，请求将在恢复连接后发送',
                type: 'warning',
                duration: 3000
              }, mergedConfig.messageType)
            }
          })
        }
        
        // 添加请求标识，用于取消重复请求
        const requestId = generateRequestId(config)
        config.headers = config.headers || {}
        config.headers['X-Request-ID'] = requestId
        
        return config
      },
      (error) => Promise.reject(error)
    )
    
    // 响应拦截器
    axiosInstance.interceptors.response.use(
      (response: AxiosResponse) => {
        // 处理业务逻辑错误
        const data = response.data
        
        // 假设API返回格式为 { code: number, message: string, data: any }
        if (data && data.code !== undefined && data.code !== 0 && data.code !== 200) {
          // 业务逻辑错误
          const errorMessage = data.message || '未知业务错误'
          
          // 处理错误
          handleApiError({
            message: errorMessage,
            code: data.code,
            response,
            config: response.config
          }, mergedConfig)
          
          // 返回被拒绝的Promise，这样调用方可以捕获到错误
          return Promise.reject(new Error(errorMessage))
        }
        
        // 正常响应
        return response
      },
      async (error: AxiosError) => {
        // 获取请求配置
        const config = error.config as InternalAxiosRequestConfig & { 
          _retryCount?: number,
          _isRetry?: boolean,
          _retryDelay?: number,
          _isRefreshingToken?: boolean
        }
        
        // 处理401错误（未授权）- 尝试刷新令牌
        if (error.response?.status === 401 && config && !config._isRefreshingToken) {
          // 如果是刷新令牌请求失败，直接跳转到登录页
          if (config.url?.includes('/auth/refresh-token')) {
            ElMessage.error('会话已过期，请重新登录')
            localStorage.removeItem('token')
            router.push('/login')
            return Promise.reject(error)
          }
          
          // 如果已经是重试请求，避免无限循环
          if (config._isRetry) {
            ElMessage.error('认证失败，请重新登录')
            localStorage.removeItem('token')
            router.push('/login')
            return Promise.reject(error)
          }
          
          try {
            // 标记为刷新令牌请求
            config._isRefreshingToken = true
            
            // 导入刷新令牌函数
            const { refreshToken } = await import('./auth')
            
            // 刷新令牌
            const { token } = await refreshToken()
            
            // 更新localStorage中的令牌
            localStorage.setItem('token', token)
            
            // 更新原始请求的令牌
            if (config.headers) {
              config.headers.Authorization = `Bearer ${token}`
            }
            
            // 重新发送原始请求
            return axiosInstance(config)
          } catch (refreshError) {
            // 刷新令牌失败，跳转到登录页
            const authStore = useAuthStore()
            await authStore.logoutAction()
            
            ElMessage.error('会话已过期，请重新登录')
            router.push(`/login?redirect=${router.currentRoute.value.fullPath}`)
            return Promise.reject(error)
          }
        }
        
        // 如果启用了重试且错误可重试
        if (
          mergedConfig.enableRetry && 
          config && 
          (!config._retryCount || config._retryCount < mergedConfig.maxRetries)
        ) {
          // 检查错误是否可重试
          const isRetryable = isErrorRetryable(error)
          
          if (isRetryable) {
            // 增加重试计数
            config._retryCount = (config._retryCount || 0) + 1
            config._isRetry = true
            
            // 计算重试延迟（指数退避策略）
            const delay = config._retryDelay || mergedConfig.retryDelay * Math.pow(2, config._retryCount - 1)
            config._retryDelay = delay * 2 // 下次重试延迟加倍
            
            // 显示重试消息
            if (mergedConfig.showErrorMessage && config._retryCount === 1) {
              showErrorMessage({
                message: `请求失败，正在尝试重新连接(${config._retryCount}/${mergedConfig.maxRetries})...`,
                type: 'info',
                duration: 2000
              }, 'message')
            }
            
            // 等待后重试
            await new Promise(resolve => setTimeout(resolve, delay))
            
            // 根据网络状态调整请求配置
            const adjustedConfig = networkMonitor.isOnline() 
              ? await networkMonitor.adjustRequestConfig(config)
              : config
            
            // 重试请求
            return axiosInstance(adjustedConfig)
          }
        }
        
        // 处理HTTP错误
        handleApiError(error, mergedConfig)
        return Promise.reject(error)
      }
    )
    
    // 监听网络状态变化
    if (mergedConfig.offlineQueueing) {
      window.addEventListener('online', () => {
        // 网络恢复时，处理离线请求队列
        processOfflineQueue(axiosInstance)
      })
    }
    
    return axiosInstance
  }
}

/**
 * 生成请求唯一标识
 * @param config 请求配置
 * @returns 请求唯一标识
 */
function generateRequestId(config: InternalAxiosRequestConfig): string {
  const { method, url, params, data } = config
  return `${method}-${url}-${JSON.stringify(params)}-${JSON.stringify(data)}`
}

/**
 * 处理离线请求队列
 * @param axiosInstance Axios实例
 */
function processOfflineQueue(axiosInstance: AxiosInstance): void {
  if (offlineRequestQueue.length === 0) return
  
  showErrorMessage({
    message: `网络已恢复，正在处理${offlineRequestQueue.length}个离线请求`,
    type: 'success',
    duration: 3000
  }, 'message')
  
  // 处理队列中的请求
  const queue = [...offlineRequestQueue]
  offlineRequestQueue.length = 0
  
  // 分批处理请求，避免一次性发送过多请求
  const batchSize = 3
  const processBatch = (startIndex: number) => {
    const batch = queue.slice(startIndex, startIndex + batchSize)
    if (batch.length === 0) return
    
    // 处理当前批次
    Promise.all(
      batch.map(({ config, resolve, reject }) => {
        return axiosInstance(config)
          .then(resolve)
          .catch(reject)
      })
    ).finally(() => {
      // 处理下一批次
      setTimeout(() => {
        processBatch(startIndex + batchSize)
      }, 300)
    })
  }
  
  // 开始处理第一批
  processBatch(0)
}

/**
 * 判断错误是否可重试
 * @param error 错误对象
 * @returns 是否可重试
 */
function isErrorRetryable(error: AxiosError): boolean {
  // 网络错误总是可重试的
  if (!error.response) {
    return true
  }
  
  // 检查状态码是否在可重试列表中
  const status = error.response.status
  const errorInfo = errorCodeMap[status]
  
  // 如果是GET请求，更容易重试
  const isGetRequest = error.config?.method?.toLowerCase() === 'get'
  
  // 如果是幂等请求（GET、HEAD、OPTIONS、PUT、DELETE），也可以考虑重试
  const isIdempotent = ['get', 'head', 'options', 'put', 'delete'].includes(
    error.config?.method?.toLowerCase() || ''
  )
  
  // 根据状态码和请求类型判断是否可重试
  if (errorInfo) {
    return errorInfo.retryable || (isGetRequest && status >= 500)
  }
  
  // 默认情况下，只有GET请求和幂等请求在网络错误时才重试
  return isIdempotent && (!error.response || error.code === 'ECONNABORTED')
}

/**
 * 显示错误消息
 * @param options 消息选项
 * @param type 消息类型
 */
function showErrorMessage(
  options: {
    message: string
    type?: 'success' | 'warning' | 'info' | 'error'
    duration?: number
    showClose?: boolean
    title?: string
  },
  messageType: 'message' | 'notification' | 'both' = 'message'
) {
  const { message, type = 'error', duration = 3000, showClose = true, title } = options
  
  if (messageType === 'message' || messageType === 'both') {
    ElMessage({
      message,
      type,
      duration,
      showClose
    })
  }
  
  if (messageType === 'notification' || messageType === 'both') {
    ElNotification({
      title: title || type === 'error' ? '错误' : type === 'warning' ? '警告' : '提示',
      message,
      type,
      duration,
      showClose
    })
  }
}

/**
 * 处理API错误
 * @param error 错误对象
 * @param config 错误处理配置
 */
function handleApiError(
  error: AxiosError | any,
  config: ErrorHandlerConfig
): void {
  // 获取错误信息
  let errorMessage = '未知网络错误'
  let errorCode = 0
  let errorSeverity = ErrorSeverity.ERROR
  let errorAction: (() => void) | undefined
  let errorDetails: Record<string, any> = {}
  let errorType = 'error'
  
  if (axios.isAxiosError(error)) {
    // Axios错误
    if (error.response) {
      // 服务器返回了错误响应
      errorCode = error.response.status
      
      // 从错误码映射表中获取错误信息
      const errorInfo = errorCodeMap[errorCode]
      if (errorInfo) {
        errorMessage = errorInfo.message
        errorSeverity = errorInfo.severity
        errorAction = errorInfo.action
        errorType = mapSeverityToMessageType(errorSeverity)
      } else {
        errorMessage = `请求失败 (${errorCode})`
      }
      
      // 如果响应中包含详细错误信息，优先使用
      const responseData = error.response.data
      if (responseData && typeof responseData === 'object') {
        if (responseData.message) {
          errorMessage = responseData.message
        } else if (responseData.error) {
          errorMessage = responseData.error
        } else if (responseData.msg) {
          errorMessage = responseData.msg
        }
        
        // 收集额外的错误详情
        errorDetails = { ...responseData }
      }
    } else if (error.request) {
      // 请求已发送但未收到响应
      if (error.code === 'ECONNABORTED') {
        errorMessage = '请求超时，请检查网络连接'
      } else if (!navigator.onLine) {
        errorMessage = '您当前处于离线状态，请检查网络连接'
      } else {
        errorMessage = '无法连接到服务器，请检查网络'
      }
      errorSeverity = ErrorSeverity.WARNING
      errorType = 'warning'
    } else {
      // 请求配置错误
      errorMessage = '请求配置错误: ' + error.message
    }
    
    // 收集请求相关信息
    const requestConfig = error.config
    if (requestConfig) {
      errorDetails = {
        ...errorDetails,
        url: requestConfig.url,
        method: requestConfig.method,
        params: requestConfig.params,
        isRetry: requestConfig._isRetry,
        retryCount: requestConfig._retryCount
      }
    }
  } else if (error.message) {
    // 非Axios错误但有错误消息
    errorMessage = error.message
    
    // 如果有错误码
    if (error.code !== undefined) {
      errorCode = error.code
    }
  }
  
  // 使用错误处理服务记录错误
  handleError(error, ErrorType.API, errorSeverity, {
    message: errorMessage,
    details: {
      code: errorCode,
      ...errorDetails,
      url: axios.isAxiosError(error) ? error.config?.url : undefined,
      method: axios.isAxiosError(error) ? error.config?.method : undefined,
      response: axios.isAxiosError(error) ? error.response?.data : undefined
    }
  })
  
  // 显示错误消息
  if (config.showErrorMessage) {
    // 如果是重试请求，不显示重复的错误消息
    const isRetry = axios.isAxiosError(error) && error.config?._isRetry
    
    if (!isRetry) {
      showErrorMessage({
        message: errorMessage,
        type: errorType as any,
        duration: 5000,
        title: `API错误 (${errorCode || '未知'})`
      }, config.messageType)
    }
  }
  
  // 更新全局错误状态
  if (config.updateGlobalError) {
    const appStore = useAppStore()
    appStore.setGlobalError(errorMessage)
    
    // 5秒后自动清除全局错误
    setTimeout(() => {
      appStore.setGlobalError(null)
    }, 5000)
  }
  
  // 执行特定错误的操作
  if (errorAction) {
    errorAction()
  }
  
  // 调用自定义错误处理函数
  if (config.customHandler && axios.isAxiosError(error)) {
    try {
      config.customHandler(error, config)
    } catch (handlerError) {
      console.error('自定义错误处理函数失败:', handlerError)
    }
  }
  
  // 记录详细错误信息到控制台
  if (config.logDetails) {
    console.group(`[API错误] ${errorMessage}`)
    console.error('错误详情:', error)
    console.error('请求配置:', axios.isAxiosError(error) ? error.config : '无')
    console.error('响应数据:', axios.isAxiosError(error) ? error.response?.data : '无')
    
    // 检查是否存在URL路径问题（重复的/api前缀）
    if (axios.isAxiosError(error) && error.config?.url) {
      const url = error.config.url;
      if (url.includes('/api/api/')) {
        console.warn('检测到URL路径问题：重复的/api前缀。请检查API配置。');
        console.warn('正确的URL应该是:', url.replace('/api/api/', '/api/'));
      }
    }
    
    console.groupEnd()
  }
  
  // 上报错误到服务器
  if (config.reportToServer) {
    reportErrorToServer(error, errorMessage, errorDetails)
  }
}

/**
 * 将错误严重程度映射到消息类型
 * @param severity 错误严重程度
 * @returns 消息类型
 */
function mapSeverityToMessageType(severity: ErrorSeverity): 'success' | 'warning' | 'info' | 'error' {
  switch (severity) {
    case ErrorSeverity.FATAL:
    case ErrorSeverity.ERROR:
      return 'error'
    case ErrorSeverity.WARNING:
      return 'warning'
    case ErrorSeverity.INFO:
      return 'info'
    default:
      return 'error'
  }
}

/**
 * 上报错误到服务器
 * @param error 错误对象
 * @param message 错误消息
 * @param details 错误详情
 */
function reportErrorToServer(error: any, message: string, details: any): void {
  try {
    // 使用Beacon API上报错误，不阻塞主线程
    if (navigator.sendBeacon) {
      const errorData = {
        message,
        details,
        url: window.location.href,
        timestamp: Date.now(),
        userAgent: navigator.userAgent,
        type: 'api_error'
      }
      
      const blob = new Blob([JSON.stringify(errorData)], { type: 'application/json' })
      navigator.sendBeacon('/api/error-report', blob)
    } else {
      // 降级处理，使用fetch API
      fetch('/api/error-report', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          message,
          details,
          url: window.location.href,
          timestamp: Date.now(),
          userAgent: navigator.userAgent,
          type: 'api_error'
        }),
        // 使用keepalive确保请求在页面卸载时仍能完成
        keepalive: true
      }).catch(() => {
        // 忽略错误
      })
    }
  } catch (e) {
    // 忽略上报过程中的错误
    console.error('上报错误失败:', e)
  }
}

/**
 * 创建默认的增强API错误处理拦截器
 * @param showErrorMessage 是否显示错误消息
 * @param updateGlobalError 是否更新全局错误状态
 * @returns 增强的API错误处理拦截器
 */
export default function createDefaultEnhancedApiErrorHandler(
  showErrorMessage: boolean = true,
  updateGlobalError: boolean = false
) {
  return createEnhancedApiErrorHandler({
    showErrorMessage,
    messageType: 'message',
    updateGlobalError,
    enableRetry: true,
    maxRetries: 3,
    retryDelay: 1000,
    offlineQueueing: true,
    logDetails: true,
    reportToServer: import.meta.env.PROD
  })
}