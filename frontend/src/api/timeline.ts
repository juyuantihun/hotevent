/**
 * 时间线相关API
 */
import request from './index'

/**
 * 获取时间线列表
 * @param params 查询参数
 * @returns 时间线列表
 */
export const getTimelineList = (params: any) => {
  return request({
    url: '/timelines',
    method: 'get',
    params
  })
}

/**
 * 获取时间线详情
 * @param id 时间线ID
 * @returns 时间线详情
 */
export const getTimelineDetail = (id: string) => {
  return request({
    url: `/timelines/${id}`,
    method: 'get'
  })
}

/**
 * 获取时间线详情（包含事件和关系）
 * @param id 时间线ID
 * @returns 时间线详情（包含事件和关系）
 */
export const getTimelineWithDetails = (id: string) => {
  return request({
    url: `/timelines/${id}/details`,
    method: 'get'
  })
}

/**
 * 获取时间线事件
 * @param id 时间线ID
 * @returns 时间线事件列表
 */
export const getTimelineEvents = (id: string) => {
  return request({
    url: `/timelines/${id}/events`,
    method: 'get'
  })
}

/**
 * 创建时间线
 * @param data 时间线数据
 * @returns 创建结果
 */
export const createTimeline = (data: any) => {
  return request({
    url: '/timelines',
    method: 'post',
    data
  })
}

/**
 * 异步生成时间线
 * @param data 时间线生成请求数据
 * @returns 生成任务信息
 */
export const generateTimelineAsync = (data: any) => {
  return request({
    url: '/timelines/generate/async',
    method: 'post',
    data
  })
}

/**
 * 获取时间线生成进度
 * @param id 时间线ID
 * @returns 生成进度信息
 */
export const getGenerationProgress = (id: string | number) => {
  return request({
    url: `/timelines/${id}/generation-progress`,
    method: 'get'
  })
}

/**
 * 取消时间线生成
 * @param id 时间线ID
 * @returns 取消结果
 */
export const cancelGeneration = (id: string | number) => {
  return request({
    url: `/timelines/${id}/cancel-generation`,
    method: 'post'
  })
}

/**
 * 更新时间线
 * @param id 时间线ID
 * @param data 时间线数据
 * @returns 更新结果
 */
export const updateTimeline = (id: string, data: any) => {
  return request({
    url: `/timelines/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除时间线
 * @param id 时间线ID
 * @returns 删除结果
 */
export const deleteTimeline = (id: string) => {
  return request({
    url: `/timelines/${id}`,
    method: 'delete'
  })
}

/**
 * 获取时间线图形数据
 * @param id 时间线ID
 * @returns 图形数据
 */
export const getTimelineGraph = (id: string) => {
  return request({
    url: `/timelines/${id}/graph`,
    method: 'get'
  })
}

/**
 * 导出时间线
 * @param id 时间线ID
 * @param format 导出格式
 * @returns 导出结果
 */
export const exportTimeline = (id: string, format: string) => {
  return request({
    url: `/timelines/export/${id}/${format}`,
    method: 'get',
    params: { includeDetails: true },
    responseType: 'blob',
    transformResponse: [(data) => data] // 防止axios自动解析响应
  })
}

/**
 * 异步导出时间线
 * @param id 时间线ID
 * @param options 导出选项
 * @returns 导出任务ID
 */
export const exportTimelineAsync = (id: string, options: any) => {
  return request({
    url: `/timelines/export/${id}/async`,
    method: 'post',
    data: options
  })
}

/**
 * 获取导出任务状态
 * @param taskId 导出任务ID
 * @returns 任务状态
 */
export const getExportTaskStatus = (taskId: string) => {
  return request({
    url: `/timelines/export/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 取消导出任务
 * @param taskId 导出任务ID
 * @returns 取消结果
 */
export const cancelExportTask = (taskId: string) => {
  return request({
    url: `/timelines/export/task/${taskId}`,
    method: 'delete'
  })
}

// 导出时间线API对象
export const timelineApi = {
  getTimelineList,
  getTimelineDetail,
  getTimelineWithDetails,
  getTimelineEvents,
  createTimeline,
  updateTimeline,
  deleteTimeline,
  getTimelineGraph,
  exportTimeline,
  exportTimelineAsync,
  getExportTaskStatus,
  cancelExportTask,
  generateTimelineAsync,
  getGenerationProgress,
  cancelGeneration
}