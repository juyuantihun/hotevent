import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { ElMessage } from 'element-plus'
import Login from '@/views/login/index.vue'
import Register from '@/views/register/index.vue'
import ForgotPassword from '@/views/forgot-password/index.vue'
import ResetPassword from '@/views/reset-password/index.vue'
import { useAuthStore } from '@/store/modules/auth'

// 模拟Element Plus消息组件
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    }
  }
})

// 模拟路由
const createTestRouter = () => {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
      { path: '/login', name: 'Login', component: Login },
      { path: '/register', name: 'Register', component: Register },
      { path: '/forgot-password', name: 'ForgotPassword', component: ForgotPassword },
      { path: '/reset-password/:token', name: 'ResetPassword', component: ResetPassword }
    ]
  })
}

describe('认证流程集成测试', () => {
  let router
  let pinia
  
  beforeEach(() => {
    // 创建并激活Pinia
    pinia = createPinia()
    setActivePinia(pinia)
    
    // 创建路由
    router = createTestRouter()
    
    // 重置所有模拟
    vi.resetAllMocks()
    
    // 清除localStorage
    localStorage.clear()
  })
  
  describe('登录页面', () => {
    it('应该显示注册和忘记密码链接', async () => {
      const wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 验证注册链接存在
      const registerLink = wrapper.find('a[href="/register"]')
      expect(registerLink.exists()).toBe(true)
      expect(registerLink.text()).toBe('注册新账户')
      
      // 验证忘记密码链接存在
      const forgotPasswordLink = wrapper.find('a[href="/forgot-password"]')
      expect(forgotPasswordLink.exists()).toBe(true)
      expect(forgotPasswordLink.text()).toBe('忘记密码')
    })
    
    it('点击注册链接应该导航到注册页面', async () => {
      const wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 模拟点击注册链接
      await wrapper.find('a[href="/register"]').trigger('click')
      
      // 等待路由更新
      await router.isReady()
      
      // 验证路由已更改
      expect(router.currentRoute.value.path).toBe('/register')
    })
    
    it('点击忘记密码链接应该导航到忘记密码页面', async () => {
      const wrapper = mount(Login, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 模拟点击忘记密码链接
      await wrapper.find('a[href="/forgot-password"]').trigger('click')
      
      // 等待路由更新
      await router.isReady()
      
      // 验证路由已更改
      expect(router.currentRoute.value.path).toBe('/forgot-password')
    })
  })
  
  describe('注册流程', () => {
    it('应该能够提交注册表单', async () => {
      const wrapper = mount(Register, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 模拟认证存储
      const authStore = useAuthStore()
      authStore.registerAction = vi.fn().mockResolvedValue(true)
      
      // 填写表单
      await wrapper.find('input[placeholder="请输入用户名"]').setValue('newuser')
      await wrapper.find('input[placeholder="请输入密码"]').setValue('password123')
      await wrapper.find('input[placeholder="请确认密码"]').setValue('password123')
      
      // 提交表单
      await wrapper.find('button.register-button').trigger('click')
      
      // 验证注册方法被调用
      expect(authStore.registerAction).toHaveBeenCalledWith({
        username: 'newuser',
        password: 'password123',
        confirmPassword: 'password123'
      })
      
      // 验证成功消息被显示
      expect(ElMessage.success).toHaveBeenCalledWith('注册成功，请登录')
    })
    
    it('密码不匹配时应该显示错误', async () => {
      const wrapper = mount(Register, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 填写表单（密码不匹配）
      await wrapper.find('input[placeholder="请输入用户名"]').setValue('newuser')
      await wrapper.find('input[placeholder="请输入密码"]').setValue('password123')
      await wrapper.find('input[placeholder="请确认密码"]').setValue('differentpassword')
      
      // 触发密码确认字段的验证
      await wrapper.find('input[placeholder="请确认密码"]').trigger('blur')
      
      // 等待验证更新
      await wrapper.vm.$nextTick()
      
      // 验证错误消息
      const errorMessage = wrapper.find('.el-form-item__error')
      expect(errorMessage.exists()).toBe(true)
      expect(errorMessage.text()).toBe('两次输入密码不一致')
    })
  })
  
  describe('忘记密码流程', () => {
    it('应该能够提交忘记密码表单', async () => {
      const wrapper = mount(ForgotPassword, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 模拟认证存储
      const authStore = useAuthStore()
      authStore.requestPasswordResetAction = vi.fn().mockResolvedValue(true)
      
      // 填写表单
      await wrapper.find('input[placeholder="请输入用户名"]').setValue('testuser')
      
      // 提交表单
      await wrapper.find('button.forgot-password-button').trigger('click')
      
      // 验证请求密码重置方法被调用
      expect(authStore.requestPasswordResetAction).toHaveBeenCalledWith('testuser')
      
      // 验证成功消息被显示
      expect(ElMessage.success).toHaveBeenCalledWith('密码重置链接已发送到您的邮箱，请查收')
    })
  })
  
  describe('重置密码流程', () => {
    it('应该能够提交重置密码表单', async () => {
      // 设置路由参数
      router.push('/reset-password/test-token-123')
      await router.isReady()
      
      const wrapper = mount(ResetPassword, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 模拟认证存储
      const authStore = useAuthStore()
      authStore.resetPasswordAction = vi.fn().mockResolvedValue(true)
      
      // 填写表单
      await wrapper.find('input[placeholder="请输入新密码"]').setValue('newpassword123')
      await wrapper.find('input[placeholder="请确认新密码"]').setValue('newpassword123')
      
      // 提交表单
      await wrapper.find('button.reset-password-button').trigger('click')
      
      // 验证重置密码方法被调用
      expect(authStore.resetPasswordAction).toHaveBeenCalledWith({
        token: 'test-token-123',
        newPassword: 'newpassword123',
        confirmNewPassword: 'newpassword123'
      })
      
      // 验证成功消息被显示
      expect(ElMessage.success).toHaveBeenCalledWith('密码重置成功，请使用新密码登录')
    })
    
    it('密码不匹配时应该显示错误', async () => {
      // 设置路由参数
      router.push('/reset-password/test-token-123')
      await router.isReady()
      
      const wrapper = mount(ResetPassword, {
        global: {
          plugins: [router, pinia]
        }
      })
      
      // 填写表单（密码不匹配）
      await wrapper.find('input[placeholder="请输入新密码"]').setValue('newpassword123')
      await wrapper.find('input[placeholder="请确认新密码"]').setValue('differentpassword')
      
      // 触发密码确认字段的验证
      await wrapper.find('input[placeholder="请确认新密码"]').trigger('blur')
      
      // 等待验证更新
      await wrapper.vm.$nextTick()
      
      // 验证错误消息
      const errorMessage = wrapper.find('.el-form-item__error')
      expect(errorMessage.exists()).toBe(true)
      expect(errorMessage.text()).toBe('两次输入密码不一致')
    })
  })
})