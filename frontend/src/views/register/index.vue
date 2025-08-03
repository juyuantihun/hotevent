<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>TimeFlow 事件管理系统</h1>
        <p>创建新账户</p>
      </div>
      <el-form
        ref="registerForm"
        :model="registerData"
        :rules="registerRules"
        class="register-form"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerData.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerData.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerData.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="register-button"
            size="large"
            @click="handleRegister"
          >
            <span v-if="!loading">注册</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>
        
        <div class="register-links">
          <span>已有账户？</span>
          <router-link to="/login" class="register-link">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'

const router = useRouter()
const authStore = useAuthStore()
const registerForm = ref<FormInstance>()
const loading = ref(false)

const registerData = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerData.confirmPassword !== '') {
      if (!registerForm.value) return
      registerForm.value.validateField('confirmPassword', () => null)
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerData.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  if (!registerForm.value) return
  
  await registerForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用注册API
        const success = await authStore.registerAction({
          username: registerData.username,
          password: registerData.password,
          confirmPassword: registerData.confirmPassword
        })
        
        if (success) {
          // 注册成功消息已在store中显示
          router.push('/login')
        }
        // 错误信息已在store中处理和显示，这里不需要额外处理
      } catch (error) {
        console.error('注册过程中发生未预期的错误:', error)
        // 只处理未被store捕获的错误
        if (!authStore.error) {
          const errorMsg = error instanceof Error ? error.message : '注册过程中发生未知错误'
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
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-box {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: 10px;
  }
  
  .register-box {
    max-width: 100%;
    padding: 30px 20px;
    border-radius: 12px;
  }
  
  .register-header h1 {
    font-size: 24px !important;
  }
  
  .register-header p {
    font-size: 13px !important;
  }
}

@media (max-width: 480px) {
  .register-box {
    padding: 20px 15px;
    border-radius: 8px;
  }
  
  .register-header {
    margin-bottom: 30px !important;
  }
  
  .register-header h1 {
    font-size: 22px !important;
  }
  
  .register-links {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h1 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.register-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.register-form {
  width: 100%;
}

.register-button {
  width: 100%;
  height: 48px;
}

.register-links {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  font-size: 14px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  margin-left: 5px;
  transition: color 0.3s;
}

.register-link:hover {
  color: #66b1ff;
  text-decoration: underline;
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
.register-button:not(.is-loading):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.register-button {
  transition: all 0.3s ease;
}
</style>