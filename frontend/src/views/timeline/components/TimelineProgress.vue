<template>
  <div class="timeline-progress">
    <div class="progress-status">
      <el-icon v-if="status === 'generating'" class="rotating">
        <Loading />
      </el-icon>
      <el-icon v-else-if="status === 'completed'" class="success-icon">
        <CircleCheck />
      </el-icon>
      <el-icon v-else-if="status === 'failed'" class="error-icon">
        <CircleClose />
      </el-icon>
      <span class="status-text">{{ getStatusText() }}</span>
    </div>

    <el-progress 
      :percentage="percentage" 
      :status="progressStatus"
      :indeterminate="status === 'generating' && percentage < 10"
      :duration="3"
    />

    <div class="progress-info">
      <div v-if="status === 'generating'">
        <p>正在处理 <strong>{{ regionCount }}</strong> 个地区的事件数据</p>
        <p v-if="regions && regions.length > 0">地区: <strong>{{ regions.join(', ') }}</strong></p>
        <p v-if="eventCount">已处理事件数: {{ eventCount }}</p>
        <p v-if="relationCount">已建立关系数: {{ relationCount }}</p>
        <p v-if="currentStep">当前步骤: {{ currentStep }}</p>
      </div>
      <div v-else-if="status === 'completed'">
        <p>时间线 <strong>{{ timelineName }}</strong> 已成功生成！</p>
        <p>共处理 {{ eventCount || 0 }} 个事件，建立 {{ relationCount || 0 }} 个关系</p>
      </div>
      <div v-else-if="status === 'failed'">
        <p>时间线生成失败</p>
        <p class="error-message">{{ errorMessage || '未知错误' }}</p>
      </div>
    </div>

    <div class="progress-actions">
      <el-button v-if="status === 'generating'" @click="handleCancel">
        取消生成
      </el-button>
      <el-button 
        v-if="status === 'completed'" 
        type="primary" 
        @click="handleView"
      >
        查看时间线
      </el-button>
      <el-button 
        v-if="status === 'completed' || status === 'failed'" 
        @click="handleClose"
      >
        关闭
      </el-button>
      <el-button 
        v-if="status === 'failed'" 
        type="primary" 
        @click="handleRetry"
      >
        重试
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, defineEmits } from 'vue'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (value: string) => ['generating', 'completed', 'failed'].includes(value)
  },
  percentage: {
    type: Number,
    default: 0
  },
  timelineName: {
    type: String,
    default: ''
  },
  regionCount: {
    type: Number,
    default: 0
  },
  regions: {
    type: Array as () => string[],
    default: () => []
  },
  eventCount: {
    type: Number,
    default: 0
  },
  relationCount: {
    type: Number,
    default: 0
  },
  currentStep: {
    type: String,
    default: ''
  },
  errorMessage: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['cancel', 'view', 'close', 'retry'])

// 计算属性
const progressStatus = computed(() => {
  if (props.status === 'completed') return 'success'
  if (props.status === 'failed') return 'exception'
  return ''
})

/**
 * 获取状态文本
 */
const getStatusText = () => {
  if (props.status === 'generating') return '正在生成时间线...'
  if (props.status === 'completed') return '时间线生成完成'
  if (props.status === 'failed') return '时间线生成失败'
  return ''
}

/**
 * 处理取消生成
 */
const handleCancel = () => {
  emit('cancel')
}

/**
 * 处理查看时间线
 */
const handleView = () => {
  emit('view')
}

/**
 * 处理关闭进度对话框
 */
const handleClose = () => {
  emit('close')
}

/**
 * 处理重试
 */
const handleRetry = () => {
  emit('retry')
}
</script>

<style scoped lang="scss">
.timeline-progress {
  .progress-status {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    
    .rotating {
      animation: rotate 1.5s linear infinite;
    }
    
    .success-icon {
      color: #67c23a;
    }
    
    .error-icon {
      color: #f56c6c;
    }
    
    .status-text {
      margin-left: 8px;
      font-size: 16px;
      font-weight: 500;
    }
  }
  
  .progress-info {
    margin: 16px 0;
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    p {
      margin: 8px 0;
    }
    
    .error-message {
      color: #f56c6c;
    }
  }
  
  .progress-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 16px;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>