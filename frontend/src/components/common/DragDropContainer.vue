<template>
  <div 
    class="drag-drop-container"
    :class="{
      'is-dragging': isDragging,
      'is-dragging-over': isDraggingOver,
      'is-disabled': disabled
    }"
    @dragover.prevent="handleDragOver"
    @dragleave.prevent="handleDragLeave"
    @drop.prevent="handleDrop"
    @click="handleClick"
  >
    <div v-if="$slots.default" class="drag-drop-content">
      <slot></slot>
    </div>
    
    <div v-else class="drag-drop-placeholder">
      <slot name="placeholder">
        <div class="placeholder-content">
          <el-icon class="placeholder-icon"><Upload /></el-icon>
          <div class="placeholder-text">
            <div class="placeholder-title">{{ title }}</div>
            <div class="placeholder-description">{{ description }}</div>
          </div>
        </div>
      </slot>
    </div>
    
    <input
      ref="fileInputRef"
      type="file"
      class="file-input"
      :accept="accept"
      :multiple="multiple"
      :disabled="disabled"
      @change="handleFileInputChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  // 是否允许多文件
  multiple: {
    type: Boolean,
    default: false
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: ''
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 占位标题
  title: {
    type: String,
    default: '拖放文件到此处'
  },
  // 占位描述
  description: {
    type: String,
    default: '或点击选择文件'
  },
  // 最大文件大小（字节）
  maxSize: {
    type: Number,
    default: 0
  },
  // 文件类型验证函数
  validateType: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['file-selected', 'file-error', 'click'])

const fileInputRef = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const isDraggingOver = ref(false)

// 处理拖拽进入
const handleDragOver = (event: DragEvent) => {
  if (props.disabled) return
  
  isDraggingOver.value = true
  
  // 设置拖拽效果
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy'
  }
}

// 处理拖拽离开
const handleDragLeave = () => {
  isDraggingOver.value = false
}

// 处理拖拽放置
const handleDrop = (event: DragEvent) => {
  if (props.disabled) return
  
  isDraggingOver.value = false
  
  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return
  
  // 处理文件
  handleFiles(props.multiple ? Array.from(files) : [files[0]])
}

// 处理文件输入变化
const handleFileInputChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  
  if (!files || files.length === 0) return
  
  // 处理文件
  handleFiles(props.multiple ? Array.from(files) : [files[0]])
  
  // 重置文件输入，以便可以选择相同的文件
  target.value = ''
}

// 处理文件
const handleFiles = (files: File[]) => {
  // 验证文件
  const validFiles = files.filter(file => {
    // 验证文件大小
    if (props.maxSize > 0 && file.size > props.maxSize) {
      emit('file-error', {
        file,
        error: 'size',
        message: `文件大小超过限制（${formatFileSize(props.maxSize)}）`
      })
      return false
    }
    
    // 验证文件类型
    if (props.validateType && !props.validateType(file)) {
      emit('file-error', {
        file,
        error: 'type',
        message: '文件类型不支持'
      })
      return false
    }
    
    return true
  })
  
  // 发出文件选择事件
  if (validFiles.length > 0) {
    emit('file-selected', props.multiple ? validFiles : validFiles[0])
  }
}

// 处理点击
const handleClick = () => {
  if (props.disabled) return
  
  // 触发文件输入点击
  fileInputRef.value?.click()
  
  emit('click')
}

// 格式化文件大小
const formatFileSize = (size: number): string => {
  if (size < 1024) {
    return `${size} B`
  } else if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(2)} KB`
  } else if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(2)} MB`
  } else {
    return `${(size / (1024 * 1024 * 1024)).toFixed(2)} GB`
  }
}
</script>

<style scoped>
.drag-drop-container {
  position: relative;
  border: 2px dashed var(--border-color, #dcdfe6);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: var(--background-color, #f5f7fa);
}

.drag-drop-container:hover {
  border-color: var(--primary-color, #409eff);
}

.drag-drop-container.is-dragging-over {
  border-color: var(--primary-color, #409eff);
  background-color: var(--primary-light, #ecf5ff);
}

.drag-drop-container.is-disabled {
  cursor: not-allowed;
  opacity: 0.7;
  border-color: var(--border-color, #dcdfe6);
  background-color: var(--disabled-bg, #f5f7fa);
}

.drag-drop-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 150px;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.placeholder-icon {
  font-size: 48px;
  color: var(--text-secondary, #909399);
}

.placeholder-text {
  text-align: center;
}

.placeholder-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--text-primary, #303133);
  margin-bottom: 8px;
}

.placeholder-description {
  font-size: 14px;
  color: var(--text-secondary, #909399);
}

.file-input {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  overflow: hidden;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .drag-drop-container {
    border-color: var(--border-base, #4a4a4a);
    background-color: var(--background-lighter, #363636);
  }
  
  .drag-drop-container:hover {
    border-color: var(--primary-color, #409eff);
  }
  
  .drag-drop-container.is-dragging-over {
    border-color: var(--primary-color, #409eff);
    background-color: var(--primary-light, #1a3a5f);
  }
  
  .drag-drop-container.is-disabled {
    border-color: var(--border-base, #4a4a4a);
    background-color: var(--background-base, #1e1e1e);
  }
  
  .placeholder-icon {
    color: var(--text-secondary, #a0a0a0);
  }
  
  .placeholder-title {
    color: var(--text-primary, #e0e0e0);
  }
  
  .placeholder-description {
    color: var(--text-secondary, #a0a0a0);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .drag-drop-container {
    padding: 15px;
  }
  
  .drag-drop-placeholder {
    min-height: 120px;
  }
  
  .placeholder-icon {
    font-size: 36px;
  }
  
  .placeholder-title {
    font-size: 14px;
  }
  
  .placeholder-description {
    font-size: 12px;
  }
}
</style>