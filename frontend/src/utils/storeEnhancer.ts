/**
 * 状态管理增强工具
 * 提供高级状态管理功能，包括状态模块通信、性能优化和调试工具
 */

import { createPinia } from 'pinia'
import { inject, provide, reactive, ref } from 'vue'

// 类型定义
type Store = {
  $id: string;
  $state: Record<string, any>;
  $patch: (partialState: Record<string, any>) => void;
  $options: Record<string, any>;
  $subscribe: (callback: (mutation: any, state: any) => void) => void;
}

type StateTree = Record<string, any>

// 定义Vue App类型
interface App {
  provide: typeof provide;
  [key: string]: any;
}

// 定义插件上下文类型
interface PiniaPluginContext {
  store: Store,
  options: Record<string, any>,
  pinia: ReturnType<typeof createPinia>
}

// 定义注入键类型
type InjectionKey<T> = Symbol

/**
 * 状态变更事件类型
 */
export type StoreChangeEventType = 'update' | 'reset' | 'patch' | 'action'

/**
 * 状态变更事件接口
 */
export interface StoreChangeEvent {
  /** 事件类型 */
  type: StoreChangeEventType
  /** 存储ID */
  storeId: string
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
}

/**
 * 状态事件总线接口
 */
export interface StoreEventBus {
  /** 发布事件 */
  emit(event: StoreChangeEvent): void
  /** 订阅事件 */
  on(callback: (event: StoreChangeEvent) => void): () => void
  /** 获取事件历史 */
  getHistory(): StoreChangeEvent[]
  /** 清除事件历史 */
  clearHistory(): void
}

/**
 * 状态事件总线注入键
 */
export const StoreEventBusKey: InjectionKey<StoreEventBus> = Symbol('StoreEventBus')

/**
 * 创建状态事件总线
 * @param maxHistorySize 最大历史记录数量
 * @returns 状态事件总线实例
 */
export function createStoreEventBus(maxHistorySize = 100): StoreEventBus {
  const listeners = new Set<(event: StoreChangeEvent) => void>()
  const history = ref<StoreChangeEvent[]>([])
  
  const emit = (event: StoreChangeEvent) => {
    // 添加到历史记录
    history.value.push(event)
    
    // 限制历史记录大小
    if (history.value.length > maxHistorySize) {
      history.value = history.value.slice(-maxHistorySize)
    }
    
    // 通知所有监听器
    listeners.forEach(listener => listener(event))
  }
  
  const on = (callback: (event: StoreChangeEvent) => void) => {
    listeners.add(callback)
    
    // 返回取消订阅函数
    return () => {
      listeners.delete(callback)
    }
  }
  
  const getHistory = () => [...history.value]
  
  const clearHistory = () => {
    history.value = []
  }
  
  return {
    emit,
    on,
    getHistory,
    clearHistory
  }
}

/**
 * 安装状态事件总线
 * @param app Vue应用实例
 * @param options 配置选项
 */
export function installStoreEventBus(app: App, options = { maxHistorySize: 100 }) {
  const eventBus = createStoreEventBus(options.maxHistorySize)
  app.provide(StoreEventBusKey, eventBus)
}

/**
 * 使用状态事件总线
 * @returns 状态事件总线实例
 */
export function useStoreEventBus(): StoreEventBus {
  const eventBus = inject(StoreEventBusKey)
  if (!eventBus) {
    throw new Error('StoreEventBus not provided. Did you call installStoreEventBus?')
  }
  return eventBus
}

/**
 * 创建状态事件总线插件
 * @returns Pinia插件
 */
export function createStoreEventBusPlugin() {
  return (context: PiniaPluginContext) => {
    const { store, options } = context
    const storeId = store.$id
    
    // 获取事件总线
    let eventBus: StoreEventBus | undefined
    
    try {
      eventBus = useStoreEventBus()
    } catch (error) {
      console.warn('StoreEventBus not available. Events will not be published.')
    }
    
    // 拦截状态变更
    store.$subscribe((mutation, state) => {
      if (!eventBus) return
      
      const event: StoreChangeEvent = {
        type: mutation.type as StoreChangeEventType,
        storeId,
        path: mutation.type === 'patch object' ? mutation.payload?.key : undefined,
        newValue: mutation.type === 'patch object' ? mutation.payload?.value : undefined,
        timestamp: Date.now()
      }
      
      eventBus.emit(event)
    })
    
    // 拦截动作
    const originalActions = options.actions
    
    if (originalActions && eventBus) {
      Object.keys(originalActions).forEach(actionName => {
        const originalAction = originalActions[actionName]
        
        // @ts-ignore - 重写action以发布事件
        originalActions[actionName] = async function(...args: any[]) {
          // 发布动作开始事件
          eventBus!.emit({
            type: 'action',
            storeId,
            actionName,
            timestamp: Date.now()
          })
          
          try {
            // 执行原始action
            const result = await originalAction.apply(this, args)
            return result
          } catch (error) {
            // 发布错误事件
            eventBus!.emit({
              type: 'action',
              storeId,
              actionName: `${actionName}:error`,
              newValue: error,
              timestamp: Date.now()
            })
            throw error
          }
        }
      })
    }
  }
}

