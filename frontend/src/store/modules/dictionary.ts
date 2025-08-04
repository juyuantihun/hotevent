/**
 * 字典数据状态管理模块
 * 管理系统中使用的各类字典数据
 */
import { defineStore } from 'pinia'
import { computed } from 'vue'
import { getDictionaryByType, getDictionaryTree } from '@/api/dictionary'
import type { Dictionary } from '@/api/dictionary'
import { ElMessage } from 'element-plus'
import { createResettableState } from '@/utils/storeHelpers'

/**
 * 字典类型
 */
export type DictionaryType = 'eventTypes' | 'subjects' | 'objects' | 'relationTypes' | 'locations'

/**
 * 字典状态接口
 * 定义字典模块的状态结构
 */
interface DictionaryState {
  /** 事件类型字典 */
  eventTypes: Dictionary[]
  /** 事件主体字典 */
  subjects: Dictionary[]
  /** 事件客体字典 */
  objects: Dictionary[]
  /** 关系类型字典 */
  relationTypes: Dictionary[]
  /** 地点数据（国家、地区、城市） */
  locations: Dictionary[]
  /** 加载状态 */
  loading: Record<DictionaryType, boolean>
  /** 上次更新时间 */
  lastUpdated: Record<DictionaryType, number | null>
  /** 缓存过期时间（毫秒） */
  cacheExpiration: number
}

/**
 * 字典选项接口
 */
export interface DictionaryOption {
  label: string
  value: string | number
}

/**
 * 字典数据状态管理存储
 * 使用组合式API风格
 */
