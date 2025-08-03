<template>
  <div class="error-report-manager">
    <!-- 错误报告按钮 -->
    <div v-if="showReportButton" class="report-button-container" :class="{ 'fixed': fixedPosition }">
      <el-button
        :type="buttonType"
        :size="buttonSize"
        :icon="buttonIcon"
        @click="openReportDialog"
      >
        {{ buttonText }}
      </el-button>
    </div>
    
    <!-- 错误报告组件 -->
    <ErrorReport
      ref="errorReportRef"
      :title="reportTitle"
      :error-info="currentError"
      @submit="handleReportSubmit"
      @close="handleReportClose"
    />
    
    <!-- 用户反馈组件 -->
    <UserFeedback
      ref="userFeedbackRef"
      :title="feedbackTitle"
      :show-button="false"
      @submit="handleFeedbackSubmit"
      @close="handleFeedbackClose"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import ErrorReport from './ErrorReport.vue'
import UserFeedback from './UserFeedback.vue'
import { errorReportService, type ErrorReport as ErrorReportType } from '@/services/errorReportService'
import { ErrorType, ErrorSeverity } from '@/services/errorHandler'

// 定义组件属性
const props = defineProps({
  // 是否显示报告按钮
  showReportButton: {
    type: Boolean,
    default: true
  },
  // 按钮文本
  buttonText: {
    type: String,
    default: '报告问题'
  },
  // 按钮类型
  buttonType: {
    type: String,
    default: 'danger'
  },
  // 按钮大小
  buttonSize: {
    type: String,
    default: 'small'
  },
  // 按钮图标
  buttonIcon: {
    type: String,
    default: 'Warning'
  },
  // 是否固定位置
  fixedPosition: {
    type: Boolean,
    default: false
  },
  // 报告对话框标题
  reportTitle: {
    type: String,
    default: '错误报告'
  },
  // 反馈对话框标题
  feedbackTitle: {
    type: String,
    default: '用户反馈'
  },
  // 是否自动捕获未处理的错误
  autoCaptureErrors: {
    type: Boolean,
    default: true
  }
})

// 定义事件
const emit = defineEmits(['report-submit', 'feedback-submit', 'error-captured'])

// 组件引用
const errorReportRef = ref<InstanceType<typeof ErrorReport> | null>(null)
const userFeedbackRef = ref<InstanceType<typeof UserFeedback> | null>(null)

// 当前错误信息
const currentError = ref<any>(null)

// 错误处理函数
let errorHandler: ((event: ErrorEvent) => void) | null = null
let rejectionHandler: ((event: PromiseRejectionEvent) => void) | null = null

/**
 * 捕获未处理的错误
 * @param event 错误事件
 */
function captureError(event: ErrorEvent): void {
  // 阻止默认行为
  event.preventDefault()
  
  // 提取错误信息
  const error = event.error || new Error(event.message)
  const errorInfo = {
    message: error.message || event.message,
    stack: error.stack,
    filename: event.filename,
    lineno: event.lineno,
    colno: event.colno,
    timestamp: Date.now()
  }
  
  // 报告错误
  const report = errorReportService.reportError(
    error,
    ErrorType.FRONTEND,
    ErrorSeverity.ERROR,
    {
      message: errorInfo.message,
      location: `${errorInfo.filename}:${errorInfo.lineno}:${errorInfo.colno}`,
      details: errorInfo
    }
  )
  
  // 触发错误捕获事件
  emit('error-captured', report)
  
  // 设置当前错误
  currentError.value = errorInfo
}

/**
 * 捕获未处理的Promise拒绝
 * @param event Promise拒绝事件
 */
function captureRejection(event: PromiseRejectionEvent): void {
  // 阻止默认行为
  event.preventDefault()
  
  // 提取错误信息
  const error = event.reason
  const errorInfo = {
    message: error.message || 'Unhandled Promise Rejection',
    stack: error.stack,
    timestamp: Date.now()
  }
  
  // 报告错误
  const report = errorReportService.reportError(
    error,
    ErrorType.PROMISE,
    ErrorSeverity.ERROR,
    {
      message: errorInfo.message,
      details: errorInfo
    }
  )
  
  // 触发错误捕获事件
  emit('error-captured', report)
  
  // 设置当前错误
  currentError.value = errorInfo
}

