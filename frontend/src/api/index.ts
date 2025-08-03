/**
 * API模块入口文件
 * 导出所有API服务
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { SecureStorage } from '@/utils/security'

// 创建axios实例
export const request = axios.create({
  baseURL: '/api', // API基础路径
  timeout: 30000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从SecureStorage获取token
    const token = SecureStorage.getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 开发环境下记录请求日志
    if (process.env.NODE_ENV === 'development') {
      console.log('发送请求:', config.method?.toUpperCase(), config.url, config.params || config.data)
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 开发环境下记录响应日志
    if (process.env.NODE_ENV === 'development') {
      console.log('收到响应:', response.config.url, response.status, response.data)
    }
    
    // 如果响应包含code字段，检查是否为成功状态
    if (response.data && response.data.code !== undefined) {
      if (response.data.code === 200) {
        return response.data.data // 返回data字段中的实际数据
      } else {
        // 业务逻辑错误
        const errorMsg = response.data.msg || response.data.message || '请求失败'
        console.error('业务逻辑错误:', errorMsg)
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }
    }
    // 直接返回数据
    return response.data
  },
  error => {
    // 处理HTTP错误
    let errorMsg = '网络错误，请稍后重试'
    
    if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 400:
          errorMsg = '请求参数错误'
          break
        case 401:
          errorMsg = '未授权，请重新登录'
          // 处理认证失败的情况
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('lastLoginTime')
          // 如果当前不在登录页面，则跳转到登录页面
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          break
        case 403:
          errorMsg = '拒绝访问'
          break
        case 404:
          errorMsg = '请求的资源不存在'
          break
        case 500:
          errorMsg = '服务器内部错误'
          break
        default:
          errorMsg = `请求失败(${error.response.status})`
      }
      
      // 如果响应中包含详细错误信息，优先使用
      if (error.response.data) {
        if (error.response.data.msg) {
          errorMsg = error.response.data.msg
        } else if (error.response.data.message) {
          errorMsg = error.response.data.message
        }
      }
    } else if (error.request) {
      // 请求已发送但未收到响应
      errorMsg = '服务器无响应，请检查网络连接'
    }
    
    // 只在非401错误时显示错误消息，避免重复显示
    if (!error.response || error.response.status !== 401) {
      ElMessage.error(errorMsg)
    }
    
    return Promise.reject(error)
  }
)

// 导出默认请求实例
export default request

// 导出各个模块的API
export * from './timeline'
export * from './event'
export * from './auth'