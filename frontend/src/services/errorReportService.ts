/**
 * 错误报告服务
 * 提供错误信息收集、上报和用户反馈功能
 */
import axios from 'axios'
import { ref, reactive } from 'vue'
import { ErrorType, ErrorSeverity } from '@/services/errorHandler'

// 重新导出错误类型和严重程度枚举，以便其他组件可以直接从这里导入
export { ErrorType, ErrorSeverity } from '@/services/errorHandler'

/**
 * 错误报告接口
 */
export interface ErrorReport {
  // 错误ID
  id?: string
  // 错误类型
  type: ErrorType
  // 错误严重程度
  severity: ErrorSeverity
  // 错误消息
  message: string
  // 错误详情
  details?: any
  // 错误位置
  location?: string
  // 错误堆栈
  stack?: string
  // 错误时间戳
  timestamp: number
  // 用户反馈
  feedback?: ErrorFeedback
  // 技术信息
  techInfo?: TechInfo
  // 会话ID
  sessionId?: string
  // 用户ID
  userId?: string
}

/**
 * 用户反馈接口
 */
export interface ErrorFeedback {
  // 反馈类型
  type: 'functional' | 'ui' | 'performance' | 'other'
  // 严重程度（1-5）
  severity: number
  // 问题描述
  description: string
  // 重现步骤
  steps?: string
  // 联系方式
  contact?: string
  // 是否包含技术信息
  includeTechInfo: boolean
  // 是否包含错误详情
  includeErrorDetails: boolean
  // 反馈时间戳
  timestamp: number
}

/**
 * 技术信息接口
 */
export interface TechInfo {
  // 浏览器信息
  browser: string
  // 平台信息
  platform: string
  // 语言信息
  language: string
  // 屏幕尺寸
  screenSize: string
  // 视口尺寸
  viewportSize: string
  // 时间戳
  timestamp: string
  // URL
  url: string
  // 来源
  referrer: string
  // 应用版本
  appVersion?: string
}

/**
 * 错误报告配置接口
 */
export interface ErrorReportConfig {
  // 是否启用错误报告
  enabled: boolean
  // 是否自动收集错误
  autoCollect: boolean
  // 是否上报到服务器
  reportToServer: boolean
  // 上报URL
  reportUrl: string
  // 上报批次大小
  batchSize: number
  // 上报间隔（毫秒）
  reportInterval: number
  // 最大缓存错误数
  maxCachedErrors: number
  // 是否收集用户反馈
  collectFeedback: boolean
  // 是否包含技术信息
  includeTechInfo: boolean
  // 采样率（0-1）
  samplingRate: number
}

/**
 * 默认错误报告配置
 */
const defaultConfig: ErrorReportConfig = {
  enabled: true,
  autoCollect: true,
  reportToServer: true,
  reportUrl: '/api/error-report',
  batchSize: 10,
  reportInterval: 60000, // 1分钟
  maxCachedErrors: 100,
  collectFeedback: true,
  includeTechInfo: true,
  samplingRate: 1.0 // 100%采样
}

// 错误报告缓存
const errorCache = ref<ErrorReport[]>([])

// 错误报告配置
const config = reactive<ErrorReportConfig>({...defaultConfig})

// 上报定时器
let reportTimer: number | null = null

// 会话ID
const sessionId = generateSessionId()

/**
 * 生成会话ID
 * @returns 会话ID
 */
