<template>
  <div class="api-error-demo">
    <h3>API错误处理演示</h3>
    <p>本组件演示了增强的API错误处理功能，包括错误反馈和请求重试机制。</p>
    
    <div class="demo-section">
      <h4>基本API错误处理</h4>
      <div class="button-group">
        <el-button type="primary" @click="sendNormalRequest" :loading="loading.normal">
          发送正常请求
        </el-button>
        <el-button type="danger" @click="sendErrorRequest" :loading="loading.error">
          触发API错误
        </el-button>
        <el-button type="warning" @click="sendTimeoutRequest" :loading="loading.timeout">
          触发请求超时
        </el-button>
      </div>
      
      <ApiErrorFeedback
        v-if="errorInfo.show"
        v-model="errorInfo.show"
        :message="errorInfo.message"
        :title="errorInfo.title"
        :details="errorInfo.details"
        :severity="errorInfo.severity"
        :has-retry-option="errorInfo.retryable"
        @retry="handleRetry"
        has-details-option
      />
      
      <div v-if="responseData" class="response-data">
        <h4>响应数据:</h4>
        <pre>{{ JSON.stringify(responseData, null, 2) }}</pre>
      </div>
    </div>
    
    <div class="demo-section">
      <h4>请求重试机制</h4>
      <p>以下演示了自动重试和手动重试功能。</p>
      
      <div class="button-group">
        <el-button type="primary" @click="sendRetryableRequest" :loading="loading.retry">
          发送可重试请求
        </el-button>
        <el-button type="warning" @click="sendManualRetryRequest" :loading="loading.manualRetry">
          需要手动重试的请求
        </el-button>
      </div>
      
      <div class="retry-status" v-if="retryStatus.show">
        <el-alert
          :title="retryStatus.title"
          :type="retryStatus.type"
          :description="retryStatus.message"
          :closable="false"
          show-icon
        />
        <div v-if="retryStatus.count > 0" class="retry-progress">
          <span>重试进度: {{ retryStatus.count }}/{{ retryStatus.max }}</span>
          <el-progress :percentage="(retryStatus.count / retryStatus.max) * 100" />
        </div>
      </div>
    </div>
    
    <div class="demo-section">
      <h4>离线请求队列</h4>
      <p>当网络离线时，请求会被加入队列，网络恢复后自动发送。</p>
      
      <div class="button-group">
        <el-button type="primary" @click="toggleNetworkStatus">
          {{ isOffline ? '恢复网络连接' : '模拟网络断开' }}
        </el-button>
        <el-button 
          type="primary" 
          @click="sendOfflineRequest" 
          :loading="loading.offline"
          :disabled="!isOffline">
          发送离线请求
        </el-button>
      </div>
      
      <div v-if="isOffline" class="offline-status">
        <el-alert
          title="网络已断开"
          type="warning"
          description="当前处于离线状态，请求将被加入队列，网络恢复后自动发送。"
          :closable="false"
          show-icon
        />
      </div>
      
      <div v-if="offlineQueue.length > 0" class="offline-queue">
        <h5>离线请求队列 ({{ offlineQueue.length }})</h5>
        <el-table :data="offlineQueue" style="width: 100%">
          <el-table-column prop="id" label="请求ID" width="100" />
          <el-table-column prop="url" label="请求URL" />
          <el-table-column prop="method" label="方法" width="100" />
          <el-table-column prop="time" label="添加时间" width="180" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import service from '@/api'
import { apiRetryService } from '@/services/apiRetryService'
import { apiErrorFeedbackService } from '@/services/apiErrorFeedbackService'
import ApiErrorFeedback from '@/components/common/ApiErrorFeedback.vue'

// 加载状态
const loading = reactive({
  normal: false,
  error: false,
  timeout: false,
  retry: false,
  manualRetry: false,
  offline: false
})

// 错误信息
const errorInfo = reactive({
  show: false,
  title: '',
  message: '',
  details: null as any,
  severity: 'error',
  retryable: false,
  retryFn: null as (() => Promise<any>) | null
})

// 响应数据
const responseData = ref<any>(null)

// 重试状态
const retryStatus = reactive({
  show: false,
  title: '',
  message: '',
  type: 'info',
  count: 0,
  max: 3
})

// 离线状态
const isOffline = ref(false)

// 离线请求队列
const offlineQueue = ref<Array<{
  id: string;
  url: string;
  method: string;
  time: string;
}>>([])

/**
 * 发送正常请求
 */
async function sendNormalRequest() {
  loading.normal = true
  responseData.value = null
  errorInfo.show = false
  
  try {
    // 发送请求到一个正常的API端点
    const response = await service.get('/api/example/success')
    responseData.value = response
    ElMessage.success('请求成功')
  } catch (error: any) {
    showError('请求失败', error.message, error, 'error', false)
  } finally {
    loading.normal = false
  }
}

/**
 * 发送错误请求
 */
async function sendErrorRequest() {
  loading.error = true
  responseData.value = null
  errorInfo.show = false
  
  try {
    // 发送请求到一个不存在的API端点，触发404错误
    await service.get('/api/non-existent-endpoint')
  } catch (error: any) {
    showError('API错误', '请求的资源不存在', error, 'error', false)
  } finally {
    loading.error = false
  }
}

/**
 * 发送超时请求
 */
async function sendTimeoutRequest() {
  loading.timeout = true
  responseData.value = null
  errorInfo.show = false
  
  try {
    // 发送一个会超时的请求
    await service.get('/api/example/timeout', { timeout: 1000 })
  } catch (error: any) {
    showError('请求超时', '请求超时，服务器未能及时响应', error, 'warning', true)
    
    // 保存重试函数
    errorInfo.retryFn = () => service.get('/api/example/timeout', { timeout: 3000 })
  } finally {
    loading.timeout = false
  }
}

