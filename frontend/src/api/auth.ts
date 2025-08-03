import request from './index'

/**
 * 用户登录
 * @param data 登录数据
 * @returns 登录结果
 */
export function login(data: { username: string; password: string }) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户登出
 * @returns 登出结果
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 * @returns 用户信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

/**
 * 刷新令牌
 * @returns 新的令牌
 */
export function refreshToken() {
  return request({
    url: '/auth/refresh-token',
    method: 'post'
  })
}

/**
 * 用户注册
 * @param data 注册数据
 * @returns 注册结果
 */
export function register(data: { username: string; password: string; confirmPassword: string }) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * 请求密码重置
 * @param data 用户身份数据
 * @returns 请求结果
 */
export function requestPasswordReset(data: { username: string }) {
  return request({
    url: '/auth/request-password-reset',
    method: 'post',
    data
  })
}

/**
 * 重置密码
 * @param data 重置数据
 * @returns 重置结果
 */
export function resetPassword(data: { token: string; newPassword: string; confirmNewPassword: string }) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}

// 导出认证API对象
export const authApi = {
  login,
  logout,
  getUserInfo,
  refreshToken,
  register,
  requestPasswordReset,
  resetPassword
}