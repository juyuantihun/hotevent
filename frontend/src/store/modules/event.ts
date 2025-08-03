/**
 * 事件状态管理模块
 * 管理事件列表和详情数据
 */
import { defineStore } from 'pinia'
import { computed } from 'vue'
import type { Event, EventQuery, EventResponse } from '@/api/event'
import { getEventList, getEventDetail, createEvent, updateEvent, deleteEvent } from '@/api/event'
import { ElMessage } from 'element-plus'
import { createResettableState } from '@/utils/storeHelpers'

/**
 * 事件状态接口
 * 定义事件模块的状态结构
 */
interface EventState {
  /** 事件列表 */
  eventList: Event[];
  /** 当前选中的事件 */
  currentEvent: Event | null;
  /** 加载状态 */
  loading: Record<string, boolean>;
  /** 总记录数 */
  total: number;
  /** 查询参数 */
  query: EventQuery;
  /** 错误信息 */
  error: string | null;
  /** 上次更新时间 */
  lastUpdated: number | null;
}

/**
 * 事件状态管理存储
 * 使用组合式API风格
 */
export const useEventStore = defineStore('event', () => {
  /**
   * 初始状态
   */
  const initialState = (): EventState => ({
    eventList: [],
    currentEvent: null,
    loading: {
      list: false,
      detail: false,
      create: false,
      update: false,
      delete: false
    },
    total: 0,
    query: {
      current: 1,
      size: 10
    },
    error: null,
    lastUpdated: null
  })
  
  // 创建可重置的状态
  const state = createResettableState<EventState>(initialState)
  
  /**
   * 计算属性
   */
  // 是否有事件数据
  const hasEvents = computed(() => state.eventList.length > 0)
  
  // 是否有选中的事件
  const hasCurrentEvent = computed(() => state.currentEvent !== null)
  
  // 获取分页信息
  const pagination = computed(() => ({
    current: state.query.current || 1,
    size: state.query.size || 10,
    total: state.total
  }))
  
  // 获取事件ID列表
  const eventIds = computed(() => state.eventList.map(event => event.id))
  
  // 是否正在加载列表
  const isLoadingList = computed(() => state.loading.list)
  
  // 是否正在提交
  const isSubmitting = computed(() => state.loading.create || state.loading.update)
  
  // 是否正在删除
  const isDeleting = computed(() => state.loading.delete)
  
  /**
   * 操作方法
   */
  // 设置查询参数
  function setQuery(params: Partial<EventQuery>): void {
    state.query = { ...state.query, ...params }
  }
  
  // 设置错误信息
  function setError(error: string | null): void {
    state.error = error
  }
  
  // 设置加载状态
  function setLoading(key: keyof EventState['loading'], status: boolean): void {
    state.loading[key] = status
  }
  
  // 更新最后更新时间
  function updateLastUpdated(): void {
    state.lastUpdated = Date.now()
  }
  
  /**
   * 获取事件列表
   * @param params 查询参数
   */
  async function fetchEventList(params?: Partial<EventQuery>): Promise<EventResponse> {
    setLoading('list', true)
    setError(null)
    
    try {
      // 合并查询参数
      const queryParams = { ...state.query, ...params }
      setQuery(queryParams)
      
      // 调用API获取事件列表
      const response = await getEventList(queryParams)
      const data = response as unknown as EventResponse
      
      // 更新状态
      state.eventList = data.records || []
      state.total = data.total || 0
      updateLastUpdated()
      
      return data
    } catch (error) {
      console.error('获取事件列表失败:', error)
      const errorMessage = error instanceof Error ? error.message : '获取事件列表失败'
      setError(errorMessage)
      ElMessage.error('获取事件列表失败')
      throw error
    } finally {
      setLoading('list', false)
    }
  }
  
  /**
   * 获取事件详情
   * @param id 事件ID
   */
  async function fetchEventDetail(id: number): Promise<Event> {
    setLoading('detail', true)
    setError(null)
    
    try {
      // 调用API获取事件详情
      const response = await getEventDetail(id)
      const data = response as unknown as Event
      
      // 更新状态
      state.currentEvent = data
      updateLastUpdated()
      
      return data
    } catch (error) {
      console.error('获取事件详情失败:', error)
      const errorMessage = error instanceof Error ? error.message : '获取事件详情失败'
      setError(errorMessage)
      ElMessage.error('获取事件详情失败')
      throw error
    } finally {
      setLoading('detail', false)
    }
  }
  
  /**
   * 创建事件
   * @param eventData 事件数据
   */
  async function createEventAction(eventData: Partial<Event>): Promise<Event> {
    setLoading('create', true)
    setError(null)
    
    try {
      // 调用API创建事件
      const response = await createEvent(eventData)
      const data = response as unknown as Event
      
      // 更新状态
      ElMessage.success('创建事件成功')
      updateLastUpdated()
      
      // 刷新列表
      await fetchEventList()
      
      return data
    } catch (error) {
      console.error('创建事件失败:', error)
      const errorMessage = error instanceof Error ? error.message : '创建事件失败'
      setError(errorMessage)
      ElMessage.error('创建事件失败')
      throw error
    } finally {
      setLoading('create', false)
    }
  }
  
  /**
   * 更新事件
   * @param id 事件ID
   * @param eventData 事件数据
   */
  async function updateEventAction(id: number, eventData: Partial<Event>): Promise<Event> {
    setLoading('update', true)
    setError(null)
    
    try {
      // 调用API更新事件
      const response = await updateEvent(id, eventData)
      const data = response as unknown as Event
      
      // 更新状态
      if (state.currentEvent && state.currentEvent.id === id) {
        state.currentEvent = data
      }
      
      // 更新列表中的事件
      const index = state.eventList.findIndex(item => item.id === id)
      if (index !== -1) {
        state.eventList[index] = data
      }
      
      ElMessage.success('更新事件成功')
      updateLastUpdated()
      
      return data
    } catch (error) {
      console.error('更新事件失败:', error)
      const errorMessage = error instanceof Error ? error.message : '更新事件失败'
      setError(errorMessage)
      ElMessage.error('更新事件失败')
      throw error
    } finally {
      setLoading('update', false)
    }
  }
  
  /**
   * 删除事件
   * @param id 事件ID
   */
  async function deleteEventAction(id: number): Promise<void> {
    setLoading('delete', true)
    setError(null)
    
    try {
      // 调用API删除事件
      await deleteEvent(id)
      
      // 更新状态
      if (state.currentEvent && state.currentEvent.id === id) {
        state.currentEvent = null
      }
      
      // 从列表中移除事件
      state.eventList = state.eventList.filter(item => item.id !== id)
      
      // 如果删除后列表为空且不是第一页，则返回上一页
      if (state.eventList.length === 0 && state.query.current && state.query.current > 1) {
        setQuery({ current: state.query.current - 1 })
        await fetchEventList()
      }
      
      ElMessage.success('删除事件成功')
      updateLastUpdated()
    } catch (error) {
      console.error('删除事件失败:', error)
      const errorMessage = error instanceof Error ? error.message : '删除事件失败'
      setError(errorMessage)
      ElMessage.error('删除事件失败')
      throw error
    } finally {
      setLoading('delete', false)
    }
  }
  
  /**
   * 批量更新事件
   * @param events 事件数据数组
   */
  async function batchUpdateEvents(events: Partial<Event>[]): Promise<void> {
    if (!events.length) return
    
    setLoading('update', true)
    setError(null)
    
    try {
      // 并行处理所有更新请求
      const updatePromises = events.map(event => {
        if (!event.id) throw new Error('事件ID不能为空')
        return updateEvent(event.id, event)
      })
      
      const results = await Promise.allSettled(updatePromises)
      
      // 处理结果
      const succeeded = results.filter(r => r.status === 'fulfilled').length
      const failed = results.filter(r => r.status === 'rejected').length
      
      if (failed > 0) {
        ElMessage.warning(`批量更新完成，成功: ${succeeded}，失败: ${failed}`)
      } else {
        ElMessage.success('批量更新成功')
      }
      
      // 刷新列表
      await fetchEventList()
      updateLastUpdated()
    } catch (error) {
      console.error('批量更新事件失败:', error)
      const errorMessage = error instanceof Error ? error.message : '批量更新事件失败'
      setError(errorMessage)
      ElMessage.error('批量更新事件失败')
      throw error
    } finally {
      setLoading('update', false)
    }
  }
  
  return {
    // 状态
    ...state,
    
    // 计算属性
    hasEvents,
    hasCurrentEvent,
    pagination,
    eventIds,
    isLoadingList,
    isSubmitting,
    isDeleting,
    
    // 方法
    setQuery,
    setError,
    setLoading,
    updateLastUpdated,
    fetchEventList,
    fetchEventDetail,
    createEventAction,
    updateEventAction,
    deleteEventAction,
    batchUpdateEvents
  }
}, {
  /**
   * 持久化配置
   */
  persist: {
    key: 'event-state',
    storage: sessionStorage, // 使用会话存储，避免长期缓存可能变化的数据
    paths: ['query'] // 只持久化查询参数
  }
})