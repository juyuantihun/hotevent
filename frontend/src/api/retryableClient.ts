/**
 * 可重试的API客户端
 * 提供自动重试和错误处理功能
 */
import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { createRetryableAxios, type RetryConfig } from '@/utils/requestRetry'
import { ElMessage } from 'element-plus'

// 扩展AxiosRequestConfig类型，添加重试相关配置
export interface RetryableRequestConfig extends AxiosRequestConfig {
  // 是否启用重试
  enableRetry?: boolean
  // 最大重试次数
  maxRetries?: number
  // 重试延迟（毫秒）
  retryDelay?: number
  // 重试回调函数
  onRetry?: (retryCount: number, error: AxiosError, delay: number) => void
}

// 创建基础axios实例
const baseAxios = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 创建可重试的axios实例
const retryableAxios = createRetryableAxios(baseAxios, {
  maxRetries: 3,
  retryDelay: 1000,
  useExponentialBackoff: true,
  maxRetryDelay: 30000
})

/**
 * 创建可重试的请求
 * @param defaultConfig 默认请求配置
 * @returns 请求函数
 */
export function createRetryableRequest<T = any>(
  defaultConfig: RetryableRequestConfig
): (config?: Partial<RetryableRequestConfig>) => Promise<T> {
  return async (config?: Partial<RetryableRequestConfig>): Promise<T> => {
    // 合并配置
    const mergedConfig: RetryableRequestConfig = {
      ...defaultConfig,
      ...config
    }
    
    // 提取重试相关配置
    const { enableRetry = true, maxRetries, retryDelay, onRetry, ...axiosConfig } = mergedConfig
    
    try {
      // 根据是否启用重试选择不同的axios实例
      const client = enableRetry ? retryableAxios : baseAxios
      
      // 如果有重试配置，添加到请求配置中
      if (enableRetry && (maxRetries !== undefined || retryDelay !== undefined || onRetry)) {
        const retryConfig: Partial<RetryConfig> = {}
        
        if (maxRetries !== undefined) {
          retryConfig.maxRetries = maxRetries
        }
        
        if (retryDelay !== undefined) {
          retryConfig.retryDelay = retryDelay
        }
        
        if (onRetry) {
          retryConfig.onRetry = onRetry
        }
        
        // 添加到请求头中，拦截器会读取这些配置
        axiosConfig.headers = {
          ...axiosConfig.headers,
          'X-Retry-Config': JSON.stringify(retryConfig)
        }
      }
      
      // 发送请求
      const response = await client(axiosConfig)
      return response.data
    } catch (error) {
      // 处理错误
      if (axios.isAxiosError(error)) {
        // 如果是API错误，提取错误信息
        const errorMessage = error.response?.data?.message || error.message
        
        // 如果不是重试过程中的错误，显示错误消息
        if (!error.config?._isRetry) {
          ElMessage.error(errorMessage)
        }
      } else {
        // 如果是其他错误，显示通用错误消息
        ElMessage.error('请求失败，请稍后重试')
      }
      
      // 重新抛出错误，让调用者可以处理
      throw error
    }
  }
}

/**
 * 创建GET请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns 请求函数
 */
export function createGetRequest<T = any>(
  url: string,
  config?: Omit<RetryableRequestConfig, 'url' | 'method'>
): (params?: any, extraConfig?: Partial<RetryableRequestConfig>) => Promise<T> {
  return (params?: any, extraConfig?: Partial<RetryableRequestConfig>): Promise<T> => {
    const request = createRetryableRequest<T>({
      url,
      method: 'get',
      params,
      ...config
    })
    
    return request(extraConfig)
  }
}

/**
 * 创建POST请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns 请求函数
 */
export function createPostRequest<T = any>(
  url: string,
  config?: Omit<RetryableRequestConfig, 'url' | 'method'>
): (data?: any, extraConfig?: Partial<RetryableRequestConfig>) => Promise<T> {
  return (data?: any, extraConfig?: Partial<RetryableRequestConfig>): Promise<T> => {
    const request = createRetryableRequest<T>({
      url,
      method: 'post',
      data,
      ...config
    })
    
    return request(extraConfig)
  }
}

/**
 * 创建PUT请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns 请求函数
 */
export function createPutRequest<T = any>(
  url: string,
  config?: Omit<RetryableRequestConfig, 'url' | 'method'>
): (data?: any, extraConfig?: Partial<RetryableRequestConfig>) => Promise<T> {
  return (data?: any, extraConfig?: Partial<RetryableRequestConfig>): Promise<T> => {
    const request = createRetryableRequest<T>({
      url,
      method: 'put',
      data,
      ...config
    })
    
    return request(extraConfig)
  }
}

/**
 * 创建DELETE请求
 * @param url 请求URL
 * @param config 请求配置
 * @returns 请求函数
 */
export function createDeleteRequest<T = any>(
  url: string,
  config?: Omit<RetryableRequestConfig, 'url' | 'method'>
): (params?: any, extraConfig?: Partial<RetryableRequestConfig>) => Promise<T> {
  return (params?: any, extraConfig?: Partial<RetryableRequestConfig>): Promise<T> => {
    const request = createRetryableRequest<T>({
      url,
      method: 'delete',
      params,
      ...config
    })
    
    return request(extraConfig)
  }
}

// 导出默认实例
export default {
  get: <T = any>(url: string, config?: RetryableRequestConfig) => retryableAxios.get<T>(url, config).then(res => res.data),
  post: <T = any>(url: string, data?: any, config?: RetryableRequestConfig) => retryableAxios.post<T>(url, data, config).then(res => res.data),
  put: <T = any>(url: string, data?: any, config?: RetryableRequestConfig) => retryableAxios.put<T>(url, data, config).then(res => res.data),
  delete: <T = any>(url: string, config?: RetryableRequestConfig) => retryableAxios.delete<T>(url, config).then(res => res.data),
  createRetryableRequest,
  createGetRequest,
  createPostRequest,
  createPutRequest,
  createDeleteRequest
}