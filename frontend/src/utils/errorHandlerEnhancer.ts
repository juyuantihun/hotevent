/**
 * 错误处理和用户反馈增强工具
 * 提供更好的错误处理、用户反馈和恢复机制
 */

import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'

// 错误类型枚举
export enum ErrorType {
  NETWORK = 'network',
  VALIDATION = 'validation',
  BUSINESS = 'business',
  SYSTEM = 'system',
  TIMEOUT = 'timeout',
  PERMISSION = 'permission',
  DUPLICATE = 'duplicate'
}

// 错误级别枚举
export enum ErrorLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  CRITICAL = 'critical'
}

// 错误信息接口
interface ErrorInfo {
  type: ErrorType
  level: ErrorLevel
  message: string
  originalError?: any
  context?: any
  timestamp: number
  userAction?: string
  recoveryActions?: RecoveryAction[]
}

// 恢复操作接口
interface RecoveryAction {
  label: string
  action: () => Promise<void> | void
  type?: 'primary' | 'success' | 'warning' | 'danger'
  icon?: string
}

// 用户反馈选项接口
interface FeedbackOptions {
  showNotification?: boolean
  showMessage?: boolean
  showDialog?: boolean
  autoClose?: boolean
  duration?: number
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left'
}

// 错误统计接口
interface ErrorStats {
  totalErrors: number
  errorsByType: Map<ErrorType, number>
  errorsByLevel: Map<ErrorLevel, number>
  recentErrors: ErrorInfo[]
  recoverySuccessRate: number
}

class ErrorHandlerEnhancer {
  private errorHistory: ErrorInfo[] = []
  private errorStats: ErrorStats = {
    totalErrors: 0,
    errorsByType: new Map(),
    errorsByLevel: new Map(),
    recentErrors: [],
    recoverySuccessRate: 0
  }
  private recoveryAttempts: Map<string, number> = new Map()
  private readonly MAX_HISTORY_SIZE = 100
  private readonly MAX_RECENT_ERRORS = 10

  /**
   * 处理错误
   * @param error 错误对象或错误信息
   * @param context 错误上下文
   * @param options 反馈选项
   * @returns 错误信息对象
   */
  handleError(
    error: any,
    context?: any,
    options: FeedbackOptions = {}
  ): ErrorInfo {
    const errorInfo = this.parseError(error, context)
    
    // 记录错误
    this.recordError(errorInfo)
    
    // 显示用户反馈
    this.showUserFeedback(errorInfo, options)
    
    // 自动恢复尝试
    this.attemptAutoRecovery(errorInfo)
    
    return errorInfo
  }

  /**
   * 解析错误
   * @param error 错误对象
   * @param context 上下文
   * @returns 错误信息
   */
  private parseError(error: any, context?: any): ErrorInfo {
    let type = ErrorType.SYSTEM
    let level = ErrorLevel.MEDIUM
    let message = '发生未知错误'
    let recoveryActions: RecoveryAction[] = []

    if (typeof error === 'string') {
      message = error
    } else if (error instanceof Error) {
      message = error.message
      
      // 根据错误消息判断类型
      if (error.message.includes('网络') || error.message.includes('Network')) {
        type = ErrorType.NETWORK
        level = ErrorLevel.HIGH
        recoveryActions = this.getNetworkRecoveryActions()
      } else if (error.message.includes('超时') || error.message.includes('timeout')) {
        type = ErrorType.TIMEOUT
        level = ErrorLevel.MEDIUM
        recoveryActions = this.getTimeoutRecoveryActions()
      } else if (error.message.includes('权限') || error.message.includes('Permission')) {
        type = ErrorType.PERMISSION
        level = ErrorLevel.HIGH
        recoveryActions = this.getPermissionRecoveryActions()
      } else if (error.message.includes('重复') || error.message.includes('duplicate')) {
        type = ErrorType.DUPLICATE
        level = ErrorLevel.LOW
        recoveryActions = this.getDuplicateRecoveryActions()
      }
    } else if (error?.response) {
      // HTTP 错误
      const status = error.response.status
      message = this.getHttpErrorMessage(status)
      
      if (status >= 500) {
        type = ErrorType.SYSTEM
        level = ErrorLevel.HIGH
      } else if (status === 401 || status === 403) {
        type = ErrorType.PERMISSION
        level = ErrorLevel.HIGH
        recoveryActions = this.getPermissionRecoveryActions()
      } else if (status === 408 || status === 504) {
        type = ErrorType.TIMEOUT
        level = ErrorLevel.MEDIUM
        recoveryActions = this.getTimeoutRecoveryActions()
      } else if (status >= 400) {
        type = ErrorType.BUSINESS
        level = ErrorLevel.MEDIUM
      }
    }

    return {
      type,
      level,
      message,
      originalError: error,
      context,
      timestamp: Date.now(),
      recoveryActions
    }
  }