/**
 * 状态模块通信接口
 */
export interface StoreMessenger {
  /** 发送消息到指定存储 */
  sendMessage(targetStoreId: string, type: string, payload?: any): void
  /** 广播消息到所有存储 */
  broadcast(type: string, payload?: any): void
  /** 监听消息 */
  onMessage(type: string, handler: (payload?: any) => void): () => void
}

/**
 * 状态消息接口
 */
interface StoreMessage {
  /** 消息类型 */
  type: string
  /** 发送者存储ID */
  fromStoreId: string
  /** 接收者存储ID */
  toStoreId?: string
  /** 消息负载 */
  payload?: any
  /** 时间戳 */
  timestamp: number
}

/**
 * 状态消息总线注入键
 */
export const StoreMessengerKey: InjectionKey<StoreMessenger> = Symbol('StoreMessenger')

/**
 * 创建状态消息总线
 * @returns 状态消息总线实例
 */
export function createStoreMessenger(): StoreMessenger {
  const listeners = new Map<string, Set<(payload?: any) => void>>()
  
  const sendMessage = (targetStoreId: string, type: string, payload?: any) => {
    const message: StoreMessage = {
      type,
      fromStoreId: 'global',
      toStoreId: targetStoreId,
      payload,
      timestamp: Date.now()
    }
    
    // 通知特定类型的监听器
    if (listeners.has(type)) {
      listeners.get(type)!.forEach(handler => handler(payload))
    }
  }
  
  const broadcast = (type: string, payload?: any) => {
    const message: StoreMessage = {
      type,
      fromStoreId: 'global',
      payload,
      timestamp: Date.now()
    }
    
    // 通知特定类型的监听器
    if (listeners.has(type)) {
      listeners.get(type)!.forEach(handler => handler(payload))
    }
  }
  
  const onMessage = (type: string, handler: (payload?: any) => void) => {
    if (!listeners.has(type)) {
      listeners.set(type, new Set())
    }
    
    listeners.get(type)!.add(handler)
    
    // 返回取消订阅函数
    return () => {
      const typeListeners = listeners.get(type)
      if (typeListeners) {
        typeListeners.delete(handler)
        if (typeListeners.size === 0) {
          listeners.delete(type)
        }
      }
    }
  }
  
  return {
    sendMessage,
    broadcast,
    onMessage
  }
}

/**
 * 安装状态消息总线
 * @param app Vue应用实例
 */
export function installStoreMessenger(app: App) {
  const messenger = createStoreMessenger()
  app.provide(StoreMessengerKey, messenger)
}

/**
 * 使用状态消息总线
 * @returns 状态消息总线实例
 */
export function useStoreMessenger(): StoreMessenger {
  const messenger = inject(StoreMessengerKey)
  if (!messenger) {
    throw new Error('StoreMessenger not provided. Did you call installStoreMessenger?')
  }
  return messenger
}

/**
 * 创建状态消息总线插件
 * @returns Pinia插件
 */
export function createStoreMessengerPlugin() {
  return (context: PiniaPluginContext) => {
    const { store } = context
    const storeId = store.$id
    
    // 获取消息总线
    let messenger: StoreMessenger | undefined
    
    try {
      messenger = useStoreMessenger()
    } catch (error) {
      console.warn('StoreMessenger not available. Messaging will not work.')
      return
    }
    
    // 添加消息发送方法
    store.$sendMessage = (targetStoreId: string, type: string, payload?: any) => {
      if (messenger) {
        messenger.sendMessage(targetStoreId, type, payload)
      }
    }
    
    // 添加广播方法
    store.$broadcast = (type: string, payload?: any) => {
      if (messenger) {
        messenger.broadcast(type, payload)
      }
    }
    
    // 添加消息监听方法
    store.$onMessage = (type: string, handler: (payload?: any) => void) => {
      if (messenger) {
        return messenger.onMessage(type, handler)
      }
      return () => {}
    }
  }
}

/**
 * 创建状态快照插件
 * 用于状态的保存和恢复
 * @returns Pinia插件
 */
