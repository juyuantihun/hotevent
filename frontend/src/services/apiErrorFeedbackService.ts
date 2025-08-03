/**
 * API错误反馈服务
 * 提供统一的API错误处理和用户反馈机制
 */
import { ref, reactive } from 'vue'
import type { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { ErrorSeverity } from '@/services/errorHandler'

// 错误反馈状态
export interface ErrorFeedbackState {
  visible: boolean
  title: string
  message: string
  details: any
  severity: 'error' | 'warning' | 'info'
  retryable: boolean
  retryFn: (() => Promise<any>) | null
  timestamp: number
}

// 创建全局错误反馈状态
const errorFeedback = reactive<ErrorFeedbackState>({
  visible: false,
  title: '',
  message: '',
  details: null,
  severity: 'error',
  retryable: false,
  retryFn: null,
  timestamp: 0
})

// 重试状态
export interface RetryState {
  active: boolean
  count: number
  max: number
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
}

// 创建全局重试状态
const retryStatus = reactive<RetryState>({
  active: false,
  count: 0,
  max: 3,
  message: '',
  type: 'info'
})

/**
 * API错误反馈服务
 */
export const apiErrorFeedbackService = {
  /**
   * 显示错误反馈
   * @param title 错误标题
   * @param message 错误消息
   * @param error 错误对象
   * @param severity 错误严重程度
   * @param retryable 是否可重试
   * @param retryFn 重试函数
   */
  showError(
    title: string,
    message: string,
    error: any,
    severity: 'error' | 'warning' | 'info' = 'error',
    retryable: boolean = false,
    retryFn: (() => Promise<any>) | null = null
  ): void {
    // 提取错误详情
    let details = null
    
    if (error) {
      if (error.response) {
        details = {
          status: error.response.status,
          statusText: error.response.statusText,
          data: error.response.data,
          headers: error.response.headers,
          config: {
            url: error.config?.url,
            method: error.config?.method,
            timeout: error.config?.timeout
          }
        }
      } else {
        details = {
          message: error.message,
          name: error.name,
          code: error.code,
          stack: error.stack
        }
      }
    }
    
    // 更新错误反馈状态
    errorFeedback.title = title
    errorFeedback.message = message
    errorFeedback.details = details
    errorFeedback.severity = severity
    errorFeedback.retryable = retryable
    errorFeedback.retryFn = retryFn
    errorFeedback.visible = true
    errorFeedback.timestamp = Date.now()
  },
  
  /**
   * 隐藏错误反馈
   */
  hideError(): void {
    errorFeedback.visible = false
  },
  
  /**
   * 重试请求
   * @returns 重试结果
   */
  async retry(): Promise<any> {
    if (errorFeedback.retryFn) {
      try {
        const response = await errorFeedback.retryFn()
        errorFeedback.visible = false
        return response
      } catch (error: any) {
        this.showError(
          '重试失败',
          error.message || '请求重试失败',
          error,
          'error',
          false
        )
        throw error
      }
    }
    return null
  },
  
  /**
   * 开始重试进度
   * @param max 最大重试次数
   */
  startRetryProgress(max: number = 3): void {
    retryStatus.active = true
    retryStatus.count = 0
    retryStatus.max = max
    retryStatus.message = '准备重试请求...'
    retryStatus.type = 'info'
  },
  
  /**
   * 更新重试进度
   * @param count 当前重试次数
   * @param message 重试消息
   */
  updateRetryProgress(count: number, message?: string): void {
    retryStatus.count = count
    if (message) {
      retryStatus.message = message
    } else {
      retryStatus.message = `第${count}次重试中...`
    }
  },
  
  /**
   * 完成重试进度
   * @param success 是否成功
   * @param message 完成消息
   */
  completeRetryProgress(success: boolean, message?: string): void {
    retryStatus.type = success ? 'success' : 'error'
    retryStatus.message = message || (success ? '重试成功' : '重试失败')
    
    // 3秒后隐藏重试状态
    setTimeout(() => {
      retryStatus.active = false
    }, 3000)
  },
  
  /**
   * 获取错误反馈状态
   * @returns 错误反馈状态
   */
  getErrorFeedback(): ErrorFeedbackState {
    return errorFeedback
  },
  
  /**
   * 获取重试状态
   * @returns 重试状态
   */
  getRetryStatus(): RetryState {
    return retryStatus
  },
  
  /**
   * 从Axios错误创建用户友好的错误消息
   * @param error Axios错误
   * @returns 用户友好的错误消息
   */
  createUserFriendlyMessage(error: AxiosError): string {
    if (!error.response) {
      // 网络错误
      if (error.code === 'ECONNABORTED') {
        return '请求超时，请检查您的网络连接并重试'
      } else if (!navigator.onLine) {
        return '您当前处于离线状态，请检查网络连接'
      } else {
        return '无法连接到服务器，请稍后重试'
      }
    } else {
      // HTTP错误
      const status = error.response.status
      
      switch (status) {
        case 400:
          return '请求参数有误，请检查输入并重试'
        case 401:
          return '您的登录已过期，请重新登录'
        case 403:
          return '您没有权限执行此操作'
        case 404:
          return '请求的资源不存在'
        case 408:
          return '请求超时，请稍后重试'
        case 429:
          return '请求过于频繁，请稍后再试'
        case 500:
          return '服务器内部错误，请稍后重试'
        case 502:
          return '网关错误，请稍后重试'
        case 503:
          return '服务暂时不可用，请稍后重试'
        case 504:
          return '网关超时，请稍后重试'
        default:
          // 尝试从响应中获取错误消息
          const data = error.response.data
          if (data && typeof data === 'object') {
            if (data.message) {
              return data.message
            } else if (data.error) {
              return data.error
            } else if (data.msg) {
              return data.msg
            }
          }
          return `请求失败 (${status})`
      }
    }
  },
  
  /**
   * 从错误对象创建错误详情
   * @param error 错误对象
   * @returns 错误详情
   */
  createErrorDetails(error: any): any {
    if (!error) return null
    
    // Axios错误
    if (error.isAxiosError) {
      return {
        request: {
          url: error.config?.url,
          method: error.config?.method,
          baseURL: error.config?.baseURL,
          timeout: error.config?.timeout,
          headers: error.config?.headers,
          params: error.config?.params,
          data: error.config?.data
        },
        response: error.response ? {
          status: error.response.status,
          statusText: error.response.statusText,
          headers: error.response.headers,
          data: error.response.data
        } : null,
        message: error.message,
        code: error.code
      }
    }
    
    // 普通错误
    return {
      message: error.message,
      name: error.name,
      stack: error.stack,
      code: error.code
    }
  },
  
  /**
   * 映射错误严重程度
   * @param severity 错误严重程度
   * @returns 消息类型
   */
  mapSeverityToMessageType(severity: ErrorSeverity): 'success' | 'warning' | 'info' | 'error' {
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
}

// 导出错误反馈状态和重试状态
export { errorFeedback, retryStatus }