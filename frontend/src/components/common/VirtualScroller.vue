<template>
  <div 
    ref="containerRef" 
    class="virtual-scroller" 
    @scroll="handleScroll"
    :style="{ height: `${height}px`, overflow: 'auto' }"
  >
    <div 
      class="virtual-scroller-phantom" 
      :style="{ height: `${totalHeight}px` }"
    ></div>
    <div 
      class="virtual-scroller-content" 
      :style="{ transform: `translateY(${offsetY}px)` }"
    >
      <slot 
        v-for="item in visibleItems" 
        :key="item.index" 
        :item="item.data" 
        :index="item.index"
      ></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useComponentLifecycle } from '@/utils/componentLifecycle'

// 组件生命周期管理
const { registerEventListener } = useComponentLifecycle('VirtualScroller')

// 组件属性
const props = defineProps({
  // 列表数据
  items: {
    type: Array,
    required: true
  },
  // 容器高度
  height: {
    type: Number,
    default: 500
  },
  // 每项高度
  itemHeight: {
    type: Number,
    default: 50
  },
  // 缓冲区大小（上下额外渲染的项数）
  buffer: {
    type: Number,
    default: 5
  }
})

// DOM引用
const containerRef = ref<HTMLElement | null>(null)

// 滚动位置
const scrollTop = ref(0)

// 处理滚动事件
const handleScroll = () => {
  if (containerRef.value) {
    scrollTop.value = containerRef.value.scrollTop
  }
}

// 计算总高度
const totalHeight = computed(() => {
  return props.items.length * props.itemHeight
})

// 计算可见区域的起始索引
const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.buffer)
})

// 计算可见区域的结束索引
const endIndex = computed(() => {
  const visibleCount = Math.ceil(props.height / props.itemHeight)
  return Math.min(props.items.length - 1, startIndex.value + visibleCount + props.buffer * 2)
})

// 计算可见项
const visibleItems = computed(() => {
  return props.items
    .slice(startIndex.value, endIndex.value + 1)
    .map((item, index) => ({
      data: item,
      index: startIndex.value + index
    }))
})

// 计算内容偏移量
const offsetY = computed(() => {
  return startIndex.value * props.itemHeight
})

// 滚动到指定索引
const scrollToIndex = (index: number) => {
  if (containerRef.value) {
    containerRef.value.scrollTop = index * props.itemHeight
  }
}

// 监听数据变化，重置滚动位置
watch(
  () => props.items,
  () => {
    nextTick(() => {
      scrollTop.value = 0
      if (containerRef.value) {
        containerRef.value.scrollTop = 0
      }
    })
  },
  { deep: false }
)

// 监听窗口大小变化
onMounted(() => {
  registerEventListener(window, 'resize', () => {
    handleScroll()
  })
})

// 暴露方法
defineExpose({
  scrollToIndex
})
</script>

<style scoped>
.virtual-scroller {
  position: relative;
  overflow-y: auto;
}

.virtual-scroller-phantom {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  z-index: -1;
}

.virtual-scroller-content {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  min-height: 100%;
}
</style>