export function createStoreSnapshotPlugin() {
  // 存储快照历史
  const snapshots = new Map<string, any[]>()
  const maxSnapshots = 10
  
  return (context: PiniaPluginContext) => {
    const { store } = context
    const storeId = store.$id
    
    // 初始化快照历史
    if (!snapshots.has(storeId)) {
      snapshots.set(storeId, [])
    }
    
    // 添加创建快照方法
    store.$createSnapshot = () => {
      const snapshot = JSON.parse(JSON.stringify(store.$state))
      const storeSnapshots = snapshots.get(storeId)!
      
      // 添加快照到历史
      storeSnapshots.push(snapshot)
      
      // 限制历史大小
      if (storeSnapshots.length > maxSnapshots) {
        storeSnapshots.shift()
      }
      
      return snapshot
    }
    
    // 添加恢复快照方法
    store.$restoreSnapshot = (index: number) => {
      const storeSnapshots = snapshots.get(storeId)!
      
      if (index >= 0 && index < storeSnapshots.length) {
        const snapshot = storeSnapshots[index]
        store.$patch(snapshot)
        return true
      }
      
      return false
    }
    
    // 添加获取快照历史方法
    store.$getSnapshots = () => {
      return [...(snapshots.get(storeId) || [])]
    }
    
    // 添加清除快照历史方法
    store.$clearSnapshots = () => {
      snapshots.set(storeId, [])
    }
  }
}

/**
 * 创建状态锁定插件
 * 防止状态被意外修改
 * @returns Pinia插件
 */
export function createStoreLockPlugin() {
  // 存储锁定状态
  const lockedStores = new Set<string>()
  
  return (context: PiniaPluginContext) => {
    const { store } = context
    const storeId = store.$id
    
    // 添加锁定方法
    store.$lock = () => {
      lockedStores.add(storeId)
    }
    
    // 添加解锁方法
    store.$unlock = () => {
      lockedStores.delete(storeId)
    }
    
    // 添加检查锁定状态方法
    store.$isLocked = () => {
      return lockedStores.has(storeId)
    }
    
    // 拦截状态变更
    const originalPatch = store.$patch
    store.$patch = function(partialStateOrMutator: any) {
      if (lockedStores.has(storeId)) {
        console.warn(`Store "${storeId}" is locked. State changes are prevented.`)
        return
      }
      
      return originalPatch.call(this, partialStateOrMutator)
    }
  }
}

/**
 * 创建状态验证插件
 * 确保状态符合预期的结构和类型
 * @param validators 验证器配置
 * @returns Pinia插件
 */
export function createStoreValidationPlugin(validators: Record<string, (state: any) => boolean | string>) {
  return (context: PiniaPluginContext) => {
    const { store } = context
    const storeId = store.$id
    
    // 如果没有为此存储定义验证器，则跳过
    if (!validators[storeId]) {
      return
    }
    
    // 获取验证器
    const validator = validators[storeId]
    
    // 拦截状态变更
    store.$subscribe((mutation, state) => {
      // 验证状态
      const validationResult = validator(state)
      
      // 如果验证失败
      if (validationResult !== true) {
        const errorMessage = typeof validationResult === 'string' 
          ? validationResult 
          : `Invalid state in store "${storeId}"`
        
        console.error(errorMessage, state)
        
        // 在开发环境中抛出错误
        if (process.env.NODE_ENV === 'development') {
          throw new Error(errorMessage)
        }
      }
    })
  }
}

/**
 * 创建状态持久化增强插件
 * 扩展Pinia的持久化功能
 * @returns Pinia插件
 */
export function createEnhancedPersistPlugin() {
  return (context: PiniaPluginContext) => {
    const { store, options } = context
    
    // 如果没有持久化配置，则跳过
    if (!options.persist) {
      return
    }
    
    // 添加手动持久化方法
    store.$persistNow = () => {
      const persistOptions = options.persist as any
      
      // 获取存储和键
      const storage = persistOptions.storage || localStorage
      const key = persistOptions.key || `${store.$id}-state`
      
      // 获取需要持久化的路径
      const paths = persistOptions.paths || null
      
      // 创建要持久化的状态对象
      let stateToPersist: any
      
      if (paths) {
        stateToPersist = {}
        paths.forEach((path: string) => {
          stateToPersist[path] = store.$state[path]
        })
      } else {
        stateToPersist = { ...store.$state }
      }
      
      // 持久化到存储
      storage.setItem(key, JSON.stringify(stateToPersist))
    }
    
    // 添加清除持久化数据方法
    store.$clearPersisted = () => {
      const persistOptions = options.persist as any
      
      // 获取存储和键
      const storage = persistOptions.storage || localStorage
      const key = persistOptions.key || `${store.$id}-state`
      
      // 从存储中移除
      storage.removeItem(key)
    }
  }
}

/**
 * 创建状态管理增强插件集合
 * 组合多个插件为一个
 * @returns Pinia插件
 */
export function createStoreEnhancerPlugins() {
  return [
    createStoreEventBusPlugin(),
    createStoreMessengerPlugin(),
    createStoreSnapshotPlugin(),
    createStoreLockPlugin(),
    createEnhancedPersistPlugin()
  ]
}