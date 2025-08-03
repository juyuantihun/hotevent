import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/store/modules/auth'
import * as authApi from '@/api/auth'

// 模拟localStorage
vi.mock('localStorage', () => ({
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn()
}))

// 模拟API模块
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getUserInfo: vi.fn(),
  refreshToken: vi.fn(),
  register: vi.fn(),
  requestPasswordReset: vi.fn(),
  resetPassword: vi.fn()
}))

describe('认证状态管理模块', () => {
  beforeEach(() => {
    // 创建一个新的 Pinia 实例并使其处于激活状态
    setActivePinia(createPinia())
    
    // 重置所有模拟
    vi.resetAllMocks()
  })

  describe('loginAction', () => {
    it('登录成功时应该设置令牌和用户信息', async () => {
      const authStore = useAuthStore()
      const loginData = { username: 'testuser', password: 'password123' }
      const mockResponse = {
        token: 'mock-token',
        user: {
          id: '1',
          username: 'testuser',
          name: 'Test User',
          roles: ['user']
        }
      }
      
      // 模拟API响应
      vi.mocked(authApi.login).mockResolvedValue(mockResponse)
      
      // 执行登录操作
      const result = await authStore.loginAction(loginData)
      
      // 验证结果
      expect(result).toBe(true)
      expect(authStore.token).toBe('mock-token')
      expect(authStore.userInfo).toEqual(mockResponse.user)
      expect(authStore.roles).toEqual(['user'])
    })
    
    it('登录失败时应该设置错误信息', async () => {
      const authStore = useAuthStore()
      const loginData = { username: 'testuser', password: 'wrongpassword' }
      
      // 模拟API错误
      vi.mocked(authApi.login).mockRejectedValue(new Error('Invalid credentials'))
      
      // 执行登录操作
      const result = await authStore.loginAction(loginData)
      
      // 验证结果
      expect(result).toBe(false)
      expect(authStore.error).toBe('Invalid credentials')
      expect(authStore.token).toBe('')
      expect(authStore.userInfo).toBeNull()
    })
  })

  describe('registerAction', () => {
    it('注册成功时应该返回true', async () => {
      const authStore = useAuthStore()
      const registerData = { 
        username: 'newuser', 
        password: 'password123', 
        confirmPassword: 'password123' 
      }
      const mockResponse = {
        success: true,
        message: '注册成功',
        user: {
          id: '2',
          username: 'newuser',
          name: 'New User',
          roles: ['user']
        }
      }
      
      // 模拟API响应
      vi.mocked(authApi.register).mockResolvedValue(mockResponse)
      
      // 执行注册操作
      const result = await authStore.registerAction(registerData)
      
      // 验证结果
      expect(result).toBe(true)
    })
    
    it('密码不匹配时应该返回false', async () => {
      const authStore = useAuthStore()
      const registerData = { 
        username: 'newuser', 
        password: 'password123', 
        confirmPassword: 'differentpassword' 
      }
      
      // 执行注册操作
      const result = await authStore.registerAction(registerData)
      
      // 验证结果
      expect(result).toBe(false)
      expect(authStore.error).toBe('两次输入的密码不一致')
      // 验证API没有被调用
      expect(authApi.register).not.toHaveBeenCalled()
    })
    
    it('注册失败时应该设置错误信息', async () => {
      const authStore = useAuthStore()
      const registerData = { 
        username: 'existinguser', 
        password: 'password123', 
        confirmPassword: 'password123' 
      }
      const mockResponse = {
        success: false,
        message: '用户名已存在'
      }
      
      // 模拟API响应
      vi.mocked(authApi.register).mockResolvedValue(mockResponse)
      
      // 执行注册操作
      const result = await authStore.registerAction(registerData)
      
      // 验证结果
      expect(result).toBe(false)
      expect(authStore.error).toBe('用户名已存在')
    })
  })

  describe('requestPasswordResetAction', () => {
    it('请求密码重置成功时应该返回true', async () => {
      const authStore = useAuthStore()
      const username = 'testuser'
      
      // 模拟API响应
      vi.mocked(authApi.requestPasswordReset).mockResolvedValue({})
      
      // 执行请求密码重置操作
      const result = await authStore.requestPasswordResetAction(username)
      
      // 验证结果
      expect(result).toBe(true)
      expect(authApi.requestPasswordReset).toHaveBeenCalledWith({ username })
    })
    
    it('请求密码重置失败时应该设置错误信息', async () => {
      const authStore = useAuthStore()
      const username = 'nonexistentuser'
      
      // 模拟API错误
      vi.mocked(authApi.requestPasswordReset).mockRejectedValue(new Error('用户不存在'))
      
      // 执行请求密码重置操作
      const result = await authStore.requestPasswordResetAction(username)
      
      // 验证结果
      expect(result).toBe(false)
      expect(authStore.error).toBe('用户不存在')
    })
  })

  describe('resetPasswordAction', () => {
    it('重置密码成功时应该返回true', async () => {
      const authStore = useAuthStore()
      const resetData = { 
        token: 'reset-token-123', 
        newPassword: 'newpassword123', 
        confirmNewPassword: 'newpassword123' 
      }
      
      // 模拟API响应
      vi.mocked(authApi.resetPassword).mockResolvedValue({})
      
      // 执行重置密码操作
      const result = await authStore.resetPasswordAction(resetData)
      
      // 验证结果
      expect(result).toBe(true)
      expect(authApi.resetPassword).toHaveBeenCalledWith(resetData)
    })
    
    it('密码不匹配时应该返回false', async () => {
      const authStore = useAuthStore()
      const resetData = { 
        token: 'reset-token-123', 
        newPassword: 'newpassword123', 
        confirmNewPassword: 'differentpassword' 
      }
      
      // 执行重置密码操作
      const result = await authStore.resetPasswordAction(resetData)
      
      // 验证结果
      expect(result).toBe(false)
      expect(authStore.error).toBe('两次输入的密码不一致')
      // 验证API没有被调用
      expect(authApi.resetPassword).not.toHaveBeenCalled()
    })
    
    it('重置密码失败时应该设置错误信息', async () => {
      const authStore = useAuthStore()
      const resetData = { 
        token: 'invalid-token', 
        newPassword: 'newpassword123', 
        confirmNewPassword: 'newpassword123' 
      }
      
      // 模拟API错误
      vi.mocked(authApi.resetPassword).mockRejectedValue(new Error('无效的重置令牌'))
      
      // 执行重置密码操作
      const result = await authStore.resetPasswordAction(resetData)
      
      // 验证结果
      expect(result).toBe(false)
      expect(authStore.error).toBe('无效的重置令牌')
    })
  })
})