/**
 * 全局错误处理服务
 * 用于捕获和处理应用中的各种错误
 */
import type { App } from 'vue'
import { useAppStore } from '@/store'
import { ElNotification } from 'element-plus'

/**
 * 错误类型枚举
 */
export enum ErrorType {
  // Vue 错误
  VUE = 'vue',
  // JavaScript 运行时错误
  RUNTIME = 'runtime',
  // Promise 未处理的拒绝
  PROMISE = 'promise',
  // 资源加载错误
  RESOURCE = 'resource',
  // API 请求错误
  API = 'api',
  // 未知错误
  UNKNOWN = 'unknown'
}

/**
 * 错误严重程度枚举
 */
export enum ErrorSeverity {
  // 致命错误，应用无法继续运行
  FATAL = 'fatal',
  // 严重错误，功能无法使用
  ERROR = 'error',
  // 警告，功能可能受影响
  WARNING = 'warning',
  // 信息，不影响功能
  INFO = 'info'
}

/**
 * 错误信息接口
 */
export interface ErrorInfo {
  // 错误类型
  type: ErrorType
  // 错误严重程度
  severity: ErrorSeverity
  // 错误消息
  message: string
  // 错误详情
  details?: any
  // 错误发生时间
  timestamp: number
  // 错误发生位置
  location?: string
  // 用户操作信息
  userAction?: string
  // 应用状态信息
  appState?: any
}

/**
 * 错误处理配置接口
 */
export interface ErrorHandlerConfig {
  // 是否显示错误通知
  showNotification: boolean
  // 是否记录到控制台
  logToConsole: boolean
  // 是否上报到服务器
  reportToServer: boolean
  // 是否影响全局状态
  affectGlobalState: boolean
  // 自定义错误处理函数
  customHandler?: (error: ErrorInfo) => void
}

// 检测是否为生产环境
const isProduction = (): boolean => {
  // 在浏览器环境中检测是否为生产环境
  // 可以通过多种方式检测，这里使用一个简单的方法
  return window.location.hostname !== 'localhost' &&
    !window.location.hostname.startsWith('127.0.0.1') &&
    !window.location.hostname.startsWith('192.168.') &&
    window.location.protocol === 'https:';
}

// 默认错误处理配置
const defaultConfig: ErrorHandlerConfig = {
  showNotification: true,
  logToConsole: true,
  reportToServer: isProduction(),
  affectGlobalState: true
}

// 当前配置
let currentConfig: ErrorHandlerConfig = { ...defaultConfig }

// 错误历史记录
const errorHistory: ErrorInfo[] = []

// 最大错误历史记录数
const MAX_ERROR_HISTORY = 50

/**
 * 格式化错误信息
 * @param error 错误对象
 * @param type 错误类型
 * @param severity 错误严重程度
 * @returns 格式化后的错误信息
 */
function formatError(
  error: any,
  type: ErrorType = ErrorType.UNKNOWN,
  severity: ErrorSeverity = ErrorSeverity.ERROR
): ErrorInfo {
  // 提取错误消息
  let message = '未知错误'
  let details = undefined
  let location = undefined

  if (error instanceof Error) {
    message = error.message
    details = {
      name: error.name,
      stack: error.stack
    }

    // 尝试从堆栈中提取位置信息
    if (error.stack) {
      const stackLines = error.stack.split('\n')
      if (stackLines.length > 1) {
        location = stackLines[1].trim()
      }
    }
  } else if (typeof error === 'string') {
    message = error
  } else if (error && typeof error === 'object') {
    message = error.message || '对象错误'
    details = error
  }

  return {
    type,
    severity,
    message,
    details,
    location,
    timestamp: Date.now()
  }
}

/**
 * 处理错误
 * @param error 错误对象
 * @param type 错误类型
 * @param severity 错误严重程度
 * @param additionalInfo 额外信息
 */
