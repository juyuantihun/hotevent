/**
 * 状态管理调试工具
 * 提供开发环境下的状态监控和调试功能
 */

import { reactive } from 'vue'
import { useStoreEventBus, type StoreChangeEvent } from './storeEnhancer.fixed'

// 类型定义
interface App {
  provide: Function;
  [key: string]: any;
}

// 定义插件上下文类型
interface PiniaPluginContext {
  store: any,
  options: Record<string, any>,
  pinia: any
}

/**
 * 状态变更日志接口
 */
export interface StateChangeLog {
  /** 存储ID */
  storeId: string
  /** 事件类型 */
  type: string
  /** 变更路径 */
  path?: string
  /** 变更前的值 */
  oldValue?: any
  /** 变更后的值 */
  newValue?: any
  /** 触发变更的动作名称 */
  actionName?: string
  /** 时间戳 */
  timestamp: number
  /** 堆栈跟踪 */
  stack?: string
}

/**
 * 状态调试器配置接口
 */
export interface StoreDebuggerOptions {
  /** 是否启用 */
  enabled: boolean
  /** 最大日志数量 */
  maxLogs: number
  /** 是否记录堆栈跟踪 */
  captureStack: boolean
  /** 是否记录到控制台 */
  logToConsole: boolean
  /** 是否记录到本地存储 */
  logToStorage: boolean
  /** 本地存储键 */
  storageKey: string
  /** 是否启用时间旅行 */
  enableTimeTravel: boolean
  /** 是否监控性能 */
  monitorPerformance: boolean
  /** 性能警告阈值（毫秒） */
  performanceThreshold: number
}

/**
 * 状态调试器类
 */
export class StoreDebugger {
  /** 配置选项 */
  private options: StoreDebuggerOptions
  /** 变更日志 */
  private logs: StateChangeLog[] = []
  /** 状态快照 */
  private snapshots: Map<number, Record<string, any>> = new Map()
  /** 性能记录 */
  private performanceRecords: Record<string, number[]> = {}
  /** 是否已初始化 */
  private initialized = false
  /** 全局状态 */
  public state = reactive({
    isRecording: true,
    isPaused: false,
    filterStoreId: '',
    filterType: '',
    currentLogIndex: -1
  })

  /**
   * 构造函数
   * @param options 配置选项
   */
  constructor(options?: Partial<StoreDebuggerOptions>) {
    this.options = {
      enabled: import.meta.env.DEV,
      maxLogs: 100,
      captureStack: true,
      logToConsole: false,
      logToStorage: false,
      storageKey: 'store-debugger-logs',
      enableTimeTravel: true,
      monitorPerformance: true,
      performanceThreshold: 50,
      ...options
    }

    // 从本地存储加载日志
    if (this.options.logToStorage) {
      this.loadLogsFromStorage()
    }

    // 添加全局访问点（仅在开发环境）
    if (import.meta.env.DEV) {
      ;(window as any).__STORE_DEBUGGER__ = this
    }
  }

  /**
   * 初始化调试器
   * @param app Vue应用实例
   */
  public init(app: App): void {
    if (this.initialized || !this.options.enabled) {
      return
    }

    try {
      // 获取事件总线
      const eventBus = useStoreEventBus()

      // 订阅状态变更事件
      eventBus.on(this.handleStoreEvent.bind(this))

      this.initialized = true
      console.info('状态调试器已初始化')
    } catch (error) {
      console.warn('状态调试器初始化失败:', error)
    }
  }

  /**
   * 处理状态变更事件
   * @param event 状态变更事件
   */
  private handleStoreEvent(event: StoreChangeEvent): void {
    if (!this.options.enabled || this.state.isPaused) {
      return
    }

    // 创建日志条目
    const log: StateChangeLog = {
      storeId: event.storeId,
      type: event.type,
      path: event.path,
      oldValue: event.oldValue,
      newValue: event.newValue,
      actionName: event.actionName,
      timestamp: event.timestamp
    }

    // 捕获堆栈跟踪
    if (this.options.captureStack) {
      const error = new Error()
      log.stack = error.stack?.split('\n').slice(2).join('\n')
    }

    // 添加到日志
    this.addLog(log)

    // 记录到控制台
    if (this.options.logToConsole) {
      this.logToConsole(log)
    }

    // 记录到本地存储
    if (this.options.logToStorage) {
      this.saveLogsToStorage()
    }

    // 记录性能
    if (this.options.monitorPerformance && event.type === 'action') {
      this.recordPerformance(event.storeId, event.actionName || 'unknown')
    }
  }

