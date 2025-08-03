<template>
  <div class="optimized-timeline-container" ref="containerRef">
    <!-- 虚拟滚动容器 -->
    <div 
      class="timeline-viewport" 
      :style="{ height: `${viewportHeight}px` }"
      @scroll="handleScroll"
      ref="viewportRef"
    >
      <!-- 时间线指示器 -->
      <div class="timeline-indicator" :style="{ height: `${totalHeight}px` }">
        <div class="timeline-line"></div>
      </div>
      
      <!-- 虚拟化事件列表 -->
      <div 
        class="timeline-events" 
        :style="{ 
          transform: `translateY(${offsetY}px)`,
          height: `${totalHeight}px`
        }"
      >
        <!-- 渲染可见的事件项 -->
        <div
          v-for="(item, index) in visibleItems"
          :key="item.id"
          class="timeline-event-item"
          :class="{
            'event-loading': item.loading,
            'event-error': item.error,
            'event-cached': item.cached
          }"
          :style="{
            transform: `translateY(${item.offsetY}px)`,
            height: `${itemHeight}px`
          }"
          @click="handleEventClick(item)"
        >
          <!-- 事件时间点 -->
          <div class="event-time-point">
            <div class="time-dot" :class="getEventTypeClass(item.eventType)"></div>
          </div>
          
          <!-- 事件内容卡片 -->
          <div class="event-card" :class="{ 'card-expanded': item.expanded }">
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="event-time">{{ formatEventTime(item.eventTime) }}</div>
              <div class="event-actions">
                <el-button 
                  size="small" 
                  type="text" 
                  @click.stop="toggleEventExpansion(item)"
                >
                  {{ item.expanded ? '收起' : '展开' }}
                </el-button>
              </div>
            </div>
            
            <!-- 卡片内容 -->
            <div class="card-content">
              <h4 class="event-title">{{ item.title }}</h4>
              <p class="event-description" v-show="item.expanded || !item.longDescription">
                {{ item.expanded ? item.description : item.shortDescription }}
              </p>
              
              <!-- 地理信息 -->
              <div class="geographic-info" v-if="item.location || item.coordinates">
                <el-icon><Location /></el-icon>
                <span class="location-text">{{ item.location }}</span>
                <span 
                  v-if="item.coordinates && showCoordinates" 
                  class="coordinates-text"
                >
                  ({{ item.coordinates.latitude.toFixed(4) }}, {{ item.coordinates.longitude.toFixed(4) }})
                </span>
              </div>
              
              <!-- 事件标签 -->
              <div class="event-tags" v-if="item.tags && item.tags.length > 0">
                <el-tag 
                  v-for="tag in item.tags" 
                  :key="tag" 
                  size="small" 
                  class="event-tag"
                >
                  {{ tag }}
                </el-tag>
              </div>
              
              <!-- 扩展信息 -->
              <div class="extended-info" v-show="item.expanded">
                <div class="info-row" v-if="item.source">
                  <span class="info-label">来源:</span>
                  <span class="info-value">{{ item.source }}</span>
                </div>
                <div class="info-row" v-if="item.credibility">
                  <span class="info-label">可信度:</span>
                  <span class="info-value">{{ item.credibility }}</span>
                </div>
                <div class="info-row" v-if="item.relatedEvents && item.relatedEvents.length > 0">
                  <span class="info-label">相关事件:</span>
                  <span class="info-value">{{ item.relatedEvents.length }}个</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 加载更多指示器 -->
    <div class="loading-indicator" v-if="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
    
    <!-- 性能统计面板（开发模式） -->
    <div class="performance-stats" v-if="showPerformanceStats && isDevelopment">
      <div class="stats-header">渲染性能统计</div>
      <div class="stats-content">
        <div class="stat-item">
          <span class="stat-label">总事件数:</span>
          <span class="stat-value">{{ totalItems }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">可见事件数:</span>
          <span class="stat-value">{{ visibleItems.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">渲染时间:</span>
          <span class="stat-value">{{ renderTime }}ms</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">滚动FPS:</span>
          <span class="stat-value">{{ scrollFPS }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">内存使用:</span>
          <span class="stat-value">{{ memoryUsage }}MB</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Loading } from '@element-plus/icons-vue'

// 类型定义
interface TimelineEvent {
  id: string
  title: string
  description: string
  shortDescription: string
  longDescription: boolean
  eventTime: string
  location?: string
  coordinates?: {
    latitude: number
    longitude: number
  }
  eventType: string
  source?: string
  credibility?: string
  tags?: string[]
  relatedEvents?: string[]
  expanded: boolean
  loading: boolean
  error: boolean
  cached: boolean
  offsetY: number
}

interface PerformanceStats {
  renderTime: number
  scrollFPS: number
  memoryUsage: number
  lastFrameTime: number
  frameCount: number
}

// Props
interface Props {
  events: any[]
  viewportHeight?: number
  itemHeight?: number
  bufferSize?: number
  showCoordinates?: boolean
  showPerformanceStats?: boolean
  enableVirtualScroll?: boolean
  enableLazyLoading?: boolean
  chunkSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  viewportHeight: 600,
  itemHeight: 120,
  bufferSize: 5,
  showCoordinates: false,
  showPerformanceStats: false,
  enableVirtualScroll: true,
  enableLazyLoading: true,
  chunkSize: 50
})

// Emits
const emit = defineEmits<{
  eventClick: [event: TimelineEvent]
  loadMore: []
  scroll: [scrollTop: number]
}>()

// 响应式状态
const containerRef = ref<HTMLElement>()
const viewportRef = ref<HTMLElement>()
const loading = ref(false)
const scrollTop = ref(0)
const offsetY = ref(0)

// 事件数据
const processedEvents = ref<TimelineEvent[]>([])
const visibleItems = ref<TimelineEvent[]>([])

// 性能统计
const performanceStats = reactive<PerformanceStats>({
  renderTime: 0,
  scrollFPS: 0,
  memoryUsage: 0,
  lastFrameTime: 0,
  frameCount: 0
})

// 计算属性
const totalItems = computed(() => processedEvents.value.length)
const totalHeight = computed(() => totalItems.value * props.itemHeight)
const visibleCount = computed(() => Math.ceil(props.viewportHeight / props.itemHeight) + props.bufferSize * 2)
const startIndex = computed(() => Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.bufferSize))
const endIndex = computed(() => Math.min(totalItems.value, startIndex.value + visibleCount.value))

const isDevelopment = computed(() => process.env.NODE_ENV === 'development')
const renderTime = computed(() => performanceStats.renderTime)
const scrollFPS = computed(() => performanceStats.scrollFPS)
const memoryUsage = computed(() => performanceStats.memoryUsage)

// 滚动性能优化
let scrollTimer: number | null = null
let lastScrollTime = 0
let frameId: number | null = null

// 监听事件数据变化
watch(() => props.events, (newEvents) => {
  processEvents(newEvents)
}, { immediate: true, deep: true })

// 监听滚动位置变化
watch([startIndex, endIndex], () => {
  updateVisibleItems()
})

// 处理事件数据
const processEvents = (events: any[]) => {
  const startTime = performance.now()
  
  processedEvents.value = events.map((event, index) => ({
    id: event.id || `event-${index}`,
    title: event.title || '未知事件',
    description: event.description || '',
    shortDescription: truncateText(event.description || '', 100),
    longDescription: (event.description || '').length > 100,
    eventTime: event.eventTime || new Date().toISOString(),
    location: event.location,
    coordinates: event.coordinates || (event.latitude && event.longitude ? {
      latitude: event.latitude,
      longitude: event.longitude
    } : undefined),
    eventType: event.eventType || 'default',
    source: event.source,
    credibility: event.credibility,
    tags: event.tags || [],
    relatedEvents: event.relatedEvents || [],
    expanded: false,
    loading: false,
    error: false,
    cached: false,
    offsetY: index * props.itemHeight
  }))
  
  // 更新性能统计
  performanceStats.renderTime = performance.now() - startTime
  
  // 更新可见项
  nextTick(() => {
    updateVisibleItems()
  })
}

// 更新可见项
const updateVisibleItems = () => {
  if (!props.enableVirtualScroll) {
    visibleItems.value = processedEvents.value
    return
  }
  
  const start = startIndex.value
  const end = endIndex.value
  
  visibleItems.value = processedEvents.value.slice(start, end).map(item => ({
    ...item,
    offsetY: item.offsetY - start * props.itemHeight
  }))
  
  offsetY.value = start * props.itemHeight
}

// 处理滚动事件
const handleScroll = (event: Event) => {
  const target = event.target as HTMLElement
  scrollTop.value = target.scrollTop
  
  // 性能监控
  const currentTime = performance.now()
  if (currentTime - lastScrollTime > 16) { // 60fps
    performanceStats.frameCount++
    performanceStats.scrollFPS = Math.round(1000 / (currentTime - performanceStats.lastFrameTime))
    performanceStats.lastFrameTime = currentTime
  }
  lastScrollTime = currentTime
  
  // 节流处理
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  
  scrollTimer = window.setTimeout(() => {
    emit('scroll', scrollTop.value)
    
    // 检查是否需要加载更多
    if (props.enableLazyLoading && 
        target.scrollTop + target.clientHeight >= target.scrollHeight - 100) {
      emit('loadMore')
    }
  }, 16) // 60fps
}

// 处理事件点击
const handleEventClick = (event: TimelineEvent) => {
  emit('eventClick', event)
}

// 切换事件展开状态
const toggleEventExpansion = (event: TimelineEvent) => {
  event.expanded = !event.expanded
  
  // 如果展开且需要懒加载详细信息
  if (event.expanded && props.enableLazyLoading && !event.cached) {
    loadEventDetails(event)
  }
}

// 懒加载事件详细信息
const loadEventDetails = async (event: TimelineEvent) => {
  if (event.loading) return
  
  event.loading = true
  
  try {
    // 模拟API调用加载详细信息
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 标记为已缓存
    event.cached = true
    event.loading = false
    
  } catch (error) {
    event.error = true
    event.loading = false
    ElMessage.error('加载事件详情失败')
  }
}

// 格式化事件时间
const formatEventTime = (timeStr: string): string => {
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return timeStr
  }
}