function generateSessionId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 15)}`
}

/**
 * 收集错误信息
 * @param error 错误对象
 * @param type 错误类型
 * @param severity 错误严重程度
 * @param additionalInfo 附加信息
 * @returns 错误报告
 */
function collectErrorInfo(
  error: any,
  type: ErrorType,
  severity: ErrorSeverity,
  additionalInfo?: {
    message?: string
    details?: any
    location?: string
  }
): ErrorReport {
  // 如果禁用了错误报告，直接返回
  if (!config.enabled) {
    return {
      type,
      severity,
      message: '',
      timestamp: Date.now()
    }
  }
  
  // 如果启用了采样，根据采样率决定是否收集
  if (Math.random() > config.samplingRate) {
    return {
      type,
      severity,
      message: '',
      timestamp: Date.now()
    }
  }
  
  // 提取错误信息
  let errorMessage = additionalInfo?.message || ''
  let errorStack = ''
  let errorDetails = additionalInfo?.details || {}
  let errorLocation = additionalInfo?.location || window.location.href
  
  if (error) {
    // 如果是Error对象
    if (error instanceof Error) {
      errorMessage = errorMessage || error.message
      errorStack = error.stack || ''
    }
    // 如果是字符串
    else if (typeof error === 'string') {
      errorMessage = errorMessage || error
    }
    // 如果是对象
    else if (typeof error === 'object') {
      errorMessage = errorMessage || error.message || JSON.stringify(error)
      errorStack = error.stack || ''
      errorDetails = { ...errorDetails, ...error }
    }
  }
  
  // 创建错误报告
  const report: ErrorReport = {
    type,
    severity,
    message: errorMessage,
    details: errorDetails,
    location: errorLocation,
    stack: errorStack,
    timestamp: Date.now(),
    sessionId
  }
  
  // 添加技术信息
  if (config.includeTechInfo) {
    report.techInfo = {
      browser: navigator.userAgent,
      platform: navigator.platform,
      language: navigator.language,
      screenSize: `${window.screen.width}x${window.screen.height}`,
      viewportSize: `${window.innerWidth}x${window.innerHeight}`,
      timestamp: new Date().toISOString(),
      url: window.location.href,
      referrer: document.referrer,
      appVersion: import.meta.env.VITE_APP_VERSION || '1.0.0'
    }
  }
  
  return report
}

/**
 * 添加错误报告到缓存
 * @param report 错误报告
 */
function addErrorToCache(report: ErrorReport): void {
  // 添加到缓存
  errorCache.value.push(report)
  
  // 如果超过最大缓存数，移除最旧的
  if (errorCache.value.length > config.maxCachedErrors) {
    errorCache.value.shift()
  }
}

/**
 * 上报错误到服务器
 * @param reports 错误报告列表
 * @returns 是否上报成功
 */
async function reportErrorsToServer(reports: ErrorReport[]): Promise<boolean> {
  if (!config.reportToServer || reports.length === 0) {
    return false
  }
  
  try {
    // 使用Beacon API上报错误，不阻塞主线程
    if (navigator.sendBeacon) {
      const blob = new Blob([JSON.stringify(reports)], { type: 'application/json' })
      return navigator.sendBeacon(config.reportUrl, blob)
    } else {
      // 降级处理，使用fetch API
      await fetch(config.reportUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(reports),
        // 使用keepalive确保请求在页面卸载时仍能完成
        keepalive: true
      })
      return true
    }
  } catch (e) {
    console.error('上报错误失败:', e)
    return false
  }
}

/**
 * 批量上报缓存中的错误
 */
async function batchReportErrors(): Promise<void> {
  if (errorCache.value.length === 0) {
    return
  }
  
  // 分批上报
  const batches = []
  for (let i = 0; i < errorCache.value.length; i += config.batchSize) {
    batches.push(errorCache.value.slice(i, i + config.batchSize))
  }
  
  // 上报成功的报告
  const successReports: ErrorReport[] = []
  
  // 逐批上报
  for (const batch of batches) {
    const success = await reportErrorsToServer(batch)
    if (success) {
      successReports.push(...batch)
    }
  }
  
  // 从缓存中移除已上报的错误
  if (successReports.length > 0) {
    errorCache.value = errorCache.value.filter(report => !successReports.includes(report))
  }
}

/**
 * 启动定时上报
 */
function startReportTimer(): void {
  if (reportTimer !== null) {
    return
  }
  
  reportTimer = window.setInterval(() => {
    batchReportErrors()
  }, config.reportInterval)
}

/**
 * 停止定时上报
 */
function stopReportTimer(): void {
  if (reportTimer !== null) {
    window.clearInterval(reportTimer)
    reportTimer = null
  }
}

/**
 * 添加用户反馈到错误报告
 * @param errorId 错误ID
 * @param feedback 用户反馈
 * @returns 是否添加成功
 */
function addFeedbackToError(errorId: string, feedback: ErrorFeedback): boolean {
  // 查找错误报告
  const report = errorCache.value.find(r => r.id === errorId)
  if (!report) {
    return false
  }
  
  // 添加用户反馈
  report.feedback = feedback
  
  // 立即上报
  reportErrorsToServer([report])
  
  return true
}

/**
 * 错误报告服务
 */
export const errorReportService = {
  /**
   * 初始化错误报告服务
   * @param customConfig 自定义配置
   */
  init(customConfig?: Partial<ErrorReportConfig>): void {
    // 合并配置
    Object.assign(config, customConfig || {})
    
    // 如果启用了错误报告，启动定时上报
    if (config.enabled && config.reportToServer) {
      startReportTimer()
      
      // 页面卸载时上报
      window.addEventListener('beforeunload', () => {
        batchReportErrors()
      })
    }
  },
  
  /**
   * 报告错误
   * @param error 错误对象
   * @param type 错误类型
   * @param severity 错误严重程度
   * @param additionalInfo 附加信息
   * @returns 错误报告
   */
  reportError(
    error: any,
    type: ErrorType = ErrorType.UNKNOWN,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    additionalInfo?: {
      message?: string
      details?: any
      location?: string
    }
  ): ErrorReport {
    // 收集错误信息
    const report = collectErrorInfo(error, type, severity, additionalInfo)
    
    // 添加到缓存
    addErrorToCache(report)
    
    // 如果错误严重程度高，立即上报
    if (severity === ErrorSeverity.FATAL || severity === ErrorSeverity.ERROR) {
      reportErrorsToServer([report])
    }
    
    return report
  },
  
  /**
   * 添加用户反馈
   * @param errorId 错误ID
   * @param feedback 用户反馈
   * @returns 是否添加成功
   */
  addFeedback(errorId: string, feedback: ErrorFeedback): boolean {
    return addFeedbackToError(errorId, feedback)
  },
  
  /**
   * 手动上报缓存中的错误
   */
  async flushErrors(): Promise<void> {
    await batchReportErrors()
  },
  
  /**
   * 获取错误缓存
   * @returns 错误缓存
   */
  getErrorCache(): ErrorReport[] {
    return [...errorCache.value]
  },
  
  /**
   * 清空错误缓存
   */
  clearErrorCache(): void {
    errorCache.value = []
  },
  
  /**
   * 更新配置
   * @param newConfig 新配置
   */
  updateConfig(newConfig: Partial<ErrorReportConfig>): void {
    // 更新配置
    Object.assign(config, newConfig)
    
    // 如果启用了错误报告，启动定时上报
    if (config.enabled && config.reportToServer) {
      startReportTimer()
    } else {
      stopReportTimer()
    }
  },
  
  /**
   * 获取当前配置
   * @returns 当前配置
   */
  getConfig(): ErrorReportConfig {
    return { ...config }
  }
}