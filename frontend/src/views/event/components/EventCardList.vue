<template>
  <div class="event-card-list">
    <div v-if="events.length === 0" class="empty-state">
      <el-empty description="暂无事件数据">
        <el-button type="primary" @click="$emit('add')">
          添加第一个事件
        </el-button>
      </el-empty>
    </div>
    
    <VirtualScroller
      v-else
      :items="events"
      :height="containerHeight"
      :item-height="cardHeight"
      :buffer="3"
    >
      <template #default="{ item: event, index }">
        <div 
          class="event-card"
          :class="{ 'card-visible': true }"
          @click="$emit('view', event)"
        >
          <div class="event-card-header">
            <div class="event-type">
              <el-tag :type="getEventTypeColor(event.eventType)">
                {{ event.eventType }}
              </el-tag>
            </div>
            <div class="event-time">
              <el-icon><Clock /></el-icon>
              {{ formatDate(event.eventTime) }}
            </div>
          </div>
          <div class="event-card-content">
            <div class="event-description">{{ event.eventDescription }}</div>
            <div class="event-participants">
              <span class="participant">
                <el-icon><User /></el-icon>
                {{ event.subject }}
              </span>
              <span class="participant">
                <el-icon><UserFilled /></el-icon>
                {{ event.object }}
              </span>
            </div>
            <div class="event-location">
              <el-icon><Location /></el-icon>
              {{ event.eventLocation }}
            </div>
          </div>
          <div class="event-card-footer">
            <el-tag :type="event.sourceType === 1 ? 'success' : 'info'" size="small">
              {{ event.sourceType === 1 ? 'DeepSeek' : '人工' }}
            </el-tag>
            <div class="card-actions">
              <el-dropdown @command="(command) => handleCommand(command, event)" trigger="click">
                <el-button type="primary" size="small" @click.stop>
                  操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="view">
                      <el-icon><View /></el-icon>详情
                    </el-dropdown-item>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon>编辑
                    </el-dropdown-item>
                    <el-dropdown-item divided command="delete">
                      <el-icon><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </template>
    </VirtualScroller>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useComponentLifecycle } from '@/utils/componentLifecycle'
import { throttle } from '@/utils/throttleDebounce'
import VirtualScroller from '@/components/common/VirtualScroller.vue'
import { Clock, User, UserFilled, Location, View, Edit, Delete, ArrowDown } from '@element-plus/icons-vue'

// 组件生命周期管理
const { registerEventListener } = useComponentLifecycle('EventCardList')

// 组件属性
const props = defineProps({
  events: {
    type: Array,
    required: true,
    default: () => []
  }
})

// 组件事件
const emit = defineEmits(['view', 'edit', 'delete', 'add'])

// 容器高度
const containerHeight = ref(500)
// 卡片高度
const cardHeight = ref(220)

// 计算容器高度
const updateContainerHeight = () => {
  const windowHeight = window.innerHeight
  // 减去其他UI元素的高度（头部、搜索区域、分页等）
  containerHeight.value = windowHeight - 350
}

/**
 * 获取事件类型颜色
 */
const getEventTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    '袭击': 'danger',
    '冲突': 'warning',
    '谈判': 'success',
    '抗议': 'info',
    '制裁': 'warning'
  }
  return colors[type] || 'info'
}

/**
 * 格式化日期
 */
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 处理操作命令
const handleCommand = (command: string, event: any) => {
  switch (command) {
    case 'view':
      emit('view', event)
      break
    case 'edit':
      emit('edit', event)
      break
    case 'delete':
      emit('delete', event)
      break
  }
}

// 节流处理的窗口大小变化处理函数
const handleResize = throttle(() => {
  updateContainerHeight()
}, 200)

onMounted(() => {
  updateContainerHeight()
  registerEventListener(window, 'resize', handleResize)
})
</script>

<style scoped>
.event-card-list {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.event-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 10px;
  opacity: 0;
  transform: translateY(10px);
  animation: fadeIn 0.3s forwards;
}

.event-card.card-visible {
  opacity: 1;
  transform: translateY(0);
}

.event-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.event-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.event-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.event-card-content {
  padding: 8px 0;
}

.event-description {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
  /* 添加多行文本截断 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-participants {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.participant {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.event-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

.card-actions {
  display: flex;
  gap: 8px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式布局调整 */
@media (max-width: 768px) {
  .event-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .event-time {
    align-self: flex-end;
  }
}

/* 小屏幕设备的额外调整 */
@media (max-width: 576px) {
  .event-participants {
    flex-direction: column;
    gap: 8px;
  }
}
</style>