  /**
   * 添加日志
   * @param log 日志条目
   */
  private addLog(log: StateChangeLog): void {
    this.logs.push(log)

    // 限制日志数量
    if (this.logs.length > this.options.maxLogs) {
      this.logs.shift()
    }

    // 更新当前日志索引
    this.state.currentLogIndex = this.logs.length - 1
  }

  /**
   * 记录到控制台
   * @param log 日志条目
   */
  private logToConsole(log: StateChangeLog): void {
    const { storeId, type, path, oldValue, newValue, actionName } = log
    const timestamp = new Date(log.timestamp).toLocaleTimeString()

    console.groupCollapsed(
      `%c状态变更: ${storeId} %c${type}${actionName ? ` (${actionName})` : ''}`,
      'color: #9E9E9E; font-weight: bold',
      'color: #03A9F4; font-weight: bold'
    )
    console.log('时间:', timestamp)
    if (path) console.log('路径:', path)
    if (oldValue !== undefined) console.log('旧值:', oldValue)
    if (newValue !== undefined) console.log('新值:', newValue)
    if (log.stack) console.log('堆栈:', log.stack)
    console.groupEnd()
  }

  /**
   * 保存日志到本地存储
   */
  private saveLogsToStorage(): void {
    try {
      localStorage.setItem(this.options.storageKey, JSON.stringify(this.logs))
    } catch (error) {
      console.warn('保存日志到本地存储失败:', error)
    }
  }

  /**
   * 从本地存储加载日志
   */
  private loadLogsFromStorage(): void {
    try {
      const storedLogs = localStorage.getItem(this.options.storageKey)
      if (storedLogs) {
        this.logs = JSON.parse(storedLogs)
        this.state.currentLogIndex = this.logs.length - 1
      }
    } catch (error) {
      console.warn('从本地存储加载日志失败:', error)
    }
  }

  /**
   * 记录性能
   * @param storeId 存储ID
   * @param actionName 动作名称
   */
  private recordPerformance(storeId: string, actionName: string): void {
    const key = `${storeId}.${actionName}`
    const startTime = performance.now()

    // 创建性能记录
    if (!this.performanceRecords[key]) {
      this.performanceRecords[key] = []
    }

    // 使用setTimeout模拟动作完成后的测量
    setTimeout(() => {
      const duration = performance.now() - startTime
      this.performanceRecords[key].push(duration)

      // 限制记录数量
      if (this.performanceRecords[key].length > 10) {
        this.performanceRecords[key].shift()
      }

      // 检查性能问题
      if (duration > this.options.performanceThreshold) {
        console.warn(
          `性能警告: ${key} 执行时间 ${duration.toFixed(2)}ms 超过阈值 ${
            this.options.performanceThreshold
          }ms`
        )
      }
    }, 0)
  }

  /**
   * 创建状态快照
   * @param stores 存储对象
   */
  public createSnapshot(stores: Record<string, any>): number {
    const timestamp = Date.now()
    const snapshot: Record<string, any> = {}

    // 复制每个存储的状态
    Object.entries(stores).forEach(([id, store]) => {
      snapshot[id] = JSON.parse(JSON.stringify(store.$state))
    })

    // 保存快照
    this.snapshots.set(timestamp, snapshot)

    // 限制快照数量
    if (this.snapshots.size > 10) {
      const oldestKey = Array.from(this.snapshots.keys())[0]
      this.snapshots.delete(oldestKey)
    }

    return timestamp
  }

