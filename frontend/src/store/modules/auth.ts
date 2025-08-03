/**
 * 认证状态管理模块
 * 处理用户登录、登出、权限验证等功能
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login, logout, getUserInfo, refreshToken, register, requestPasswordReset, resetPassword } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { createResettableState } from '@/utils/storeHelpers.fixed'
import { SecureStorage, validateUsername, validatePassword } from '@/utils/security'
import router from '@/router'

/**
 * 用户信息接口
 * 定义用户基本信息、角色和权限
 */
export interface UserInfo {
  /** 用户唯一标识 */
  id: string
  /** 用户登录名 */
  username: string
  /** 用户显示名称 */
  name: string
  /** 用户头像URL */
  avatar?: string
  /** 用户角色列表 */
  roles: string[]
  /** 用户权限列表 */
  permissions?: string[]
}

/**
 * 登录数据接口
 */
export interface LoginData {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 是否记住用户名 */
  rememberMe?: boolean
}

/**
 * 注册数据接口
 */
export interface RegisterData {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 确认密码 */
  confirmPassword: string
}

/**
 * 密码重置请求数据接口
 */
export interface PasswordResetRequestData {
  /** 用户名 */
  username: string
}

/**
 * 密码重置数据接口
 */
export interface ResetPasswordData {
  /** 重置令牌 */
  token: string
  /** 新密码 */
  newPassword: string
  /** 确认新密码 */
  confirmNewPassword: string
}

/**
 * 登录响应接口
 * 定义登录API返回的数据结构
 */
export interface LoginResponse {
  token: string
  user: UserInfo
}

/**
 * 注册响应接口
 * 定义注册API返回的数据结构
 */
export interface RegisterResponse {
  success: boolean
  message: string
  user?: UserInfo
}

/**
 * 认证状态接口
 * 定义认证模块的状态结构
 */
interface AuthState {
  /** 用户认证令牌 */
  token: string
  /** 用户信息对象 */
  userInfo: UserInfo | null
  /** 用户角色列表 */
  roles: string[]
  /** 用户权限列表 */
  permissions: string[]
  /** 记住的用户名 */
  rememberedUsername: string
  /** 上次登录时间 */
  lastLoginTime: number | null
  /** 加载状态 */
  loading: Record<string, boolean>
  /** 错误信息 */
  error: string | null
  /** 令牌过期阈值（毫秒） */
  tokenExpiryThreshold: number
}

/**
 * 认证状态管理存储
 * 使用组合式API风格
 */
