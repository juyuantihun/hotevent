/**
 * API请求重试服务
 * 提供统一的请求重试管理
 */
import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { apiErrorFeedbackService } from './apiErrorFeedbackService'
import { isErrorRetryable, calculateRetryDelay } from '@/utils/requestRetry'
import { networkMonitor } from './networkMonitor'

/**
 * 重试配置接口
 */
export interface RetryOptions {
  // 最大重试次数
  maxRetries?: number
  // 初始重试延迟（毫秒）
  retryDelay?: number
  // 是否使用指数退避策略
  useExponentialBackoff?: boolean
  // 最大重试延迟（毫秒）
  maxRetryDelay?: number
  // 重试条件函数
  retryCondition?: (error: AxiosError) => boolean
  // 重试前回调函数
  onRetry?: (retryCount: number, error: AxiosError, delay: number) => void
  // 是否显示重试进度
  showProgress?: boolean
  // 是否显示错误消息
  showErrorMessage?: boolean
}

/**
 * 默认重试配置
 */
const defaultRetryOptions: RetryOptions = {
  maxRetries: 3,
  retryDelay: 1000,
  useExponentialBackoff: true,
  maxRetryDelay: 30000,
  retryCondition: isErrorRetryable,
  showProgress: true,
  showErrorMessage: true
}

/**
 * 带重试的请求函数
 * @param requestFn 请求函数
 * @param options 重试选项
 * @returns 请求结果
 */
export async function withRetry<T>(
  requestFn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<T> {
  // 合并选项
  const mergedOptions: RetryOptions = {
    ...defaultRetryOptions,
    ...options
  }
  
  const {
    maxRetries = 3,
    retryDelay = 1000,
    useExponentialBackoff = true,
    maxRetryDelay = 30000,
    retryCondition = isErrorRetryable,
    onRetry,
    showProgress = true,
    showErrorMessage = true
  } = mergedOptions
  
  // 重试计数
  let retryCount = 0
  // 当前延迟
  let currentDelay = retryDelay
  
  // 如果显示进度，初始化重试进度
  if (showProgress) {
    apiErrorFeedbackService.startRetryProgress(maxRetries)
  }
  
  while (true) {
    try {
      // 尝试请求
      const result = await requestFn()
      
      // 如果显示进度且进行了重试，显示成功消息
      if (showProgress && retryCount > 0) {
        apiErrorFeedbackService.completeRetryProgress(true, '请求重试成功')
      }
      
      return result
    } catch (error) {
      // 如果不是Axios错误或已达到最大重试次数，则抛出错误
      if (!axios.isAxiosError(error) || retryCount >= maxRetries) {
        // 如果显示进度且进行了重试，显示失败消息
        if (showProgress && retryCount > 0) {
          apiErrorFeedbackService.completeRetryProgress(false, '达到最大重试次数，请求仍然失败')
        }
        
        // 如果显示错误消息
        if (showErrorMessage) {
          const errorMessage = axios.isAxiosError(error)
            ? apiErrorFeedbackService.createUserFriendlyMessage(error)
            : error.message || '请求失败'
          
          ElMessage.error(errorMessage)
        }
        
        throw error
      }
      
      // 检查是否满足重试条件
      if (!axios.isAxiosError(error) || !retryCondition(error)) {
        // 如果显示错误消息
        if (showErrorMessage) {
          const errorMessage = axios.isAxiosError(error)
            ? apiErrorFeedbackService.createUserFriendlyMessage(error)
            : error.message || '请求失败'
          
          ElMessage.error(errorMessage)
        }
        
        throw error
      }
      
      // 增加重试计数
      retryCount++
      
      // 计算下一次重试延迟
      if (useExponentialBackoff) {
        // 指数退避策略：每次重试延迟加倍，但不超过最大延迟
        currentDelay = Math.min(
          currentDelay * 2,
          maxRetryDelay
        )
      }
      
      // 如果是Axios错误，使用智能延迟计算
      if (axios.isAxiosError(error)) {
        currentDelay = calculateRetryDelay(error, currentDelay)
      }
      
      // 添加随机抖动，避免多个请求同时重试
      const jitter = Math.random() * 300
      const finalDelay = currentDelay + jitter
      
      // 调用重试前回调
      if (onRetry) {
        onRetry(retryCount, error as AxiosError, finalDelay)
      }
      
      // 如果显示进度，更新重试进度
      if (showProgress) {
        apiErrorFeedbackService.updateRetryProgress(
          retryCount,
          `第${retryCount}次重试中，等待${Math.round(finalDelay / 1000)}秒...`
        )
      }
      
      // 等待一段时间后重试
      await new Promise(resolve => setTimeout(resolve, finalDelay))
    }
  }
}

/**
 * 带重试的Axios请求
 * @param config Axios请求配置
 * @param options 重试选项
 * @returns 请求结果
 */
export async function retryableRequest<T = any>(
  config: AxiosRequestConfig,
  options: RetryOptions = {}
): Promise<AxiosResponse<T>> {
  return withRetry(() => axios(config), options)
}

/**
 * 带重试的GET请求
 * @param url 请求URL
 * @param config Axios请求配置
 * @param options 重试选项
 * @returns 请求结果
 */
export async function retryableGet<T = any>(
  url: string,
  config?: AxiosRequestConfig,
  options?: RetryOptions
): Promise<AxiosResponse<T>> {
  return retryableRequest<T>({
    ...config,
    url,
    method: 'get'
  }, options)
}

/**
 * 带重试的POST请求
 * @param url 请求URL
 * @param data 请求数据
 * @param config Axios请求配置
 * @param options 重试选项
 * @returns 请求结果
 */
export async function retryablePost<T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig,
  options?: RetryOptions
): Promise<AxiosResponse<T>> {
  return retryableRequest<T>({
    ...config,
    url,
    method: 'post',
    data
  }, options)
}

/**
 * 带重试的PUT请求
 * @param url 请求URL
 * @param data 请求数据
 * @param config Axios请求配置
 * @param options 重试选项
 * @returns 请求结果
 */
export async function retryablePut<T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig,
  options?: RetryOptions
): Promise<AxiosResponse<T>> {
  return retryableRequest<T>({
    ...config,
    url,
    method: 'put',
    data
  }, options)
}

/**
 * 带重试的DELETE请求
 * @param url 请求URL
 * @param config Axios请求配置
 * @param options 重试选项
 * @returns 请求结果
 */
export async function retryableDelete<T = any>(
  url: string,
  config?: AxiosRequestConfig,
  options?: RetryOptions
): Promise<AxiosResponse<T>> {
  return retryableRequest<T>({
    ...config,
    url,
    method: 'delete'
  }, options)
}

/**
 * API请求重试服务
 */
export const apiRetryService = {
  withRetry,
  retryableRequest,
  retryableGet,
  retryablePost,
  retryablePut,
  retryableDelete,
  
  /**
   * 创建可重试的请求函数
   * @param requestFn 原始请求函数
   * @param options 重试选项
   * @returns 可重试的请求函数
   */
  createRetryableRequest<T>(
    requestFn: (...args: any[]) => Promise<T>,
    options: RetryOptions = {}
  ): (...args: any[]) => Promise<T> {
    return (...args: any[]) => withRetry(() => requestFn(...args), options)
  }
}