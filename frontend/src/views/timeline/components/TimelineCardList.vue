<template>
  <div class="timeline-card-list">
    <div v-if="timelines.length === 0" class="empty-state">
      <el-empty description="暂无时间线数据">
        <el-button type="primary" @click="$emit('create')">
          创建第一个时间线
        </el-button>
      </el-empty>
    </div>

    <VirtualScroller v-else :items="timelines" :height="containerHeight" :item-height="cardHeight" :buffer="3">
      <template #default="{ item, index }">
        <div class="timeline-card" :class="{ 'card-visible': true }" @click="$emit('view', item as TimelineItem)">
          <div class="card-header">
            <h3>{{ (item as TimelineItem).title }}</h3>
            <div class="card-actions">
              <el-tag :type="getTimelineStatusType((item as TimelineItem).status)">
                {{ getTimelineStatusText((item as TimelineItem).status) }}
              </el-tag>
              <el-dropdown trigger="click">
                <el-button text icon="MoreFilled" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.stop="$emit('edit', item as TimelineItem)">
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="$emit('duplicate', item as TimelineItem)">
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item @click.stop="$emit('delete', item as TimelineItem)">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="card-content">
            <div class="timeline-stats">
              <div class="stat-item">
                <span class="stat-label">事件数量</span>
                <span class="stat-value">{{ (item as TimelineItem).eventCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">关系数量</span>
                <span class="stat-value">{{ (item as TimelineItem).relationCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">时间跨度</span>
                <span class="stat-value">{{ formatTimeSpan((item as TimelineItem).timeSpan) }}</span>
              </div>
            </div>

            <div class="timeline-preview">
              <div class="timeline-line">
                <div v-for="(node, nodeIndex) in getPreviewNodes(item as TimelineItem)" :key="nodeIndex"
                  class="timeline-node" :class="getNodeTypeClass(node.nodeType)">
                  <div class="node-dot"></div>
                  <div class="node-label">{{ getNodeTitle(node) }}</div>
                </div>
                <div v-if="hasMoreNodes(item as TimelineItem)" class="more-nodes">
                  +{{ getMoreNodesCount(item as TimelineItem) }}
                </div>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <span class="create-time">
              创建时间: {{ formatDate((item as TimelineItem).createdAt) }}
            </span>
            <span class="update-time">
              更新时间: {{ formatDate((item as TimelineItem).updatedAt) }}
            </span>
          </div>
        </div>
      </template>
    </VirtualScroller>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useComponentLifecycle } from '@/utils/componentLifecycle'
import { throttle } from '@/utils/throttleDebounce'
import VirtualScroller from '@/components/common/VirtualScroller.vue'

// 组件生命周期管理
const { registerEventListener } = useComponentLifecycle('TimelineCardList')

// 定义节点类型接口
interface TimelineNode {
  event?: {
    title: string
  };
  nodeType?: string;
}

// 定义时间线类型接口
interface TimelineItem {
  id: string;
  title: string;
  status: string;
  eventCount?: number;
  relationCount?: number;
  timeSpan?: string;
  createdAt: string;
  updatedAt: string;
  nodes?: TimelineNode[];
}

// 组件属性
const props = withDefaults(defineProps<{
  timelines: TimelineItem[]
}>(), {
  timelines: () => []
})

// 组件事件
const emit = defineEmits(['view', 'edit', 'duplicate', 'delete', 'create'])

// 容器高度
const containerHeight = ref(500)
// 卡片高度
const cardHeight = ref(280)

// 计算容器高度
const updateContainerHeight = () => {
  const windowHeight = window.innerHeight
  // 减去其他UI元素的高度（头部、搜索区域、分页等）
  containerHeight.value = windowHeight - 300
}

// 获取预览节点
const getPreviewNodes = (timeline: TimelineItem) => {
  if (!timeline.nodes || !Array.isArray(timeline.nodes)) {
    return []
  }
  return timeline.nodes.slice(0, 5)
}

// 检查是否有更多节点
const hasMoreNodes = (timeline: TimelineItem) => {
  return timeline.nodes && Array.isArray(timeline.nodes) && timeline.nodes.length > 5
}

// 获取更多节点数量
const getMoreNodesCount = (timeline: TimelineItem) => {
  if (!timeline.nodes || !Array.isArray(timeline.nodes)) {
    return 0
  }
  return timeline.nodes.length - 5
}

// 获取节点标题
const getNodeTitle = (node: TimelineNode) => {
  if (!node || !node.event || !node.event.title) {
    return '未命名'
  }
  const title = node.event.title
  return title.length > 10 ? `${title.substring(0, 10)}...` : title
}

/**
 * 获取时间线状态类型
 */
const getTimelineStatusType = (status: string) => {
  const typeMap: { [key: string]: string } = {
    'COMPLETED': 'success',
    'PROCESSING': 'warning',
    'FAILED': 'danger',
    'DRAFT': 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取时间线状态文本
 */
const getTimelineStatusText = (status: string) => {
  const textMap: { [key: string]: string } = {
    'COMPLETED': '已完成',
    'PROCESSING': '处理中',
    'FAILED': '失败',
    'DRAFT': '草稿'
  }
  return textMap[status] || '未知'
}

/**
 * 获取节点类型样式
 */
const getNodeTypeClass = (nodeType: string | undefined) => {
  return `node-type-${nodeType?.toLowerCase() || 'normal'}`
}

/**
 * 格式化时间跨度
 */
const formatTimeSpan = (timeSpan: string | undefined) => {
  if (!timeSpan) return '-'
  // 解析时间跨度，这里简化处理
  return timeSpan.replace('PT', '').replace('H', '小时').replace('M', '分钟')
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
.timeline-card-list {
  width: 100%;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.timeline-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 10px;
  opacity: 0;
  transform: translateY(10px);
  animation: fadeIn 0.3s forwards;
}

.timeline-card.card-visible {
  opacity: 1;
  transform: translateY(0);
}

.timeline-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  flex: 1;
  margin-right: 12px;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.timeline-line {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  position: relative;
}

.timeline-line::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: #e4e7ed;
  z-index: 0;
}

.timeline-node {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.node-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #409eff;
  border: 2px solid white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.node-label {
  font-size: 10px;
  color: #606266;
  white-space: nowrap;
  max-width: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-type-source .node-dot {
  background: #67c23a;
}

.node-type-hub .node-dot {
  background: #e6a23c;
}

.node-type-terminal .node-dot {
  background: #f56c6c;
}

.node-type-hot .node-dot {
  background: #ff4757;
  animation: pulse 2s infinite;
}

.more-nodes {
  color: #909399;
  font-size: 12px;
  background: #f0f2f5;
  padding: 4px 8px;
  border-radius: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
  font-size: 12px;
  color: #909399;
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

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>