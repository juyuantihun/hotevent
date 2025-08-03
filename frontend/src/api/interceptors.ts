/**
 * API请求拦截器模块
 * 用于优化网络请求、处理错误和提升用户体验
 * 
 * 包含多种拦截器：
 * - 请求重试拦截器
 * - 网络状态拦截器
 * - 请求合并拦截器
 * - 请求取消拦截器
 * - 性能监控拦截器
 */
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { networkMonitor, adjustRequestConfigByNetwork } from '@/services/networkMonitor'

/**
 * 创建请求重试拦截器
 * 当请求失败时，根据条件自动重试
 * 
 * @param {number} maxRetries 最大重试次数，默认3次
 * @param {number} retryDelay 重试延迟（毫秒），默认1000ms
 * @param {Function} retryCondition 重试条件函数，判断是否需要重试
 * @returns {Function} 请求拦截器函数
 */
export function createRetryInterceptor(
  maxRetries: number = 3,
  retryDelay: number = 1000,
  retryCondition: (error: AxiosError) => boolean = (error) => {
    // 默认重试条件：网络错误或5xx服务器错误
    return (
      !error.response ||
      error.code === 'ECONNABORTED' ||
      (error.response && error.response.status >= 500)
    )
  }
) {
  return (axiosInstance: AxiosInstance) => {
    axiosInstance.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        // 获取请求配置
        const config = error.config as AxiosRequestConfig & { _retryCount?: number }
        
        // 如果没有配置或已达到最大重试次数，则拒绝Promise
        if (!config || !retryCondition(error) || (config._retryCount || 0) >= maxRetries) {
          return Promise.reject(error)
        }
        
        // 增加重试计数
        config._retryCount = (config._retryCount || 0) + 1
        
        // 等待一段时间后重试
        await new Promise(resolve => setTimeout(resolve, retryDelay * config._retryCount))
        
        // 重试请求
        return axiosInstance(config)
      }
    )
    
    return axiosInstance
  }
}

/**
 * 创建网络状态拦截器
 * 根据当前网络状态调整请求配置，优化网络体验
 * 
 * @returns {Function} 请求拦截器函数
 */
export function createNetworkInterceptor() {
  return (axiosInstance: AxiosInstance) => {
    axiosInstance.interceptors.request.use(
      (config) => {
        // 根据网络状态调整请求配置
        const adjustedConfig = adjustRequestConfigByNetwork(config)
        return adjustedConfig
      },
      (error) => Promise.reject(error)
    )
    
    return axiosInstance
  }
}

/**
 * 创建请求合并拦截器
 * 在短时间内合并相同的GET请求，减少重复请求
 * 
 * @param {number} mergeWindow 合并时间窗口（毫秒），默认50ms
 * @returns {Function} 请求拦截器函数
 */
export function createRequestMergeInterceptor(mergeWindow: number = 50) {
  // 存储正在进行的请求
  const pendingRequests = new Map<string, Promise<AxiosResponse>>()
  
  return (axiosInstance: AxiosInstance) => {
    axiosInstance.interceptors.request.use(
      (config) => {
        // 如果是GET请求，尝试合并
        if (config.method?.toLowerCase() === 'get') {
          // 生成请求键
          const requestKey = `${config.url}:${JSON.stringify(config.params || {})}`
          
          // 检查是否有相同的请求正在进行
          const pendingRequest = pendingRequests.get(requestKey)
          if (pendingRequest) {
            // 返回正在进行的请求Promise
            return {
              ...config,
              adapter: () => pendingRequest
            }
          }
          
          // 存储新的请求Promise
          const requestPromise = new Promise<AxiosResponse>((resolve, reject) => {
            // 保存原始适配器
            const originalAdapter = config.adapter
            
            // 设置新的适配器
            config.adapter = async (config) => {
              try {
                // 使用原始适配器发送请求
                const response = await originalAdapter!(config)
                resolve(response)
                return response
              } catch (error) {
                reject(error)
                throw error
              } finally {
                // 请求完成后，从Map中移除
                setTimeout(() => {
                  pendingRequests.delete(requestKey)
                }, mergeWindow)
              }
            }
          })
          
          // 存储请求Promise
          pendingRequests.set(requestKey, requestPromise as Promise<AxiosResponse>)
        }
        
        return config
      },
      (error) => Promise.reject(error)
    )
    
    return axiosInstance
  }
}

