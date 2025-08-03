/**
 * 防重复提交工具类
 * 用于防止用户在短时间内重复提交相同的请求
 */

interface SubmissionRecord {
  timestamp: number
  fingerprint: string
  isSubmitting: boolean
  submitCount: number // 提交次数
  lastError?: string // 最后一次错误信息
}

interface ButtonState {
  disabled: boolean
  loading: boolean
  text: string
  clickCount: number
  lastClickTime: number
}

class DuplicationPrevention {
  private submissions: Map<string, SubmissionRecord> = new Map()
  private buttonStates: Map<string, ButtonState> = new Map()
  private readonly DEFAULT_COOLDOWN = 3000 // 默认冷却时间：3秒
  private readonly CLEANUP_INTERVAL = 60000 // 清理间隔：1分钟
  private readonly BUTTON_CLICK_THRESHOLD = 5 // 按钮点击阈值
  private readonly BUTTON_CLICK_WINDOW = 10000 // 按钮点击时间窗口：10秒
  
  constructor() {
    // 定期清理过期的提交记录
    setInterval(() => {
      this.cleanupExpiredRecords()
    }, this.CLEANUP_INTERVAL)
  }
  
  /**
   * 生成请求指纹
   * @param data 请求数据
   * @returns 请求指纹
   */
  private generateFingerprint(data: any): string {
    try {
      // 对数据进行序列化并生成简单的哈希
      const jsonStr = JSON.stringify(data, Object.keys(data).sort())
      return this.simpleHash(jsonStr)
    } catch (error) {
      console.warn('生成请求指纹失败:', error)
      return Math.random().toString(36).substring(2)
    }
  }
  
