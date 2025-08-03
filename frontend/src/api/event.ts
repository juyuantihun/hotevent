/**
 * 事件相关API
 */
import request from './index'

/**
 * 获取事件列表
 * @param params 查询参数
 * @returns 事件列表
 */
export const getEventList = (params: any) => {
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
export const getStats = () => {
  return request({
    url: '/event/stats',
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
  getStats
}