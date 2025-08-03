<template>
  <div class="list-container">
    <div v-if="items.length === 0" class="empty-state">
      <EmptyState :description="emptyText">
        <slot name="empty-actions"></slot>
      </EmptyState>
    </div>
    
    <VirtualScroller
      v-else
      :items="items"
      :height="containerHeight"
      :item-height="itemHeight"
      :buffer="buffer"
    >
      <template #default="slotProps">
        <slot v-bind="slotProps"></slot>
      </template>
    </VirtualScroller>
  </div>
</template>

<script setup lang="ts">
/**
 * 列表容器组件
 * 用于高效渲染大型列表，支持虚拟滚动和空状态显示
 */
import { ref, onMounted, watch } from 'vue'
import { useComponentLifecycle } from '@/utils/componentLifecycle'
import { throttle } from '@/utils/throttleDebounce'
import VirtualScroller from '@/components/common/VirtualScroller.vue'
import EmptyState from '@/components/common/EmptyState.vue'

// 组件生命周期管理
const { registerEventListener } = useComponentLifecycle('ListContainer')

/**
 * 组件属性定义
 */
const props = defineProps({
  // 列表数据
  items: {
    type: Array,
    required: true,
    default: () => []
  },
  // 容器高度
  height: {
    type: Number,
    default: 500
  },
  // 项目高度
  itemHeight: {
    type: Number,
    default: 200
  },
  // 缓冲区大小
  buffer: {
    type: Number,
    default: 3
  },
  // 空状态文本
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  // 是否自动调整高度
  autoHeight: {
    type: Boolean,
    default: true
  },
  // 高度偏移量（用于减去其他UI元素的高度）
  heightOffset: {
    type: Number,
    default: 300
  }
})

/**
 * 容器高度响应式引用
 */
const containerHeight = ref(props.height)

/**
 * 更新容器高度
 * 根据autoHeight属性决定是使用固定高度还是自适应高度
 */
const updateContainerHeight = () => {
  if (props.autoHeight) {
    const windowHeight = window.innerHeight
    // 减去其他UI元素的高度
    containerHeight.value = windowHeight - props.heightOffset
  } else {
    containerHeight.value = props.height
  }
}

/**
 * 处理窗口大小变化事件
 * 使用节流函数优化性能
 */
const handleResize = throttle(() => {
  updateContainerHeight()
}, 200)

/**
 * 组件挂载时初始化容器高度并注册事件监听
 */
onMounted(() => {
  updateContainerHeight()
  if (props.autoHeight) {
    registerEventListener(window, 'resize', handleResize)
  }
})

/**
 * 监听高度属性变化
 * 当非自动高度模式下，高度属性变化时更新容器高度
 */
watch(() => props.height, (newHeight) => {
  if (!props.autoHeight) {
    containerHeight.value = newHeight
  }
})

/**
 * 监听高度偏移量变化
 * 当自动高度模式下，偏移量变化时重新计算容器高度
 */
watch(() => props.heightOffset, () => {
  if (props.autoHeight) {
    updateContainerHeight()
  }
})
</script>

<style scoped>
.list-container {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .empty-state {
    background: #1a1a1a;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
}
</style>