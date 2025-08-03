import { describe, it, expect, vi, beforeEach } from 'vitest'
import { login, logout, getUserInfo, refreshToken, register, requestPasswordReset, resetPassword } from '@/api/auth'
import request from '@/api/index'

// 模拟API请求模块
vi.mock('@/api/index', () => ({
  default: vi.fn()
}))

describe('认证API模块', () => {
  beforeEach(() => {
    // 重置模拟函数
    vi.resetAllMocks()
  })

  describe('login', () => {
    it('应该使用正确的参数调用请求', () => {
      const loginData = { username: 'testuser', password: 'password123' }
      login(loginData)
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/login',
        method: 'post',
        data: loginData
      })
    })
  })

  describe('logout', () => {
    it('应该使用正确的参数调用请求', () => {
      logout()
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/logout',
        method: 'post'
      })
    })
  })

  describe('getUserInfo', () => {
    it('应该使用正确的参数调用请求', () => {
      getUserInfo()
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/user-info',
        method: 'get'
      })
    })
  })

  describe('refreshToken', () => {
    it('应该使用正确的参数调用请求', () => {
      refreshToken()
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/refresh-token',
        method: 'post'
      })
    })
  })

  describe('register', () => {
    it('应该使用正确的参数调用请求', () => {
      const registerData = { 
        username: 'newuser', 
        password: 'password123', 
        confirmPassword: 'password123' 
      }
      register(registerData)
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/register',
        method: 'post',
        data: registerData
      })
    })
  })

  describe('requestPasswordReset', () => {
    it('应该使用正确的参数调用请求', () => {
      const resetRequestData = { username: 'testuser' }
      requestPasswordReset(resetRequestData)
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/request-password-reset',
        method: 'post',
        data: resetRequestData
      })
    })
  })

  describe('resetPassword', () => {
    it('应该使用正确的参数调用请求', () => {
      const resetData = { 
        token: 'reset-token-123', 
        newPassword: 'newpassword123', 
        confirmNewPassword: 'newpassword123' 
      }
      resetPassword(resetData)
      
      expect(request).toHaveBeenCalledWith({
        url: '/auth/reset-password',
        method: 'post',
        data: resetData
      })
    })
  })
})