export function handleError(
  error: any,
  type: ErrorType = ErrorType.UNKNOWN,
  severity: ErrorSeverity = ErrorSeverity.ERROR,
  additionalInfo: Partial<ErrorInfo> = {}
): void {
  // 格式化错误信息
  const errorInfo: ErrorInfo = {
    ...formatError(error, type, severity),
    ...additionalInfo
  }

  // 添加到错误历史
  addToErrorHistory(errorInfo)

  // 根据配置处理错误
  if (currentConfig.logToConsole) {
    logErrorToConsole(errorInfo)
  }

  if (currentConfig.showNotification) {
    showErrorNotification(errorInfo)
  }

  if (currentConfig.reportToServer) {
    reportErrorToServer(errorInfo)
  }

  if (currentConfig.affectGlobalState) {
    updateGlobalErrorState(errorInfo)
  }

  // 调用自定义处理函数
  if (currentConfig.customHandler) {
    try {
      currentConfig.customHandler(errorInfo)
    } catch (handlerError) {
      console.error('自定义错误处理函数失败:', handlerError)
    }
  }
}

/**
 * 添加错误到历史记录
 * @param errorInfo 错误信息
 */
function addToErrorHistory(errorInfo: ErrorInfo): void {
  errorHistory.unshift(errorInfo)

  // 限制历史记录大小
  if (errorHistory.length > MAX_ERROR_HISTORY) {
    errorHistory.pop()
  }
}

/**
 * 记录错误到控制台
 * @param errorInfo 错误信息
 */
function logErrorToConsole(errorInfo: ErrorInfo): void {
  const { type, severity, message, details } = errorInfo

  console.group(`[${severity.toUpperCase()}] ${type}: ${message}`)
  console.error(message)
  if (details) {
    console.error('详情:', details)
  }
  console.error('信息:', errorInfo)
  console.groupEnd()
}

/**
 * 显示错误通知
 * @param errorInfo 错误信息
 */
function showErrorNotification(errorInfo: ErrorInfo): void {
  // 使用 Element Plus 的通知组件
  const { severity, message } = errorInfo

  try {
    ElNotification({
      title: getSeverityTitle(severity),
      message,
      type: mapSeverityToType(severity),
      duration: severity === ErrorSeverity.FATAL ? 0 : 5000
    })
  } catch (e) {
    // 如果通知组件失败，使用 alert 作为备选
    if (severity === ErrorSeverity.FATAL || severity === ErrorSeverity.ERROR) {
      alert(`${getSeverityTitle(severity)}: ${message}`)
    }
    console.error('显示错误通知失败:', e)
  }
}

/**
 * 获取严重程度对应的标题
 * @param severity 错误严重程度
 * @returns 标题
 */
function getSeverityTitle(severity: ErrorSeverity): string {
  switch (severity) {
    case ErrorSeverity.FATAL:
      return '致命错误'
    case ErrorSeverity.ERROR:
      return '错误'
    case ErrorSeverity.WARNING:
      return '警告'
    case ErrorSeverity.INFO:
      return '提示'
    default:
      return '错误'
  }
}

/**
 * 将严重程度映射到通知类型
 * @param severity 错误严重程度
 * @returns 通知类型
 */
