/**
 * 优化的API服务
 * 包含缓存、批处理和错误重试功能
 */
import request from './index'
import { timelineApi } from './timeline'
import { eventApi } from './event'

// 缓存存储
const apiCache = new Map()

// 缓存生存时间（毫秒）
const CACHE_TTL = 60000 // 1分钟

/**
 * 带缓存的API调用
 * @param key 缓存键
 * @param apiCall API调用函数
 * @param ttl 缓存生存时间（毫秒）
 * @param forceRefresh 是否强制刷新缓存
 * @returns API调用结果
 */
const withCache = async (key: string, apiCall: () => Promise<any>, ttl = CACHE_TTL, forceRefresh = false) => {
  // 检查缓存是否有效
  const cached = apiCache.get(key)
  if (!forceRefresh && cached && Date.now() - cached.timestamp < ttl) {
    console.log(`使用缓存数据: ${key}`)
    return cached.data
  }

  // 调用API
  console.log(`请求新数据: ${key}`)
  const data = await apiCall()

  // 更新缓存
  apiCache.set(key, {
    data,
    timestamp: Date.now()
  })

  return data
}

/**
 * 清除指定键的缓存
 * @param key 缓存键
 */
const clearCache = (key: string) => {
  apiCache.delete(key)
}

/**
 * 清除所有缓存
 */
const clearAllCache = () => {
  apiCache.clear()
}

/**
 * 带重试的API调用
 * @param apiCall API调用函数
 * @param retries 重试次数
 * @param delay 重试延迟（毫秒）
 * @returns API调用结果
 */
const withRetry = async (apiCall: () => Promise<any>, retries = 3, delay = 1000) => {
  try {
    return await apiCall()
  } catch (error) {
    if (retries <= 0) {
      throw error
    }

    // 等待指定时间
    await new Promise(resolve => setTimeout(resolve, delay))

    // 重试
    return withRetry(apiCall, retries - 1, delay * 2)
  }
}

// 优化的时间线API
export const optimizedTimelineApi = {
  /**
   * 获取时间线列表（带缓存）
   * @param params 查询参数
   * @param forceRefresh 是否强制刷新缓存
   * @returns 时间线列表
   */
  getTimelineList: (params: any, forceRefresh = false) => {
    const cacheKey = `timeline-list-${JSON.stringify(params)}`
    return withCache(cacheKey, () => timelineApi.getTimelineList(params), CACHE_TTL, forceRefresh)
  },

  /**
   * 获取时间线详情（带缓存）
   * @param id 时间线ID
   * @returns 时间线详情
   */
  getTimelineDetail: (id: string) => {
    const cacheKey = `timeline-detail-${id}`
    return withCache(cacheKey, () => timelineApi.getTimelineDetail(id))
  },

  /**
   * 获取时间线详情（包含事件和关系，带缓存）
   * @param id 时间线ID
   * @returns 时间线详情（包含事件和关系）
   */
  getTimelineWithDetails: (id: string) => {
    const cacheKey = `timeline-details-${id}`
    return withCache(cacheKey, () => timelineApi.getTimelineWithDetails(id))
  },
  
  /**
   * 获取时间线事件（带缓存）
   * @param id 时间线ID
   * @returns 时间线事件列表
   */
  getTimelineEvents: (id: string) => {
    const cacheKey = `timeline-events-${id}`
    return withCache(cacheKey, () => timelineApi.getTimelineEvents(id))
  },

  /**
   * 创建时间线（带重试）
   * @param data 时间线数据
   * @returns 创建结果
   */
  createTimeline: (data: any) => {
    return withRetry(() => timelineApi.createTimeline(data))
  },

  /**
   * 更新时间线（带重试，并清除缓存）
   * @param id 时间线ID
   * @param data 时间线数据
   * @returns 更新结果
   */
  updateTimeline: async (id: string, data: any) => {
    const result = await withRetry(() => timelineApi.updateTimeline(id, data))
    clearCache(`timeline-detail-${id}`)
    clearCache(`timeline-details-${id}`)
    return result
  },

  /**
   * 删除时间线（带重试，并清除缓存）
   * @param id 时间线ID
   * @returns 删除结果
   */
  deleteTimeline: async (id: string) => {
    const result = await withRetry(() => timelineApi.deleteTimeline(id))
    clearCache(`timeline-detail-${id}`)
    clearCache(`timeline-details-${id}`)
    return result
  },

  /**
   * 获取时间线图形数据（带缓存）
   * @param id 时间线ID
   * @returns 图形数据
   */
  getTimelineGraph: (id: string) => {
    const cacheKey = `timeline-graph-${id}`
    return withCache(cacheKey, () => timelineApi.getTimelineGraph(id))
  },

  /**
   * 导出时间线
   * @param id 时间线ID
   * @param format 导出格式
   * @returns 导出结果
   */
  exportTimeline: (id: string, format: string) => {
    return timelineApi.exportTimeline(id, format)
  },

  /**
   * 异步导出时间线
   * @param id 时间线ID
   * @param options 导出选项
   * @returns 导出任务ID
   */
  exportTimelineAsync: (id: string, options: any) => {
    return timelineApi.exportTimelineAsync(id, options)
  },

  /**
   * 获取导出任务状态
   * @param taskId 导出任务ID
   * @returns 任务状态
   */
  getExportTaskStatus: (taskId: string) => {
    return timelineApi.getExportTaskStatus(taskId)
  },

  /**
   * 取消导出任务
   * @param taskId 导出任务ID
   * @returns 取消结果
   */
  cancelExportTask: (taskId: string) => {
    return timelineApi.cancelExportTask(taskId)
  },

  /**
   * 异步生成时间线（带重试）
   * @param data 时间线生成请求数据
   * @returns 生成任务信息
   */
  generateTimelineAsync: (data: any) => {
    return withRetry(() => timelineApi.generateTimelineAsync(data))
  },

  /**
   * 获取时间线生成进度
   * @param id 时间线ID
   * @returns 生成进度信息
   */
  getGenerationProgress: (id: string | number) => {
    // 不缓存进度信息，因为需要实时更新
    return timelineApi.getGenerationProgress(id)
  },

  /**
   * 取消时间线生成（带重试）
   * @param id 时间线ID
   * @returns 取消结果
   */
  cancelGeneration: (id: string | number) => {
    return withRetry(() => timelineApi.cancelGeneration(id))
  },

  /**
   * 清除时间线相关缓存
   */
  clearCache: () => {
    console.log('清除时间线缓存')
    // 清除所有以timeline开头的缓存键
    const keys = Array.from(apiCache.keys()).filter(key => key.startsWith('timeline'))
    console.log('要清除的缓存键:', keys)
    keys.forEach(key => apiCache.delete(key))
    
    // 特别清除时间线列表缓存
    Array.from(apiCache.keys()).forEach(key => {
      if (key.includes('timeline-list')) {
        console.log('清除时间线列表缓存:', key)
        apiCache.delete(key)
      }
    })
  }
}

