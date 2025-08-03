<template>
  <div class="error-report-demo">
    <h3>错误报告功能演示</h3>
    <p>本组件演示了错误报告和用户反馈功能，包括错误信息收集、上报和用户反馈机制。</p>
    
    <div class="demo-section">
      <h4>错误报告</h4>
      <p>您可以通过以下按钮触发不同类型的错误，或者手动报告问题。</p>
      
      <div class="button-group">
        <el-button type="danger" @click="triggerError">
          触发JavaScript错误
        </el-button>
        <el-button type="danger" @click="triggerPromiseError">
          触发Promise错误
        </el-button>
        <el-button type="warning" @click="reportCustomError">
          报告自定义错误
        </el-button>
        <el-button type="primary" @click="openErrorReport">
          打开错误报告对话框
        </el-button>
      </div>
      
      <div v-if="lastError" class="error-info">
        <el-alert
          :title="lastError.message"
          type="error"
          :description="lastError.details"
          :closable="true"
          show-icon
        />
      </div>
    </div>
    
    <div class="demo-section">
      <h4>用户反馈</h4>
      <p>用户可以提交不同类型的反馈，包括功能建议、问题报告和使用体验。</p>
      
      <div class="button-group">
        <el-button type="success" @click="openFeedback('feature')">
          功能建议
        </el-button>
        <el-button type="warning" @click="openFeedback('bug')">
          问题报告
        </el-button>
        <el-button type="info" @click="openFeedback('experience')">
          使用体验
        </el-button>
      </div>
    </div>
    
    <div class="demo-section">
      <h4>错误报告管理</h4>
      <p>查看已收集的错误报告和配置错误报告服务。</p>
      
      <el-collapse>
        <el-collapse-item title="错误报告列表" name="1">
          <el-table :data="errorReports" style="width: 100%">
            <el-table-column prop="timestamp" label="时间" width="180">
              <template #default="scope">
                {{ formatTime(scope.row.timestamp) }}
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="120">
              <template #default="scope">
                {{ formatErrorType(scope.row.type) }}
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="严重程度" width="120">
              <template #default="scope">
                {{ formatSeverity(scope.row.severity) }}
              </template>
            </el-table-column>
            <el-table-column prop="message" label="错误消息" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" @click="viewErrorDetails(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="table-actions">
            <el-button type="primary" size="small" @click="flushErrors">
              上报所有错误
            </el-button>
            <el-button type="danger" size="small" @click="clearErrors">
              清空错误列表
            </el-button>
          </div>
        </el-collapse-item>
        
        <el-collapse-item title="错误报告配置" name="2">
          <el-form :model="reportConfig" label-width="180px">
            <el-form-item label="启用错误报告">
              <el-switch v-model="reportConfig.enabled" />
            </el-form-item>
            <el-form-item label="自动收集错误">
              <el-switch v-model="reportConfig.autoCollect" />
            </el-form-item>
            <el-form-item label="上报到服务器">
              <el-switch v-model="reportConfig.reportToServer" />
            </el-form-item>
            <el-form-item label="上报URL">
              <el-input v-model="reportConfig.reportUrl" />
            </el-form-item>
            <el-form-item label="上报批次大小">
              <el-input-number v-model="reportConfig.batchSize" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="上报间隔（毫秒）">
              <el-input-number v-model="reportConfig.reportInterval" :min="1000" :step="1000" />
            </el-form-item>
            <el-form-item label="最大缓存错误数">
              <el-input-number v-model="reportConfig.maxCachedErrors" :min="10" :max="1000" />
            </el-form-item>
            <el-form-item label="收集用户反馈">
              <el-switch v-model="reportConfig.collectFeedback" />
            </el-form-item>
            <el-form-item label="包含技术信息">
              <el-switch v-model="reportConfig.includeTechInfo" />
            </el-form-item>
            <el-form-item label="采样率">
              <el-slider v-model="reportConfig.samplingRate" :min="0" :max="1" :step="0.1" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updateConfig">
                更新配置
              </el-button>
              <el-button @click="resetConfig">
                重置配置
              </el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </div>
    
    <!-- 错误报告管理器 -->
    <ErrorReportManager
      ref="errorReportManagerRef"
      :show-report-button="true"
      :fixed-position="true"
      button-text="报告问题"
      button-type="danger"
      button-size="small"
      button-icon="Warning"
      @report-submit="handleReportSubmit"
      @feedback-submit="handleFeedbackSubmit"
      @error-captured="handleErrorCaptured"
    />
    
    <!-- 错误详情对话框 -->
    <el-dialog
      v-model="errorDetailsVisible"
      title="错误详情"
      width="800px"
    >
      <div v-if="selectedError" class="error-details">
        <div class="error-header">
          <h3>{{ selectedError.message }}</h3>
          <p>{{ formatTime(selectedError.timestamp) }}</p>
          <p>类型: {{ formatErrorType(selectedError.type) }} | 严重程度: {{ formatSeverity(selectedError.severity) }}</p>
          <p v-if="selectedError.location">位置: {{ selectedError.location }}</p>
        </div>
        
        <el-divider content-position="center">详细信息</el-divider>
        
        <el-tabs>
          <el-tab-pane label="错误详情">
            <pre class="error-json">{{ JSON.stringify(selectedError.details, null, 2) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="堆栈跟踪" v-if="selectedError.stack">
            <pre class="error-stack">{{ selectedError.stack }}</pre>
          </el-tab-pane>
          <el-tab-pane label="技术信息" v-if="selectedError.techInfo">
            <pre class="error-json">{{ JSON.stringify(selectedError.techInfo, null, 2) }}</pre>
          </el-tab-pane>
          <el-tab-pane label="用户反馈" v-if="selectedError.feedback">
            <pre class="error-json">{{ JSON.stringify(selectedError.feedback, null, 2) }}</pre>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ErrorReportManager from '@/components/common/ErrorReportManager.vue'
import { errorReportService, type ErrorReportConfig, ErrorType, ErrorSeverity } from '@/services/errorReportService'

// 错误报告管理器引用
const errorReportManagerRef = ref<InstanceType<typeof ErrorReportManager> | null>(null)

// 最后一个错误
const lastError = ref<any>(null)

// 错误报告列表
const errorReports = ref<any[]>([])

// 错误报告配置
const reportConfig = reactive<ErrorReportConfig>({
  enabled: true,
  autoCollect: true,
  reportToServer: true,
  reportUrl: '/api/error-report',
  batchSize: 10,
  reportInterval: 60000,
  maxCachedErrors: 100,
  collectFeedback: true,
  includeTechInfo: true,
  samplingRate: 1.0
})

// 错误详情对话框
const errorDetailsVisible = ref(false)
const selectedError = ref<any>(null)

/**
 * 触发JavaScript错误
 */
function triggerError() {
  try {
    // 故意触发错误
    const obj = null
    obj.nonExistentMethod()
  } catch (error) {
    // 记录错误
    lastError.value = {
      message: error.message,
      details: '尝试调用空对象的方法',
      timestamp: Date.now()
    }
    
    // 抛出错误，让全局错误处理器捕获
    throw error
  }
}

/**
 * 触发Promise错误
 */
function triggerPromiseError() {
  // 故意触发Promise错误
  new Promise((resolve, reject) => {
    reject(new Error('Promise被拒绝'))
  }).then(() => {
    // 不会执行到这里
  })
  
  // 记录错误
  lastError.value = {
    message: 'Promise被拒绝',
    details: '未处理的Promise拒绝',
    timestamp: Date.now()
  }
}

/**
 * 报告自定义错误
 */
function reportCustomError() {
  // 创建自定义错误
  const error = {
    message: '自定义错误',
    details: '这是一个手动报告的自定义错误',
    timestamp: Date.now()
  }
  
  // 记录错误
  lastError.value = error
  
  // 报告错误
  if (errorReportManagerRef.value) {
    errorReportManagerRef.value.reportError(
      error,
      ErrorType.CUSTOM,
      ErrorSeverity.WARNING,
      {
        message: error.message,
        details: error.details
      }
    )
  }
}

/**
 * 打开错误报告对话框
 */
function openErrorReport() {
  if (errorReportManagerRef.value) {
    errorReportManagerRef.value.openReportDialog({
      message: '请报告您遇到的问题',
      timestamp: Date.now()
    })
  }
}

/**
 * 打开反馈对话框
 * @param type 反馈类型
 */
function openFeedback(type: string) {
  if (errorReportManagerRef.value) {
    errorReportManagerRef.value.openFeedbackDialog(type)
  }
}

/**
 * 查看错误详情
 * @param error 错误对象
 */
function viewErrorDetails(error: any) {
  selectedError.value = error
  errorDetailsVisible.value = true
}

/**
 * 上报所有错误
 */
async function flushErrors() {
  try {
    await errorReportService.flushErrors()
    ElMessage.success('所有错误已上报')
  } catch (error) {
    ElMessage.error('上报错误失败')
  }
}

/**
 * 清空错误列表
 */
function clearErrors() {
  ElMessageBox.confirm('确定要清空错误列表吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    errorReportService.clearErrorCache()
    errorReports.value = []
    ElMessage.success('错误列表已清空')
  }).catch(() => {
    // 取消操作
  })
}

/**
 * 更新配置
 */
function updateConfig() {
  errorReportService.updateConfig(reportConfig)
  ElMessage.success('配置已更新')
}

/**
 * 重置配置
 */
function resetConfig() {
  Object.assign(reportConfig, errorReportService.getConfig())
  ElMessage.info('配置已重置')
}

/**
 * 处理报告提交
 * @param report 错误报告
 */
function handleReportSubmit(report: any) {
  ElMessage.success('错误报告已提交')
  refreshErrorReports()
}

/**
 * 处理反馈提交
 * @param feedback 用户反馈
 */
function handleFeedbackSubmit(feedback: any) {
  ElMessage.success('用户反馈已提交')
  refreshErrorReports()
}

/**
 * 处理错误捕获
 * @param error 错误对象
 */
function handleErrorCaptured(error: any) {
  lastError.value = {
    message: error.message,
    details: '自动捕获的错误',
    timestamp: Date.now()
  }
  
  refreshErrorReports()
}

/**
 * 刷新错误报告列表
 */
function refreshErrorReports() {
  errorReports.value = errorReportService.getErrorCache()
}

/**
 * 格式化时间
 * @param timestamp 时间戳
 * @returns 格式化后的时间
 */
function formatTime(timestamp: number | string | Date): string {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  return date.toLocaleString()
}

/**
 * 格式化错误类型
 * @param type 错误类型
 * @returns 格式化后的错误类型
 */
function formatErrorType(type: ErrorType): string {
  switch (type) {
    case ErrorType.FRONTEND:
      return '前端错误'
    case ErrorType.API:
      return 'API错误'
    case ErrorType.NETWORK:
      return '网络错误'
    case ErrorType.PROMISE:
      return 'Promise错误'
    case ErrorType.CUSTOM:
      return '自定义错误'
    case ErrorType.USER_REPORTED:
      return '用户报告'
    case ErrorType.FEEDBACK:
      return '用户反馈'
    default:
      return '未知错误'
  }
}

/**
 * 格式化严重程度
 * @param severity 严重程度
 * @returns 格式化后的严重程度
 */
function formatSeverity(severity: ErrorSeverity): string {
  switch (severity) {
    case ErrorSeverity.FATAL:
      return '致命'
    case ErrorSeverity.ERROR:
      return '错误'
    case ErrorSeverity.WARNING:
      return '警告'
    case ErrorSeverity.INFO:
      return '信息'
    default:
      return '未知'
  }
}

// 组件挂载时
onMounted(() => {
  // 初始化错误报告服务
  errorReportService.init(reportConfig)
  
  // 获取错误报告列表
  refreshErrorReports()
})
</script>

<style scoped>
.error-report-demo {
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

.error-info {
  margin-top: 20px;
}

.table-actions {
  margin-top: 15px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.error-details {
  padding: 0 10px;
}

.error-header {
  margin-bottom: 20px;
}

.error-header h3 {
  margin: 0 0 10px 0;
  color: #F56C6C;
}

.error-header p {
  margin: 5px 0;
  color: #606266;
}

.error-json, .error-stack {
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
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
  
  .error-header p {
    color: #c0c0c0;
  }
  
  .error-json, .error-stack {
    background-color: #2c2c2c;
  }
}
</style>