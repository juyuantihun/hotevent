<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">错误处理演示</h1>
      <p class="page-description">本页面用于演示全局错误处理机制的功能</p>
    </div>
    
    <div class="content">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="组件错误处理" name="component">
          <el-card class="section">
            <template #header>
              <div class="section-header">
                <h2>错误边界演示</h2>
              </div>
            </template>
            
            <p class="section-description">
              错误边界组件可以捕获子组件中的渲染错误，防止整个应用崩溃。
            </p>
            
            <div class="demo-area">
              <ErrorBoundary @error="handleBoundaryError">
                <template v-if="!showErrorComponent">
                  <el-button type="danger" @click="showErrorComponent = true">
                    触发组件错误
                  </el-button>
                </template>
                <template v-else>
                  <BrokenComponent />
                </template>
              </ErrorBoundary>
              
              <div v-if="boundaryError" class="error-log">
                <h4>捕获的错误:</h4>
                <pre>{{ boundaryError }}</pre>
              </div>
            </div>
          </el-card>
          
          <el-card class="section">
            <template #header>
              <div class="section-header">
                <h2>全局错误处理演示</h2>
              </div>
            </template>
            
            <p class="section-description">
              全局错误处理可以捕获应用中的各种错误，包括运行时错误、Promise错误和资源加载错误等。
            </p>
            
            <div class="demo-area">
              <ErrorTester />
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="API错误处理" name="api">
          <ApiErrorDemo />
        </el-tab-pane>
        
        <el-tab-pane label="错误报告" name="report">
          <ErrorReportDemo />
        </el-tab-pane>
        
        <el-tab-pane label="错误历史" name="history">
          <el-card class="section">
            <template #header>
              <div class="section-header">
                <h2>错误历史记录</h2>
                <div class="header-actions">
                  <el-button type="primary" size="small" @click="refreshErrorHistory">
                    刷新
                  </el-button>
                  <el-button type="danger" size="small" @click="clearErrorHistory">
                    清除
                  </el-button>
                </div>
              </div>
            </template>
            
            <p class="section-description">
              显示最近捕获的错误历史记录，包括组件错误、运行时错误和API错误等。
            </p>
            
            <div class="error-history">
              <el-empty v-if="errorHistory.length === 0" description="暂无错误记录" />
              
              <el-table
                v-else
                :data="errorHistory"
                style="width: 100%"
                :default-sort="{ prop: 'timestamp', order: 'descending' }"
              >
                <el-table-column prop="timestamp" label="时间" width="180" sortable>
                  <template #default="scope">
                    {{ formatTimestamp(scope.row.timestamp) }}
                  </template>
                </el-table-column>
                
                <el-table-column prop="type" label="类型" width="120">
                  <template #default="scope">
                    <el-tag :type="getErrorTypeTagType(scope.row.type)">
                      {{ getErrorTypeText(scope.row.type) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column prop="severity" label="严重程度" width="120">
                  <template #default="scope">
                    <el-tag :type="getErrorSeverityTagType(scope.row.severity)">
                      {{ getErrorSeverityText(scope.row.severity) }}
                    </el-tag>
                  </template>
                </el-table-column>
                
                <el-table-column prop="message" label="错误消息">
                  <template #default="scope">
                    <div class="error-message-cell">{{ scope.row.message }}</div>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button
                      size="small"
                      type="primary"
                      @click="showErrorDetails(scope.row)"
                    >
                      详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 错误详情对话框 -->
    <el-dialog
      v-model="errorDetailsVisible"
      title="错误详情"
      width="60%"
      destroy-on-close
    >
      <div v-if="selectedError" class="error-details-dialog">
        <div class="error-details-header">
          <div class="error-type">
            <el-tag :type="getErrorTypeTagType(selectedError.type)">
              {{ getErrorTypeText(selectedError.type) }}
            </el-tag>
            <el-tag :type="getErrorSeverityTagType(selectedError.severity)">
              {{ getErrorSeverityText(selectedError.severity) }}
            </el-tag>
          </div>
          <div class="error-time">
            {{ formatTimestamp(selectedError.timestamp) }}
          </div>
        </div>
        
        <div class="error-message-full">
          <h4>错误消息:</h4>
          <p>{{ selectedError.message }}</p>
        </div>
        
        <div v-if="selectedError.location" class="error-location">
          <h4>错误位置:</h4>
          <p>{{ selectedError.location }}</p>
        </div>
        
        <div v-if="selectedError.details" class="error-details-content">
          <h4>详细信息:</h4>
          <pre>{{ JSON.stringify(selectedError.details, null, 2) }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import ErrorBoundary from '@/components/common/ErrorBoundary.vue'
import ErrorTester from '@/components/common/ErrorTester.vue'
import ApiErrorDemo from './ApiErrorDemo.vue'
import ErrorReportDemo from './ErrorReportDemo.vue'
import { getErrorHistory, clearErrorHistory as clearHistory, ErrorType, ErrorSeverity, ErrorInfo } from '@/services/errorHandler'

// 当前激活的标签页
const activeTab = ref('component')

// 定义一个会导致错误的组件
const BrokenComponent = {
  template: '<div>{{ nonExistentProperty.value }}</div>'
}

// 控制是否显示错误组件
const showErrorComponent = ref(false)

// 错误边界捕获的错误
const boundaryError = ref<string | null>(null)

// 错误历史记录
const errorHistory = ref<ErrorInfo[]>([])

// 错误详情对话框
const errorDetailsVisible = ref(false)
const selectedError = ref<ErrorInfo | null>(null)

/**
 * 处理错误边界捕获的错误
 */
function handleBoundaryError(event: { error: Error, info: string }) {
  boundaryError.value = `${event.error.message}\n${event.info}`
  showErrorComponent.value = false
  
  // 3秒后清除错误信息
  setTimeout(() => {
    boundaryError.value = null
  }, 3000)
}

/**
 * 刷新错误历史记录
 */
function refreshErrorHistory() {
  errorHistory.value = getErrorHistory()
  ElMessage.success('错误历史记录已刷新')
}

/**
 * 清除错误历史记录
 */
function clearErrorHistory() {
  clearHistory()
  errorHistory.value = []
  ElMessage.success('错误历史记录已清除')
}

/**
 * 显示错误详情
 */
function showErrorDetails(error: ErrorInfo) {
  selectedError.value = error
  errorDetailsVisible.value = true
}

/**
 * 获取错误类型对应的文本
 */
function getErrorTypeText(type: ErrorType): string {
  switch (type) {
    case ErrorType.VUE:
      return 'Vue错误'
    case ErrorType.RUNTIME:
      return '运行时错误'
    case ErrorType.PROMISE:
      return 'Promise错误'
    case ErrorType.RESOURCE:
      return '资源加载错误'
    case ErrorType.API:
      return 'API请求错误'
    default:
      return '未知错误'
  }
}

/**
 * 获取错误类型对应的标签类型
 */
function getErrorTypeTagType(type: ErrorType): '' | 'success' | 'warning' | 'info' | 'danger' {
  switch (type) {
    case ErrorType.VUE:
      return 'danger'
    case ErrorType.RUNTIME:
      return 'danger'
    case ErrorType.PROMISE:
      return 'warning'
    case ErrorType.RESOURCE:
      return 'info'
    case ErrorType.API:
      return 'warning'
    default:
      return ''
  }
}

/**
 * 获取错误严重程度对应的文本
 */
function getErrorSeverityText(severity: ErrorSeverity): string {
  switch (severity) {
    case ErrorSeverity.FATAL:
      return '致命错误'
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

/**
 * 获取错误严重程度对应的标签类型
 */
function getErrorSeverityTagType(severity: ErrorSeverity): '' | 'success' | 'warning' | 'info' | 'danger' {
  switch (severity) {
    case ErrorSeverity.FATAL:
      return 'danger'
    case ErrorSeverity.ERROR:
      return 'danger'
    case ErrorSeverity.WARNING:
      return 'warning'
    case ErrorSeverity.INFO:
      return 'info'
    default:
      return ''
  }
}

/**
 * 获取错误类型对应的颜色
 */
function getErrorTypeColor(severity: ErrorSeverity): 'primary' | 'success' | 'warning' | 'danger' {
  switch (severity) {
    case ErrorSeverity.FATAL:
      return 'danger'
    case ErrorSeverity.ERROR:
      return 'danger'
    case ErrorSeverity.WARNING:
      return 'warning'
    case ErrorSeverity.INFO:
      return 'primary'
    default:
      return 'primary'
  }
}

/**
 * 格式化时间戳
 */
function formatTimestamp(timestamp: number): string {
  return new Date(timestamp).toLocaleString()
}

// 组件挂载时刷新错误历史
onMounted(() => {
  refreshErrorHistory()
})
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.section-description {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.demo-area {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f8f9fa;
}

.error-log {
  margin-top: 16px;
  padding: 12px;
  background-color: #fff5f7;
  border: 1px solid #fde2e2;
  border-radius: 4px;
  color: #f56c6c;
}

.error-log h4 {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
}

.error-log pre {
  margin: 0;
  white-space: pre-wrap;
  font-family: monospace;
  font-size: 12px;
}

.error-history {
  margin-top: 16px;
  max-height: 600px;
  overflow-y: auto;
}

.error-message-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.error-details-dialog {
  padding: 0 16px;
}

.error-details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.error-type {
  display: flex;
  gap: 8px;
}

.error-message-full {
  margin-bottom: 16px;
}

.error-message-full h4 {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
}

.error-message-full p {
  margin: 0;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  word-break: break-word;
}

.error-location {
  margin-bottom: 16px;
}

.error-location h4 {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
}

.error-location p {
  margin: 0;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  font-family: monospace;
}

.error-details-content {
  margin-bottom: 16px;
}

.error-details-content h4 {
  margin-top: 0;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
}

.error-details-content pre {
  margin: 0;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow-x: auto;
  font-family: monospace;
  font-size: 12px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .demo-area {
    background-color: #1a1a1a;
    border-color: #4c4c4c;
  }
  
  .error-log {
    background-color: #2d1a1a;
    border-color: #5c2d2d;
    color: #f78989;
  }
  
  .section-description {
    color: #c0c0c0;
  }
  
  .error-message-full p,
  .error-location p,
  .error-details-content pre {
    background-color: #2c2c2c;
  }
}
</style>