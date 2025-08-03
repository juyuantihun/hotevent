/**
 * 状态管理辅助工具
 * 提供状态管理相关的工具函数和类型定义
 */

import { ref, watch, isRef } from 'vue'

// 类型定义
type StateTree = Record<string, any>

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
      const currentState = this
      
      // 创建新的状态对象
      const newState: Record<string, any> = {}
      
      // 需要保留的键
      const keysToPreserve = new Set(options.preserveKeys || [])
      
      // 处理每个状态键
      Object.keys(originalState).forEach(key => {
        // 检查是否需要保留此键
        if (keysToPreserve.has(key)) {
          newState[key] = currentState[key]
          return
        }
        
        // 否则使用初始值
        newState[key] = originalState[key]
      })
      
      // 更新状态
      Object.keys(newState).forEach(key => {
        ;(this as any)[key] = newState[key]
      })
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