  /**
   * 获取HTTP错误消息
   * @param status HTTP状态码
   * @returns 错误消息
   */
  private getHttpErrorMessage(status: number): string {
    const messages: Record<number, string> = {
      400: '请求参数错误',
      401: '未授权，请重新登录',
      403: '权限不足',
      404: '请求的资源不存在',
      408: '请求超时',
      409: '资源冲突',
      422: '数据验证失败',
      429: '请求过于频繁',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务不可用',
      504: '网关超时'
    }
    
    return messages[status] || `HTTP错误 ${status}`
  }

  /**
   * 记录错误
   * @param errorInfo 错误信息
   */
  private recordError(errorInfo: ErrorInfo): void {
    // 添加到历史记录
    this.errorHistory.push(errorInfo)
    if (this.errorHistory.length > this.MAX_HISTORY_SIZE) {
      this.errorHistory.shift()
    }

    // 更新统计信息
    this.errorStats.totalErrors++
    
    const typeCount = this.errorStats.errorsByType.get(errorInfo.type) || 0
    this.errorStats.errorsByType.set(errorInfo.type, typeCount + 1)
    
    const levelCount = this.errorStats.errorsByLevel.get(errorInfo.level) || 0
    this.errorStats.errorsByLevel.set(errorInfo.level, levelCount + 1)
    
    // 更新最近错误
    this.errorStats.recentErrors.push(errorInfo)
    if (this.errorStats.recentErrors.length > this.MAX_RECENT_ERRORS) {
      this.errorStats.recentErrors.shift()
    }
  }

  /**
   * 显示用户反馈
   * @param errorInfo 错误信息
   * @param options 反馈选项
   */
  private showUserFeedback(errorInfo: ErrorInfo, options: FeedbackOptions): void {
    const {
      showNotification = true,
      showMessage = false,
      showDialog = false,
      autoClose = true,
      duration = 4500,
      position = 'top-right'
    } = options

    // 显示消息提示
    if (showMessage) {
      const messageType = this.getMessageType(errorInfo.level)
      ElMessage({
        type: messageType,
        message: errorInfo.message,
        duration: autoClose ? duration : 0,
        showClose: true
      })
    }

    // 显示通知
    if (showNotification) {
      const notificationType = this.getNotificationType(errorInfo.level)
      ElNotification({
        type: notificationType,
        title: this.getErrorTitle(errorInfo.type),
        message: errorInfo.message,
        duration: autoClose ? duration : 0,
        position,
        showClose: true,
        onClick: () => {
          if (errorInfo.recoveryActions && errorInfo.recoveryActions.length > 0) {
            this.showRecoveryDialog(errorInfo)
          }
        }
      })
    }

    // 显示对话框
    if (showDialog || errorInfo.level === ErrorLevel.CRITICAL) {
      this.showErrorDialog(errorInfo)
    }
  }

  /**
   * 显示错误对话框
   * @param errorInfo 错误信息
   */
  private async showErrorDialog(errorInfo: ErrorInfo): Promise<void> {
    const actions = errorInfo.recoveryActions || []
    
    if (actions.length > 0) {
      this.showRecoveryDialog(errorInfo)
    } else {
      await ElMessageBox.alert(errorInfo.message, this.getErrorTitle(errorInfo.type), {
        type: this.getMessageType(errorInfo.level),
        confirmButtonText: '确定'
      })
    }
  }

  /**
   * 显示恢复操作对话框
   * @param errorInfo 错误信息
   */
  private async showRecoveryDialog(errorInfo: ErrorInfo): Promise<void> {
    const actions = errorInfo.recoveryActions || []
    
    try {
      const result = await ElMessageBox.confirm(
        `${errorInfo.message}\n\n请选择恢复操作：`,
        this.getErrorTitle(errorInfo.type),
        {
          type: this.getMessageType(errorInfo.level),
          confirmButtonText: actions[0]?.label || '重试',
          cancelButtonText: '取消',
          showCancelButton: true
        }
      )
      
      if (result && actions[0]) {
        await this.executeRecoveryAction(actions[0], errorInfo)
      }
    } catch (error) {
      // 用户取消
    }
  }