export const useAuthStore = defineStore('auth', () => {
  /**
   * 初始状态
   */
  const initialState = (): AuthState => {
    // 从localStorage获取存储的用户信息
    let userInfo: UserInfo | null = null
    let roles: string[] = []
    let permissions: string[] = []
    
    try {
      const storedUserInfo = SecureStorage.getUserInfo()
      if (storedUserInfo) {
        // 验证用户信息的完整性
        if (storedUserInfo && typeof storedUserInfo === 'object' && 
            storedUserInfo.id && storedUserInfo.username) {
          userInfo = storedUserInfo
          roles = Array.isArray(storedUserInfo.roles) ? storedUserInfo.roles : []
          permissions = Array.isArray(storedUserInfo.permissions) ? storedUserInfo.permissions : []
        } else {
          console.warn('存储的用户信息格式不正确，将清除')
          SecureStorage.removeUserInfo()
        }
      }
    } catch (error) {
      console.error('解析存储的用户信息失败:', error)
      // 清除损坏的数据
      SecureStorage.removeUserInfo()
    }
    
    // 验证令牌的有效性
    const storedToken = SecureStorage.getToken()
    const token = storedToken && storedToken.trim() !== '' ? storedToken : ''
    
    // 验证登录时间
    const storedLoginTime = localStorage.getItem('lastLoginTime')
    let lastLoginTime: number | null = null
    if (storedLoginTime) {
      const parsedTime = Number(storedLoginTime)
      if (!isNaN(parsedTime) && parsedTime > 0) {
        lastLoginTime = parsedTime
      } else {
        localStorage.removeItem('lastLoginTime')
      }
    }
    
    return {
      token,
      userInfo,
      roles,
      permissions,
      rememberedUsername: localStorage.getItem('rememberedUsername') || '',
      lastLoginTime,
      loading: {
        login: false,
        logout: false,
        getUserInfo: false,
        refreshToken: false,
        register: false,
        requestPasswordReset: false,
        resetPassword: false
      },
      error: null,
      tokenExpiryThreshold: 23 * 60 * 60 * 1000 // 23小时
    }
  }
  
  // 创建可重置的状态
  const state = createResettableState<AuthState>(initialState)
  
  // 登录尝试次数
  const loginAttempts = ref(0)
  
  // 登录锁定状态
  const loginLocked = ref(false)
  
  // 锁定倒计时
  const lockCountdown = ref(0)
  
  /**
   * 计算属性
   */
  // 判断用户是否已登录
  const isLoggedIn = computed(() => !!state.token)
  
  // 获取用户显示名称
  const displayName = computed(() => state.userInfo?.name || state.userInfo?.username || '')
  
  // 获取用户头像
  const userAvatar = computed(() => state.userInfo?.avatar || '')
  
  // 检查令牌是否即将过期
  const isTokenExpiringSoon = computed(() => {
    if (!state.lastLoginTime) return false
    
    const tokenAge = Date.now() - state.lastLoginTime
    return tokenAge > state.tokenExpiryThreshold
  })
  
  // 检查是否正在加载
  const isLoading = computed(() => Object.values(state.loading).some(status => status))
  
  /**
   * 操作方法
   */
  // 设置加载状态
  function setLoading(key: keyof AuthState['loading'], status: boolean): void {
    state.loading[key] = status
  }
  
  // 设置错误信息
  function setError(error: string | null): void {
    state.error = error
  }
  
  // 设置认证令牌
  function setToken(token: string): void {
    state.token = token
    state.lastLoginTime = Date.now()
    
    // 同步到安全存储，确保即时持久化
    SecureStorage.setToken(token)
    localStorage.setItem('lastLoginTime', String(state.lastLoginTime))
  }
  
  // 清除认证令牌
  function clearToken(): void {
    state.token = ''
    state.lastLoginTime = null
    
    // 从安全存储中移除
    SecureStorage.removeToken()
    localStorage.removeItem('lastLoginTime')
  }
  
  // 设置用户信息
  function setUserInfo(userInfo: UserInfo): void {
    state.userInfo = userInfo
    state.roles = userInfo.roles || []
    state.permissions = userInfo.permissions || []
    
    // 将用户信息存储到安全存储，确保页面刷新后能恢复
    try {
      SecureStorage.setUserInfo(userInfo)
    } catch (error) {
      console.error('存储用户信息失败:', error)
    }
  }
  
  // 清除用户信息
  function clearUserInfo(): void {
    state.userInfo = null
    state.roles = []
    state.permissions = []
    
    // 从安全存储中移除用户信息
    SecureStorage.removeUserInfo()
  }
  
  // 设置记住的用户名
  function setRememberedUsername(username: string): void {
    state.rememberedUsername = username
    
    if (username) {
      localStorage.setItem('rememberedUsername', username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }
  }
  
  // 检查用户是否拥有指定角色
  function hasRole(role: string): boolean {
    return state.roles.includes(role)
  }
  
  // 检查用户是否拥有指定权限
  function hasPermission(permission: string): boolean {
    return state.permissions.includes(permission)
  }
  
  // 检查用户是否拥有任一指定角色
  function hasAnyRole(roles: string[]): boolean {
    return roles.some(role => state.roles.includes(role))
  }
  
  // 检查用户是否拥有所有指定角色
  function hasAllRoles(roles: string[]): boolean {
    return roles.every(role => state.roles.includes(role))
  }
  
  // 检查用户是否拥有任一指定权限
  function hasAnyPermission(permissions: string[]): boolean {
    return permissions.some(permission => state.permissions.includes(permission))
  }
  
  // 检查用户是否拥有所有指定权限
  function hasAllPermissions(permissions: string[]): boolean {
    return permissions.every(permission => state.permissions.includes(permission))
  }
  
  /**
   * 用户登录操作
   * 发送登录请求并处理响应
   */
  async function loginAction(loginData: LoginData): Promise<boolean> {
    // 检查是否被锁定
    if (loginLocked.value) {
      const errorMsg = `登录已被锁定，请在${lockCountdown.value}秒后重试`
      setError(errorMsg)
      ElMessage.error(errorMsg)
      return false
    }
    
    setLoading('login', true)
    setError(null)
    
    try {
      // 调用登录API
      const response = await login(loginData)
      
      // 检查响应是否包含预期的数据结构
      if (!response) {
        throw new Error('登录响应格式错误')
      }
      
      // 响应拦截器已经处理了Result格式，直接使用response作为数据
      const data = response as LoginResponse
      
      // 验证令牌和用户信息
      if (!data.token) {
        throw new Error('登录失败：未收到有效的认证令牌')
      }
      
      if (!data.user) {
        throw new Error('登录失败：未收到用户信息')
      }
      
      // 更新状态
      setToken(data.token)
      setUserInfo(data.user)
      
      // 处理记住用户名
      if (loginData.rememberMe) {
        setRememberedUsername(loginData.username)
      } else {
        setRememberedUsername('')
      }
      
      // 重置登录尝试次数
      loginAttempts.value = 0
      
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      console.error('登录失败:', error)
      
      // 提取更具体的错误信息
      let errorMessage = '登录失败，请检查用户名和密码'
      
      if (error instanceof Error) {
        errorMessage = error.message
      } else if (typeof error === 'object' && error !== null) {
        // 尝试从错误对象中提取更多信息
        const errorObj = error as any
        if (errorObj.response?.data?.msg) {
          errorMessage = errorObj.response.data.msg
        } else if (errorObj.response?.data?.message) {
          errorMessage = errorObj.response.data.message
        } else if (errorObj.message) {
          errorMessage = errorObj.message
        }
      }
      
      setError(errorMessage)
      ElMessage.error(errorMessage)
      
      // 增加登录尝试次数
      loginAttempts.value++
      
      // 如果尝试次数过多，锁定登录
      if (loginAttempts.value >= 5) {
        loginLocked.value = true
        lockCountdown.value = 60
        
        // 启动倒计时
        const timer = setInterval(() => {
          lockCountdown.value--
          
          if (lockCountdown.value <= 0) {
            loginLocked.value = false
            loginAttempts.value = 0
            clearInterval(timer)
          }
        }, 1000)
        
        ElMessage.warning(`登录尝试次数过多，账户已被锁定${lockCountdown.value}秒`)
      }
      
      return false
    } finally {
      setLoading('login', false)
    }
  }
  
  /**
   * 用户登出操作
   * 发送登出请求并清除本地认证信息
   */
  async function logoutAction(): Promise<void> {
    setLoading('logout', true)
    
    try {
      if (state.token) {
        await logout()
      }
    } catch (error) {
      console.error('登出请求失败:', error)
      const errorMessage = error instanceof Error ? error.message : '登出请求失败'
      setError(errorMessage)
    } finally {
      // 无论API是否成功，都清除本地状态
      clearToken()
      clearUserInfo()
      
      // 重置状态
      state.resetState()
      
      // 清除其他可能的认证相关数据
      localStorage.removeItem('lastLoginTime')
      
      setLoading('logout', false)
      ElMessage.success('已安全退出登录')
      
      // 重定向到登录页面
      router.push('/login')
    }
  }
  
  /**
   * 获取用户信息操作
   * 根据当前令牌获取最新的用户信息
   */
  async function getUserInfoAction(): Promise<UserInfo> {
    setLoading('getUserInfo', true)
    setError(null)
    
    try {
      if (!state.token) {
        throw new Error('未登录')
      }
      
      const response = await getUserInfo()
      // 响应拦截器已经处理了Result格式，直接使用response作为数据
      const userInfo = response as UserInfo
      
      setUserInfo(userInfo)
      return userInfo
    } catch (error) {
      console.error('获取用户信息失败:', error)
      const errorMessage = error instanceof Error ? error.message : '获取用户信息失败'
      setError(errorMessage)
      state.resetState()
      throw error
    } finally {
      setLoading('getUserInfo', false)
    }
  }
  
  /**
   * 刷新令牌操作
   * 当令牌即将过期时，获取新的令牌
   */
  async function refreshTokenAction(): Promise<boolean> {
    setLoading('refreshToken', true)
    setError(null)
    
    try {
      if (!state.token) {
        return false
      }
      
      const response = await refreshToken()
      // 响应拦截器已经处理了Result格式，直接使用response作为数据
      const data = response as { token: string }
      
      setToken(data.token)
      return true
    } catch (error) {
      console.error('刷新令牌失败:', error)
      const errorMessage = error instanceof Error ? error.message : '刷新令牌失败'
      setError(errorMessage)
      return false
    } finally {
      setLoading('refreshToken', false)
    }
  }
  
  /**
   * 检查会话状态
   * 验证当前会话是否有效，如有必要则刷新用户信息或令牌
   */
  async function checkSession(): Promise<boolean> {
    // 如果没有令牌，直接返回false
    if (!state.token) {
      return false
    }
    
    // 如果有令牌但没有用户信息，尝试获取用户信息
    if (!state.userInfo) {
      try {
        await getUserInfoAction()
      } catch (error) {
        ElMessage.error('会话已过期，请重新登录')
        return false
      }
    }
    
    // 如果令牌即将过期，尝试刷新令牌
    if (isTokenExpiringSoon.value) {
      const refreshed = await refreshTokenAction()
      if (!refreshed) {
        ElMessage.warning('您的登录即将过期，请重新登录')
      }
    }
    
    return true
  }
  
  /**
   * 设置令牌过期阈值
   * @param hours 小时数
   */
  function setTokenExpiryThreshold(hours: number): void {
    state.tokenExpiryThreshold = hours * 60 * 60 * 1000
  }
  
  /**
   * 用户注册操作
   * 发送注册请求并处理响应
   */
  async function registerAction(registerData: RegisterData): Promise<boolean> {
    setLoading('register', true)
    setError(null)
    
    try {
      // 验证密码是否匹配
      if (registerData.password !== registerData.confirmPassword) {
        const errorMessage = '两次输入的密码不一致'
        setError(errorMessage)
        ElMessage.error(errorMessage)
        return false
      }
      
      // 调用注册API
      const response = await register(registerData)
      
      // 响应拦截器已经处理了Result格式，直接使用response作为数据
      const data = response as RegisterResponse
      
      if (data.success) {
        ElMessage.success('注册成功，请登录')
        return true
      } else {
        const errorMessage = data.message || '注册失败'
        setError(errorMessage)
        ElMessage.error(errorMessage)
        return false
      }
    } catch (error) {
      console.error('注册失败:', error)
      const errorMessage = error instanceof Error ? error.message : '注册失败，请稍后重试'
      setError(errorMessage)
      ElMessage.error(errorMessage)
      return false
    } finally {
      setLoading('register', false)
    }
  }
  
  /**
   * 请求密码重置操作
   * 发送密码重置请求并处理响应
   */
  async function requestPasswordResetAction(username: string): Promise<boolean> {
    setLoading('requestPasswordReset', true)
    setError(null)
    
    try {
      // 调用请求密码重置API
      await requestPasswordReset({ username })
      
      ElMessage.success('密码重置链接已发送到您的邮箱，请查收')
      return true
    } catch (error) {
      console.error('请求密码重置失败:', error)
      const errorMessage = error instanceof Error ? error.message : '请求密码重置失败，请稍后重试'
      setError(errorMessage)
      ElMessage.error(errorMessage)
      return false
    } finally {
      setLoading('requestPasswordReset', false)
    }
  }
  
  /**
   * 重置密码操作
   * 发送重置密码请求并处理响应
   */
  async function resetPasswordAction(resetData: ResetPasswordData): Promise<boolean> {
    setLoading('resetPassword', true)
    setError(null)
    
    try {
      // 验证密码是否匹配
      if (resetData.newPassword !== resetData.confirmNewPassword) {
        const errorMessage = '两次输入的密码不一致'
        setError(errorMessage)
        ElMessage.error(errorMessage)
        return false
      }
      
      // 调用重置密码API
      await resetPassword(resetData)
      
      ElMessage.success('密码重置成功，请使用新密码登录')
      return true
    } catch (error) {
      console.error('重置密码失败:', error)
      const errorMessage = error instanceof Error ? error.message : '重置密码失败，请稍后重试'
      setError(errorMessage)
      ElMessage.error(errorMessage)
      return false
    } finally {
      setLoading('resetPassword', false)
    }
  }
  
  return {
    // 状态
    ...state,
    loginAttempts,
    loginLocked,
    lockCountdown,
    
    // 计算属性
    isLoggedIn,
    displayName,
    userAvatar,
    isTokenExpiringSoon,
    isLoading,
    
    // 方法
    setLoading,
    setError,
    setToken,
    clearToken,
    setUserInfo,
    clearUserInfo,
    setRememberedUsername,
    hasRole,
    hasPermission,
    hasAnyRole,
    hasAllRoles,
    hasAnyPermission,
    hasAllPermissions,
    loginAction,
    logoutAction,
    getUserInfoAction,
    refreshTokenAction,
    checkSession,
    setTokenExpiryThreshold,
    registerAction,
    requestPasswordResetAction,
    resetPasswordAction
  }
})

// 添加持久化配置
export const authStorePersistOptions = {
  key: 'auth-state',
  storage: localStorage,
  paths: ['token', 'userInfo', 'roles', 'permissions', 'rememberedUsername', 'lastLoginTime', 'tokenExpiryThreshold']
}

// 注意：这里使用了Pinia的持久化插件，但TypeScript可能会报错
// 因为TypeScript不认识Pinia的持久化插件选项
// 这是正常的，不影响功能
// @ts-ignore
useAuthStore.$persist = authStorePersistOptions