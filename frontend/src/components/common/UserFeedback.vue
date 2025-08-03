<template>
  <div class="user-feedback">
    <el-button
      v-if="showButton"
      :type="buttonType"
      :size="buttonSize"
      :icon="buttonIcon"
      @click="openFeedback"
    >
      {{ buttonText }}
    </el-button>
    
    <el-dialog
      v-model="dialogVisible"
      :title="title"
      width="600px"
      :close-on-click-modal="false"
      :show-close="true"
      @closed="handleDialogClosed"
    >
      <div class="feedback-content">
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="反馈类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择反馈类型" class="w-100">
              <el-option label="功能建议" value="feature" />
              <el-option label="问题报告" value="bug" />
              <el-option label="使用体验" value="experience" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          
          <el-form-item v-if="form.type === 'bug'" label="问题严重程度" prop="severity">
            <el-rate
              v-model="form.severity"
              :colors="severityColors"
              :texts="severityTexts"
              show-text
              :max="5"
            />
          </el-form-item>
          
          <el-form-item label="反馈内容" prop="content">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="4"
              :placeholder="contentPlaceholder"
            />
          </el-form-item>
          
          <el-form-item v-if="form.type === 'bug'" label="重现步骤" prop="steps">
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
              placeholder="您的邮箱或其他联系方式，以便我们跟进反馈"
            />
          </el-form-item>
          
          <el-form-item label="附加信息">
            <div class="tech-info">
              <el-checkbox v-model="form.includeTechInfo">
                包含系统信息（浏览器、操作系统等）
              </el-checkbox>
              <el-tooltip
                content="这些信息有助于我们更好地理解您的使用环境"
                placement="top"
              >
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </div>
            
            <div v-if="form.includeTechInfo" class="tech-info-preview">
              <pre>{{ techInfoPreview }}</pre>
            </div>
            
            <div v-if="form.type === 'bug'" class="screenshot-upload">
              <el-upload
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :limit="3"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="el-upload__tip">
                    可以上传截图帮助我们更好地理解问题（可选，最多3张）
                  </div>
                </template>
              </el-upload>
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
          <el-button type="primary" @click="submitFeedback" :loading="submitting">
            提交反馈
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'

// 定义组件属性
const props = defineProps({
  // 对话框标题
  title: {
    type: String,
    default: '用户反馈'
  },
  // 是否显示按钮
  showButton: {
    type: Boolean,
    default: true
  },
  // 按钮文本
  buttonText: {
    type: String,
    default: '反馈'
  },
  // 按钮类型
  buttonType: {
    type: String,
    default: 'primary'
  },
  // 按钮大小
  buttonSize: {
    type: String,
    default: 'default'
  },
  // 按钮图标
  buttonIcon: {
    type: String,
    default: ''
  },
  // 初始反馈类型
  initialType: {
    type: String,
    default: 'feature'
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

// 文件列表
const fileList = ref<UploadFile[]>([])

// 表单数据
const form = reactive({
  type: props.initialType,
  severity: 3,
  content: '',
  steps: '',
  contact: '',
  includeTechInfo: true,
  screenshots: [] as File[]
})

// 表单验证规则
const rules = reactive<FormRules>({
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请填写反馈内容', trigger: 'blur' },
    { min: 10, message: '内容至少需要10个字符', trigger: 'blur' }
  ],
  contact: [
    { pattern: /^$|^.+@.+\..+$/, message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
})

// 严重程度颜色
const severityColors = ['#67C23A', '#E6A23C', '#E6A23C', '#F56C6C', '#F56C6C']

// 严重程度文本
const severityTexts = ['轻微', '低', '中等', '高', '严重']

// 内容占位符
const contentPlaceholder = computed(() => {
  switch (form.type) {
    case 'feature':
      return '请描述您希望添加的功能或改进建议'
    case 'bug':
      return '请描述您遇到的问题，包括您期望的行为和实际发生的情况'
    case 'experience':
      return '请分享您使用产品的体验和感受'
    default:
      return '请输入您的反馈内容'
  }
})

// 技术信息预览
const techInfoPreview = computed(() => {
  const info = {
    browser: navigator.userAgent,
    platform: navigator.platform,
    language: navigator.language,
    screenSize: `${window.screen.width}x${window.screen.height}`,
    viewportSize: `${window.innerWidth}x${window.innerHeight}`,
    url: window.location.href
  }
  
  return JSON.stringify(info, null, 2)
})

/**
 * 处理文件变更
 * @param file 文件
 * @param fileList 文件列表
 */
function handleFileChange(file: UploadFile) {
  if (file.raw) {
    form.screenshots.push(file.raw)
  }
}

/**
 * 处理文件移除
 * @param file 文件
 * @param fileList 文件列表
 */
function handleFileRemove(file: UploadFile) {
  const index = form.screenshots.findIndex(f => f.name === file.name)
  if (index !== -1) {
    form.screenshots.splice(index, 1)
  }
}

/**
 * 打开反馈对话框
 * @param type 反馈类型
 */
function openFeedback(type?: string) {
  if (type) {
    form.type = type
  }
  dialogVisible.value = true
}

/**
 * 提交反馈
 */
async function submitFeedback() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      try {
        // 构建反馈数据
        const feedbackData = {
          ...form,
          techInfo: form.includeTechInfo ? {
            browser: navigator.userAgent,
            platform: navigator.platform,
            language: navigator.language,
            screenSize: `${window.screen.width}x${window.screen.height}`,
            viewportSize: `${window.innerWidth}x${window.innerHeight}`,
            url: window.location.href
          } : null,
          timestamp: Date.now()
        }
        
        // 发送反馈
        // 这里可以调用API发送反馈，或者使用其他方式
        // 例如：await api.post('/api/feedback', feedbackData)
        
        // 如果有截图，需要使用FormData
        if (form.screenshots.length > 0) {
          const formData = new FormData()
          formData.append('data', JSON.stringify(feedbackData))
          
          form.screenshots.forEach((file, index) => {
            formData.append(`screenshot_${index}`, file)
          })
          
          // 发送带截图的反馈
          // 例如：await api.post('/api/feedback-with-screenshots', formData)
        }
        
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 触发提交事件
        emit('submit', feedbackData)
        
        // 显示成功消息
        ElMessage({
          type: 'success',
          message: '感谢您的反馈！我们会认真考虑您的意见。'
        })
        
        // 关闭对话框
        dialogVisible.value = false
      } catch (error) {
        console.error('提交反馈失败:', error)
        
        // 显示错误消息
        ElMessage({
          type: 'error',
          message: '提交反馈失败，请稍后重试'
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
  
  // 清空文件列表
  fileList.value = []
  form.screenshots = []
  
  // 触发关闭事件
  emit('close')
}

// 暴露方法
defineExpose({
  openFeedback
})
</script>

<style scoped>
.feedback-content {
  padding: 0 10px;
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

.screenshot-upload {
  margin-top: 15px;
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
  .tech-info-preview {
    background-color: #2c2c2c;
  }
}
</style>