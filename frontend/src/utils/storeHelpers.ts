/**
 * 状态管理辅助工具
 * 提供状态管理相关的工具函数和类型定义
 */

import { createPinia } from 'pinia'
import { ref, watch, isRef } from 'vue'

// 类型定义
type Store = {
  $id: string;
  $state: Record<string, any>;
  $patch: (partialState: Record<string, any>) => void;
  $options: Record<string, any>;
  $subscribe: (callback: (mutation: any, state: any) => void) => void;
}

type StateTree = Record<string, any>

type Ref<T> = { value: T }

// 定义插件上下文类型
interface PiniaPluginContext {
  store: Store,
  options: Record<string, any>,
  pinia: ReturnType<typeof createPinia>
}

/**
 * 状态重置选项接口
 */
export interface ResetOptions {
  /** 是否保留持久化数据 */
  preservePersisted?: boolean
  /** 需要保留的状态键列表 */
  preserveKeys?: string[]
}

/**
 * 创建可重置的状态
 * @param initialState 初始状态函数
 * @returns 带有重置功能的状态对象
 */
export function createResettableState<T extends StateTree>(initialState: () => T) {
  // 保存初始状态的副本
  const originalState = initialState()
  
  return {
    ...initialState(),
    /**
     * 重置状态到初始值
     * @param options 重置选项
     */
    resetState(options: ResetOptions = {}) {
      const store = this as Store
      const storeId = store.$id
      
      // 获取当前状态
      const currentState = store.$state
      
      // 创建新的状态对象
      const newState: Record<string, any> = {}
      
      // 获取持久化配置
      const persistOptions = (store.$options.persist as any)
      
      // 需要保留的键
      const keysToPreserve = new Set(options.preserveKeys || [])
      
      // 处理每个状态键
      Object.keys(originalState).forEach(key => {
        // 检查是否需要保留此键
        if (keysToPreserve.has(key)) {
          newState[key] = currentState[key]
          return
        }
        
        // 检查是否需要保留持久化数据
        if (options.preservePersisted && persistOptions) {
          // 检查此键是否在持久化路径中
          const shouldPreserve = Array.isArray(persistOptions.paths) 
            ? persistOptions.paths.includes(key)
            : true // 如果没有指定paths，则所有状态都会被持久化
          
          if (shouldPreserve) {
            newState[key] = currentState[key]
            return
          }
        }
        
        // 否则使用初始值
        newState[key] = originalState[key]
      })
      
      // 更新状态
      store.$patch(newState)
    }
  }
}

/**
 * 创建防抖状态
 * 用于减少状态频繁更新导致的性能问题
 * @param initialValue 初始值
 * @param delay 防抖延迟时间（毫秒）
 * @returns 防抖状态对象
 */
export function createDebouncedState<T>(initialValue: T, delay = 300): {
  value: any
  debouncedValue: any
  flush: () => void
} {
  const value = isRef(initialValue) ? initialValue : ref(initialValue)
  const debouncedValue = ref(value.value)
  
  let timeout: number | null = null
  
  // 监听原始值的变化
  watch(value, (newValue) => {
    if (timeout) {
      clearTimeout(timeout)
    }
    
    timeout = window.setTimeout(() => {
      debouncedValue.value = newValue
      timeout = null
    }, delay)
  })
  
  // 立即同步值的方法
  const flush = () => {
    if (timeout) {
      clearTimeout(timeout)
      timeout = null
    }
    debouncedValue.value = value.value
  }
  
  return { value, debouncedValue, flush }
}

/**
 * 创建批量更新插件
 * 用于优化多个状态同时更新的性能
 * @returns Pinia插件
 */
export function createBatchUpdatePlugin() {
  return (context: PiniaPluginContext) => {
    const { store } = context
    
    // 添加批量更新方法
    store.$batchUpdate = (updates: Record<string, any>) => {
      store.$patch(updates)
    }
    
    // 添加防抖更新方法
    let batchTimeout: number | null = null
    const pendingUpdates: Record<string, any> = {}
    
    store.$debouncedUpdate = (key: string, value: any, delay = 300) => {
      pendingUpdates[key] = value
      
      if (batchTimeout) {
        clearTimeout(batchTimeout)
      }
      
      batchTimeout = window.setTimeout(() => {
        store.$patch(pendingUpdates)
        Object.keys(pendingUpdates).forEach(k => delete pendingUpdates[k])
        batchTimeout = null
      }, delay)
    }
  }
}

/**
 * 创建状态监控插件
 * 用于监控状态变化和性能问题
 * @returns Pinia插件
 */
export function createStateMonitorPlugin() {
  return (context: PiniaPluginContext) => {
    const { store, options } = context
    const storeId = store.$id
    
    // 在开发环境下监控状态变化
    if (process.env.NODE_ENV === 'development') {
      store.$subscribe((mutation, state) => {
        console.log(`[Store: ${storeId}] 状态更新:`, mutation.type, mutation.payload)
      })
    }
    
    // 添加性能监控
    const originalActions = options.actions
    
    if (originalActions) {
      Object.keys(originalActions).forEach(actionName => {
        const originalAction = originalActions[actionName]
        
        // @ts-ignore - 重写action以添加性能监控
        originalActions[actionName] = async function(...args: any[]) {
          const startTime = performance.now()
          
          try {
            // 执行原始action
            const result = await originalAction.apply(this, args)
            
            // 记录执行时间
            const executionTime = performance.now() - startTime
            
            // 如果执行时间过长，记录警告
            if (executionTime > 100) {
              console.warn(`[性能警告] Store action ${storeId}.${actionName} 执行时间: ${executionTime.toFixed(2)}ms`)
            }
            
            return result
          } catch (error) {
            console.error(`[Store Error] ${storeId}.${actionName}:`, error)
            throw error
          }
        }
      })
    }
  }
}