  /**
   * 执行恢复操作
   * @param action 恢复操作
   * @param errorInfo 错误信息
   */
  private async executeRecoveryAction(action: RecoveryAction, errorInfo: ErrorInfo): Promise<void> {
    const actionKey = `${errorInfo.type}-${errorInfo.message}`
    const attempts = this.recoveryAttempts.get(actionKey) || 0
    
    try {
      await action.action()
      
      // 恢复成功
      this.recoveryAttempts.delete(actionKey)
      ElMessage.success('问题已解决')
      
      // 更新成功率
      this.updateRecoverySuccessRate(true)
      
    } catch (error) {
      // 恢复失败
      this.recoveryAttempts.set(actionKey, attempts + 1)
      this.updateRecoverySuccessRate(false)
      
      if (attempts < 2) {
        ElMessage.warning('恢复操作失败，请稍后重试')
      } else {
        ElMessage.error('多次恢复尝试失败，请联系技术支持')
      }
    }
  }

  /**
   * 尝试自动恢复
   * @param errorInfo 错误信息
   */
  private attemptAutoRecovery(errorInfo: ErrorInfo): void {
    // 只对特定类型的错误进行自动恢复
    if (errorInfo.type === ErrorType.NETWORK || errorInfo.type === ErrorType.TIMEOUT) {
      const actionKey = `auto-${errorInfo.type}-${errorInfo.message}`
      const attempts = this.recoveryAttempts.get(actionKey) || 0
      
      if (attempts < 3) {
        setTimeout(() => {
          // 这里可以实现自动重试逻辑
          console.log(`自动恢复尝试 ${attempts + 1}: ${errorInfo.message}`)
          this.recoveryAttempts.set(actionKey, attempts + 1)
        }, 1000 * (attempts + 1)) // 递增延迟
      }
    }
  }

  /**
   * 获取网络错误恢复操作
   */
  private getNetworkRecoveryActions(): RecoveryAction[] {
    return [
      {
        label: '重新连接',
        action: async () => {
          // 检查网络连接
          if (navigator.onLine) {
            window.location.reload()
          } else {
            throw new Error('网络仍然不可用')
          }
        },
        type: 'primary',
        icon: 'Refresh'
      },
      {
        label: '刷新页面',
        action: () => {
          window.location.reload()
        },
        type: 'warning',
        icon: 'RefreshRight'
      }
    ]
  }

  /**
   * 获取超时错误恢复操作
   */
  private getTimeoutRecoveryActions(): RecoveryAction[] {
    return [
      {
        label: '重试',
        action: async () => {
          // 这里应该重新执行原始操作
          console.log('重试操作')
        },
        type: 'primary',
        icon: 'Refresh'
      }
    ]
  }

  /**
   * 获取权限错误恢复操作
   */
  private getPermissionRecoveryActions(): RecoveryAction[] {
    return [
      {
        label: '重新登录',
        action: async () => {
          // 跳转到登录页面
          window.location.href = '/login'
        },
        type: 'primary',
        icon: 'User'
      }
    ]
  }

  /**
   * 获取重复错误恢复操作
   */
  private getDuplicateRecoveryActions(): RecoveryAction[] {
    return [
      {
        label: '查看已存在的记录',
        action: async () => {
          // 这里应该跳转到已存在的记录
          console.log('查看已存在的记录')
        },
        type: 'success',
        icon: 'View'
      },
      {
        label: '修改后重试',
        action: async () => {
          // 这里应该返回表单让用户修改
          console.log('修改后重试')
        },
        type: 'warning',
        icon: 'Edit'
      }
    ]
  }

  /**
   * 获取错误标题
   * @param type 错误类型
   * @returns 标题
   */
  private getErrorTitle(type: ErrorType): string {
    const titles: Record<ErrorType, string> = {
      [ErrorType.NETWORK]: '网络错误',
      [ErrorType.VALIDATION]: '验证错误',
      [ErrorType.BUSINESS]: '业务错误',
      [ErrorType.SYSTEM]: '系统错误',
      [ErrorType.TIMEOUT]: '超时错误',
      [ErrorType.PERMISSION]: '权限错误',
      [ErrorType.DUPLICATE]: '重复错误'
    }
    
    return titles[type] || '错误'
  }

  /**
   * 获取消息类型
   * @param level 错误级别
   * @returns 消息类型
   */
  private getMessageType(level: ErrorLevel): 'success' | 'warning' | 'info' | 'error' {
    switch (level) {
      case ErrorLevel.LOW:
        return 'info'
      case ErrorLevel.MEDIUM:
        return 'warning'
      case ErrorLevel.HIGH:
      case ErrorLevel.CRITICAL:
        return 'error'
      default:
        return 'warning'
    }
  }

