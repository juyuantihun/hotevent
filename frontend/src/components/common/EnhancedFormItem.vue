<template>
  <el-form-item
    :label="label"
    :prop="prop"
    :label-width="labelWidth"
    :required="required"
    :rules="rules"
    :error="error"
    :show-message="showMessage"
    :inline-message="inlineMessage"
    :size="size"
    :validate-status="validateStatus"
  >
    <template v-if="$slots.label" #label>
      <slot name="label"></slot>
    </template>
    
    <template v-if="$slots.error" #error="scope">
      <slot name="error" v-bind="scope"></slot>
    </template>
    
    <div class="form-item-content">
      <slot></slot>
      
      <div v-if="helpText && !error" class="form-item-help">
        <el-icon v-if="showHelpIcon" class="help-icon"><InfoFilled /></el-icon>
        <span>{{ helpText }}</span>
      </div>
      
      <div v-if="characterCount && hasInputContent" class="character-count" :class="{ 'is-limit': isNearLimit }">
        {{ currentLength }} / {{ maxLength }}
      </div>
    </div>
  </el-form-item>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'

const props = defineProps({
  // 标签文本
  label: {
    type: String,
    default: ''
  },
  // 表单域字段
  prop: {
    type: String,
    default: ''
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: ''
  },
  // 是否必填
  required: {
    type: Boolean,
    default: undefined
  },
  // 表单验证规则
  rules: {
    type: [Object, Array] as () => FormItemRule | FormItemRule[],
    default: () => []
  },
  // 表单验证错误信息
  error: {
    type: String,
    default: ''
  },
  // 是否显示校验错误信息
  showMessage: {
    type: Boolean,
    default: true
  },
  // 是否以行内形式展示校验信息
  inlineMessage: {
    type: Boolean,
    default: false
  },
  // 表单尺寸
  size: {
    type: String,
    default: ''
  },
  // 校验状态
  validateStatus: {
    type: String,
    default: '',
    validator: (value: string) => ['', 'success', 'error', 'validating'].includes(value)
  },
  // 帮助文本
  helpText: {
    type: String,
    default: ''
  },
  // 是否显示帮助图标
  showHelpIcon: {
    type: Boolean,
    default: true
  },
  // 是否显示字符计数
  characterCount: {
    type: Boolean,
    default: false
  },
  // 最大字符数
  maxLength: {
    type: Number,
    default: 0
  },
  // 当前输入内容
  modelValue: {
    type: [String, Number],
    default: ''
  },
  // 接近限制的阈值百分比（0-1之间）
  limitThreshold: {
    type: Number,
    default: 0.9
  }
})

// 计算当前输入长度
const currentLength = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined) return 0
  return String(props.modelValue).length
})

// 判断是否有输入内容
const hasInputContent = computed(() => {
  return props.modelValue !== null && props.modelValue !== undefined && props.modelValue !== ''
})

// 判断是否接近字符限制
const isNearLimit = computed(() => {
  if (!props.maxLength || props.maxLength <= 0) return false
  return currentLength.value >= props.maxLength * props.limitThreshold
})
</script>

<style scoped>
.form-item-content {
  position: relative;
}

.form-item-help {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary, #909399);
  line-height: 1.4;
}

.help-icon {
  font-size: 14px;
  color: var(--info-color, #909399);
}

.character-count {
  position: absolute;
  bottom: -18px;
  right: 0;
  font-size: 12px;
  color: var(--text-secondary, #909399);
}

.character-count.is-limit {
  color: var(--warning-color, #e6a23c);
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .form-item-help {
    color: var(--text-secondary, #a0a0a0);
  }
  
  .help-icon {
    color: var(--info-color, #909399);
  }
  
  .character-count {
    color: var(--text-secondary, #a0a0a0);
  }
  
  .character-count.is-limit {
    color: var(--warning-color, #e6a23c);
  }
}
</style>