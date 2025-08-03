<template>
  <div class="progress-monitor" :class="{ 'is-active': isActive }">
    <!-- 进度头部 -->
    <div class="progress-header">
      <div class="progress-title">
        <el-icon class="title-icon" :class="statusIconClass">
          <component :is="statusIcon" />
        </el-icon>
        <h4>{{ title }}</h4>
      </div>
      
      <div class="progress-actions">
        <el-button 
          v-if="showCancelButton && canCancel"
          type="danger" 
          size="small" 
          :loading="cancelling"
          @click="handleCancel"
        >
          {{ cancelling ? '取消中...' : '取消' }}
        </el-button>
        
        <el-button 
          v-if="showCloseButton && !isActive"
          size="small" 
          @click="handleClose"
        >
          关闭
        </el-button>
      </div>
    </div>
    
    <!-- 进度条 -->
    <div class="progress-bar-container">
      <el-progress 
        :percentage="progress" 
        :status="progressStatus"
        :stroke-width="strokeWidth"
        :show-text="showProgressText"
        :format="formatProgress"
      />
      
      <!-- 进度详情 -->
      <div class="progress-details" v-if="showDetails">
        <div class="current-step">
          <span class="step-label">当前步骤：</span>
          <span class="step-text">{{ currentStep }}</span>
        </div>
        
        <div class="progress-message" v-if="message">
          {{ message }}
        </div>
      </div>
    </div>
    
    <!-- 统计信息 -->
    <div class="progress-stats" v-if="showStats && stats.length > 0">
      <div 
        class="stat-item" 
        v-for="stat in stats" 
        :key="stat.label"
      >
        <span class="stat-label">{{ stat.label }}：</span>
        <span class="stat-value" :class="stat.valueClass">
          {{ stat.value }}
        </span>
      </div>
    </div>
    
    <!-- 时间信息 -->
    <div class="progress-time" v-if="showTimeInfo">
      <div class="time-item">
        <span class="time-label">已耗时：</span>
        <span class="time-value">{{ formatDuration(elapsedTime) }}</span>
      </div>
      
      <div class="time-item" v-if="estimatedTime > 0">
        <span class="time-label">预计剩余：</span>
        <span class="time-value">{{ formatDuration(estimatedTime) }}</span>
      </div>
    </div>
    
    <!-- 错误信息 -->
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
      class="progress-error"
    />
    
    <!-- 警告信息 -->
    <el-alert
      v-if="warning"
      :title="warning"
      type="warning"
      :closable="true"
      show-icon
      class="progress-warning"
      @close="warning = ''"
    />
    
    <!-- 成功信息 -->
    <el-alert
      v-if="success"
      :title="success"
      type="success"
      :closable="true"
      show-icon
      class="progress-success"
      @close="success = ''"
    />
    
    <!-- 详细日志 -->
    <div class="progress-logs" v-if="showLogs && logs.length > 0">
      <div class="logs-header">
        <span>详细日志</span>
        <el-button 
          size="small" 
          text 
          @click="clearLogs"
        >
          清空
        </el-button>
      </div>
      
      <div class="logs-content">
        <div 
          class="log-item" 
          v-for="(log, index) in logs" 
          :key="index"
          :class="`log-${log.level}`"
        >
          <span class="log-time">{{ formatTime(log.timestamp) }}</span>
          <span class="log-level">{{ log.level.toUpperCase() }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { 
  Loading, 
  CircleCheck, 
  CircleClose, 
  Warning, 
  Clock,
  Refresh
} from '@element-plus/icons-vue'

// 定义接口
interface ProgressStat {
  label: string
  value: string | number
  valueClass?: string
}

interface ProgressLog {
  timestamp: number
  level: 'info' | 'warn' | 'error' | 'success'
  message: string
}

// 定义属性
interface Props {
  // 基础属性
  title?: string
  progress?: number
  status?: 'active' | 'success' | 'exception' | 'warning'
  currentStep?: string
  message?: string
  
  // 时间相关
  startTime?: number
  elapsedTime?: number
  estimatedTime?: number
  
  // 统计信息
  stats?: ProgressStat[]
  
  // 错误和警告
  error?: string
  warning?: string
  success?: string
  
  // 显示选项
  showDetails?: boolean
  showStats?: boolean
  showTimeInfo?: boolean
  showLogs?: boolean
  showProgressText?: boolean
  showCancelButton?: boolean
  showCloseButton?: boolean
  
  // 样式选项
  strokeWidth?: number
  
  // 状态控制
  canCancel?: boolean
  cancelling?: boolean
  
  // 日志
  logs?: ProgressLog[]
}

const props = withDefaults(defineProps<Props>(), {
  title: '处理中...',
  progress: 0,
  status: 'active',
  currentStep: '',
  message: '',
  startTime: 0,
  elapsedTime: 0,
  estimatedTime: 0,
  stats: () => [],
  showDetails: true,
  showStats: true,
  showTimeInfo: true,
  showLogs: false,
  showProgressText: true,
  showCancelButton: true,
  showCloseButton: true,
  strokeWidth: 8,
  canCancel: true,
  cancelling: false,
  logs: () => []
})

// 定义事件
const emit = defineEmits<{
  cancel: []
  close: []
  'log-clear': []
}>()

// 响应式状态
const warning = ref(props.warning || '')
const success = ref(props.success || '')

// 计算属性
const isActive = computed(() => {
  return props.status === 'active' && props.progress < 100
})

const progressStatus = computed(() => {
  switch (props.status) {
    case 'success':
      return 'success'
    case 'exception':
      return 'exception'
    case 'warning':
      return 'warning'
    default:
      return undefined
  }
})

const statusIcon = computed(() => {
  switch (props.status) {
    case 'success':
      return CircleCheck
    case 'exception':
      return CircleClose
    case 'warning':
      return Warning
    case 'active':
      return props.progress < 100 ? Loading : CircleCheck
    default:
      return Clock
  }
})

const statusIconClass = computed(() => {
  return {
    'status-active': props.status === 'active',
    'status-success': props.status === 'success',
    'status-error': props.status === 'exception',
    'status-warning': props.status === 'warning'
  }
})

// 方法
const handleCancel = () => {
  emit('cancel')
}

const handleClose = () => {
  emit('close')
}

const clearLogs = () => {
  emit('log-clear')
}

const formatProgress = (percentage: number): string => {
  return `${percentage}%`
}

const formatDuration = (milliseconds: number): string => {
  if (milliseconds < 1000) return '< 1秒'
  
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

const formatTime = (timestamp: number): string => {
  return new Date(timestamp).toLocaleTimeString()
}

// 监听属性变化
watch(() => props.warning, (newWarning) => {
  warning.value = newWarning || ''
})

watch(() => props.success, (newSuccess) => {
  success.value = newSuccess || ''
})

// 自动滚动日志到底部
watch(() => props.logs, () => {
  if (props.showLogs && props.logs.length > 0) {
    setTimeout(() => {
      const logsContent = document.querySelector('.logs-content')
      if (logsContent) {
        logsContent.scrollTop = logsContent.scrollHeight
      }
    }, 100)
  }
}, { deep: true })
</script>

<style scoped>
.progress-monitor {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.progress-monitor.is-active {
  border-left: 4px solid #409eff;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.title-icon.status-active {
  color: #409eff;
  animation: spin 2s linear infinite;
}

.title-icon.status-success {
  color: #67c23a;
}

.title-icon.status-error {
  color: #f56c6c;
}

.title-icon.status-warning {
  color: #e6a23c;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.progress-title h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.progress-actions {
  display: flex;
  gap: 10px;
}

.progress-bar-container {
  margin-bottom: 20px;
}

.progress-details {
  margin-top: 15px;
}

.current-step {
  margin-bottom: 10px;
  font-size: 14px;
}

.step-label {
  color: #909399;
  margin-right: 8px;
}

.step-text {
  color: #303133;
  font-weight: 500;
}

.progress-message {
  padding: 10px;
  background-color: #ecf5ff;
  border-left: 4px solid #409eff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  margin-top: 10px;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.progress-time {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #fafafa;
  border-radius: 4px;
  font-size: 13px;
}

.time-label {
  color: #909399;
  margin-right: 8px;
}

.time-value {
  color: #303133;
  font-weight: 500;
}

.progress-error,
.progress-warning,
.progress-success {
  margin-bottom: 15px;
}

.progress-logs {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.logs-content {
  max-height: 200px;
  overflow-y: auto;
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px;
}

.log-item {
  display: flex;
  gap: 10px;
  margin-bottom: 5px;
  font-size: 12px;
  line-height: 1.4;
}

.log-time {
  color: #909399;
  min-width: 80px;
}

.log-level {
  min-width: 50px;
  font-weight: 600;
}

.log-message {
  flex: 1;
  color: #303133;
}

.log-info .log-level {
  color: #409eff;
}

.log-warn .log-level {
  color: #e6a23c;
}

.log-error .log-level {
  color: #f56c6c;
}

.log-success .log-level {
  color: #67c23a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .progress-stats {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .progress-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .progress-time {
    flex-direction: column;
    gap: 5px;
  }
}

/* 滚动条样式 */
.logs-content::-webkit-scrollbar {
  width: 6px;
}

.logs-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.logs-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.logs-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>