// 获取事件类型样式类
const getEventTypeClass = (eventType: string): string => {
  const typeClassMap: Record<string, string> = {
    'political': 'dot-political',
    'economic': 'dot-economic',
    'social': 'dot-social',
    'technology': 'dot-technology',
    'natural': 'dot-natural',
    'default': 'dot-default'
  }
  return typeClassMap[eventType] || 'dot-default'
}

// 截断文本
const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 监控内存使用情况
const monitorMemoryUsage = () => {
  if ('memory' in performance) {
    const memory = (performance as any).memory
    performanceStats.memoryUsage = Math.round(memory.usedJSHeapSize / 1024 / 1024)
  }
}

// 组件挂载
onMounted(() => {
  // 启动性能监控
  if (props.showPerformanceStats) {
    const memoryTimer = setInterval(monitorMemoryUsage, 1000)
    
    onUnmounted(() => {
      clearInterval(memoryTimer)
    })
  }
  
  // 初始化滚动位置
  nextTick(() => {
    updateVisibleItems()
  })
})

// 组件卸载
onUnmounted(() => {
  if (scrollTimer) {
    clearTimeout(scrollTimer)
  }
  if (frameId) {
    cancelAnimationFrame(frameId)
  }
})

// 暴露方法给父组件
defineExpose({
  scrollToTop: () => {
    if (viewportRef.value) {
      viewportRef.value.scrollTop = 0
    }
  },
  scrollToEvent: (eventId: string) => {
    const index = processedEvents.value.findIndex(event => event.id === eventId)
    if (index !== -1 && viewportRef.value) {
      viewportRef.value.scrollTop = index * props.itemHeight
    }
  },
  getPerformanceStats: () => ({ ...performanceStats }),
  refreshEvents: () => {
    processEvents(props.events)
  }
})
</script>

