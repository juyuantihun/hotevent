<template>
  <slot v-if="!error"></slot>
  <div v-else class="error-boundary">
    <ErrorState 
      :title="errorTitle" 
      :message="errorMessage"
      :small="small">
      <template #icon v-if="$slots.errorIcon">
        <slot name="errorIcon"></slot>
      </template>
      <template v-if="$slots.errorActions || showResetButton">
        <button 
          v-if="showResetButton" 
          class="reset-button"
          @click="resetError">
          重试
        </button>
        <slot name="errorActions"></slot>
      </template>
    </ErrorState>
  </div>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured, provide, inject, onMounted } from 'vue'
import ErrorState from './ErrorState.vue'
import { useAppStore } from '@/store'

// 定义组件属性
const props = defineProps({
  // 是否将错误上报到全局状态
  reportToGlobal: {
    type: Boolean,
    default: false
  },
  // 自定义错误标题
  errorTitle: {
    type: String,
    default: '组件渲染错误'
  },
  // 自定义错误消息
  errorMessage: {
    type: String,
    default: '组件渲染过程中发生错误，请尝试刷新页面或联系管理员'
  },
  // 是否显示重置按钮
  showResetButton: {
    type: Boolean,
    default: true
  },
  // 是否使用小尺寸
  small: {
    type: Boolean,
    default: false
  },
  // 是否记录错误日志
  logError: {
    type: Boolean,
    default: true
  }
})

// 定义事件
const emit = defineEmits(['error', 'reset'])

// 错误状态
const error = ref<Error | null>(null)
const errorInfo = ref<string | null>(null)

// 获取应用状态
const appStore = useAppStore()

// 父级错误边界
const parentErrorBoundary = inject('errorBoundary', null)

// 提供给子组件的错误边界
provide('errorBoundary', {
  captureError: (err: Error, info: string) => {
    handleError(err, info)
    return true
  }
})

/**
 * 处理错误
 * @param err 错误对象
 * @param info 错误信息
 */
function handleError(err: Error, info: string) {
  // 设置错误状态
  error.value = err
  errorInfo.value = info
  
  // 发出错误事件
  emit('error', { error: err, info })
  
  // 如果需要上报到全局状态
  if (props.reportToGlobal) {
    appStore.setGlobalError(`${props.errorTitle}: ${err.message}`)
  }
  
  // 记录错误日志
  if (props.logError) {
    console.error('[ErrorBoundary]', err, info)
    
    // 这里可以添加错误上报到服务器的逻辑
    // logErrorToServer(err, info)
  }
  
  return true
}

/**
 * 重置错误状态
 */
function resetError() {
  error.value = null
  errorInfo.value = null
  
  // 如果已上报到全局状态，清除全局错误
  if (props.reportToGlobal) {
    appStore.setGlobalError(null)
  }
  
  // 发出重置事件
  emit('reset')
}

// 捕获子组件错误
onErrorCaptured((err, instance, info) => {
  // 如果父级错误边界已经处理了这个错误，则不再处理
  if (parentErrorBoundary && parentErrorBoundary.captureError(err, info)) {
    return false
  }
  
  return handleError(err, info)
})

// 组件挂载时
onMounted(() => {
  // 如果已有全局错误，可以在这里处理
})
</script>

<style scoped>
.error-boundary {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.reset-button {
  padding: 8px 16px;
  background-color: var(--primary-color, #409eff);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.reset-button:hover {
  background-color: var(--primary-hover, #66b1ff);
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .reset-button {
    background-color: var(--primary-color-dark, #409eff);
  }
  
  .reset-button:hover {
    background-color: var(--primary-hover-dark, #66b1ff);
  }
}
</style>