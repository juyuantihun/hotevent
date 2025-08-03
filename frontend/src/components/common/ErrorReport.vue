<template>
  <div class="error-report">
    <el-dialog
      v-model="dialogVisible"
      :title="title"
      width="600px"
      :close-on-click-modal="false"
      :show-close="true"
      @closed="handleDialogClosed"
    >
      <div class="error-report-content">
        <div v-if="errorInfo" class="error-summary">
          <div class="error-icon">
            <el-icon :size="32" color="#F56C6C"><WarningFilled /></el-icon>
          </div>
          <div class="error-details">
            <h3>{{ errorInfo.message || '发生了一个错误' }}</h3>
            <p class="error-time">{{ formatTime(errorInfo.timestamp) }}</p>
            <p class="error-location">{{ errorInfo.location }}</p>
          </div>
        </div>
        
        <el-divider content-position="center">错误报告</el-divider>
        
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="错误类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择错误类型" class="w-100">
              <el-option label="功能错误" value="functional" />
              <el-option label="界面显示问题" value="ui" />
              <el-option label="性能问题" value="performance" />
              <el-option label="其他问题" value="other" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="严重程度" prop="severity">
            <el-rate
              v-model="form.severity"
              :colors="severityColors"
              :texts="severityTexts"
              show-text
              :max="5"
            />
          </el-form-item>
          
          <el-form-item label="问题描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请描述您遇到的问题，包括您期望的行为和实际发生的情况"
            />
          </el-form-item>
          
          <el-form-item label="重现步骤" prop="steps">
            <el-input
              v-model="form.steps"
              type="textarea"
              :rows="3"
              placeholder="请描述如何重现这个问题（可选）"
            />
          </el-form-item>
          
          <el-form-item label="联系方式（可选）" prop="contact">
            <el-input
              v-model="form.contact"
              placeholder="您的邮箱或其他联系方式，以便我们跟进问题"
            />
          </el-form-item>
          
          <el-form-item label="附加技术信息">
            <div class="tech-info">
              <el-checkbox v-model="form.includeTechInfo">
                包含技术信息（浏览器、操作系统等）
              </el-checkbox>
              <el-tooltip
                content="这些信息有助于我们更快地定位和解决问题"
                placement="top"
              >
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            
            <div v-if="form.includeTechInfo" class="tech-info-preview">
              <pre>{{ techInfoPreview }}</pre>
              <el-checkbox v-model="form.includeErrorDetails">
                包含详细错误信息
              </el-checkbox>
            </div>
          </el-form-item>
        </el-form>
        
        <div class="privacy-notice">
          <el-alert
            title="隐私提示"
            type="info"
            description="您提交的信息仅用于改进产品质量，我们不会收集您的个人敏感信息。"
            :closable="false"
            show-icon
          />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport" :loading="submitting">
            提交报告
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { WarningFilled, InfoFilled } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

// 定义组件属性
const props = defineProps({
  // 对话框标题
  title: {
    type: String,
    default: '错误报告'
  },
  // 错误信息
  errorInfo: {
    type: Object,
    default: null
  }
})

// 定义事件
const emit = defineEmits(['submit', 'close'])

// 对话框可见性
const dialogVisible = ref(false)

// 表单引用
const formRef = ref<FormInstance>()

// 提交状态
const submitting = ref(false)

// 表单数据
const form = reactive({
  type: 'functional',
  severity: 3,
  description: '',
  steps: '',
  contact: '',
  includeTechInfo: true,
  includeErrorDetails: true
})

// 表单验证规则
const rules = reactive<FormRules>({
  type: [
    { required: true, message: '请选择错误类型', trigger: 'change' }
  ],
  severity: [
    { required: true, message: '请选择严重程度', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请描述您遇到的问题', trigger: 'blur' },
    { min: 10, message: '描述至少需要10个字符', trigger: 'blur' }
  ]
})

// 严重程度颜色
const severityColors = ['#67C23A', '#E6A23C', '#E6A23C', '#F56C6C', '#F56C6C']

// 严重程度文本
const severityTexts = ['轻微', '低', '中等', '高', '严重']

// 技术信息预览
const techInfoPreview = computed(() => {
  const info = {
    browser: navigator.userAgent,
    platform: navigator.platform,
    language: navigator.language,
    screenSize: `${window.screen.width}x${window.screen.height}`,
    viewportSize: `${window.innerWidth}x${window.innerHeight}`,
    timestamp: new Date().toISOString(),
    url: window.location.href,
    referrer: document.referrer
  }
  
  return JSON.stringify(info, null, 2)
})

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
 * 提交报告
 */
async function submitReport() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        // 构建报告数据
        const reportData = {
          ...form,
          errorInfo: form.includeErrorDetails ? props.errorInfo : null,
          techInfo: form.includeTechInfo ? {
            browser: navigator.userAgent,
            platform: navigator.platform,
            language: navigator.language,
            screenSize: `${window.screen.width}x${window.screen.height}`,
            viewportSize: `${window.innerWidth}x${window.innerHeight}`,
            timestamp: new Date().toISOString(),
            url: window.location.href,
            referrer: document.referrer
          } : null,
          timestamp: Date.now()
        }
        
        // 发送报告
        // 这里可以调用API发送报告，或者使用其他方式
        // 例如：await api.post('/api/error-report', reportData)
        
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 触发提交事件
        emit('submit', reportData)
        
        // 显示成功消息
        ElMessage({
          type: 'success',
          message: '错误报告已提交，感谢您的反馈！'
        })
        
        // 关闭对话框
        dialogVisible.value = false
      } catch (error) {
        console.error('提交错误报告失败:', error)
        
        // 显示错误消息
        ElMessage({
          type: 'error',
          message: '提交错误报告失败，请稍后重试'
        })
      } finally {
        submitting.value = false
      }
    }
  })
}

/**
 * 处理对话框关闭
 */
function handleDialogClosed() {
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields()
  }
  
  // 触发关闭事件
  emit('close')
}

/**
 * 打开错误报告对话框
 */
function open() {
  dialogVisible.value = true
  
  // 如果有错误信息，自动填充描述
  if (props.errorInfo?.message) {
    form.description = `我遇到了以下错误: "${props.errorInfo.message}"`
  }
}

/**
 * 关闭错误报告对话框
 */
function close() {
  dialogVisible.value = false
}

// 暴露方法
defineExpose({
  open,
  close
})
</script>

<style scoped>
.error-report-content {
  padding: 0 10px;
}

.error-summary {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #FEF0F0;
  border-radius: 4px;
}

.error-icon {
  margin-right: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-details {
  flex: 1;
}

.error-details h3 {
  margin: 0 0 10px 0;
  color: #F56C6C;
  font-size: 16px;
}

.error-time, .error-location {
  margin: 5px 0;
  font-size: 13px;
  color: #606266;
}

.tech-info {
  display: flex;
  align-items: center;
}

.tech-info .el-icon {
  margin-left: 5px;
  color: #909399;
  cursor: help;
}

.tech-info-preview {
  margin-top: 10px;
  padding: 10px;
  background-color: #F5F7FA;
  border-radius: 4px;
  font-size: 12px;
  max-height: 150px;
  overflow-y: auto;
}

.tech-info-preview pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.privacy-notice {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

.w-100 {
  width: 100%;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .error-summary {
    background-color: rgba(245, 108, 108, 0.1);
  }
  
  .tech-info-preview {
    background-color: #2c2c2c;
  }
}
</style>