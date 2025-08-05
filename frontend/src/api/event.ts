/**
 * 事件相关API
 */
import request from './index'

/**
 * 事件数据类型
 */
export interface Event {
  id?: string
  eventCode?: string
  eventTime?: string
  eventType?: string
  subject?: string
  object?: string
  eventLocation?: string
  eventDescription?: string
  relationType?: string
  relationName?: string
  intensityLevel?: number
  keywords?: string[] | string
  latitude?: number
  longitude?: number
  sourceType?: number
  status?: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 事件查询参数类型
 */
export interface EventQuery {
  current?: number
  size?: number
  eventType?: string
  subject?: string
  object?: string
  startTime?: string
  endTime?: string
  sourceType?: string
  sortField?: string
  sortOrder?: string
  keyword?: string
}

/**
 * 分页结果类型
 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/**
 * 统计数据类型
 */
export interface StatsData {
  totalEvents: number
  todayEvents: number
  manualEvents: number
  deepseekEvents: number
}

/**
 * 地理分布统计数据类型
 */
export interface GeographicStatsData {
  countryStats: Array<{
    name: string
    value: number
  }>
  mapData: Array<{
    name: string
    value: [number, number, number]
  }>
  totalCountries: number
  totalEvents: number
}

/**
 * 事件类型分布统计数据类型
 */
export interface EventTypeStatsData {
  typeDistribution: Array<{
    name: string
    value: number
  }>
  totalCount: number
  typeCount: number
}

/**
 * 获取事件列表
 * @param params 查询参数
 * @returns 事件列表
 */
export const getEventList = (params: EventQuery): Promise<PageResult<Event>> => {
  // 使用新路径
  return request({
    url: '/event/list',
    method: 'get',
    params
  })
}

/**
 * 获取事件详情
 * @param id 事件ID
 * @returns 事件详情
 */
export const getEventDetail = (id: string) => {
  return request({
    url: `/event/detail/${id}`,
    method: 'get'
  })
}

/**
 * 创建事件
 * @param data 事件数据
 * @returns 创建结果
 */
export const createEvent = (data: any) => {
  return request({
    url: '/event/create',
    method: 'post',
    data
  })
}

/**
 * 更新事件
 * @param id 事件ID
 * @param data 事件数据
 * @returns 更新结果
 */
export const updateEvent = (id: string, data: any) => {
  // 注意：后端API需要整个事件对象，而不是路径参数
  data.id = id;
  return request({
    url: '/event/update',
    method: 'put',
    data
  })
}

/**
 * 删除事件
 * @param id 事件ID
 * @returns 删除结果
 */
export const deleteEvent = (id: string) => {
  return request({
    url: `/event/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 批量导入事件
 * @param data 事件数据数组
 * @returns 导入结果
 */
export const batchImportEvents = (data: any[]) => {
  return request({
    url: '/event/batch',
    method: 'post',
    data
  })
}

/**
 * 批量创建事件（别名，与batchImportEvents功能相同）
 * @param data 事件数据数组
 * @returns 创建结果
 */
export const createEventsBatch = batchImportEvents

/**
 * 获取事件关系
 * @param eventId 事件ID
 * @returns 事件关系
 */
export const getEventRelations = (eventId: string) => {
  return request({
    url: `/event/relation/graph/${eventId}`,
    method: 'get'
  })
}

/**
 * 添加事件关系
 * @param data 关系数据
 * @returns 添加结果
 */
export const addEventRelation = (data: any) => {
  return request({
    url: '/relations',
    method: 'post',
    data
  })
}

/**
 * 删除事件关系
 * @param id 关系ID
 * @returns 删除结果
 */
export const deleteEventRelation = (id: string) => {
  return request({
    url: `/relations/${id}`,
    method: 'delete'
  })
}

/**
 * 导出所有事件
 * @param format 导出格式
 * @returns 导出结果
 */
export const exportAllEvents = (format: string = 'json') => {
  return request({
    url: '/event/export',
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

/**
 * 获取统计数据
 * @returns 统计数据
 */
export const getStats = (): Promise<StatsData> => {
  return request({
    url: '/event/stats',
    method: 'get'
  })
}

/**
 * 获取地理分布统计数据
 * @returns 地理分布统计数据
 */
export const getGeographicStats = (): Promise<GeographicStatsData> => {
  return request({
    url: '/event/geographic-stats',
    method: 'get'
  })
}

/**
 * 获取事件类型分布统计数据
 * @returns 事件类型分布统计数据
 */
export const getEventTypeStats = (): Promise<EventTypeStatsData> => {
  return request({
    url: '/event/type-stats',
    method: 'get'
  })
}

// 导出事件API对象
export const eventApi = {
  getEventList,
  getEventDetail,
  createEvent,
  updateEvent,
  deleteEvent,
  batchImportEvents,
  createEventsBatch,
  getEventRelations,
  addEventRelation,
  deleteEventRelation,
  exportAllEvents,
  getStats,
  getGeographicStats,
  getEventTypeStats
}