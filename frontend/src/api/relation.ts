import request from './index'

export interface SubjectObjectRelation {
  id?: number
  subjectCode: string
  subjectName?: string
  objectCode: string
  objectName?: string
  relationType: string
  relationTypeName?: string
  relationName: string
  intensityLevel: number
  intensityLevelName?: string
  description?: string
  status?: number
  statusName?: string
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface RelationQuery {
  current?: number
  size?: number
  subjectCode?: string
  objectCode?: string
  relationType?: string
  relationName?: string
  intensityLevel?: number
  status?: number
}

// 分页查询主体客体关系列表
export function getRelationPage(params: RelationQuery) {
  return request({
    url: '/subject-object-relation/page',
    method: 'get',
    params
  })
}

// 获取主体客体关系详情
export function getRelationDetail(id: number) {
  return request({
    url: `/subject-object-relation/detail/${id}`,
    method: 'get'
  })
}

// 创建主体客体关系
export function createRelation(data: SubjectObjectRelation) {
  return request({
    url: '/subject-object-relation/create',
    method: 'post',
    data
  })
}

// 更新主体客体关系
export function updateRelation(data: SubjectObjectRelation) {
  return request({
    url: '/subject-object-relation/update',
    method: 'put',
    data
  })
}

// 删除主体客体关系
export function deleteRelation(id: number) {
  return request({
    url: `/subject-object-relation/delete/${id}`,
    method: 'delete'
  })
}

// 批量删除主体客体关系
export function deleteRelationsBatch(ids: number[]) {
  return request({
    url: '/subject-object-relation/batch-delete',
    method: 'delete',
    data: { ids }
  })
}

// 根据主体编码查询关系
export function getRelationsBySubject(subjectCode: string) {
  return request({
    url: `/subject-object-relation/by-subject/${subjectCode}`,
    method: 'get'
  })
}

// 根据客体编码查询关系
export function getRelationsByObject(objectCode: string) {
  return request({
    url: `/subject-object-relation/by-object/${objectCode}`,
    method: 'get'
  })
}

// 根据关系类型查询关系
export function getRelationsByType(relationType: string) {
  return request({
    url: `/subject-object-relation/by-type/${relationType}`,
    method: 'get'
  })
}

// 根据主体和客体查询关系
export function getRelationsBySubjectAndObject(subjectCode: string, objectCode: string) {
  return request({
    url: '/subject-object-relation/by-subject-object',
    method: 'get',
    params: { subjectCode, objectCode }
  })
}

// 检查是否存在特定关系
export function existsRelation(subjectCode: string, objectCode: string, relationType: string) {
  return request({
    url: '/subject-object-relation/exists',
    method: 'get',
    params: { subjectCode, objectCode, relationType }
  })
} 