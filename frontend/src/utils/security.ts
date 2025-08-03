/**
 * 前端安全工具类
 * 提供XSS防护、输入验证、CSRF防护等安全功能
 */

/**
 * XSS防护 - HTML实体编码
 * @param str 需要编码的字符串
 * @returns 编码后的安全字符串
 */
export function escapeHtml(str: string): string {
  if (!str) return ''
  
  const div = document.createElement('div')
  div.textContent = str
  return div.innerHTML
}

/**
 * XSS防护 - 移除HTML标签
 * @param str 需要清理的字符串
 * @returns 清理后的纯文本
 */
export function stripHtml(str: string): string {
  if (!str) return ''
  
  const div = document.createElement('div')
  div.innerHTML = str
  return div.textContent || div.innerText || ''
}

/**
 * 输入验证 - 用户名格式验证
 * @param username 用户名
 * @returns 验证结果
 */
export function validateUsername(username: string): { valid: boolean; message?: string } {
  if (!username) {
    return { valid: false, message: '用户名不能为空' }
  }
  
  if (username.length < 3 || username.length > 20) {
    return { valid: false, message: '用户名长度必须在3-20个字符之间' }
  }
  
  // 只允许字母、数字、下划线和中文
  const usernameRegex = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/
  if (!usernameRegex.test(username)) {
    return { valid: false, message: '用户名只能包含字母、数字、下划线和中文字符' }
  }
  
  return { valid: true }
}

/**
 * 输入验证 - 密码强度验证
 * @param password 密码
 * @returns 验证结果
 */
export function validatePassword(password: string): { valid: boolean; message?: string; strength?: string } {
  if (!password) {
    return { valid: false, message: '密码不能为空' }
  }
  
  if (password.length < 6) {
    return { valid: false, message: '密码长度不能少于6个字符' }
  }
  
  if (password.length > 50) {
    return { valid: false, message: '密码长度不能超过50个字符' }
  }
  
  // 检查密码强度
  let strength = 'weak'
  let score = 0
  
  // 包含小写字母
  if (/[a-z]/.test(password)) score++
  // 包含大写字母
  if (/[A-Z]/.test(password)) score++
  // 包含数字
  if (/\d/.test(password)) score++
  // 包含特殊字符
  if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>/?]/.test(password)) score++
  // 长度大于8
  if (password.length >= 8) score++
  
  if (score >= 4) {
    strength = 'strong'
  } else if (score >= 2) {
    strength = 'medium'
  }
  
  return { valid: true, strength }
}

/**
 * 安全的令牌存储
 * 使用加密存储敏感信息
 */
export class SecureStorage {
  private static readonly TOKEN_KEY = 'auth_token'
  private static readonly USER_INFO_KEY = 'user_info'
  
  /**
   * 简单的字符串加密（Base64 + 简单混淆）
   * 注意：这不是真正的加密，只是基本的混淆
   */
  private static encode(str: string): string {
    try {
      // 添加时间戳作为盐值
      const salt = Date.now().toString(36)
      const data = salt + '|' + str
      return btoa(encodeURIComponent(data))
    } catch (error) {
      console.error('编码失败:', error)
      return str
    }
  }
  
  /**
   * 解码字符串
   */
  private static decode(encodedStr: string): string {
    try {
      const decoded = decodeURIComponent(atob(encodedStr))
      const parts = decoded.split('|')
      return parts.length > 1 ? parts.slice(1).join('|') : decoded
    } catch (error) {
      console.error('解码失败:', error)
      return encodedStr
    }
  }
  
  /**
   * 安全存储令牌
   */
  static setToken(token: string): void {
    if (!token) return
    
    try {
      const encodedToken = this.encode(token)
      localStorage.setItem(this.TOKEN_KEY, encodedToken)
    } catch (error) {
      console.error('存储令牌失败:', error)
      // 降级到普通存储
      localStorage.setItem(this.TOKEN_KEY, token)
    }
  }
  
  /**
   * 获取令牌
   */
  static getToken(): string | null {
    try {
      const encodedToken = localStorage.getItem(this.TOKEN_KEY)
      if (!encodedToken) return null
      
      return this.decode(encodedToken)
    } catch (error) {
      console.error('获取令牌失败:', error)
      // 尝试直接获取
      return localStorage.getItem(this.TOKEN_KEY)
    }
  }
  
  /**
   * 移除令牌
   */
  static removeToken(): void {
    localStorage.removeItem(this.TOKEN_KEY)
  }
  