<style scoped>
.optimized-timeline-container {
  position: relative;
  width: 100%;
  height: 100%;
  background: #f8f9fa;
}

.timeline-viewport {
  position: relative;
  width: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  scroll-behavior: smooth;
}

.timeline-indicator {
  position: absolute;
  left: 50px;
  top: 0;
  width: 2px;
  z-index: 1;
}

.timeline-line {
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, #e1e8ed 0%, #409eff 50%, #e1e8ed 100%);
  border-radius: 1px;
}

.timeline-events {
  position: relative;
  padding-left: 80px;
  padding-right: 20px;
}

.timeline-event-item {
  position: absolute;
  width: calc(100% - 100px);
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.timeline-event-item.event-loading {
  opacity: 0.6;
}

.timeline-event-item.event-error {
  opacity: 0.5;
  filter: grayscale(50%);
}

.timeline-event-item.event-cached {
  border-left: 3px solid #67c23a;
}

.event-time-point {
  position: absolute;
  left: -30px;
  top: 10px;
  z-index: 2;
}

.time-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.dot-political { background: #f56c6c; }
.dot-economic { background: #e6a23c; }
.dot-social { background: #409eff; }
.dot-technology { background: #67c23a; }
.dot-natural { background: #909399; }
.dot-default { background: #409eff; }

.event-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  overflow: hidden;
}

.event-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-expanded {
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.event-time {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.event-actions {
  display: flex;
  gap: 8px;
}

.card-content {
  padding: 16px;
}

.event-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.event-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.geographic-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 12px;
  color: #909399;
}

.location-text {
  font-weight: 500;
}

.coordinates-text {
  color: #c0c4cc;
}

.event-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.event-tag {
  font-size: 11px;
}

.extended-info {
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  margin-top: 12px;
}

.info-row {
  display: flex;
  margin-bottom: 6px;
  font-size: 12px;
}

.info-label {
  width: 60px;
  color: #909399;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
}

.loading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.performance-stats {
  position: fixed;
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 12px;
  border-radius: 6px;
  font-size: 12px;
  z-index: 1000;
  min-width: 200px;
}

.stats-header {
  font-weight: 600;
  margin-bottom: 8px;
  text-align: center;
  border-bottom: 1px solid #444;
  padding-bottom: 4px;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
}

.stat-label {
  color: #ccc;
}

.stat-value {
  color: #67c23a;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .timeline-events {
    padding-left: 60px;
    padding-right: 10px;
  }
  
  .timeline-event-item {
    width: calc(100% - 70px);
  }
  
  .event-time-point {
    left: -25px;
  }
  
  .time-dot {
    width: 10px;
    height: 10px;
  }
  
  .card-content {
    padding: 12px;
  }
  
  .event-title {
    font-size: 14px;
  }
  
  .event-description {
    font-size: 13px;
  }
}

/* 滚动条样式 */
.timeline-viewport::-webkit-scrollbar {
  width: 6px;
}

.timeline-viewport::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.timeline-viewport::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.timeline-viewport::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>