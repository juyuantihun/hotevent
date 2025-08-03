/**
 * 请求重试工具
 * 提供智能重试策略，优化网络请求成功率
 */
import axios, { AxiosError, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { networkMonitor } from '@/services/networkMonitor'

/**
 * 重试配置接口
 */
export interface RetryConfig {
  // 最大重试次数
  maxRetries: number
  // 初始重试延迟（毫秒）
  retryDelay: number
  // 是否使用指数退避策略
  useExponentialBackoff: boolean
  // 最大重试延迟（毫秒）
  maxRetryDelay: number
  // 重试条件函数
  retryCondition: (error: AxiosError) => boolean
  // 请求超时时间（毫秒）
  timeout: number
  // 重试前回调函数
  onRetry?: (retryCount: number, error: AxiosError, delay: number) => void
}

/**
 * 默认重试配置
 */
const defaultRetryConfig: RetryConfig = {
  maxRetries: 3,
  retryDelay: 1000,
  useExponentialBackoff: true,
  maxRetryDelay: 30000,
  retryCondition: (error: AxiosError) => {
    // 默认重试条件：网络错误、请求超时或服务器错误（5xx）
    return (
      !error.response ||
      error.code === 'ECONNABORTED' ||
      (error.response && error.response.status >= 500)
    )
  },
  timeout: 10000
}

/**
 * 创建带重试功能的请求函数
 * @param requestFn 原始请求函数
 * @param config 重试配置
 * @returns 带重试功能的请求函数
 */
export function withRetry<T>(
  requestFn: (config: AxiosRequestConfig) => Promise<AxiosResponse<T>>,
  config: Partial<RetryConfig> = {}
): (requestConfig: AxiosRequestConfig) => Promise<AxiosResponse<T>> {
  // 合并配置
  const mergedConfig: RetryConfig = {
    ...defaultRetryConfig,
    ...config
  }
  
  return async (requestConfig: AxiosRequestConfig): Promise<AxiosResponse<T>> => {
    // 设置超时时间
    requestConfig.timeout = requestConfig.timeout || mergedConfig.timeout
    
    // 重试计数
    let retryCount = 0
    // 当前延迟
    let currentDelay = mergedConfig.retryDelay
    
    while (true) {
      try {
        // 尝试请求
        return await requestFn(requestConfig)
      } catch (error) {
        // 如果不是Axios错误或已达到最大重试次数，则抛出错误
        if (!axios.isAxiosError(error) || retryCount >= mergedConfig.maxRetries) {
          throw error
        }
        
        // 检查是否满足重试条件
        if (!mergedConfig.retryCondition(error)) {
          throw error
        }
        
        // 增加重试计数
        retryCount++
        
        // 计算下一次重试延迟
        if (mergedConfig.useExponentialBackoff) {
          // 指数退避策略：每次重试延迟加倍，但不超过最大延迟
          currentDelay = Math.min(
            currentDelay * 2,
            mergedConfig.maxRetryDelay
          )
        }
        
        // 添加随机抖动，避免多个请求同时重试
        const jitter = Math.random() * 300
        const finalDelay = currentDelay + jitter
        
        // 调用重试前回调
        if (mergedConfig.onRetry) {
          mergedConfig.onRetry(retryCount, error, finalDelay)
        }
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, finalDelay))
        
        // 根据网络状态调整请求配置
        if (networkMonitor.isOnline()) {
          requestConfig = await networkMonitor.adjustRequestConfig(requestConfig)
        }
      }
    }
  }
}

/**
 * 重试请求选项接口
 */
export interface RetryRequestOptions {
  // 最大重试次数
  maxRetries?: number
  // 基础延迟（毫秒）
  baseDelay?: number
  // 抖动因子（0-1之间）
  jitterFactor?: number
  // 最大延迟（毫秒）
  maxDelay?: number
  // 重试条件函数
  retryCondition?: (error: any) => boolean
  // 延迟计算函数
  delayFn?: (retryCount: number) => number
  // 重试前回调函数
  onRetry?: (error: any, retryCount: number) => void
  // 达到最大重试次数回调函数
  onMaxRetries?: (error: any) => void
}