// 优化的事件API
export const optimizedEventApi = {
  /**
   * 获取事件列表（带缓存）
   * @param params 查询参数
   * @returns 事件列表
   */
  getEventList: (params: any) => {
    const cacheKey = `event-list-${JSON.stringify(params)}`
    return withCache(cacheKey, () => eventApi.getEventList(params))
  },

  /**
   * 获取事件详情（带缓存）
   * @param id 事件ID
   * @returns 事件详情
   */
  getEventDetail: (id: string) => {
    const cacheKey = `event-detail-${id}`
    return withCache(cacheKey, () => eventApi.getEventDetail(id))
  },

  /**
   * 创建事件（带重试）
   * @param data 事件数据
   * @returns 创建结果
   */
  createEvent: (data: any) => {
    return withRetry(() => eventApi.createEvent(data))
  },

  /**
   * 更新事件（带重试，并清除缓存）
   * @param id 事件ID
   * @param data 事件数据
   * @returns 更新结果
   */
  updateEvent: async (id: string, data: any) => {
    const result = await withRetry(() => eventApi.updateEvent(id, data))
    clearCache(`event-detail-${id}`)
    return result
  },

  /**
   * 删除事件（带重试，并清除缓存）
   * @param id 事件ID
   * @returns 删除结果
   */
  deleteEvent: async (id: string) => {
    const result = await withRetry(() => eventApi.deleteEvent(id))
    clearCache(`event-detail-${id}`)
    return result
  },

  /**
   * 批量导入事件（带重试）
   * @param data 事件数据数组
   * @returns 导入结果
   */
  batchImportEvents: (data: any[]) => {
    return withRetry(() => eventApi.batchImportEvents(data))
  },

  /**
   * 获取事件关系（带缓存）
   * @param eventId 事件ID
   * @returns 事件关系
   */
  getEventRelations: (eventId: string) => {
    const cacheKey = `event-relations-${eventId}`
    return withCache(cacheKey, () => eventApi.getEventRelations(eventId))
  },

  /**
   * 添加事件关系（带重试，并清除缓存）
   * @param data 关系数据
   * @returns 添加结果
   */
  addEventRelation: async (data: any) => {
    const result = await withRetry(() => eventApi.addEventRelation(data))
    if (data.sourceId) {
      clearCache(`event-relations-${data.sourceId}`)
    }
    if (data.targetId) {
      clearCache(`event-relations-${data.targetId}`)
    }
    return result
  },

  /**
   * 删除事件关系（带重试，并清除缓存）
   * @param id 关系ID
   * @param sourceId 源事件ID
   * @param targetId 目标事件ID
   * @returns 删除结果
   */
  deleteEventRelation: async (id: string, sourceId?: string, targetId?: string) => {
    const result = await withRetry(() => eventApi.deleteEventRelation(id))
    if (sourceId) {
      clearCache(`event-relations-${sourceId}`)
    }
    if (targetId) {
      clearCache(`event-relations-${targetId}`)
    }
    return result
  },

  /**
   * 清除事件相关缓存
   */
  clearCache: () => {
    const keys = Array.from(apiCache.keys()).filter(key => key.startsWith('event'))
    keys.forEach(key => apiCache.delete(key))
  }
}

// 导出API工具函数
export {
  withCache,
  withRetry,
  clearCache,
  clearAllCache,
  timelineApi,
  eventApi
}