/**
 * 发送可重试请求
 */
async function sendRetryableRequest() {
  loading.retry = true
  responseData.value = null
  errorInfo.show = false
  retryStatus.show = true
  retryStatus.count = 0
  retryStatus.title = '自动重试中'
  retryStatus.message = '请求失败，正在自动重试...'
  retryStatus.type = 'info'
  
  try {
    // 创建一个带重试功能的请求
    const request = apiRetryService.createRetryableRequest(() => 
      service.get('/api/example/unstable')
    )
    
    // 发送请求
    const response = await request({
      onRetry: (retryCount: number) => {
        retryStatus.count = retryCount
        retryStatus.message = `第${retryCount}次重试中...`
      }
    } as any)
    
    responseData.value = response
    retryStatus.title = '请求成功'
    retryStatus.message = '经过自动重试，请求已成功'
    retryStatus.type = 'success'
    
    // 3秒后隐藏重试状态
    setTimeout(() => {
      retryStatus.show = false
    }, 3000)
  } catch (error: any) {
    retryStatus.title = '重试失败'
    retryStatus.message = '达到最大重试次数，请求仍然失败'
    retryStatus.type = 'error'
    
    showError('重试失败', '达到最大重试次数，请求仍然失败', error, 'error', false)
    
    // 3秒后隐藏重试状态
    setTimeout(() => {
      retryStatus.show = false
    }, 3000)
  } finally {
    loading.retry = false
  }
}

/**
 * 发送需要手动重试的请求
 */
async function sendManualRetryRequest() {
  loading.manualRetry = true
  responseData.value = null
  errorInfo.show = false
  
  try {
    // 发送一个会失败的请求
    await service.get('/api/example/manual-retry')
  } catch (error: any) {
    showError(
      '需要手动重试', 
      '此请求需要手动重试才能成功', 
      error, 
      'warning', 
      true
    )
    
    // 保存重试函数
    errorInfo.retryFn = async () => {
      try {
        loading.manualRetry = true
        const response = await service.get('/api/example/manual-retry-success')
        responseData.value = response
        errorInfo.show = false
        ElMessage.success('手动重试成功')
        return response
      } finally {
        loading.manualRetry = false
      }
    }
  } finally {
    loading.manualRetry = false
  }
}

/**
 * 处理重试
 */
async function handleRetry() {
  if (errorInfo.retryFn) {
    try {
      const response = await errorInfo.retryFn()
      if (response) {
        responseData.value = response
        errorInfo.show = false
      }
    } catch (error: any) {
      showError('重试失败', error.message, error, 'error', false)
    }
  }
}

/**
 * 显示错误信息
 */
function showError(title: string, message: string, error: any, severity: string, retryable: boolean) {
  errorInfo.title = title
  errorInfo.message = message
  errorInfo.severity = severity
  errorInfo.retryable = retryable
  
  // 提取错误详情
  if (error) {
    if (error.response) {
      errorInfo.details = {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data,
        headers: error.response.headers,
        config: {
          url: error.config?.url,
          method: error.config?.method,
          timeout: error.config?.timeout
        }
      }
    } else {
      errorInfo.details = {
        message: error.message,
        name: error.name,
        code: error.code,
        stack: error.stack
      }
    }
  }
  
  errorInfo.show = true
}

/**
 * 切换网络状态
 */
function toggleNetworkStatus() {
  isOffline.value = !isOffline.value
  
  if (isOffline.value) {
    // 模拟网络断开
    Object.defineProperty(navigator, 'onLine', { value: false, configurable: true })
    ElMessage.warning('已模拟网络断开')
  } else {
    // 恢复网络连接
    Object.defineProperty(navigator, 'onLine', { value: true, configurable: true })
    ElMessage.success('已恢复网络连接')
    
    // 触发online事件
    window.dispatchEvent(new Event('online'))
    
    // 清空离线队列
    setTimeout(() => {
      offlineQueue.value = []
    }, 1000)
  }
}

/**
 * 发送离线请求
 */
async function sendOfflineRequest() {
  if (!isOffline.value) {
    ElMessage.warning('当前网络已连接，请先模拟网络断开')
    return
  }
  
  loading.offline = true
  
  try {
    // 添加到离线队列
    const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
    offlineQueue.value.push({
      id: requestId,
      url: '/api/example/offline-request',
      method: 'GET',
      time: new Date().toLocaleString()
    })
    
    // 尝试发送请求（会被加入离线队列）
    await service.get('/api/example/offline-request')
    
    ElMessage.info('请求已加入离线队列，网络恢复后将自动发送')
  } catch (error: any) {
    // 通常不会进入这里，因为请求会被加入队列而不是立即拒绝
    console.error('离线请求错误:', error)
  } finally {
    loading.offline = false
  }
}
</script>

<style scoped>
.api-error-demo {
  padding: 20px;
}

h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

p {
  margin-bottom: 20px;
  color: #606266;
}

.demo-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
}

.demo-section h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.response-data {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow: auto;
}

.response-data h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 14px;
}

.response-data pre {
  margin: 0;
  font-family: monospace;
  font-size: 12px;
}

.retry-status {
  margin-top: 20px;
}

.retry-progress {
  margin-top: 10px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.offline-status {
  margin-top: 20px;
}

.offline-queue {
  margin-top: 20px;
}

.offline-queue h5 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 14px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .demo-section {
    background-color: #1a1a1a;
    border-color: #4c4c4c;
  }
  
  p {
    color: #c0c0c0;
  }
  
  .response-data {
    background-color: #2c2c2c;
  }
  
  .retry-progress {
    background-color: #2c2c2c;
  }
}
</style>