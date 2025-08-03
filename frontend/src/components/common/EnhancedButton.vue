<template>
  <el-button
    :type="type"
    :size="size"
    :icon="currentIcon"
    :loading="buttonState.loading"
    :disabled="buttonState.disabled || disabled"
    :class="[
      'enhanced-button',
      {
        'is-submitting': buttonState.loading,
        'is-disabled': buttonState.disabled,
        'has-countdown': remainingCooldown > 0
      }
    ]"
    @click="handleClick"
  >
    <span v-if="buttonState.loading && loadingText">
      {{ loadingText }}
    </span>
    <span v-else-if="remainingCooldown > 0">
      {{ cooldownText || `请等待 ${remainingCooldown}s` }}
    </span>
    <span v-else>
      <slot>{{ buttonState.text || text }}</slot>
    </span>
    
    <!-- 点击次数提示 -->
    <span 
      v-if="showClickCount && buttonState.clickCount > 1" 
      class="click-count"
    >
      ({{ buttonState.clickCount }})
    </span>
  </el-button>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { duplicationPrevention } from '@/utils/duplicationPrevention'

// 定义属性
interface Props {
  // 基础属性
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'large' | 'default' | 'small'
  icon?: string
  disabled?: boolean
  text?: string
  
  // 防重复提交相关
  buttonKey: string // 按钮唯一标识
  submissionKey?: string // 提交标识，用于防重复提交检查
  submissionData?: any // 提交数据，用于生成指纹
  cooldown?: number // 冷却时间（毫秒）
  
  // 状态文本
  loadingText?: string
  cooldownText?: string
  
  // 显示选项
  showClickCount?: boolean
  autoReset?: boolean // 是否自动重置状态
}

const props = withDefaults(defineProps<Props>(), {
  type: 'default',
  size: 'default',
  disabled: false,
  text: '',
  cooldown: 3000,
  loadingText: '处理中...',
  showClickCount: true,
  autoReset: true
})

// 定义事件
const emit = defineEmits<{
  click: [event: MouseEvent]
  submit: [data: any]
  blocked: [reason: string]
}>()

// 响应式状态
const buttonState = ref(duplicationPrevention.getButtonState(props.buttonKey))
const remainingCooldown = ref(0)
const currentIcon = ref(props.icon)

// 计算属性
const canClick = computed(() => {
  if (props.disabled || buttonState.value.disabled || buttonState.value.loading) {
    return false
  }
  
  if (props.submissionKey && props.submissionData) {
    const status = duplicationPrevention.getSubmissionStatus(props.submissionKey, props.submissionData)
    return status.canSubmit
  }
  
  return true
})

// 定时器
let cooldownTimer: NodeJS.Timeout | null = null
let stateUpdateTimer: NodeJS.Timeout | null = null

// 处理点击事件
const handleClick = (event: MouseEvent) => {
  // 检查按钮点击频率
  if (!duplicationPrevention.handleButtonClick(props.buttonKey)) {
    emit('blocked', '点击过于频繁')
    return
  }
  
  // 检查提交状态
  if (props.submissionKey && props.submissionData) {
    if (!duplicationPrevention.canSubmit(props.submissionKey, props.submissionData, props.cooldown)) {
      emit('blocked', '请求过于频繁')
      return
    }
  }
  
  // 更新按钮状态
  updateButtonState()
  
  // 触发点击事件
  emit('click', event)
  
  // 如果有提交数据，触发提交事件
  if (props.submissionData) {
    emit('submit', props.submissionData)
  }
}

// 更新按钮状态
const updateButtonState = () => {
  buttonState.value = duplicationPrevention.getButtonState(props.buttonKey)
  
  // 更新冷却倒计时
  if (props.submissionKey && props.submissionData) {
    const status = duplicationPrevention.getSubmissionStatus(props.submissionKey, props.submissionData)
    remainingCooldown.value = Math.ceil(status.remainingCooldown / 1000)
    
    // 启动倒计时
    if (remainingCooldown.value > 0) {
      startCooldownTimer()
    }
  }
}

// 启动冷却倒计时
const startCooldownTimer = () => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
  
  cooldownTimer = setInterval(() => {
    if (remainingCooldown.value > 0) {
      remainingCooldown.value--
    } else {
      clearInterval(cooldownTimer!)
      cooldownTimer = null
    }
  }, 1000)
}

// 启动状态更新定时器
const startStateUpdateTimer = () => {
  if (stateUpdateTimer) {
    clearInterval(stateUpdateTimer)
  }
  
  stateUpdateTimer = setInterval(() => {
    updateButtonState()
  }, 500) // 每500ms更新一次状态
}

// 设置加载状态
const setLoading = (loading: boolean, text?: string) => {
  duplicationPrevention.setButtonState(props.buttonKey, {
    loading,
    text: text || props.loadingText
  })
  updateButtonState()
}

// 设置禁用状态
const setDisabled = (disabled: boolean) => {
  duplicationPrevention.setButtonState(props.buttonKey, { disabled })
  updateButtonState()
}

// 重置按钮状态
const reset = () => {
  duplicationPrevention.resetButtonState(props.buttonKey)
  updateButtonState()
  remainingCooldown.value = 0
  
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
    cooldownTimer = null
  }
}

// 启用按钮
const enable = () => {
  duplicationPrevention.enableButton(props.buttonKey)
  updateButtonState()
}

// 监听属性变化
watch(() => props.icon, (newIcon) => {
  currentIcon.value = newIcon
})

// 暴露方法给父组件
defineExpose({
  setLoading,
  setDisabled,
  reset,
  enable,
  buttonState: computed(() => buttonState.value),
  canClick
})

// 生命周期
onMounted(() => {
  updateButtonState()
  startStateUpdateTimer()
})

onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
  if (stateUpdateTimer) {
    clearInterval(stateUpdateTimer)
  }
  
  // 如果设置了自动重置，清理状态
  if (props.autoReset) {
    duplicationPrevention.resetButtonState(props.buttonKey)
  }
})
</script>

<style scoped>
.enhanced-button {
  position: relative;
  transition: all 0.3s ease;
}

.enhanced-button.is-submitting {
  cursor: not-allowed;
}

.enhanced-button.is-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.enhanced-button.has-countdown {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.enhanced-button.has-countdown:hover {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.click-count {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}

/* 加载动画 */
.enhanced-button.is-submitting::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: loading-shimmer 1.5s infinite;
}

@keyframes loading-shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

/* 倒计时动画 */
.enhanced-button.has-countdown {
  animation: countdown-pulse 1s infinite;
}

@keyframes countdown-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>