  /**
   * 应用快照
   * @param timestamp 快照时间戳
   * @param stores 存储对象
   */
  public applySnapshot(timestamp: number, stores: Record<string, any>): boolean {
    const snapshot = this.snapshots.get(timestamp)
    if (!snapshot) {
      return false
    }

    // 应用快照到每个存储
    Object.entries(snapshot).forEach(([id, state]) => {
      if (stores[id]) {
        stores[id].$patch(state)
      }
    })

    return true
  }

  /**
   * 清除日志
   */
  public clearLogs(): void {
    this.logs = []
    this.state.currentLogIndex = -1

    if (this.options.logToStorage) {
      localStorage.removeItem(this.options.storageKey)
    }
  }

  /**
   * 暂停记录
   */
  public pause(): void {
    this.state.isPaused = true
  }

  /**
   * 恢复记录
   */
  public resume(): void {
    this.state.isPaused = false
  }

  /**
   * 获取日志
   * @param filter 过滤条件
   */
  public getLogs(filter?: {
    storeId?: string
    type?: string
    startTime?: number
    endTime?: number
  }): StateChangeLog[] {
    if (!filter) {
      return [...this.logs]
    }

    return this.logs.filter(log => {
      if (filter.storeId && log.storeId !== filter.storeId) {
        return false
      }
      if (filter.type && log.type !== filter.type) {
        return false
      }
      if (filter.startTime && log.timestamp < filter.startTime) {
        return false
      }
      if (filter.endTime && log.timestamp > filter.endTime) {
        return false
      }
      return true
    })
  }

  /**
   * 获取性能统计
   */
  public getPerformanceStats(): Record<string, { avg: number; min: number; max: number }> {
    const stats: Record<string, { avg: number; min: number; max: number }> = {}

    Object.entries(this.performanceRecords).forEach(([key, durations]) => {
      if (durations.length === 0) {
        return
      }

      const sum = durations.reduce((acc, val) => acc + val, 0)
      const avg = sum / durations.length
      const min = Math.min(...durations)
      const max = Math.max(...durations)

      stats[key] = { avg, min, max }
    })

    return stats
  }

  /**
   * 导出日志
   */
  public exportLogs(): string {
    return JSON.stringify({
      logs: this.logs,
      performanceStats: this.getPerformanceStats(),
      timestamp: Date.now()
    })
  }

  /**
   * 导入日志
   * @param data 日志数据
   */
  public importLogs(data: string): boolean {
    try {
      const parsed = JSON.parse(data)
      if (Array.isArray(parsed.logs)) {
        this.logs = parsed.logs
        this.state.currentLogIndex = this.logs.length - 1
        return true
      }
      return false
    } catch (error) {
      console.error('导入日志失败:', error)
      return false
    }
  }
}

/**
 * 全局状态调试器实例
 */
export const storeDebugger = new StoreDebugger()

/**
 * 创建状态调试器插件
 * @returns Pinia插件
 */
export function createStoreDebuggerPlugin() {
  return (context: PiniaPluginContext) => {
    const { store } = context
    const storeId = store.$id

    // 添加调试方法
    store.$debug = {
      // 获取当前存储的日志
      getLogs: (filter?: { type?: string; startTime?: number; endTime?: number }) => {
        return storeDebugger.getLogs({
          storeId,
          ...filter
        })
      },

      // 创建当前存储的快照
      createSnapshot: () => {
        return storeDebugger.createSnapshot({ [storeId]: store })
      },

      // 应用快照到当前存储
      applySnapshot: (timestamp: number) => {
        return storeDebugger.applySnapshot(timestamp, { [storeId]: store })
      },

      // 获取性能统计
      getPerformanceStats: () => {
        const allStats = storeDebugger.getPerformanceStats()
        const storeStats: Record<string, { avg: number; min: number; max: number }> = {}

        Object.entries(allStats).forEach(([key, stats]) => {
          if (key.startsWith(`${storeId}.`)) {
            const actionName = key.substring(storeId.length + 1)
            storeStats[actionName] = stats
          }
        })

        return storeStats
      }
    }
  }
}

/**
 * 初始化状态调试器
 * @param app Vue应用实例
 */
export function initializeStoreDebugger(app: App): void {
  if (import.meta.env.DEV) {
    storeDebugger.init(app)
  }
}