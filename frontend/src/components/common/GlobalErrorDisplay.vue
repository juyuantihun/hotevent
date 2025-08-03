<template>
  <Transition name="fade">
    <div v-if="globalError" class="global-error-container" :class="{ 'is-overlay': overlay }">
      <ErrorState 
        :title="errorTitle" 
        :message="globalError"
        :small="false">
        <template #icon>
          <slot name="icon">
            <svg class="error-icon" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
              <path fill="currentColor" d="M24 4C12.95 4 4 12.95 4 24s8.95 20 20 20 20-8.95 20-20S35.05 4 24 4zm0 36c-8.84 0-16-7.16-16-16S15.16 8 24 8s16 7.16 16 16-7.16 16-16 16z"/>
              <path fill="currentColor" d="M24 14c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2s2-.9 2-2V16c0-1.1-.9-2-2-2z"/>
              <circle fill="currentColor" cx="24" cy="34" r="2"/>
            </svg>
          </slot>
        </template>
        <button class="dismiss-button" @click="dismissError">
          {{ dismissButtonText }}
        </button>
        <slot name="actions"></slot>
      </ErrorState>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/store'
import ErrorState from './ErrorState.vue'

// 定义组件属性
const props = defineProps({
  // 错误标题
  errorTitle: {
    type: String,
    default: '系统错误'
  },
  // 关闭按钮文本
  dismissButtonText: {
    type: String,
    default: '关闭'
  },
  // 是否作为覆盖层显示
  overlay: {
    type: Boolean,
    default: true
  },
  // 自动关闭时间（毫秒），0表示不自动关闭
  autoDismiss: {
    type: Number,
    default: 0
  }
})

// 获取应用状态
const appStore = useAppStore()

// 全局错误信息
const globalError = computed(() => appStore.globalError)

// 关闭错误显示
function dismissError() {
  appStore.setGlobalError(null)
}

// 如果设置了自动关闭，则启动定时器
if (props.autoDismiss > 0 && globalError.value) {
  setTimeout(() => {
    if (globalError.value) {
      dismissError()
    }
  }, props.autoDismiss)
}
</script>

<style scoped>
.global-error-container {
  padding: 16px;
  background-color: var(--bg-color, #fff);
  border: 1px solid var(--danger-color, #f56c6c);
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 16px;
}

.global-error-container.is-overlay {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 9999;
  min-width: 300px;
  max-width: 80%;
  background-color: var(--bg-color, #fff);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.error-icon {
  width: 64px;
  height: 64px;
  color: var(--danger-color, #f56c6c);
}

.dismiss-button {
  padding: 8px 16px;
  background-color: var(--primary-color, #409eff);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.dismiss-button:hover {
  background-color: var(--primary-hover, #66b1ff);
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -60%);
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .global-error-container {
    background-color: var(--bg-color-dark, #1a1a1a);
    border-color: var(--danger-color-dark, #f56c6c);
  }
  
  .dismiss-button {
    background-color: var(--primary-color-dark, #409eff);
  }
  
  .dismiss-button:hover {
    background-color: var(--primary-hover-dark, #66b1ff);
  }
}
</style>