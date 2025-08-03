<template>
  <el-form
    ref="formRef"
    :model="model"
    :rules="rules"
    :label-width="labelWidth"
    :label-position="labelPosition"
    :size="size"
    :disabled="disabled"
    :validate-on-rule-change="validateOnRuleChange"
    :hide-required-asterisk="hideRequiredAsterisk"
    :status-icon="statusIcon"
    :inline="inline"
    :scroll-to-error="scrollToError"
    @submit.prevent="handleSubmit"
  >
    <slot></slot>
    
    <div v-if="showActions" class="form-actions" :class="{ 'is-inline': inline }">
      <slot name="actions">
        <el-button 
          v-if="showCancelButton" 
          :size="size" 
          @click="handleCancel"
        >
          {{ cancelButtonText }}
        </el-button>
        <el-button 
          type="primary" 
          :size="size" 
          :loading="submitLoading" 
          @click="handleSubmit"
        >
          {{ submitButtonText }}
        </el-button>
      </slot>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'

const props = defineProps({
  // 表单数据模型
  model: {
    type: Object,
    required: true
  },
  // 表单验证规则
  rules: {
    type: Object as () => FormRules,
    default: () => ({})
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: '100px'
  },
  // 标签位置
  labelPosition: {
    type: String,
    default: 'right',
    validator: (value: string) => ['left', 'right', 'top'].includes(value)
  },
  // 表单尺寸
  size: {
    type: String,
    default: 'default',
    validator: (value: string) => ['large', 'default', 'small'].includes(value)
  },
  // 是否禁用表单
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否在规则变化时触发验证
  validateOnRuleChange: {
    type: Boolean,
    default: true
  },
  // 是否隐藏必填字段的星号
  hideRequiredAsterisk: {
    type: Boolean,
    default: false
  },
  // 是否显示校验状态图标
  statusIcon: {
    type: Boolean,
    default: false
  },
  // 是否行内表单
  inline: {
    type: Boolean,
    default: false
  },
  // 是否在提交表单且校验失败时滚动到错误表单项
  scrollToError: {
    type: Boolean,
    default: true
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 是否显示取消按钮
  showCancelButton: {
    type: Boolean,
    default: true
  },
  // 提交按钮文本
  submitButtonText: {
    type: String,
    default: '提交'
  },
  // 取消按钮文本
  cancelButtonText: {
    type: String,
    default: '取消'
  },
  // 是否在提交时自动验证表单
  validateOnSubmit: {
    type: Boolean,
    default: true
  },
  // 是否在提交时显示加载状态
  showSubmitLoading: {
    type: Boolean,
    default: true
  },
  // 提交加载状态持续时间（毫秒），0表示不自动关闭
  loadingDuration: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits([
  'submit',
  'submit-success',
  'submit-error',
  'cancel',
  'validate',
  'validate-error'
])

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
let loadingTimer: number | null = null

// 暴露表单实例
defineExpose({
  formRef,
  validate,
  validateField,
  resetFields,
  scrollToField,
  clearValidate
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  if (props.validateOnSubmit) {
    try {
      await validate()
      submitForm()
    } catch (error) {
      emit('validate-error', error)
    }
  } else {
    submitForm()
  }
}

// 执行表单提交
const submitForm = () => {
  if (props.showSubmitLoading) {
    submitLoading.value = true
    
    // 如果设置了加载持续时间，则在指定时间后自动关闭加载状态
    if (props.loadingDuration > 0) {
      if (loadingTimer) {
        clearTimeout(loadingTimer)
      }
      
      loadingTimer = window.setTimeout(() => {
        submitLoading.value = false
      }, props.loadingDuration)
    }
  }
  
  emit('submit', props.model)
  
  // 使用nextTick确保在DOM更新后触发submit-success事件
  nextTick(() => {
    emit('submit-success', props.model)
  })
}

// 取消表单
const handleCancel = () => {
  emit('cancel')
}

// 验证表单
const validate = () => {
  if (!formRef.value) return Promise.reject('表单实例不存在')
  
  return new Promise((resolve, reject) => {
    formRef.value!.validate((valid, fields) => {
      if (valid) {
        emit('validate', true)
        resolve(true)
      } else {
        emit('validate', false, fields)
        reject(fields)
      }
    })
  })
}

// 验证表单字段
const validateField = (props: string | string[]) => {
  if (!formRef.value) return Promise.reject('表单实例不存在')
  
  return formRef.value.validateField(props)
}

// 重置表单字段
const resetFields = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 滚动到表单字段
const scrollToField = (prop: string) => {
  if (formRef.value) {
    formRef.value.scrollToField(prop)
  }
}

// 清除表单验证
const clearValidate = (props?: string | string[]) => {
  if (formRef.value) {
    formRef.value.clearValidate(props)
  }
}

// 在组件卸载前清除定时器
watch(() => props.disabled, (newVal) => {
  if (newVal && submitLoading.value) {
    submitLoading.value = false
    if (loadingTimer) {
      clearTimeout(loadingTimer)
      loadingTimer = null
    }
  }
})
</script>

<style scoped>
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid var(--border-lighter, #ebeef5);
}

.form-actions.is-inline {
  border-top: none;
  padding-top: 0;
  margin-top: 0;
  margin-left: 12px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .form-actions {
    border-top-color: var(--border-base, #4a4a4a);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .form-actions {
    flex-direction: column-reverse;
    align-items: stretch;
  }
  
  .form-actions.is-inline {
    flex-direction: row;
    align-items: center;
  }
}
</style>