function mapSeverityToType(severity: ErrorSeverity): 'success' | 'warning' | 'info' | 'error' {
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
 * @param errorInfo 错误信息
 */
function reportErrorToServer(errorInfo: ErrorInfo): void {
  // 这里可以实现上报错误到服务器的逻辑
  // 例如使用 Beacon API 或 AJAX 请求

  // 示例实现
  try {
    if (navigator.sendBeacon) {
      const blob = new Blob([JSON.stringify(errorInfo)], { type: 'application/json' })
      navigator.sendBeacon('/api/error-report', blob)
    } else {
      // 降级处理
      fetch('/api/error-report', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(errorInfo),
        // 使用 keepalive 确保请求在页面卸载时仍能完成
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
 * 更新全局错误状态
 * @param errorInfo 错误信息
 */
function updateGlobalErrorState(errorInfo: ErrorInfo): void {
  try {
    const appStore = useAppStore()

    // 只有严重错误才更新全局状态
    if (errorInfo.severity === ErrorSeverity.FATAL || errorInfo.severity === ErrorSeverity.ERROR) {
      appStore.setGlobalError(errorInfo.message)
    }
  } catch (e) {
    console.error('更新全局错误状态失败:', e)
  }
}

/**
 * 配置错误处理器
 * @param config 配置
 */
export function configureErrorHandler(config: Partial<ErrorHandlerConfig>): void {
  currentConfig = {
    ...currentConfig,
    ...config
  }
}

/**
 * 获取错误历史记录
 * @returns 错误历史记录
 */
export function getErrorHistory(): ReadonlyArray<ErrorInfo> {
  return [...errorHistory]
}

/**
 * 清除错误历史记录
 */
export function clearErrorHistory(): void {
  errorHistory.length = 0
}

/**
 * 安装全局错误处理器
 * @param app Vue 应用实例
 */
export function installGlobalErrorHandler(app: App): void {
  // 设置 Vue 错误处理器
  app.config.errorHandler = (error, instance, info) => {
    handleError(error, ErrorType.VUE, ErrorSeverity.ERROR, {
      details: { componentInfo: info, componentInstance: instance }
    })

    // 确保错误显示在控制台
    console.error(error)
  }

  // 捕获未处理的 Promise 拒绝
  window.addEventListener('unhandledrejection', (event) => {
    handleError(event.reason, ErrorType.PROMISE, ErrorSeverity.ERROR, {
      details: { event }
    })
  })

  // 捕获全局错误
  window.addEventListener('error', (event) => {
    // 区分资源加载错误和运行时错误
    const isResourceError = event.target && (
      event.target instanceof HTMLScriptElement ||
      event.target instanceof HTMLLinkElement ||
      event.target instanceof HTMLImageElement
    )

    if (isResourceError) {
      handleError(
        `资源加载失败: ${(event.target as HTMLElement).outerHTML}`,
        ErrorType.RESOURCE,
        ErrorSeverity.WARNING,
        {
          details: { event, target: event.target }
        }
      )
    } else {
      handleError(event.error || event.message, ErrorType.RUNTIME, ErrorSeverity.ERROR, {
        details: { event },
        location: `${event.filename}:${event.lineno}:${event.colno}`
      })
    }

    // 防止默认处理
    event.preventDefault()
  }, true)
}

/**
 * 创建错误
 * @param message 错误消息
 * @param options 错误选项
 * @returns 错误对象
 */
export function createError(message: string, options?: ErrorOptions): Error {
  return new Error(message, options)
}

/**
 * 包装函数，捕获并处理其中的错误
 * @param fn 要包装的函数
 * @param errorType 错误类型
 * @param severity 错误严重程度
 * @returns 包装后的函数
 */
export function withErrorHandling<T extends (...args: any[]) => any>(
  fn: T,
  errorType: ErrorType = ErrorType.UNKNOWN,
  severity: ErrorSeverity = ErrorSeverity.ERROR
): (...args: Parameters<T>) => ReturnType<T> | undefined {
  return (...args: Parameters<T>): ReturnType<T> | undefined => {
    try {
      return fn(...args)
    } catch (error) {
      handleError(error, errorType, severity, {
        userAction: `调用函数: ${fn.name || '匿名函数'}`
      })
      return undefined
    }
  }
}

/**
 * 包装异步函数，捕获并处理其中的错误
 * @param fn 要包装的异步函数
 * @param errorType 错误类型
 * @param severity 错误严重程度
 * @returns 包装后的异步函数
 */
export function withAsyncErrorHandling<T extends (...args: any[]) => Promise<any>>(
  fn: T,
  errorType: ErrorType = ErrorType.UNKNOWN,
  severity: ErrorSeverity = ErrorSeverity.ERROR
): (...args: Parameters<T>) => Promise<Awaited<ReturnType<T>> | undefined> {
  return async (...args: Parameters<T>): Promise<Awaited<ReturnType<T>> | undefined> => {
    try {
      return await fn(...args)
    } catch (error) {
      handleError(error, errorType, severity, {
        userAction: `调用异步函数: ${fn.name || '匿名函数'}`
      })
      return undefined
    }
  }
}

// 导出默认对象
export default {
  handleError,
  configureErrorHandler,
  getErrorHistory,
  clearErrorHistory,
  installGlobalErrorHandler,
  createError,
  withErrorHandling,
  withAsyncErrorHandling,
  ErrorType,
  ErrorSeverity
}