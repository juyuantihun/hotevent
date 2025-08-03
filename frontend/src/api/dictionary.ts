import request from './index'

export interface Dictionary {
  id?: number
  dictType: string
  dictCode: string
  dictName: string
  dictDescription?: string
  parentId?: number
  parentCode?: string
  sortOrder?: number
  status: number
  isAutoAdded?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
  children?: Dictionary[]
}

export interface DictionaryQuery {
  current?: number
  size?: number
  dictType?: string
  dictName?: string
  parentId?: number
  isAutoAdded?: 0 | 1
}

export interface DictionaryDetailResponse {
  entityType: string
  data: any
}

// 获取字典列表
export function getDictionaryList(params: DictionaryQuery) {
  return request({
    url: '/dictionary/list',
    method: 'get',
    params
  })
}

// 获取字典树形结构
export function getDictionaryTree(dictType?: string) {
  return request({
    url: '/dictionary/tree',
    method: 'get',
    params: { dictType }
  })
}

// 根据类型获取字典项
export function getDictionaryByType(dictType: string) {
  return request({
    url: `/dictionary/type/${dictType}`,
    method: 'get'
  })
}

// 获取字典详情
export function getDictionaryDetail(id: number): Promise<DictionaryDetailResponse> {
  return request({
    url: `/dictionary/detail/${id}`,
    method: 'get'
  })
}

// 创建字典项
export function createDictionary(data: Dictionary) {
  return request({
    url: '/dictionary/create',
    method: 'post',
    data
  })
}

// 更新字典项
export function updateDictionary(data: Dictionary) {
  return request({
    url: '/dictionary/update',
    method: 'put',
    data
  })
}

// 删除字典项
export function deleteDictionary(id: number) {
  return request({
    url: `/dictionary/delete/${id}`,
    method: 'delete'
  })
}

// 批量删除字典项
export function deleteDictionariesBatch(ids: number[]) {
  return request({
    url: '/dictionary/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

// 获取字典类型列表
export function getDictionaryTypes() {
  return request({
    url: '/dictionary/types',
    method: 'get'
  })
} 