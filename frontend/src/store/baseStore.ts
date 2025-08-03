/**
 * 状态管理基础模块
 * 提供通用的状态管理功能和模式
 */
import { defineStore } from 'pinia'
import { createResettableState } from '@/utils/storeHelpers'

/**
 * 基础状态接口
 * 所有状态模块共享的基本状态结构
 */
export interface BaseState {
  /** 加载状态 */
  loading: boolean | Record<string, boolean>
  /** 错误信息 */
  error: string | null
  /** 上次更新时间 */
  lastUpdated: number | null
}

/**
 * 创建基础状态存储
 * @param id 存储ID
 * @param initialState 初始状态函数
 * @returns 状态存储定义
 */
export function createBaseStore<T extends BaseState>(
  id: string,
  initialState: () => T
) {
  return defineStore(id, () => {
    // 创建可重置的状态
    const state = createResettableState<T>(initialState)
    
    /**
     * 设置加载状态
     * @param status 加载状态
     * @param key 可选的加载键名
     */
    function setLoading(status: boolean, key?: string) {
      if (typeof state.loading === 'boolean' && !key) {
        state.loading = status
      } else if (typeof state.loading === 'object' && key) {
        state.loading = {
          ...state.loading,
          [key]: status
        }
      }
    }
    
    /**
     * 设置错误信息
     * @param error 错误信息
     */
    function setError(error: string | null) {
      state.error = error
    }
    
    /**
     * 更新最后更新时间
     */
    function updateLastUpdated() {
      state.lastUpdated = Date.now()
    }
    
    /**
     * 创建异步操作包装器
     * 自动处理加载状态和错误
     * @param key 加载状态键名
     * @param operation 异步操作函数
     * @returns 包装后的异步函数
     */
    function withAsync<Args extends any[], R>(
      key: string,
      operation: (...args: Args) => Promise<R>
    ) {
      return async (...args: Args): Promise<R> => {
        setLoading(true, key)
        setError(null)
        
        try {
          const result = await operation(...args)
          updateLastUpdated()
          return result
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : '操作失败'
          setError(errorMessage)
          throw error
        } finally {
          setLoading(false, key)
        }
      }
    }
    
    return {
      ...state,
      setLoading,
      setError,
      updateLastUpdated,
      withAsync
    }
  })
}