  /**
   * 获取通知类型
   * @param level 错误级别
   * @returns 通知类型
   */
  private getNotificationType(level: ErrorLevel): 'success' | 'warning' | 'info' | 'error' {
    return this.getMessageType(level)
  }

  /**
   * 更新恢复成功率
   * @param success 是否成功
   */
  private updateRecoverySuccessRate(success: boolean): void {
    // 简单的成功率计算，实际应用中可以更复杂
    const totalAttempts = Array.from(this.recoveryAttempts.values()).reduce((sum, count) => sum + count, 0)
    if (totalAttempts > 0) {
      // 这里需要更精确的成功率计算逻辑
      this.errorStats.recoverySuccessRate = success ? 
        Math.min(this.errorStats.recoverySuccessRate + 0.1, 1) :
        Math.max(this.errorStats.recoverySuccessRate - 0.05, 0)
    }
  }

  /**
   * 获取错误统计
   * @returns 错误统计信息
   */
  getErrorStats(): ErrorStats {
    return { ...this.errorStats }
  }

  /**
   * 获取错误历史
   * @param limit 限制数量
   * @returns 错误历史
   */
  getErrorHistory(limit?: number): ErrorInfo[] {
    const history = [...this.errorHistory].reverse()
    return limit ? history.slice(0, limit) : history
  }

  /**
   * 清除错误历史
   */
  clearErrorHistory(): void {
    this.errorHistory = []
    this.errorStats = {
      totalErrors: 0,
      errorsByType: new Map(),
      errorsByLevel: new Map(),
      recentErrors: [],
      recoverySuccessRate: 0
    }
    this.recoveryAttempts.clear()
  }

  /**
   * 导出错误报告
   * @returns 错误报告
   */
  exportErrorReport(): string {
    const report = {
      timestamp: new Date().toISOString(),
      stats: this.getErrorStats(),
      recentErrors: this.getErrorHistory(20),
      userAgent: navigator.userAgent,
      url: window.location.href
    }
    
    return JSON.stringify(report, null, 2)
  }
}

// 时间线专用错误处理器
export class TimelineErrorHandler extends ErrorHandlerEnhancer {
  /**
   * 处理时间线创建错误
   * @param error 错误
   * @param formData 表单数据
   * @returns 错误信息
   */
  handleTimelineCreationError(error: any, formData?: any): ErrorInfo {
    const context = {
      operation: 'timeline-creation',
      formData: formData ? { ...formData, sensitiveData: '[HIDDEN]' } : undefined
    }
    
    // 特殊处理时间线创建相关的错误
    if (error?.message?.includes('重复')) {
      return this.handleError(error, context, {
        showNotification: true,
        showMessage: false,
        showDialog: true
      })
    }
    
    return this.handleError(error, context, {
      showNotification: true,
      showMessage: true
    })
  }

  /**
   * 处理进度获取错误
   * @param error 错误
   * @param timelineId 时间线ID
   * @returns 错误信息
   */
  handleProgressError(error: any, timelineId?: number): ErrorInfo {
    const context = {
      operation: 'progress-tracking',
      timelineId
    }
    
    return this.handleError(error, context, {
      showNotification: false,
      showMessage: true,
      autoClose: true,
      duration: 3000
    })
  }

  /**
   * 处理取消操作错误
   * @param error 错误
   * @param timelineId 时间线ID
   * @returns 错误信息
   */
  handleCancelError(error: any, timelineId?: number): ErrorInfo {
    const context = {
      operation: 'cancel-generation',
      timelineId
    }
    
    return this.handleError(error, context, {
      showNotification: true,
      showMessage: false
    })
  }
}

// 创建全局实例
export const errorHandlerEnhancer = new ErrorHandlerEnhancer()
export const timelineErrorHandler = new TimelineErrorHandler()

// 工具函数
export const showSuccessMessage = (message: string, duration = 3000) => {
  ElMessage.success({
    message,
    duration,
    showClose: true
  })
}

export const showWarningMessage = (message: string, duration = 4000) => {
  ElMessage.warning({
    message,
    duration,
    showClose: true
  })
}

export const showInfoMessage = (message: string, duration = 3000) => {
  ElMessage.info({
    message,
    duration,
    showClose: true
  })
}

export default errorHandlerEnhancer