/**
 * 创建请求取消拦截器
 * 自动取消重复的请求，避免重复提交和竞态问题
 * 
 * @returns {Function} 请求拦截器函数
 */
export function createCancelInterceptor() {
  // 存储取消令牌
  const cancelTokens = new Map<string, AbortController>()
  
  return (axiosInstance: AxiosInstance) => {
    axiosInstance.interceptors.request.use(
      (config) => {
        // 生成请求键
        const requestKey = `${config.method}:${config.url}:${JSON.stringify(config.params || {})}:${JSON.stringify(config.data || {})}`
        
        // 如果存在相同的请求，取消它
        if (cancelTokens.has(requestKey)) {
          const controller = cancelTokens.get(requestKey)!
          controller.abort()
          cancelTokens.delete(requestKey)
        }
        
        // 创建新的AbortController
        const controller = new AbortController()
        config.signal = controller.signal
        
        // 存储取消令牌
        cancelTokens.set(requestKey, controller)
        
        // 设置请求完成后的清理函数
        const originalAdapter = config.adapter
        config.adapter = async (config) => {
          try {
            const response = await originalAdapter!(config)
            return response
          } finally {
            // 请求完成后，从Map中移除
            cancelTokens.delete(requestKey)
          }
        }
        
        return config
      },
      (error) => Promise.reject(error)
    )
    
    return axiosInstance
  }
}

/**
 * 创建性能监控拦截器
 * 监控API请求的性能指标，记录请求耗时
 * 
 * @returns {Function} 请求拦截器函数
 */
export function createPerformanceInterceptor() {
  return (axiosInstance: AxiosInstance) => {
    axiosInstance.interceptors.request.use(
      (config) => {
        // 记录请求开始时间
        config.metadata = {
          ...config.metadata,
          startTime: Date.now()
        }
        return config
      },
      (error) => Promise.reject(error)
    )
    
    axiosInstance.interceptors.response.use(
      (response) => {
        // 计算请求耗时
        const startTime = response.config.metadata?.startTime
        if (startTime) {
          const endTime = Date.now()
          const duration = endTime - startTime
          
          // 记录请求耗时
          response.config.metadata = {
            ...response.config.metadata,
            duration
          }
          
          // 如果请求耗时超过阈值，记录日志
          if (duration > 1000) {
            console.warn(`[性能] 请求耗时过长: ${duration}ms, URL: ${response.config.url}`)
          }
        }
        
        return response
      },
      (error) => {
        // 计算请求耗时
        const startTime = error.config?.metadata?.startTime
        if (startTime) {
          const endTime = Date.now()
          const duration = endTime - startTime
          
          // 记录请求耗时
          if (error.config) {
            error.config.metadata = {
              ...error.config.metadata,
              duration
            }
          }
        }
        
        return Promise.reject(error)
      }
    )
    
    return axiosInstance
  }
}

import createApiErrorHandler from './errorHandler'

/**
 * 应用所有拦截器
 * 将所有拦截器按顺序应用到Axios实例
 * 
 * @param {AxiosInstance} axiosInstance Axios实例
 * @returns {AxiosInstance} 应用了拦截器的Axios实例
 */
export function applyInterceptors(axiosInstance: AxiosInstance): AxiosInstance {
  // 应用网络状态拦截器
  createNetworkInterceptor()(axiosInstance)
  
  // 应用请求合并拦截器
  createRequestMergeInterceptor()(axiosInstance)
  
  // 应用请求取消拦截器
  createCancelInterceptor()(axiosInstance)
  
  // 应用请求重试拦截器
  createRetryInterceptor()(axiosInstance)
  
  // 应用性能监控拦截器
  createPerformanceInterceptor()(axiosInstance)
  
  // 应用API错误处理拦截器
  createApiErrorHandler(true, false)(axiosInstance)
  
  return axiosInstance
}