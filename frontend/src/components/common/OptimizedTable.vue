<template>
  <div class="optimized-table">
    <el-table
      ref="tableRef"
      v-bind="$attrs"
      :data="visibleData"
      v-on="$listeners"
      @scroll="handleScroll"
    >
      <slot></slot>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useComponentLifecycle } from '@/utils/componentLifecycle'
import { throttle } from '@/utils/throttleDebounce'
import { useBatchUpdate } from '@/utils/renderOptimizer'

// 组件生命周期管理
const { registerComponentCleanup } = useComponentLifecycle('OptimizedTable')

// 组件属性
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    required: true,
    default: () => []
  },
  // 是否启用虚拟滚动
  virtualScroll: {
    type: Boolean,
    default: false
  },
  // 每次渲染的最大行数
  batchSize: {
    type: Number,
    default: 100
  },
  // 是否启用批量更新
  batchUpdate: {
    type: Boolean,
    default: true
  }
})

// 表格引用
const tableRef = ref(null)

// 使用批量更新
const { state: visibleData, updateState: updateVisibleData } = useBatchUpdate([], 50)

// 当前滚动位置
const scrollTop = ref(0)

// 处理滚动事件
const handleScroll = throttle((e: Event) => {
  if (!props.virtualScroll) return
  
  const target = e.target as HTMLElement
  scrollTop.value = target.scrollTop
}, 100)

// 更新可见数据
const updateData = () => {
  if (!props.batchUpdate) {
    // 如果不使用批量更新，直接设置数据
    visibleData.value = props.data
    return
  }
  
  if (props.data.length <= props.batchSize) {
    // 如果数据量小于批处理大小，直接设置所有数据
    updateVisibleData(props.data)
    return
  }
  
  // 否则，分批次渲染数据
  const renderBatch = (startIndex: number, endIndex: number) => {
    const batch = props.data.slice(0, endIndex)
    updateVisibleData(batch)
    
    // 如果还有更多数据，继续渲染
    if (endIndex < props.data.length) {
      setTimeout(() => {
        renderBatch(endIndex, Math.min(endIndex + props.batchSize, props.data.length))
      }, 16) // 约一帧的时间
    }
  }
  
  // 开始批量渲染
  renderBatch(0, props.batchSize)
}

// 监听数据变化
watch(
  () => props.data,
  () => {
    updateData()
  },
  { deep: true }
)

// 组件挂载后初始化数据
onMounted(() => {
  nextTick(() => {
    updateData()
  })
})

// 注册清理函数
registerComponentCleanup('scrollHandler', () => {
  // 清理相关资源
})

// 暴露方法
defineExpose({
  // 刷新表格数据
  refreshTable: () => {
    updateData()
  }
})
</script>

<style scoped>
.optimized-table {
  width: 100%;
}
</style>