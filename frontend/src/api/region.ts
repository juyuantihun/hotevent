// 地区API服务
import { request } from '@/api/index'

/**
 * 地区API接口定义
 */
interface RegionParams {
  page?: number
  size?: number
  name?: string
  type?: string
}

interface RegionData {
  id: number
  name: string
  type: string
  parentId?: number
  description?: string
}

/**
 * 地区API
 */
export const regionApi = {
  /**
   * 获取地区列表
   * @param params 查询参数
   * @returns 请求Promise
   */
  getRegions(params?: RegionParams) {
    return request({
      url: '/regions',
      method: 'get',
      params
    })
  },

  /**
   * 创建地区
   * @param data 地区数据
   * @returns 请求Promise
   */
  createRegion(data: RegionData) {
    return request({
      url: '/regions',
      method: 'post',
      data
    })
  },

  /**
   * 更新地区
   * @param id 地区ID
   * @param data 地区数据
   * @returns 请求Promise
   */
  updateRegion(id: number, data: RegionData) {
    return request({
      url: `/regions/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除地区
   * @param id 地区ID
   * @returns 请求Promise
   */
  deleteRegion(id: number) {
    return request({
      url: `/regions/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取地区详情
   * @param id 地区ID
   * @returns 请求Promise
   */
  getRegionDetail(id: number) {
    return request({
      url: `/regions/${id}`,
      method: 'get'
    })
  },

  /**
   * 获取地区包含的字典项
   * @param id 地区ID
   * @returns 请求Promise
   */
  getRegionItems(id: number) {
    return request({
      url: `/regions/${id}/items`,
      method: 'get'
    })
  },

  /**
   * 添加字典项到地区
   * @param id 地区ID
   * @param dictionaryId 字典项ID
   * @returns 请求Promise
   */
  addRegionItem(id: number, dictionaryId: number) {
    return request({
      url: `/regions/${id}/items`,
      method: 'post',
      data: { dictionaryId }
    })
  },

  /**
   * 从地区移除字典项
   * @param id 地区ID
   * @param itemId 字典项ID
   * @returns 请求Promise
   */
  removeRegionItem(id: number, itemId: number) {
    return request({
      url: `/regions/${id}/items/${itemId}`,
      method: 'delete'
    })
  },

  /**
   * 获取地区树形结构
   * @returns 请求Promise
   */
  getRegionTree() {
    return request({
      url: '/regions/tree',
      method: 'get'
    })
  },

  /**
   * 根据ID获取地区信息
   * @param id 地区ID
   * @returns 请求Promise
   */
  getRegionById(id: number) {
    return request({
      url: `/regions/by-id/${id}`,
      method: 'get'
    })
  },

  /**
   * 获取地区的子地区
   * @param id 地区ID
   * @returns 请求Promise
   */
  getRegionDescendants(id: number) {
    return request({
      url: `/regions/${id}/children`,
      method: 'get'
    })
  },

  /**
   * 获取地区的祖先地区
   * @param id 地区ID
   * @returns 请求Promise
   */
  getRegionAncestors(id: number) {
    return request({
      url: `/regions/${id}/ancestors`,
      method: 'get'
    })
  },

  /**
   * 搜索地区
   * @param keyword 搜索关键词
   * @param params 分页参数
   * @returns 请求Promise
   */
  searchRegions(keyword: string, params: RegionParams = {}) {
    return request({
      url: '/regions/search',
      method: 'get',
      params: {
        keyword,
        ...params
      }
    })
  }
}

export default regionApi