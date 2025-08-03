<template>
  <div class="enhanced-select" :class="{ 'is-loading': loading }">
    <el-select
      v-model="selectedValue"
      :multiple="multiple"
      :disabled="disabled || loading"
      :clearable="clearable"
      :collapse-tags="collapseTags"
      :collapse-tags-tooltip="collapseTagsTooltip"
      :max-collapse-tags="maxCollapseTags"
      :placeholder="placeholder"
      :filterable="filterable"
      :allow-create="allowCreate"
      :filter-method="customFilterMethod || undefined"
      :remote="remote"
      :remote-method="remoteMethod"
      :loading="loading"
      :remote-show-suffix="remoteShowSuffix"
      :persistent="persistent"
      :automatic-dropdown="automaticDropdown"
      :clear-icon="clearIcon"
      :effect="effect"
      :tag-type="tagType"
      :validate-event="validateEvent"
      :size="size"
      :teleported="teleported"
      :placement="placement"
      :popper-class="popperClass"
      :popper-options="popperOptions"
      :popper-append-to-body="popperAppendToBody"
      :reserve-keyword="reserveKeyword"
      :value-key="valueKey"
      :default-first-option="defaultFirstOption"
      :no-match-text="noMatchText"
      :no-data-text="noDataText"
      @change="handleChange"
      @visible-change="handleVisibleChange"
      @remove-tag="handleRemoveTag"
      @clear="handleClear"
      @blur="handleBlur"
      @focus="handleFocus"
    >
      <template v-if="$slots.prefix" #prefix>
        <slot name="prefix"></slot>
      </template>
      
      <template v-if="$slots.empty" #empty>
        <slot name="empty"></slot>
      </template>
      
      <template v-if="loading && !$slots.empty">
        <el-option disabled>
          <div class="select-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ loadingText }}</span>
          </div>
        </el-option>
      </template>
      
      <template v-else-if="options.length === 0 && !$slots.empty">
        <el-option disabled>
          <div class="select-empty">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ noDataText }}</span>
          </div>
        </el-option>
      </template>
      
      <template v-else>
        <el-option-group 
          v-for="group in groupedOptions" 
          :key="group.label" 
          :label="group.label"
          v-if="group.options.length > 0"
        >
          <el-option
            v-for="item in group.options"
            :key="item[valueKey]"
            :label="item[labelKey]"
            :value="item[valueKey]"
            :disabled="item.disabled"
          >
            <slot name="option" :item="item">
              <div class="option-content">
                <span>{{ item[labelKey] }}</span>
                <span v-if="item.description" class="option-description">{{ item.description }}</span>
              </div>
            </slot>
          </el-option>
        </el-option-group>
        
        <el-option
          v-for="item in ungroupedOptions"
          :key="item[valueKey]"
          :label="item[labelKey]"
          :value="item[valueKey]"
          :disabled="item.disabled"
        >
          <slot name="option" :item="item">
            <div class="option-content">
              <span>{{ item[labelKey] }}</span>
              <span v-if="item.description" class="option-description">{{ item.description }}</span>
            </div>
          </slot>
        </el-option>
      </template>
    </el-select>
    
    <div v-if="helpText" class="select-help-text">
      {{ helpText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Loading, InfoFilled } from '@element-plus/icons-vue'

interface SelectOption {
  [key: string]: any
  disabled?: boolean
  group?: string
  description?: string
}

const props = defineProps({
  // 选择器的值
  modelValue: {
    type: [String, Number, Boolean, Object, Array],
    default: ''
  },
  // 选项数据
  options: {
    type: Array as () => SelectOption[],
    default: () => []
  },
  // 是否多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 是否可清空
  clearable: {
    type: Boolean,
    default: true
  },
  // 多选时是否将选中值按文字的形式展示
  collapseTags: {
    type: Boolean,
    default: false
  },
  // 当鼠标悬停于折叠标签的文本时，是否显示所有选中的标签
  collapseTagsTooltip: {
    type: Boolean,
    default: false
  },
  // 最多显示多少个标签
  maxCollapseTags: {
    type: Number,
    default: 1
  },
  // 占位文本
  placeholder: {
    type: String,
    default: '请选择'
  },
  // 是否可搜索
  filterable: {
    type: Boolean,
    default: false
  },
  // 是否允许用户创建新条目
  allowCreate: {
    type: Boolean,
    default: false
  },
  // 自定义搜索方法
  customFilterMethod: {
    type: Function,
    default: null
  },
  // 是否为远程搜索
  remote: {
    type: Boolean,
    default: false
  },
  // 远程搜索方法
  remoteMethod: {
    type: Function,
    default: null
  },
  // 是否正在加载数据
  loading: {
    type: Boolean,
    default: false
  },
  // 加载中显示的文本
  loadingText: {
    type: String,
    default: '加载中...'
  },
  // 远程搜索方法显示后缀图标
  remoteShowSuffix: {
    type: Boolean,
    default: false
  },
  // 选择器下拉菜单是否持久显示
  persistent: {
    type: Boolean,
    default: false
  },
  // 是否自动弹出下拉菜单
  automaticDropdown: {
    type: Boolean,
    default: false
  },
  // 自定义清除图标
  clearIcon: {
    type: String,
    default: 'CircleClose'
  },
  // 下拉菜单的主题
  effect: {
    type: String,
    default: 'light',
    validator: (value: string) => ['light', 'dark'].includes(value)
  },
  // 多选标签的类型
  tagType: {
    type: String,
    default: 'info',
    validator: (value: string) => ['success', 'info', 'warning', 'danger', ''].includes(value)
  },
  // 是否触发表单验证
  validateEvent: {
    type: Boolean,
    default: true
  },
  // 选择器大小
  size: {
    type: String,
    default: 'default',
    validator: (value: string) => ['large', 'default', 'small'].includes(value)
  },
  // 是否使用 teleport
  teleported: {
    type: Boolean,
    default: true
  },
  // 下拉框的定位
  placement: {
    type: String,
    default: 'bottom-start',
    validator: (value: string) => [
      'top', 'top-start', 'top-end',
      'bottom', 'bottom-start', 'bottom-end',
      'left', 'left-start', 'left-end',
      'right', 'right-start', 'right-end'
    ].includes(value)
  },
  // 下拉框的自定义类名
  popperClass: {
    type: String,
    default: ''
  },
  // 下拉框的自定义配置
  popperOptions: {
    type: Object,
    default: () => ({})
  },
  // 是否将下拉框插入至 body 元素
  popperAppendToBody: {
    type: Boolean,
    default: true
  },
  // 搜索时，是否在选项中保留关键字
  reserveKeyword: {
    type: Boolean,
    default: true
  },
  // 选项对象中用于显示的键名
  labelKey: {
    type: String,
    default: 'label'
  },
  // 选项对象中值的键名
  valueKey: {
    type: String,
    default: 'value'
  },
  // 是否在输入框按下回车时，选择第一个匹配项
  defaultFirstOption: {
    type: Boolean,
    default: false
  },
  // 搜索条件无匹配时显示的文字
  noMatchText: {
    type: String,
    default: '无匹配数据'
  },
  // 选项为空时显示的文字
  noDataText: {
    type: String,
    default: '无数据'
  },
  // 帮助文本
  helpText: {
    type: String,
    default: ''
  },
  // 是否按组显示选项
  groupByKey: {
    type: String,
    default: 'group'
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'visible-change', 'remove-tag', 'clear', 'blur', 'focus'])

// 选中的值
const selectedValue = ref(props.modelValue)

// 监听modelValue变化
watch(() => props.modelValue, (newVal) => {
  selectedValue.value = newVal
})

// 监听selectedValue变化
watch(() => selectedValue.value, (newVal) => {
  emit('update:modelValue', newVal)
})

// 计算分组后的选项
const groupedOptions = computed(() => {
  // 如果没有设置groupByKey，则返回空数组
  if (!props.groupByKey) return []
  
  // 获取所有分组
  const groups = [...new Set(props.options
    .filter(item => item[props.groupByKey])
    .map(item => item[props.groupByKey]))]
  
  // 为每个分组创建选项组
  return groups.map(group => ({
    label: group,
    options: props.options.filter(item => item[props.groupByKey] === group)
  }))
})

// 计算未分组的选项
const ungroupedOptions = computed(() => {
  return props.options.filter(item => !item[props.groupByKey])
})

// 处理值变化
const handleChange = (value: any) => {
  emit('change', value)
}

// 处理下拉框可见性变化
const handleVisibleChange = (visible: boolean) => {
  emit('visible-change', visible)
}

// 处理移除标签
const handleRemoveTag = (tag: any) => {
  emit('remove-tag', tag)
}

// 处理清空
const handleClear = () => {
  emit('clear')
}

// 处理失去焦点
const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

// 处理获得焦点
const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}
</script>

<style scoped>
.enhanced-select {
  width: 100%;
}

.enhanced-select :deep(.el-select) {
  width: 100%;
}

.select-help-text {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary, #909399);
  line-height: 1.4;
}

.select-loading,
.select-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 0;
  color: var(--text-secondary, #909399);
}

.option-content {
  display: flex;
  flex-direction: column;
}

.option-description {
  font-size: 12px;
  color: var(--text-secondary, #909399);
  margin-top: 2px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .select-help-text {
    color: var(--text-secondary, #a0a0a0);
  }
  
  .select-loading,
  .select-empty {
    color: var(--text-secondary, #a0a0a0);
  }
  
  .option-description {
    color: var(--text-secondary, #a0a0a0);
  }
}
</style>