  /**
   * 简单哈希函数
   * @param str 输入字符串
   * @returns 哈希值
   */
  private simpleHash(str: string): string {
    let hash = 0
    if (str.length === 0) return hash.toString()
    
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i)
      hash = ((hash << 5) - hash) + char
      hash = hash & hash // 转换为32位整数
    }
    
    return Math.abs(hash).toString(36)
  }
  
  /**
   * 检查是否可以提交
   * @param key 提交键（通常是API路径或操作标识）
   * @param data 请求数据
   * @param cooldown 冷却时间（毫秒），默认3秒
   * @returns 是否可以提交
   */
  canSubmit(key: string, data: any, cooldown: number = this.DEFAULT_COOLDOWN): boolean {
    const fingerprint = this.generateFingerprint(data)
    const submissionKey = `${key}:${fingerprint}`
    const now = Date.now()
    
    const record = this.submissions.get(submissionKey)
    
    if (!record) {
      // 没有记录，可以提交
      return true
    }
    
    if (record.isSubmitting) {
      // 正在提交中，不能重复提交
      console.warn('请求正在处理中，请勿重复提交')
      return false
    }
    
    if (now - record.timestamp < cooldown) {
      // 在冷却时间内，不能提交
      const remainingTime = Math.ceil((cooldown - (now - record.timestamp)) / 1000)
      console.warn(`请求过于频繁，请等待 ${remainingTime} 秒后再试`)
      return false
    }
    
    return true
  }
  
  /**
   * 标记开始提交
   * @param key 提交键
   * @param data 请求数据
   */
  markSubmitting(key: string, data: any): void {
    const fingerprint = this.generateFingerprint(data)
    const submissionKey = `${key}:${fingerprint}`
    const now = Date.now()
    
    const existingRecord = this.submissions.get(submissionKey)
    
    this.submissions.set(submissionKey, {
      timestamp: now,
      fingerprint,
      isSubmitting: true,
      submitCount: (existingRecord?.submitCount || 0) + 1,
      lastError: undefined
    })
  }
  
  /**
   * 标记提交完成
   * @param key 提交键
   * @param data 请求数据
   */
  markSubmitted(key: string, data: any): void {
    const fingerprint = this.generateFingerprint(data)
    const submissionKey = `${key}:${fingerprint}`
    const existingRecord = this.submissions.get(submissionKey)
    
    this.submissions.set(submissionKey, {
      timestamp: Date.now(),
      fingerprint,
      isSubmitting: false,
      submitCount: existingRecord?.submitCount || 1,
      lastError: undefined
    })
  }

  /**
   * 标记提交失败
   * @param key 提交键
   * @param data 请求数据
   * @param error 错误信息
   */
  markSubmitFailed(key: string, data: any, error: string): void {
    const fingerprint = this.generateFingerprint(data)
    const submissionKey = `${key}:${fingerprint}`
    const existingRecord = this.submissions.get(submissionKey)
    
    this.submissions.set(submissionKey, {
      timestamp: Date.now(),
      fingerprint,
      isSubmitting: false,
      submitCount: existingRecord?.submitCount || 1,
      lastError: error
    })
  }
  
  /**
   * 清除提交记录
   * @param key 提交键
   * @param data 请求数据（可选）
   */
  clearSubmission(key: string, data?: any): void {
    if (data) {
      const fingerprint = this.generateFingerprint(data)
      const submissionKey = `${key}:${fingerprint}`
      this.submissions.delete(submissionKey)
    } else {
      // 清除所有以该key开头的记录
      for (const [submissionKey] of this.submissions) {
        if (submissionKey.startsWith(`${key}:`)) {
          this.submissions.delete(submissionKey)
        }
      }
    }
  }
  
  /**
   * 清理过期的记录
   */
  private cleanupExpiredRecords(): void {
    const now = Date.now()
    const EXPIRE_TIME = 5 * 60 * 1000 // 5分钟过期
    
    for (const [key, record] of this.submissions) {
      if (now - record.timestamp > EXPIRE_TIME) {
        this.submissions.delete(key)
      }
    }
  }
  
  /**
   * 获取提交状态
   * @param key 提交键
   * @param data 请求数据
   * @returns 提交状态信息
   */
  getSubmissionStatus(key: string, data: any): {
    isSubmitting: boolean
    lastSubmitTime: number | null
    canSubmit: boolean
    remainingCooldown: number
    submitCount: number
    lastError?: string
  } {
    const fingerprint = this.generateFingerprint(data)
    const submissionKey = `${key}:${fingerprint}`
    const record = this.submissions.get(submissionKey)
    const now = Date.now()
    
    if (!record) {
      return {
        isSubmitting: false,
        lastSubmitTime: null,
        canSubmit: true,
        remainingCooldown: 0,
        submitCount: 0
      }
    }
    
    const remainingCooldown = Math.max(0, this.DEFAULT_COOLDOWN - (now - record.timestamp))
    
    return {
      isSubmitting: record.isSubmitting,
      lastSubmitTime: record.timestamp,
      canSubmit: !record.isSubmitting && remainingCooldown === 0,
      remainingCooldown,
      submitCount: record.submitCount,
      lastError: record.lastError
    }
  }

  /**
   * 处理按钮点击
   * @param buttonKey 按钮标识
   * @returns 是否允许点击
   */
  handleButtonClick(buttonKey: string): boolean {
    const now = Date.now()
    const buttonState = this.buttonStates.get(buttonKey)
    
    if (!buttonState) {
      // 首次点击
      this.buttonStates.set(buttonKey, {
        disabled: false,
        loading: false,
        text: '',
        clickCount: 1,
        lastClickTime: now
      })
      return true
    }
    
    // 检查是否在时间窗口内
    if (now - buttonState.lastClickTime < this.BUTTON_CLICK_WINDOW) {
      buttonState.clickCount++
      
      // 如果点击次数超过阈值，禁用按钮
      if (buttonState.clickCount > this.BUTTON_CLICK_THRESHOLD) {
        buttonState.disabled = true
        console.warn(`按钮点击过于频繁，已被禁用`)
        return false
      }
    } else {
      // 超出时间窗口，重置计数
      buttonState.clickCount = 1
    }
    
    buttonState.lastClickTime = now
    return !buttonState.disabled && !buttonState.loading
  }

  /**
   * 设置按钮状态
   * @param buttonKey 按钮标识
   * @param state 按钮状态
   */
  setButtonState(buttonKey: string, state: Partial<ButtonState>): void {
    const currentState = this.buttonStates.get(buttonKey) || {
      disabled: false,
      loading: false,
      text: '',
      clickCount: 0,
      lastClickTime: 0
    }
    
    this.buttonStates.set(buttonKey, {
      ...currentState,
      ...state
    })
  }

  /**
   * 获取按钮状态
   * @param buttonKey 按钮标识
   * @returns 按钮状态
   */
  getButtonState(buttonKey: string): ButtonState {
    return this.buttonStates.get(buttonKey) || {
      disabled: false,
      loading: false,
      text: '',
      clickCount: 0,
      lastClickTime: 0
    }
  }

  /**
   * 重置按钮状态
   * @param buttonKey 按钮标识
   */
  resetButtonState(buttonKey: string): void {
    this.buttonStates.delete(buttonKey)
  }

  /**
   * 启用按钮（解除禁用状态）
   * @param buttonKey 按钮标识
   */
  enableButton(buttonKey: string): void {
    const state = this.getButtonState(buttonKey)
    state.disabled = false
    state.loading = false
    state.clickCount = 0
    this.buttonStates.set(buttonKey, state)
  }
}

// 创建全局实例
export const duplicationPrevention = new DuplicationPrevention()

/**
 * 防重复提交装饰器
 * 用于装饰异步函数，自动处理防重复提交逻辑
 */
export function preventDuplication(key: string, cooldown?: number) {
  return function (target: any, propertyName: string, descriptor: PropertyDescriptor) {
    const method = descriptor.value
    
    descriptor.value = async function (...args: any[]) {
      const data = args[0] // 假设第一个参数是请求数据
      
      // 检查是否可以提交
      if (!duplicationPrevention.canSubmit(key, data, cooldown)) {
        throw new Error('请求过于频繁，请稍后再试')
      }
      
      // 标记开始提交
      duplicationPrevention.markSubmitting(key, data)
      
      try {
        // 执行原方法
        const result = await method.apply(this, args)
        
        // 标记提交完成
        duplicationPrevention.markSubmitted(key, data)
        
        return result
      } catch (error) {
        // 提交失败，清除提交状态
        duplicationPrevention.clearSubmission(key, data)
        throw error
      }
    }
  }
}

export default duplicationPrevention