export const useDictionaryStore = defineStore('dictionary', () => {
  /**
   * 初始状态
   */
  const initialState = (): DictionaryState => ({
    eventTypes: [],
    subjects: [],
    objects: [],
    relationTypes: [],
    locations: [],
    loading: {
      eventTypes: false,
      subjects: false,
      objects: false,
      relationTypes: false,
      locations: false
    },
    lastUpdated: {
      eventTypes: null,
      subjects: null,
      objects: null,
      relationTypes: null,
      locations: null
    },
    cacheExpiration: 60 * 60 * 1000 // 默认1小时
  })
  
  // 创建可重置的状态
  const state = createResettableState<DictionaryState>(initialState)
  
  /**
   * 计算属性
   */
  // 获取事件类型选项
  const eventTypeOptions = computed<DictionaryOption[]>(() => 
    state.eventTypes.map(item => ({
      label: item.name,
      value: item.id
    }))
  )
  
  // 获取事件主体选项
  const subjectOptions = computed<DictionaryOption[]>(() => 
    state.subjects.map(item => ({
      label: item.name,
      value: item.id
    }))
  )
  
  // 获取事件客体选项
  const objectOptions = computed<DictionaryOption[]>(() => 
    state.objects.map(item => ({
      label: item.name,
      value: item.id
    }))
  )
  
  // 获取关系类型选项
  const relationTypeOptions = computed<DictionaryOption[]>(() => 
    state.relationTypes.map(item => ({
      label: item.name,
      value: item.id
    }))
  )
  
  /**
   * 操作方法
   */
  // 设置加载状态
  function setLoading(type: DictionaryType, status: boolean): void {
    state.loading[type] = status
  }
  
  // 更新最后更新时间
  function updateLastUpdated(type: DictionaryType): void {
    state.lastUpdated[type] = Date.now()
  }
  
  // 设置缓存过期时间
  function setCacheExpiration(time: number): void {
    state.cacheExpiration = time
  }
  
  // 检查数据是否需要刷新
  function needsRefresh(type: DictionaryType): boolean {
    const lastUpdate = state.lastUpdated[type]
    if (!lastUpdate) return true
    
    const now = Date.now()
    return now - lastUpdate > state.cacheExpiration
  }
  
  /**
   * 通用字典获取方法
   * @param type 字典类型
   * @param dictType 字典类型参数
   * @param forceRefresh 是否强制刷新
   * @returns 字典数据
   */
  async function fetchDictionary(
    type: DictionaryType, 
    dictType: string, 
    forceRefresh = false
  ): Promise<Dictionary[]> {
    // 如果数据已存在且不需要刷新，则直接返回
    if (state[type].length > 0 && !forceRefresh && !needsRefresh(type)) {
      return state[type]
    }
    
    setLoading(type, true)
    try {
      const response = await getDictionaryByType(dictType)
      const data = response as unknown as Dictionary[]
      state[type] = data || []
      updateLastUpdated(type)
      return state[type]
    } catch (error) {
      console.error(`获取${dictType}字典失败:`, error)
      ElMessage.error(`获取${dictType}字典失败`)
      return []
    } finally {
      setLoading(type, false)
    }
  }
  
  /**
   * 获取事件类型字典
   * @param forceRefresh 是否强制刷新
   */
  async function getEventTypes(forceRefresh = false): Promise<Dictionary[]> {
    return fetchDictionary('eventTypes', '事件类型', forceRefresh)
  }
  
  /**
   * 获取事件主体字典
   * @param forceRefresh 是否强制刷新
   */
  async function getSubjects(forceRefresh = false): Promise<Dictionary[]> {
    return fetchDictionary('subjects', '事件主体', forceRefresh)
  }
  
  /**
   * 获取事件客体字典
   * @param forceRefresh 是否强制刷新
   */
  async function getObjects(forceRefresh = false): Promise<Dictionary[]> {
    return fetchDictionary('objects', '事件客体', forceRefresh)
  }
  
  /**
   * 获取关系类型字典
   * @param forceRefresh 是否强制刷新
   */
  async function getRelationTypes(forceRefresh = false): Promise<Dictionary[]> {
    return fetchDictionary('relationTypes', '关系类型', forceRefresh)
  }
  
  /**
   * 获取地点数据（包含国家、地区、城市）
   * @param forceRefresh 是否强制刷新
   */
  async function fetchLocationTree(forceRefresh = false): Promise<Dictionary[]> {
    // 如果数据已存在且不需要刷新，则直接返回
    if (state.locations.length > 0 && !forceRefresh && !needsRefresh('locations')) {
      return state.locations
    }
    
    setLoading('locations', true)
    try {
      const response = await getDictionaryTree('国家')
      const data = response as unknown as Dictionary[]
      state.locations = data || []
      updateLastUpdated('locations')
      return state.locations
    } catch (error) {
      console.error('获取地点数据失败:', error)
      const errorMessage = error instanceof Error ? error.message : '获取地点数据失败'
      ElMessage.error('获取地点数据失败')
      return []
    } finally {
      setLoading('locations', false)
    }
  }
  
  /**
   * 初始化所有字典数据
   * 用于应用启动时预加载常用字典
   */
  async function initAllDictionaries(): Promise<void> {
    try {
      // 使用Promise.allSettled确保即使部分请求失败也不会影响其他请求
      const results = await Promise.allSettled([
        getEventTypes(),
        getSubjects(),
        getObjects(),
        getRelationTypes()
      ])
      
      // 检查结果
      const failed = results.filter(r => r.status === 'rejected').length
      if (failed > 0) {
        console.warn(`初始化字典数据部分失败: ${failed}/${results.length} 个请求失败`)
      }
    } catch (error) {
      console.error('初始化字典数据失败:', error)
    }
  }
  
  /**
   * 根据ID查找字典项
   * @param type 字典类型
   * @param id 字典项ID
   * @returns 字典项或undefined
   */
  function findDictionaryItem(type: DictionaryType, id: string | number): Dictionary | undefined {
    return state[type].find(item => item.id === id)
  }
  
  /**
   * 根据名称查找字典项
   * @param type 字典类型
   * @param name 字典项名称
   * @returns 字典项或undefined
   */
  function findDictionaryItemByName(type: DictionaryType, name: string): Dictionary | undefined {
    return state[type].find(item => item.name === name)
  }
  
  return {
    // 状态
    ...state,
    
    // 计算属性
    eventTypeOptions,
    subjectOptions,
    objectOptions,
    relationTypeOptions,
    
    // 方法
    setLoading,
    updateLastUpdated,
    setCacheExpiration,
    needsRefresh,
    getEventTypes,
    getSubjects,
    getObjects,
    getRelationTypes,
    fetchLocationTree,
    initAllDictionaries,
    findDictionaryItem,
    findDictionaryItemByName
  }
}, {
  /**
   * 持久化配置
   */
  persist: {
    key: 'dictionary-state',
    storage: localStorage,
    paths: ['eventTypes', 'subjects', 'objects', 'relationTypes', 'locations', 'lastUpdated', 'cacheExpiration']
  }
})