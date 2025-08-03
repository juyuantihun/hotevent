<template>
  <div v-if="visible" class="api-error-feedback" :class="[`severity-${severity}`, { 'is-dismissible': dismissible }]">
    <div class="error-icon">
      <slot name="icon">
        <svg v-if="severity === 'error'" class="icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 11c-.55 0-1-.45-1-1V8c0-.55.45-1 1-1s1 .45 1 1v4c0 .55-.45 1-1 1zm1 4h-2v-2h2v2z"/>
        </svg>
        <svg v-else-if="severity === 'warning'" class="icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path fill="currentColor" d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
        </svg>
        <svg v-else-if="severity === 'info'" class="icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/>
        </svg>
        <svg v-else class="icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 15h2v-2h-2v2zm0-4h2V7h-2v6z"/>
        </svg>
      </slot>
    </div>
    
    <div class="error-content">
      <div v-if="title" class="error-title">{{ title }}</div>
      <div class="error-message">
        <slot>{{ message }}</slot>
      </div>
      <div v-if="details && showDetails" class="error-details">
        <pre>{{ formattedDetails }}</pre>
      </div>
      <div v-if="hasRetryOption || hasDetailsOption || hasFeedbackOption" class="error-actions">
        <button v-if="hasRetryOption" class="action-button retry-button" @click="handleRetry">
          <svg class="action-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path fill="currentColor" d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
          </svg>
          {{ retryText }}
        </button>
        <button v-if="hasDetailsOption" class="action-button details-button" @click="toggleDetails">
          <svg class="action-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path v-if="showDetails" fill="currentColor" d="M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"/>
            <path v-else fill="currentColor" d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z"/>
          </svg>
          {{ showDetails ? '隐藏详情' : '显示详情' }}
        </button>
        <button v-if="hasFeedbackOption" class="action-button feedback-button" @click="handleFeedback">
          <svg class="action-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path fill="currentColor" d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 12h-2v-2h2v2zm0-4h-2V6h2v4z"/>
          </svg>
          {{ feedbackText }}
        </button>
      </div>
    </div>
    
    <button v-if="dismissible" class="dismiss-button" @click="dismiss">
      <svg class="dismiss-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
        <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

// 定义组件属性
const props = defineProps({
  // 错误消息
  message: {
    type: String,
    default: ''
  },
  // 错误标题
  title: {
    type: String,
    default: ''
  },
  // 错误详情
  details: {
    type: [Object, String],
    default: null
  },
  // 错误严重程度
  severity: {
    type: String,
    default: 'error',
    validator: (value: string) => ['error', 'warning', 'info'].includes(value)
  },
  // 是否可关闭
  dismissible: {
    type: Boolean,
    default: true
  },
  // 是否显示
  modelValue: {
    type: Boolean,
    default: true
  },
  // 自动关闭时间（毫秒），0表示不自动关闭
  autoDismiss: {
    type: Number,
    default: 0
  },
  // 是否显示重试按钮
  hasRetryOption: {
    type: Boolean,
    default: false
  },
  // 重试按钮文本
  retryText: {
    type: String,
    default: '重试'
  },
  // 是否显示详情按钮
  hasDetailsOption: {
    type: Boolean,
    default: false
  },
  // 是否显示反馈按钮
  hasFeedbackOption: {
    type: Boolean,
    default: false
  },
  // 反馈按钮文本
  feedbackText: {
    type: String,
    default: '反馈问题'
  }
})

// 定义事件
const emit = defineEmits(['update:modelValue', 'retry', 'feedback', 'dismiss'])

// 是否显示
const visible = ref(props.modelValue)

// 是否显示详情
const showDetails = ref(false)

// 格式化的详情
const formattedDetails = computed(() => {
  if (!props.details) return ''
  
  if (typeof props.details === 'string') {
    return props.details
  }
  
  try {
    return JSON.stringify(props.details, null, 2)
  } catch (e) {
    return String(props.details)
  }
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  visible.value = newValue
})

// 关闭错误提示
function dismiss() {
  visible.value = false
  emit('update:modelValue', false)
  emit('dismiss')
}

// 处理重试
function handleRetry() {
  emit('retry')
}

// 处理反馈
function handleFeedback() {
  emit('feedback')
}

// 切换显示详情
function toggleDetails() {
  showDetails.value = !showDetails.value
}

// 组件挂载时
onMounted(() => {
  // 如果设置了自动关闭，则启动定时器
  if (props.autoDismiss > 0) {
    setTimeout(() => {
      dismiss()
    }, props.autoDismiss)
  }
})
</script>

<style scoped>
.api-error-feedback {
  display: flex;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  background-color: var(--bg-color, #fff);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
}

.api-error-feedback.severity-error {
  border-left: 4px solid var(--danger-color, #f56c6c);
}

.api-error-feedback.severity-warning {
  border-left: 4px solid var(--warning-color, #e6a23c);
}

.api-error-feedback.severity-info {
  border-left: 4px solid var(--info-color, #909399);
}

.error-icon {
  margin-right: 12px;
  display: flex;
  align-items: flex-start;
}

.icon {
  width: 24px;
  height: 24px;
}

.api-error-feedback.severity-error .icon {
  color: var(--danger-color, #f56c6c);
}

.api-error-feedback.severity-warning .icon {
  color: var(--warning-color, #e6a23c);
}

.api-error-feedback.severity-info .icon {
  color: var(--info-color, #909399);
}

.error-content {
  flex: 1;
  min-width: 0;
}

.error-title {
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--text-primary, #303133);
}

.error-message {
  color: var(--text-regular, #606266);
  word-break: break-word;
}

.error-details {
  margin-top: 8px;
  padding: 8px;
  background-color: var(--bg-color-light, #f5f7fa);
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}

.error-details pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: monospace;
}

.error-actions {
  display: flex;
  margin-top: 12px;
  gap: 8px;
  flex-wrap: wrap;
}

.action-button {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 4px;
  border: 1px solid var(--border-color, #dcdfe6);
  background-color: transparent;
  color: var(--text-regular, #606266);
  cursor: pointer;
  transition: all 0.3s;
}

.action-button:hover {
  color: var(--primary-color, #409eff);
  border-color: var(--primary-color, #409eff);
}

.action-icon {
  width: 14px;
  height: 14px;
  margin-right: 4px;
}

.retry-button {
  color: var(--primary-color, #409eff);
  border-color: var(--primary-color-light, #c6e2ff);
}

.retry-button:hover {
  background-color: var(--primary-color-light, #c6e2ff);
}

.dismiss-button {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-secondary, #909399);
  transition: color 0.3s;
}

.dismiss-button:hover {
  color: var(--text-regular, #606266);
}

.dismiss-icon {
  width: 16px;
  height: 16px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .api-error-feedback {
    background-color: var(--bg-color-dark, #1a1a1a);
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  }
  
  .error-title {
    color: var(--text-primary-dark, #e0e0e0);
  }
  
  .error-message {
    color: var(--text-regular-dark, #c0c0c0);
  }
  
  .error-details {
    background-color: var(--bg-color-light-dark, #2c2c2c);
  }
  
  .action-button {
    border-color: var(--border-color-dark, #4c4c4c);
    color: var(--text-regular-dark, #c0c0c0);
  }
  
  .action-button:hover {
    color: var(--primary-color-dark, #409eff);
    border-color: var(--primary-color-dark, #409eff);
  }
  
  .retry-button {
    border-color: var(--primary-color-light-dark, #18385f);
  }
  
  .retry-button:hover {
    background-color: var(--primary-color-light-dark, #18385f);
  }
}
</style>