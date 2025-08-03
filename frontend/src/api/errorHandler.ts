/**
 * API错误处理拦截器
 * 用于统一处理API请求错误
 * 
 * 更新：
 * - 使用更现代的错误处理方式
 * - 增强错误重试机制
 * - 改进错误分类和处理逻辑
 * - 支持离线错误处理
 */
import axios from 'axios'
import type { AxiosInstance, AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useAppStore } from '@/store'
import { handleError, ErrorType, ErrorSeverity } from '@/services/errorHandler'
import { useAuthStore } from '@/store/modules/auth'
import router from '@/router'

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
  showErrorMessage: boolean
  updateGlobalError: boolean
  enableRetry: boolean
  maxRetries: number
  retryDelay: number
  offlineQueueing: boolean
}

/**
 * 默认错误处理配置
 */
const defaultConfig: ErrorHandlerConfig = {
  showErrorMessage: true,
  updateGlobalError: false,
  enableRetry: true,
  maxRetries: 3,
  retryDelay: 1000,
  offlineQueueing: true
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
 * 创建API错误处理拦截器
 * @param config 错误处理配置
 * @returns API错误处理拦截器
 */
export function createApiErrorHandler(
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
              ElMessage({
                message: '您当前处于离线状态，请求将在恢复连接后发送',
                type: 'warning',
                duration: 3000
              })
            }
          })
        }
        
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
          _isRetry?: boolean
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
            const delay = mergedConfig.retryDelay * Math.pow(2, config._retryCount - 1)
            
            // 等待后重试
            await new Promise(resolve => setTimeout(resolve, delay))
            
            // 重试请求
            return axiosInstance(config)
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
 * 处理离线请求队列
 * @param axiosInstance Axios实例
 */
function processOfflineQueue(axiosInstance: AxiosInstance): void {
  if (offlineRequestQueue.length === 0) return
  
  ElMessage({
    message: '网络已恢复，正在处理离线请求',
    type: 'success',
    duration: 3000
  })
  
  // 处理队列中的请求
  const queue = [...offlineRequestQueue]
  offlineRequestQueue.length = 0
  
  queue.forEach(({ config, resolve, reject }) => {
    axiosInstance(config)
      .then(resolve)
      .catch(reject)
  })
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
  
  return errorInfo ? errorInfo.retryable : false
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
      ElMessage({
        message: errorMessage,
        type: mapSeverityToMessageType(errorSeverity),
        duration: 5000
      })
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
 * 创建默认的API错误处理拦截器
 */
export default function createDefaultApiErrorHandler() {
  return createApiErrorHandler({
    showErrorMessage: true,
    updateGlobalError: false,
    enableRetry: true,
    maxRetries: 3,
    retryDelay: 1000,
    offlineQueueing: true
  })
}