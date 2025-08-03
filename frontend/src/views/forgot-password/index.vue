<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <h1>TimeFlow 事件管理系统</h1>
        <p>找回密码</p>
      </div>
      <el-form
        ref="forgotPasswordForm"
        :model="forgotPasswordData"
        :rules="forgotPasswordRules"
        class="forgot-password-form"
        @keyup.enter="handleForgotPassword"
      >
        <div class="form-description">
          <p>请输入您的用户名，我们将向您的邮箱发送密码重置链接。</p>
        </div>
        <el-form-item prop="username">
          <el-input
            v-model="forgotPasswordData.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="forgot-password-button"
            size="large"
            @click="handleForgotPassword"
          >
            <span v-if="!loading">发送重置链接</span>
            <span v-else>发送中...</span>
          </el-button>
        </el-form-item>
        
        <div class="forgot-password-links">
          <router-link to="/login" class="forgot-password-link">返回登录</router-link>
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
const forgotPasswordForm = ref<FormInstance>()
const loading = ref(false)

const forgotPasswordData = reactive({
  username: ''
})

const forgotPasswordRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ]
}

/**
 * 处理忘记密码
 */
const handleForgotPassword = async () => {
  if (!forgotPasswordForm.value) return
  
  await forgotPasswordForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用请求密码重置API
        const success = await authStore.requestPasswordResetAction(forgotPasswordData.username)
        
        if (success) {
          // 成功消息已在store中显示
          router.push('/login')
        }
        // 错误信息已在store中处理和显示，这里不需要额外处理
      } catch (error) {
        console.error('请求密码重置过程中发生未预期的错误:', error)
        // 只处理未被store捕获的错误
        if (!authStore.error) {
          const errorMsg = error instanceof Error ? error.message : '请求密码重置过程中发生未知错误'
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
.forgot-password-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.forgot-password-box {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .forgot-password-container {
    padding: 10px;
  }
  
  .forgot-password-box {
    max-width: 100%;
    padding: 30px 20px;
    border-radius: 12px;
  }
  
  .forgot-password-header h1 {
    font-size: 24px !important;
  }
  
  .forgot-password-header p {
    font-size: 13px !important;
  }
}

@media (max-width: 480px) {
  .forgot-password-box {
    padding: 20px 15px;
    border-radius: 8px;
  }
  
  .forgot-password-header {
    margin-bottom: 30px !important;
  }
  
  .forgot-password-header h1 {
    font-size: 22px !important;
  }
  
  .form-description p {
    font-size: 13px !important;
  }
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 40px;
}

.forgot-password-header h1 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.forgot-password-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.form-description {
  margin-bottom: 20px;
  text-align: center;
}

.form-description p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.forgot-password-form {
  width: 100%;
}

.forgot-password-button {
  width: 100%;
  height: 48px;
}

.forgot-password-links {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  font-size: 14px;
}

.forgot-password-link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-password-link:hover {
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
.forgot-password-button:not(.is-loading):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.forgot-password-button {
  transition: all 0.3s ease;
}
</style>