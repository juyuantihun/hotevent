<template>
  <div class="reset-password-container">
    <div class="reset-password-box">
      <div class="reset-password-header">
        <h1>TimeFlow 事件管理系统</h1>
        <p>重置密码</p>
      </div>
      <el-form
        ref="resetPasswordForm"
        :model="resetPasswordData"
        :rules="resetPasswordRules"
        class="reset-password-form"
        @keyup.enter="handleResetPassword"
      >
        <div class="form-description">
          <p>请设置您的新密码。</p>
        </div>
        <el-form-item prop="newPassword">
          <el-input
            v-model="resetPasswordData.newPassword"
            type="password"
            placeholder="请输入新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmNewPassword">
          <el-input
            v-model="resetPasswordData.confirmNewPassword"
            type="password"
            placeholder="请确认新密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            class="reset-password-button"
            size="large"
            @click="handleResetPassword"
          >
            重置密码
          </el-button>
        </el-form-item>
        
        <div class="reset-password-links">
          <router-link to="/login" class="reset-password-link">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const resetPasswordForm = ref<FormInstance>()
const loading = ref(false)

const resetPasswordData = reactive({
  token: '',
  newPassword: '',
  confirmNewPassword: ''
})

// 从路由参数中获取重置令牌
onMounted(() => {
  const token = route.params.token
  if (token && typeof token === 'string') {
    resetPasswordData.token = token
  } else {
    ElMessage.error('无效的重置链接')
    router.push('/login')
  }
})

const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else {
    if (resetPasswordData.confirmNewPassword !== '') {
      if (!resetPasswordForm.value) return
      resetPasswordForm.value.validateField('confirmNewPassword', () => null)
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== resetPasswordData.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const resetPasswordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

/**
 * 处理重置密码
 */
const handleResetPassword = async () => {
  if (!resetPasswordForm.value) return
  
  await resetPasswordForm.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用重置密码API
        const success = await authStore.resetPasswordAction({
          token: resetPasswordData.token,
          newPassword: resetPasswordData.newPassword,
          confirmNewPassword: resetPasswordData.confirmNewPassword
        })
        
        if (success) {
          ElMessage.success('密码重置成功，请使用新密码登录')
          router.push('/login')
        }
      } catch (error) {
        console.error('重置密码失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.reset-password-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.reset-password-box {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.reset-password-header {
  text-align: center;
  margin-bottom: 40px;
}

.reset-password-header h1 {
  color: #333;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.reset-password-header p {
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

.reset-password-form {
  width: 100%;
}

.reset-password-button {
  width: 100%;
  height: 48px;
}

.reset-password-links {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  font-size: 14px;
}

.reset-password-link {
  color: #409eff;
  text-decoration: none;
  transition: color 0.3s;
}

.reset-password-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button) {
  border-radius: 8px;
}
</style>