/**
 * 打开报告对话框
 * @param error 错误信息
 */
function openReportDialog(error?: any): void {
  if (error) {
    currentError.value = error
  }
  
  if (errorReportRef.value) {
    errorReportRef.value.open()
  }
}

/**
 * 打开反馈对话框
 * @param type 反馈类型
 */
function openFeedbackDialog(type?: string): void {
  if (userFeedbackRef.value) {
    userFeedbackRef.value.openFeedback(type)
  }
}

/**
 * 处理报告提交
 * @param report 错误报告
 */
function handleReportSubmit(report: any): void {
  // 上报错误
  errorReportService.reportError(
    currentError.value,
    ErrorType.USER_REPORTED,
    ErrorSeverity.ERROR,
    {
      message: report.description,
      details: {
        ...report,
        userFeedback: true
      }
    }
  )
  
  // 触发提交事件
  emit('report-submit', report)
  
  // 清空当前错误
  currentError.value = null
}

/**
 * 处理报告关闭
 */
function handleReportClose(): void {
  // 清空当前错误
  currentError.value = null
}

/**
 * 处理反馈提交
 * @param feedback 用户反馈
 */
function handleFeedbackSubmit(feedback: any): void {
  // 上报反馈
  errorReportService.reportError(
    null,
    ErrorType.FEEDBACK,
    feedback.type === 'bug' ? ErrorSeverity.WARNING : ErrorSeverity.INFO,
    {
      message: feedback.content,
      details: {
        ...feedback,
        userFeedback: true
      }
    }
  )
  
  // 触发提交事件
  emit('feedback-submit', feedback)
}

/**
 * 处理反馈关闭
 */
function handleFeedbackClose(): void {
  // 不需要特殊处理
}

/**
 * 报告错误
 * @param error 错误对象
 * @param type 错误类型
 * @param severity 错误严重程度
 * @param additionalInfo 附加信息
 * @returns 错误报告
 */
function reportError(
  error: any,
  type: ErrorType = ErrorType.FRONTEND,
  severity: ErrorSeverity = ErrorSeverity.ERROR,
  additionalInfo?: {
    message?: string
    details?: any
    location?: string
  }
): ErrorReportType {
  // 报告错误
  const report = errorReportService.reportError(error, type, severity, additionalInfo)
  
  // 如果错误严重，打开报告对话框
  if (severity === ErrorSeverity.FATAL || severity === ErrorSeverity.ERROR) {
    currentError.value = {
      message: additionalInfo?.message || error?.message || '发生了一个错误',
      details: additionalInfo?.details || error,
      location: additionalInfo?.location || window.location.href,
      timestamp: Date.now()
    }
    
    // 打开报告对话框
    openReportDialog()
  }
  
  return report
}

// 组件挂载时
onMounted(() => {
  // 如果启用了自动捕获错误
  if (props.autoCaptureErrors) {
    // 设置全局错误处理函数
    errorHandler = captureError
    rejectionHandler = captureRejection
    
    // 添加事件监听器
    window.addEventListener('error', errorHandler)
    window.addEventListener('unhandledrejection', rejectionHandler)
  }
})

// 组件卸载时
onUnmounted(() => {
  // 如果设置了错误处理函数，移除事件监听器
  if (errorHandler) {
    window.removeEventListener('error', errorHandler)
  }
  
  if (rejectionHandler) {
    window.removeEventListener('unhandledrejection', rejectionHandler)
  }
})

// 暴露方法
defineExpose({
  openReportDialog,
  openFeedbackDialog,
  reportError
})
</script>

<style scoped>
.report-button-container {
  margin: 10px 0;
}

.report-button-container.fixed {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}
</style>