/**
 * 重试请求函数
 * @param requestFn 请求函数
 * @param options 重试选项
 * @returns 请求结果
 */
export async function retryRequest<T>(
  requestFn: () => Promise<T>,
  options: RetryRequestOptions = {}
): Promise<T> {
  // 默认选项
  const {
    maxRetries = 3,
    baseDelay = 1000,
    jitterFactor = 0,
    maxDelay = Number.POSITIVE_INFINITY,
    retryCondition = isErrorRetryable,
    delayFn,
    onRetry,
    onMaxRetries
  } = options
  
  // 重试计数
  let retryCount = 0
  
  while (true) {
    try {
      // 尝试请求
      return await requestFn()
    } catch (error) {
      // 增加重试计数
      retryCount++
      
      // 如果已达到最大重试次数，则抛出错误
      if (retryCount > maxRetries) {
        if (onMaxRetries) {
          onMaxRetries(error)
        }
        throw error
      }
      
      // 检查是否满足重试条件
      if (!retryCondition(error)) {
        throw error
      }
      
      // 计算延迟时间
      let delay
      if (delayFn) {
        delay = delayFn(retryCount)
      } else {
        delay = calculateBackoff(retryCount, baseDelay, jitterFactor, maxDelay)
      }
      
      // 调用重试前回调
      if (onRetry) {
        onRetry(error, retryCount)
      }
      
      // 等待一段时间后重试
      await new Promise(resolve => setTimeout(resolve, delay))
    }
  }
}

/**
 * 创建带重试功能的Axios实例
 * @param axiosInstance Axios实例
 * @param config 重试配置
 * @returns 带重试功能的Axios实例
 */
export function createRetryableAxios(
  axiosInstance: typeof axios,
  config: Partial<RetryConfig> = {}
): any {
  // 合并配置
  const mergedConfig: RetryConfig = {
    ...defaultRetryConfig,
    ...config
  }
  
  // 创建新的Axios实例
  const newInstance = axios.create(axiosInstance.defaults)
  
  // 添加响应拦截器
  newInstance.interceptors.response.use(
    response => response,
    async (error: AxiosError) => {
      // 如果没有配置或不是Axios错误，则拒绝Promise
      if (!axios.isAxiosError(error) || !error.config) {
        return Promise.reject(error)
      }
      
      // 获取请求配置
      const config = error.config as AxiosRequestConfig & { 
        _retryCount?: number,
        _retryDelay?: number
      }
      
      // 如果已达到最大重试次数或不满足重试条件，则拒绝Promise
      if (
        (config._retryCount || 0) >= mergedConfig.maxRetries ||
        !mergedConfig.retryCondition(error)
      ) {
        return Promise.reject(error)
      }
      
      // 增加重试计数
      config._retryCount = (config._retryCount || 0) + 1
      
      // 计算重试延迟
      let retryDelay = config._retryDelay || mergedConfig.retryDelay
      
      if (mergedConfig.useExponentialBackoff) {
        // 指数退避策略
        retryDelay = Math.min(
          retryDelay * 2,
          mergedConfig.maxRetryDelay
        )
      }
      
      // 保存当前延迟用于下次计算
      config._retryDelay = retryDelay
      
      // 添加随机抖动
      const jitter = Math.random() * 300
      const finalDelay = retryDelay + jitter
      
      // 调用重试前回调
      if (mergedConfig.onRetry) {
        mergedConfig.onRetry(config._retryCount, error, finalDelay)
      }
      
      // 等待一段时间后重试
      await new Promise(resolve => setTimeout(resolve, finalDelay))
      
      // 根据网络状态调整请求配置
      if (networkMonitor.isOnline()) {
        const adjustedConfig = await networkMonitor.adjustRequestConfig(config)
        return newInstance(adjustedConfig)
      }
      
      // 重试请求
      return newInstance(config)
    }
  )
  
  return newInstance
}