  /**
   * 安全存储用户信息
   */
  static setUserInfo(userInfo: any): void {
    if (!userInfo) return
    
    try {
      const jsonStr = JSON.stringify(userInfo)
      const encodedInfo = this.encode(jsonStr)
      localStorage.setItem(this.USER_INFO_KEY, encodedInfo)
    } catch (error) {
      console.error('存储用户信息失败:', error)
      // 降级到普通存储
      localStorage.setItem(this.USER_INFO_KEY, JSON.stringify(userInfo))
    }
  }
  
  /**
   * 获取用户信息
   */
  static getUserInfo(): any | null {
    try {
      const encodedInfo = localStorage.getItem(this.USER_INFO_KEY)
      if (!encodedInfo) return null
      
      const jsonStr = this.decode(encodedInfo)
      return JSON.parse(jsonStr)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 尝试直接获取
      const info = localStorage.getItem(this.USER_INFO_KEY)
      return info ? JSON.parse(info) : null
    }
  }
  
  /**
   * 移除用户信息
   */
  static removeUserInfo(): void {
    localStorage.removeItem(this.USER_INFO_KEY)
  }
  
  /**
   * 清除所有认证相关数据
   */
  static clear(): void {
    this.removeToken()
    this.removeUserInfo()
  }
}

/**
 * CSRF防护 - 生成随机令牌
 */
export function generateCSRFToken(): string {
  const array = new Uint8Array(32)
  crypto.getRandomValues(array)
  return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('')
}

/**
 * 安全的URL参数解析
 * 防止XSS攻击
 */
export function safeParseURLParams(url: string): Record<string, string> {
  const params: Record<string, string> = {}
  
  try {
    const urlObj = new URL(url)
    urlObj.searchParams.forEach((value, key) => {
      // 对参数值进行HTML实体编码
      params[escapeHtml(key)] = escapeHtml(value)
    })
  } catch (error) {
    console.error('URL解析失败:', error)
  }
  
  return params
}

/**
 * 检查URL是否安全（防止开放重定向）
 */
export function isSafeURL(url: string): boolean {
  if (!url) return false
  
  try {
    // 只允许相对路径或同域名的绝对路径
    if (url.startsWith('/')) {
      return true
    }
    
    const urlObj = new URL(url)
    const currentOrigin = window.location.origin
    
    return urlObj.origin === currentOrigin
  } catch (error) {
    return false
  }
}

/**
 * 安全的localStorage操作
 * 包含错误处理和容量检查
 */
export class SafeLocalStorage {
  /**
   * 安全设置项目
   */
  static setItem(key: string, value: string): boolean {
    try {
      // 检查存储空间
      const testKey = '__storage_test__'
      localStorage.setItem(testKey, 'test')
      localStorage.removeItem(testKey)
      
      localStorage.setItem(key, value)
      return true
    } catch (error) {
      console.error('localStorage设置失败:', error)
      
      // 如果是存储空间不足，尝试清理
      if (error instanceof DOMException && error.code === 22) {
        console.warn('localStorage空间不足，尝试清理...')
        this.cleanup()
        
        try {
          localStorage.setItem(key, value)
          return true
        } catch (retryError) {
          console.error('重试设置localStorage失败:', retryError)
        }
      }
      
      return false
    }
  }
  
  /**
   * 安全获取项目
   */
  static getItem(key: string): string | null {
    try {
      return localStorage.getItem(key)
    } catch (error) {
      console.error('localStorage获取失败:', error)
      return null
    }
  }
  
  /**
   * 安全移除项目
   */
  static removeItem(key: string): boolean {
    try {
      localStorage.removeItem(key)
      return true
    } catch (error) {
      console.error('localStorage移除失败:', error)
      return false
    }
  }
  
  /**
   * 清理过期或无用的数据
   */
  static cleanup(): void {
    try {
      const keysToRemove: string[] = []
      
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i)
        if (key) {
          // 移除明显过期的数据（可以根据需要自定义规则）
          if (key.startsWith('temp_') || key.includes('_expired_')) {
            keysToRemove.push(key)
          }
        }
      }
      
      keysToRemove.forEach(key => {
        localStorage.removeItem(key)
      })
      
      console.log(`清理了 ${keysToRemove.length} 个localStorage项目`)
    } catch (error) {
      console.error('localStorage清理失败:', error)
    }
  }
}