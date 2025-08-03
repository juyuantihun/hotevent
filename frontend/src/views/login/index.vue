<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>TimeFlow 事件管理系统</h1>
        <p>国际热点事件管理与分析平台</p>
      </div>
      <el-form
        ref="loginForm"
        :model="loginData"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginData.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginData.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginData.rememberMe">
            记住密码
          </el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="login-button"
            size="large"
            @click="handleLogin"
          >
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
        
        <div class="login-links">
          <router-link to="/register" class="login-link">注册新账户</router-link>
          <span class="divider">|</span>
          <router-link to="/forgot-password" class="login-link">忘记密码</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { validateUsername, validatePassword, isSafeURL, escapeHtml } from '@/utils/security'

const router = useRouter()
const authStore = useAuthStore()
const loginForm = ref<FormInstance>()
const loading = ref(false)

const loginData = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        const result = validateUsername(value)
        if (!result.valid) {
          callback(new Error(result.message))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        const result = validatePassword(value)
        if (!result.valid) {
          callback(new Error(result.message))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 检查是否已登录，如果已登录则跳转到首页或重定向页面
onMounted(async () => {
  // 获取重定向路径（如果有）
  const route = router.currentRoute.value
  let redirectPath = route.query.redirect ? String(route.query.redirect) : '/dashboard'
  
  // 验证重定向路径的有效性
  // 防止重定向到登录页面或外部URL
  if (redirectPath === '/login' || !isSafeURL(redirectPath)) {
    console.warn('检测到无效的重定向路径，将重定向到首页')
    redirectPath = '/dashboard'
  }
  
  // 确保路径以 / 开头
  if (!redirectPath.startsWith('/')) {
    redirectPath = '/' + redirectPath
  }
  
  // 如果已经登录，直接跳转到重定向页面或首页
  if (await authStore.checkSession()) {
    await router.push(redirectPath).catch(err => {
      console.error('导航失败:', err)
      // 如果导航失败，回退到首页
      router.push('/dashboard')
    })
    return
  }
  
  // 如果有记住的用户名，自动填充
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    loginData.username = rememberedUsername
    loginData.rememberMe = true
  }
})

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!loginForm.value) return
  
  await loginForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用登录API
        const success = await authStore.loginAction({
          username: loginData.username,
          password: loginData.password,
          rememberMe: loginData.rememberMe
        })
        
        if (success) {
          // 登录成功消息已在store中显示，这里不重复显示
          console.log('登录成功，当前认证状态:', {
            hasToken: !!authStore.token,
            hasUserInfo: !!authStore.userInfo,
            userInfo: authStore.userInfo
          })
          
          // 获取重定向路径（如果有）
          const route = router.currentRoute.value
          let redirectPath = route.query.redirect ? String(route.query.redirect) : '/dashboard'
          
          // 验证重定向路径的有效性
          // 防止重定向到登录页面或外部URL
          if (redirectPath === '/login' || !isSafeURL(redirectPath)) {
            console.warn('检测到无效的重定向路径，将重定向到首页')
            redirectPath = '/dashboard'
          }
          
          // 确保路径以 / 开头
          if (!redirectPath.startsWith('/')) {
            redirectPath = '/' + redirectPath
          }
          
          console.log('准备跳转到:', redirectPath)
          
          // 登录成功后立即跳转到重定向页面或首页
          try {
            await router.push(redirectPath)
            console.log('路由跳转成功')
          } catch (err) {
            console.error('导航失败:', err)
            // 如果导航失败，回退到首页
            await router.push('/dashboard')
          }
        }
        // 错误信息已在store中处理和显示，这里不需要额外处理
      } catch (error) {
        console.error('登录过程中发生未预期的错误:', error)
        // 只处理未被store捕获的错误
        if (!authStore.error) {
          const errorMsg = error instanceof Error ? error.message : '登录过程中发生未知错误'
          ElMessage.error(errorMsg)
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    padding: 10px;
  }
  
  .login-box {
    max-width: 100%;
    padding: 30px 20px;
    border-radius: 12px;
  }
  
  .login-header h1 {
    font-size: 24px !important;
  }
  
  .login-header p {
    font-size: 13px !important;
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 20px 15px;
    border-radius: 8px;
  }
  
  .login-header {
    margin-bottom: 30px !important;
  }
  
  .login-header h1 {
    font-size: 22px !important;
  }
  
  .login-links {
    flex-direction: column;
    gap: 8px;
  }
  
  .divider {
    display: none;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.login-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-button {
  width: 100%;
  height: 48px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}

/* 加载状态样式优化 */
:deep(.el-button.is-loading) {
  position: relative;
}

:deep(.el-button.is-loading .el-icon) {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 按钮悬停效果 */
.login-button:not(.is-loading):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.login-button {
  transition: all 0.3s ease;
}

.login-links {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  font-size: 14px;
}

.login-link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s;
}

.login-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.divider {
  margin: 0 10px;
  color: #dcdfe6;
}
</style> 