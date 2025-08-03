/**
 * 状态管理类型定义
 * 为状态管理系统提供类型支持
 */

import { Store } from 'pinia'

/**
 * 重置选项接口
 */
export interface ResetOptions {
  /** 是否保留持久化数据 */
  preservePersisted?: boolean
  /** 需要保留的状态键列表 */
  preserveKeys?: string[]
}

/**
 * 状态调试接口
 */
export interface StoreDebugMethods {
  /** 获取当前存储的日志 */
  getLogs: (filter?: { type?: string; startTime?: number; endTime?: number }) => any[]
  /** 创建当前存储的快照 */
  createSnapshot: () => number
  /** 应用快照到当前存储 */
  applySnapshot: (timestamp: number) => boolean
  /** 获取性能统计 */
  getPerformanceStats: () => Record<string, { avg: number; min: number; max: number }>
}

/**
 * 增强的存储接口
 * 扩展Pinia存储类型
 */
declare module 'pinia' {
  export interface PiniaCustomProperties {
    /**
     * 重置状态到初始值
     * @param options 重置选项
     */
    resetState: (options?: ResetOptions) => void

    /**
     * 批量更新状态
     * @param updates 更新对象
     */
    $batchUpdate: (updates: Record<string, any>) => void

    /**
     * 防抖更新状态
     * @param key 状态键
     * @param value 新值
     * @param delay 延迟时间（毫秒）
     */
    $debouncedUpdate: (key: string, value: any, delay?: number) => void

    /**
     * 发送消息到指定存储
     * @param targetStoreId 目标存储ID
     * @param type 消息类型
     * @param payload 消息负载
     */
    $sendMessage?: (targetStoreId: string, type: string, payload?: any) => void

    /**
     * 广播消息到所有存储
     * @param type 消息类型
     * @param payload 消息负载
     */
    $broadcast?: (type: string, payload?: any) => void

    /**
     * 监听消息
     * @param type 消息类型
     * @param handler 处理函数
     * @returns 取消监听函数
     */
    $onMessage?: (type: string, handler: (payload?: any) => void) => () => void

    /**
     * 创建状态快照
     * @returns 快照对象
     */
    $createSnapshot?: () => any

    /**
     * 恢复状态快照
     * @param index 快照索引
     * @returns 是否成功
     */
    $restoreSnapshot?: (index: number) => boolean

    /**
     * 获取快照历史
     * @returns 快照历史数组
     */
    $getSnapshots?: () => any[]

    /**
     * 清除快照历史
     */
    $clearSnapshots?: () => void

    /**
     * 锁定状态
     * 防止状态被修改
     */
    $lock?: () => void

    /**
     * 解锁状态
     * 允许状态被修改
     */
    $unlock?: () => void

    /**
     * 检查状态是否被锁定
     * @returns 是否锁定
     */
    $isLocked?: () => boolean

    /**
     * 手动触发状态持久化
     */
    $persistNow?: () => void

    /**
     * 清除持久化数据
     */
    $clearPersisted?: () => void

    /**
     * 调试方法
     * 仅在开发环境可用
     */
    $debug?: StoreDebugMethods
  }
}