/**
 * 智能判断错误是否可重试
 * @param error Axios错误
 * @returns 是否可重试
 */
export function isErrorRetryable(error: AxiosError | any): boolean {
  // 检查自定义可重试标记
  if (error && (error as any).isRetryable === true) {
    return true
  }
  
  // 检查错误消息是否包含网络错误或超时关键词
  if (error && typeof error.message === 'string') {
    const message = error.message.toLowerCase()
    if (message.includes('network error') || message.includes('timeout')) {
      return true
    }
  }
  
  // 如果是Axios错误
  if (axios.isAxiosError(error)) {
    // 如果没有响应，可能是网络错误，可以重试
    if (!error.response) {
      return true
    }
    
    // 获取状态码
    const status = error.response.status
    
    // 服务器错误（5xx）可以重试
    if (status >= 500) {
      return true
    }
    
    // 请求超时（408）可以重试
    if (status === 408) {
      return true
    }
    
    // 请求过多（429）可以重试，但应该增加延迟
    if (status === 429) {
      return true
    }
    
    // 其他客户端错误（4xx）通常不需要重试
    if (status >= 400 && status < 500) {
      // 特殊情况：401可能是token过期，可以在刷新token后重试
      if (status === 401) {
        // 检查是否是刷新token的请求，如果是则不重试
        if (error.config?.url?.includes('/auth/refresh-token')) {
          return false
        }
        return true
      }
      
      return false
    }
  }
  
  // 默认不重试
  return false
}

/**
 * 根据错误类型计算最佳重试延迟
 * @param error Axios错误
 * @param baseDelay 基础延迟
 * @returns 计算后的延迟
 */
export function calculateRetryDelay(error: AxiosError, baseDelay: number = 1000): number {
  // 如果是请求过多（429），通常服务器会返回Retry-After头
  if (error.response?.status === 429) {
    const retryAfter = error.response.headers['retry-after']
    if (retryAfter) {
      // Retry-After可能是秒数或日期
      if (/^\d+$/.test(retryAfter)) {
        return parseInt(retryAfter, 10) * 1000
      } else {
        const date = new Date(retryAfter)
        const now = new Date()
        return Math.max(date.getTime() - now.getTime(), baseDelay)
      }
    }
    
    // 如果没有Retry-After头，使用较长的延迟
    return baseDelay * 2
  }
  
  // 如果是服务器错误（5xx），使用标准延迟
  if (error.response?.status && error.response.status >= 500) {
    return baseDelay
  }
  
  // 如果是网络错误，根据网络状况调整延迟
  if (!error.response) {
    // 检查网络连接质量
    if ('connection' in navigator && (navigator as any).connection) {
      const connection = (navigator as any).connection
      
      // 如果网络较慢，增加延迟
      if (connection.effectiveType === '2g' || connection.effectiveType === 'slow-2g') {
        return baseDelay * 2
      }
      
      // 如果网络不稳定，增加延迟
      if (connection.rtt > 500) {
        return baseDelay * 1.5
      }
    }
    
    return baseDelay
  }
  
  // 默认延迟
  return baseDelay
}

/**
 * 计算指数退避延迟
 * @param retryCount 重试次数
 * @param baseDelay 基础延迟
 * @param jitterFactor 抖动因子（0-1之间）
 * @param maxDelay 最大延迟
 * @returns 计算后的延迟
 */
export function calculateBackoff(
  retryCount: number,
  baseDelay: number,
  jitterFactor: number = 0,
  maxDelay: number = Number.POSITIVE_INFINITY
): number {
  // 计算指数退避延迟
  let delay = baseDelay * Math.pow(2, retryCount - 1)
  
  // 添加随机抖动
  if (jitterFactor > 0) {
    const jitter = delay * jitterFactor * Math.random()
    delay += jitter
  }
  
  // 限制最大延迟
  return Math.min(delay, maxDelay)
}