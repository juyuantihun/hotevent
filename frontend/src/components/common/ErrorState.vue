<template>
  <div class="error-state" :class="{ 'is-fullscreen': fullscreen }">
    <div class="error-icon">
      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 22C6.477 22 2 17.523 2 12S6.477 2 12 2s10 4.477 10 10-4.477 10-10 10zm0-2a8 8 0 100-16 8 8 0 000 16zm-1-5h2v2h-2v-2zm0-8h2v6h-2V7z" fill="currentColor"/>
      </svg>
    </div>
    <h3 class="error-title">{{ title || '出错了' }}</h3>
    <p class="error-message">{{ message || '发生了一个错误，请稍后再试' }}</p>
    <div class="error-actions">
      <slot name="actions">
        <button class="error-retry-button" @click="handleRetry">
          重试
        </button>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps({
  title: {
    type: String,
    default: '出错了'
  },
  message: {
    type: String,
    default: '发生了一个错误，请稍后再试'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()

const handleRetry = () => {
  // 刷新当前页面
  router.go(0)
}
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  text-align: center;
}

.error-state.is-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #fff;
  z-index: 9999;
}

.error-icon {
  width: 64px;
  height: 64px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.error-title {
  font-size: 20px;
  font-weight: 500;
  margin: 0 0 8px;
  color: #303133;
}

.error-message {
  font-size: 14px;
  color: #606266;
  margin: 0 0 24px;
  max-width: 500px;
}

.error-actions {
  display: flex;
  justify-content: center;
}

.error-retry-button {
  padding: 8px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.error-retry-button:hover {
  background-color: #66b1ff;
}
</style>