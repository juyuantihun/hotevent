<template>
  <div class="timeline-detail-view" v-loading="loading">
    <!-- 高级导出对话框 -->
    <el-dialog v-model="showExportDialog" title="高级导出选项" width="500px" :close-on-click-modal="false">
      <el-form :model="exportOptions" label-width="120px">
        <el-form-item label="导出格式">
          <el-select v-model="exportOptions.format" style="width: 100%">
            <el-option label="JSON" value="json" />
            <el-option label="CSV" value="csv" />
            <el-option label="PDF" value="pdf" />
          </el-select>
        </el-form-item>

        <el-form-item label="包含详细信息">
          <el-switch v-model="exportOptions.includeDetails" />
        </el-form-item>

        <el-form-item label="异步导出">
          <el-switch v-model="exportOptions.isAsync" />
          <div class="form-tip">
            大型时间线建议使用异步导出，避免浏览器超时
          </div>
        </el-form-item>

        <div v-if="exportLoading" class="export-progress">
          <el-progress :percentage="exportProgress" :status="exportStatus === 'FAILED' ? 'exception' : undefined" />
          <div class="progress-status">{{ exportStatus }}</div>
        </div>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showExportDialog = false">取消</el-button>
          <el-button v-if="exportLoading" type="danger" @click="cancelExportTask">
            取消导出
          </el-button>
          <el-button v-else type="primary"
            @click="exportOptions.isAsync ? exportTimelineAsync() : exportTimeline(exportOptions.format)"
            :loading="exportLoading">
            开始导出
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 头部信息 -->
    <div v-if="currentTimeline" class="detail-header">
      <div class="header-info">
        <h2>{{ currentTimeline.title || currentTimeline.name }}</h2>
        <div class="timeline-meta">
          <el-tag :type="getStatusType(currentTimeline.status)" size="small">
            {{ getStatusText(currentTimeline.status) }}
          </el-tag>
          <span class="meta-item">
            <el-icon>
              <Calendar />
            </el-icon>
            {{ formatDate(currentTimeline.createTime || currentTimeline.createdAt || currentTimeline.created_at) }}
          </span>
          <span class="meta-item">
            <el-icon>
              <Document />
            </el-icon>
            {{ currentTimeline.eventCount || currentTimeline.event_count || currentTimeline.nodes?.length || 0 }} 个事件
          </span>
          <span class="meta-item">
            <el-icon>
              <Connection />
            </el-icon>
            {{ currentTimeline.relationCount || currentTimeline.relation_count || currentTimeline.relationships?.length
              || 0
            }} 个关系
          </span>
        </div>
        <div class="timeline-description" v-if="currentTimeline.description">
          {{ currentTimeline.description }}
        </div>
        <div class="timeline-time-range" v-if="currentTimeline.startTime || currentTimeline.start_time">
          <span>时间范围: </span>
          <span>{{ formatDateTime(currentTimeline.startTime || currentTimeline.start_time) }}</span>
          <span> 至 </span>
          <span>{{ formatDateTime(currentTimeline.endTime || currentTimeline.end_time) }}</span>
        </div>
      </div>

      <div class="header-actions">
        <el-button-group>
          <el-button :type="viewMode === 'timeline' ? 'primary' : 'default'" @click="viewMode = 'timeline'"
            size="small">
            <el-icon>
              <Clock />
            </el-icon>
            时间线视图
          </el-button>
          <!-- 关系图视图按钮已隐藏 -->
          <!-- <el-button :type="viewMode === 'graph' ? 'primary' : 'default'" @click="viewMode = 'graph'" size="small">
            <el-icon>
              <Share />
            </el-icon>
            关系图视图
          </el-button> -->
          <el-button :type="viewMode === 'table' ? 'primary' : 'default'" @click="viewMode = 'table'" size="small">
            <el-icon>
              <List />
            </el-icon>
            列表视图
          </el-button>
        </el-button-group>

        <el-dropdown trigger="click">
          <el-button type="success" size="small">
            <el-icon>
              <Download />
            </el-icon>
            导出
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="exportTimeline('json')">导出为JSON</el-dropdown-item>
              <el-dropdown-item @click="exportTimeline('csv')">导出为CSV</el-dropdown-item>
              <el-dropdown-item @click="exportTimeline('pdf')">导出为PDF</el-dropdown-item>
              <el-dropdown-item divided @click="showExportDialog = true">高级导出选项</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button @click="$emit('close')" size="small">
          <el-icon>
            <Close />
          </el-icon>
          关闭
        </el-button>
      </div>
    </div>

    <!-- 无数据提示 -->
    <div v-if="!currentTimeline && !loading" class="no-data">
      <el-empty description="暂无时间线数据">
        <el-button type="primary" @click="$emit('close')">返回列表</el-button>
      </el-empty>
    </div>

    <!-- 主要内容区域 -->
    <div v-if="currentTimeline" class="detail-content">
      <!-- 时间线视图 -->
      <div v-if="viewMode === 'timeline'" class="timeline-view">
        <div class="timeline-container">
          <div class="timeline-header-controls">
            <el-slider v-model="timelineZoom" :min="0.5" :max="5" :step="0.1" :show-tooltip="false"
              style="width: 200px;" @change="handleZoomChange" />
            <span class="zoom-label">缩放: {{ Math.round(timelineZoom * 100) }}%</span>
            <span class="zoom-detail" v-if="timelineZoom >= 2">（分钟级）</span>
            <span class="zoom-detail" v-else-if="timelineZoom >= 1.5">（小时级）</span>
            <span class="zoom-detail" v-else>（日期级）</span>
          </div>

          <div class="timeline-scroll" ref="timelineScrollRef">
            <div class="timeline-track vertical" :style="{ height: `${800 * timelineZoom}px` }">
              <!-- 简洁的垂直时间轴（光棍样式） -->
              <div class="time-axis-vertical">
                <!-- 隐藏中间时间线上的时间标签，只保留简洁的线条 -->
                <div class="timeline-spine"></div>
              </div>

              <!-- 事件节点 -->
              <div class="events-track-vertical">
                <div v-for="(event, index) in timelineEvents" :key="event.id || index" class="timeline-item" :class="[
                  `node-type-${event.nodeType?.toLowerCase() || 'normal'}`,
                  { active: selectedEventId === event.id },
                  { 'timeline-item-right': event.side === 'right' },
                  { 'timeline-item-left': event.side === 'left' },
                  { 'draggable': true },
                  { 'dragging': isDragging && draggedEvent === event }
                ]" :style="{ top: event.position + '%' }" @mousedown.stop="startDrag(event, $event)">

                  <div class="timeline-item-content" @click.stop="selectEvent(event)">
                    <div class="timeline-date">{{ formatDate(event.eventTime) }}</div>
                    <h3 class="timeline-title">{{ event.title }}</h3>
                    <div class="timeline-time">{{ formatTime(event.eventTime) }}</div>
                    <div class="timeline-location" v-if="event.location">
                      <el-icon>
                        <Location />
                      </el-icon>
                      {{ event.location }}
                    </div>
                    <!-- 地理坐标信息显示 -->
                    <div class="timeline-coordinates"
                      v-if="event.coordinates || event.subjectCoordinate || event.objectCoordinate">
                      <div v-if="event.coordinates" class="coordinate-item">
                        <el-icon>
                          <Position />
                        </el-icon>
                        <span class="coordinate-label">坐标:</span>
                        <span class="coordinate-value">{{ formatCoordinates(event.coordinates) }}</span>
                      </div>
                      <div v-if="event.subjectCoordinate" class="coordinate-item">
                        <el-icon>
                          <User />
                        </el-icon>
                        <span class="coordinate-label">主体:</span>
                        <span class="coordinate-value">{{ formatCoordinates(event.subjectCoordinate) }}</span>
                      </div>
                      <div v-if="event.objectCoordinate" class="coordinate-item">
                        <el-icon>
                          <Flag />
                        </el-icon>
                        <span class="coordinate-label">客体:</span>
                        <span class="coordinate-value">{{ formatCoordinates(event.objectCoordinate) }}</span>
                      </div>
                    </div>
                    <div class="timeline-badge" :class="`badge-${event.nodeType?.toLowerCase() || 'normal'}`">
                      <el-icon v-if="event.nodeType === 'hot'">
                        <Star />
                      </el-icon>
                      <el-icon v-else-if="event.nodeType === 'source'">
                        <TopRight />
                      </el-icon>
                      <el-icon v-else-if="event.nodeType === 'terminal'">
                        <BottomRight />
                      </el-icon>
                      <el-icon v-else-if="event.nodeType === 'hub'">
                        <Connection />
                      </el-icon>
                      <el-icon v-else>
                        <InfoFilled />
                      </el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 事件详情侧边栏 -->
        <div v-if="selectedEvent" class="event-sidebar">
          <div class="sidebar-header">
            <h4>事件详情</h4>
            <el-button @click="selectedEvent = null" text size="small">
              <el-icon>
                <Close />
              </el-icon>
            </el-button>
          </div>

          <div class="sidebar-content">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="标题">
                {{ selectedEvent.title }}
              </el-descriptions-item>
              <el-descriptions-item label="时间">
                {{ formatDateTime(selectedEvent.eventTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="地点" v-if="selectedEvent.location">
                {{ selectedEvent.location }}
              </el-descriptions-item>
              <el-descriptions-item label="类型" v-if="selectedEvent.nodeType">
                <el-tag size="small" :type="getNodeTypeTagType(selectedEvent.nodeType)">
                  {{ getNodeTypeText(selectedEvent.nodeType) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="重要性" v-if="selectedEvent.importanceScore !== undefined">
                <el-rate v-model="selectedEvent.importanceScore" :max="1" :step="0.01" :show-score="true"
                  :score-template="(selectedEvent.importanceScore * 100).toFixed(0) + '%'" disabled />
              </el-descriptions-item>
              <el-descriptions-item label="描述" v-if="selectedEvent.description || selectedEvent.event?.description">
                {{ selectedEvent.description || selectedEvent.event?.description }}
              </el-descriptions-item>
            </el-descriptions>

            <!-- 相关事件 -->
            <div class="related-events" v-if="getRelatedEvents(selectedEvent.id).length > 0">
              <h5>相关事件</h5>
              <el-table :data="getRelatedEvents(selectedEvent.id)" size="small" style="width: 100%">
                <el-table-column label="关系" width="80">
                  <template #default="{ row }">
                    <el-tag size="small">{{ getRelationTypeText(row.type) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="事件" min-width="120">
                  <template #default="{ row }">
                    <span class="related-event-title" @click="selectEventById(row.targetId)">
                      {{ getEventTitleById(row.targetId) }}
                    </span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>

      <!-- 关系图视图 -->
      <div v-else-if="viewMode === 'graph'" class="graph-view">
        <div class="graph-container">
          <div class="graph-controls">
            <el-button-group>
              <el-button size="small" @click="zoomIn">
                <el-icon>
                  <ZoomIn />
                </el-icon>
              </el-button>
              <el-button size="small" @click="zoomOut">
                <el-icon>
                  <ZoomOut />
                </el-icon>
              </el-button>
              <el-button size="small" @click="resetZoom">
                <el-icon>
                  <Refresh />
                </el-icon>
              </el-button>
            </el-button-group>

            <el-select v-model="graphLayout" placeholder="布局" size="small" style="width: 120px; margin-left: 10px;">
              <el-option label="力导向图" value="force" />
              <el-option label="环形布局" value="circular" />
              <el-option label="层次布局" value="hierarchical" />
            </el-select>

            <el-switch v-model="showNodeLabels" active-text="显示标签" inactive-text="" style="margin-left: 10px;" />
          </div>

          <div class="graph-canvas" ref="graphContainer">
            <div v-if="!graphInitialized" class="graph-loading">
              <el-skeleton :rows="10" animated />
              <div class="loading-text">正在加载关系图...</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else-if="viewMode === 'table'" class="table-view">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="事件列表" name="events">
            <!-- 事件列表过滤器 -->
            <div class="filter-container">
              <el-input v-model="eventsFilter.keyword" placeholder="搜索事件标题或地点" clearable @input="filterEvents"
                style="width: 220px; margin-right: 10px;">
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
              </el-input>

              <el-select v-model="eventsFilter.nodeType" placeholder="事件类型" clearable @change="filterEvents"
                style="width: 140px; margin-right: 10px;">
                <el-option label="源事件" value="source" />
                <el-option label="终端事件" value="terminal" />
                <el-option label="枢纽事件" value="hub" />
                <el-option label="热点事件" value="hot" />
                <el-option label="普通事件" value="normal" />
              </el-select>

              <el-button @click="resetEventsFilter">
                重置过滤器
              </el-button>
            </div>

            <el-table :data="filteredEvents" style="width: 100%"
              :default-sort="{ prop: 'eventTime', order: 'ascending' }" @sort-change="handleEventsSortChange">
              <el-table-column prop="title" label="事件标题" min-width="200" sortable="custom" />
              <el-table-column prop="eventTime" label="时间" width="180" sortable="custom">
                <template #default="{ row }">
                  {{ formatDateTime(row.eventTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="location" label="地点" width="150" sortable="custom" />
              <el-table-column prop="nodeType" label="类型" width="120" sortable="custom">
                <template #default="{ row }">
                  <el-tag size="small" :type="getNodeTypeTagType(row.nodeType)">
                    {{ getNodeTypeText(row.nodeType) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="importanceScore" label="重要性" width="120" sortable="custom">
                <template #default="{ row }">
                  <el-progress v-if="row.importanceScore !== undefined"
                    :percentage="Math.round(row.importanceScore * 100)"
                    :color="getImportanceColor(row.importanceScore)" />
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button @click="selectEvent(row)" text size="small">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container" v-if="filteredEventsAll.length > eventsPagination.size">
              <el-pagination v-model:current-page="eventsPagination.page" v-model:page-size="eventsPagination.size"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
                :total="filteredEventsAll.length" @size-change="handleEventsSizeChange"
                @current-change="handleEventsCurrentChange" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="关系列表" name="relations">
            <!-- 关系列表过滤器 -->
            <div class="filter-container">
              <el-input v-model="relationsFilter.keyword" placeholder="搜索关系描述" clearable @input="filterRelations"
                style="width: 220px; margin-right: 10px;">
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
              </el-input>

              <el-select v-model="relationsFilter.type" placeholder="关系类型" clearable @change="filterRelations"
                style="width: 140px; margin-right: 10px;">
                <el-option label="导致" value="cause" />
                <el-option label="触发" value="trigger" />
                <el-option label="引发" value="lead_to" />
                <el-option label="促成" value="enable" />
                <el-option label="相关" value="related" />
                <el-option label="后续" value="follow_up" />
              </el-select>

              <el-button @click="resetRelationsFilter">
                重置过滤器
              </el-button>
            </div>

            <el-table :data="filteredRelations" style="width: 100%" @sort-change="handleRelationsSortChange">
              <el-table-column label="源事件" min-width="180" sortable="custom" prop="sourceId">
                <template #default="{ row }">
                  <span class="clickable-cell" @click="selectEventById(row.sourceId)">
                    {{ getEventTitleById(row.sourceId) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="关系类型" width="120" sortable="custom" prop="type">
                <template #default="{ row }">
                  <el-tag size="small">{{ getRelationTypeText(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="目标事件" min-width="180" sortable="custom" prop="targetId">
                <template #default="{ row }">
                  <span class="clickable-cell" @click="selectEventById(row.targetId)">
                    {{ getEventTitleById(row.targetId) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip sortable="custom" />
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container" v-if="filteredRelationsAll.length > relationsPagination.size">
              <el-pagination v-model:current-page="relationsPagination.page"
                v-model:page-size="relationsPagination.size" :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next" :total="filteredRelationsAll.length"
                @size-change="handleRelationsSizeChange" @current-change="handleRelationsCurrentChange" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, watch, reactive, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import {
  Calendar, Document, Connection, Clock, Share, List, Close, Location,
  ZoomIn, ZoomOut, Refresh, Download, MoreFilled, Search, Position, User, Flag,
  Star, TopRight, BottomRight, InfoFilled
} from '@element-plus/icons-vue'
import { timelineApi } from '@/api/timeline'
import { ElMessageBox } from 'element-plus'

// 类型定义
interface TimelineEvent {
  id: string
  title: string
  eventTime?: string
  location?: string
  nodeType?: string
  importanceScore?: number
  description?: string
  event?: {
    title?: string
    eventTime?: string
    location?: string
    description?: string
  }
  position?: number
  side?: 'left' | 'right'
}

interface TimelineRelation {
  id: string
  sourceId: string
  targetId: string
  type: string
  description?: string
}

interface TimelineNode {
  id: string
  event?: {
    title?: string
    eventTime?: string
    location?: string
    description?: string
  }
  eventTime?: string
  location?: string
  nodeType?: string
  importanceScore?: number
}

interface TimelineDetail {
  id: string
  title: string
  status: string
  eventCount?: number
  relationCount?: number
  createTime: string
  updateTime?: string
  nodes?: TimelineNode[]
  relationships?: TimelineRelation[]
}

// 定义 props - 现在可选，因为可以通过路由参数获取
interface Props {
  timeline?: any
}

const props = defineProps<Props>()

// 定义 emits
defineEmits<{
  close: []
}>()

// 路由
const route = useRoute()

// 响应式数据
const viewMode = ref('timeline')
const activeTab = ref('events')
const loading = ref(false)
const timeline = ref<any>(null)
const selectedEvent = ref<any>(null)
const selectedEventId = ref<string | null>(null)
const timelineZoom = ref(1)
const showExportDialog = ref(false)
const exportLoading = ref(false)
const exportOptions = reactive({
  format: 'json',
  includeDetails: true,
  isAsync: false
})
const exportTaskId = ref('')
const exportProgress = ref(0)
const exportStatus = ref('')

// 拖拽相关状态
const isDragging = ref(false)
const draggedEvent = ref<any>(null)
const dragStartY = ref(0)
const dragStartPosition = ref(0)
const timelineScrollRef = ref<HTMLElement | null>(null)

// 计算属性
const currentTimeline = computed(() => {
  console.log('计算currentTimeline:', props.timeline, timeline.value)
  const result = props.timeline || timeline.value
  console.log('currentTimeline结果:', result)
  return result
})

// 时间线关系计算属性
const timelineRelations = computed(() => {
  return currentTimeline.value?.relationships || []
})

// 获取原始事件数据
const getOriginalEvents = () => {
  const nodes = currentTimeline.value?.nodes || []
  console.log('获取原始事件数据，节点数量:', nodes.length)
  return nodes.map((node: any, index: number) => {
    console.log(`处理节点 ${index + 1}:`, node)
    const event = {
      id: node.id || `event-${index}`,
      title: node.event_description || node.event?.title || node.title || '未知事件',
      eventTime: node.event_time || node.eventTime || node.event?.eventTime,
      location: node.event_location || node.event?.location || node.location,
      nodeType: node.nodeType || 'normal',
      importanceScore: node.intensity_level || node.importanceScore || 0.5
    }
    console.log(`处理后的事件 ${index + 1}:`, event)
    return event
  })
}

// 计算事件在时间线上的位置
const calculateEventPosition = (eventTime: string, index: number, allEvents: any[]) => {
  if (!eventTime) {
    // 如果没有时间，根据索引均匀分布
    return index * (90 / allEvents.length) + 5
  }

  // 过滤出有时间的事件
  const eventsWithTime = allEvents.filter(e => e.eventTime)

  if (eventsWithTime.length <= 1) {
    return 50 // 只有一个事件，放在中间
  }

  // 获取所有时间并排序
  const times = eventsWithTime.map(e => {
    const timeStr = e.eventTime
    return timeStr ? new Date(timeStr).getTime() : 0
  })
    .filter(time => !isNaN(time) && time > 0)
    .sort((a, b) => a - b)

  const currentTime = new Date(eventTime).getTime()

  if (isNaN(currentTime) || times.length === 0) {
    return index * (90 / allEvents.length) + 5
  }

  const minTime = times[0]
  const maxTime = times[times.length - 1]

  if (maxTime === minTime) {
    return 50
  }

  // 计算相对位置（5% 到 95% 的范围内）
  return ((currentTime - minTime) / (maxTime - minTime)) * 90 + 5
}

// 时间线事件计算属性（用于时间线视图）
const timelineEvents = computed(() => {
  const nodes = currentTimeline.value?.nodes
  console.log('计算timelineEvents，节点数据:', nodes)
  if (!nodes || !Array.isArray(nodes)) {
    console.warn('节点数据不是数组或为空')
    return []
  }

  const originalEvents = getOriginalEvents()
  console.log('原始事件数据:', originalEvents)

  // 创建事件数组的副本，以便我们可以在计算垂直位置时引用它
  const events = originalEvents.map((event: any, index: number) => {
    // 计算时间线位置
    const position = calculateEventPosition(event.eventTime, index, originalEvents)
    console.log(`事件 ${index + 1} 位置:`, position)
    return {
      ...event,
      position: position,
      side: index % 2 === 0 ? 'right' : 'left' // 交替分配左右侧
    }
  })

  // 按位置排序事件
  events.sort((a: TimelineEvent, b: TimelineEvent) => {
    const posA = a.position ?? 0;
    const posB = b.position ?? 0;
    return posA - posB;
  });

  // 防止事件气泡重叠
  const minDistance = 15; // 最小垂直距离（百分比）
  const leftEvents = events.filter((e: TimelineEvent) => e.side === 'left');
  const rightEvents = events.filter((e: TimelineEvent) => e.side === 'right');

  // 处理左侧事件的重叠
  adjustOverlappingEvents(leftEvents, minDistance);

  // 处理右侧事件的重叠
  adjustOverlappingEvents(rightEvents, minDistance);

  console.log('调整后的事件数据:', events)
  return events
})

// 所有事件数据（用于列表视图，不包含时间线特定的位置信息）
const allEventsData = computed(() => {
  const nodes = currentTimeline.value?.nodes
  if (!nodes || !Array.isArray(nodes)) {
    return []
  }
  return getOriginalEvents()
})

// 调整重叠的事件
const adjustOverlappingEvents = (events: any[], minDistance: number) => {
  if (events.length <= 1) return;

  for (let i = 1; i < events.length; i++) {
    const currentEvent = events[i];
    const prevEvent = events[i - 1];

    // 检查是否与前一个事件重叠
    if (currentEvent.position - prevEvent.position < minDistance) {
      // 将当前事件向下移动，确保最小距离
      currentEvent.position = prevEvent.position + minDistance;

      // 确保不超出范围
      if (currentEvent.position > 95) {
        // 如果超出范围，尝试压缩所有事件的间距
        compressEventPositions(events, minDistance);
        break;
      }
    }
  }
}

// 压缩事件位置，使所有事件都在可见范围内
const compressEventPositions = (events: any[], minDistance: number) => {
  if (events.length <= 1) return;

  // 计算需要的总空间
  const totalSpaceNeeded = (events.length - 1) * minDistance;

  // 如果总空间超过了可用空间，需要压缩
  if (totalSpaceNeeded > 90) {
    // 计算压缩后的最小距离
    const compressedDistance = 90 / (events.length - 1);

    // 重新分配位置
    events.forEach((event, index) => {
      event.position = 5 + index * compressedDistance;
    });
  } else {
    // 如果空间足够，均匀分布
    events.forEach((event, index) => {
      event.position = 5 + (index * 90 / (events.length - 1));
    });
  }
}

// 时间标记计算属性
const timeMarkers = computed(() => {
  console.log('计算timeMarkers，事件数量:', timelineEvents.value.length)
  if (timelineEvents.value.length === 0) {
    console.warn('没有事件数据，无法计算时间标记')
    return []
  }

  const events = timelineEvents.value.filter((e: TimelineEvent) => e.eventTime)
  console.log('有时间的事件数量:', events.length)
  if (events.length === 0) {
    console.warn('没有带时间的事件，无法计算时间标记')
    return []
  }

  const times = events.map((e: any) => {
    const time = new Date(e.eventTime).getTime()
    console.log(`事件时间 ${e.title}:`, e.eventTime, time)
    return time
  }).sort((a: number, b: number) => a - b)

  console.log('排序后的时间戳:', times)
  const startTime = times[0]
  const endTime = times[times.length - 1]
  console.log('开始时间:', new Date(startTime).toISOString(), '结束时间:', new Date(endTime).toISOString())
  const duration = endTime - startTime

  if (duration === 0) return [{ position: 50, label: formatDateTime(startTime) }]

  // 根据缩放级别和时间跨度确定时间标记的数量和格式
  const markers = []
  let markerCount = 5
  let formatFunc = formatDate

  // 根据缩放级别调整标记数量和格式
  if (timelineZoom.value >= 2) {
    // 高缩放级别 - 显示更多标记，精确到分钟
    markerCount = Math.min(10, Math.max(5, Math.floor(duration / (60 * 60 * 1000)) + 1))
    formatFunc = formatDateTimeMinute
  } else if (timelineZoom.value >= 1.5) {
    // 中等缩放级别 - 显示小时级别
    markerCount = Math.min(8, Math.max(4, Math.floor(duration / (3 * 60 * 60 * 1000)) + 1))
    formatFunc = formatDateTime
  } else {
    // 低缩放级别 - 显示日期级别
    markerCount = Math.min(5, Math.max(2, Math.floor(duration / (24 * 60 * 60 * 1000)) + 1))
    formatFunc = formatDate
  }

  for (let i = 0; i < markerCount; i++) {
    const time = startTime + (duration * i) / (markerCount - 1)
    markers.push({
      position: (i * 100) / (markerCount - 1),
      label: formatFunc(time)
    })
  }

  return markers
})

// 方法

// 格式化坐标信息
const formatCoordinates = (coordinates: any) => {
  if (!coordinates) return ''

  if (typeof coordinates === 'object') {
    const lat = coordinates.latitude || coordinates.lat
    const lng = coordinates.longitude || coordinates.lng || coordinates.lon

    if (lat !== undefined && lng !== undefined) {
      return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`
    }

    // 如果有位置名称，也显示出来
    if (coordinates.locationName) {
      return coordinates.locationName
    }
  }

  return String(coordinates)
}

// 格式化日期（保留在事件卡片上）
const formatDate = (dateStr: string | number) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (e) {
    return ''
  }
}

// 格式化时间（保留在事件卡片上）
const formatTime = (dateStr: string | number) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return ''
  }
}

// 格式化日期时间
const formatDateTime = (dateStr: string | number) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return ''
  }
}

// 格式化日期时间（精确到分钟）
const formatDateTimeMinute = (dateStr: string | number) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return ''
  }
}

const loadTimelineDetail = async (id: string) => {
  console.log('加载时间线详情:', id)
  loading.value = true

  try {
    // 直接发送请求到后端API
    const url = `/api/timelines/${id}`
    console.log('发送请求到:', url)

    const response = await fetch(url)
    console.log('收到响应:', response.status)

    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const responseData = await response.json()
    console.log('解析响应数据:', responseData)

    // 组合数据
    const timelineData: any = {}

    // 处理详情响应
    if (responseData && typeof responseData === 'object') {
      console.log('处理详情响应:', responseData)

      // 检查是否是ApiResponse格式
      if (responseData.code !== undefined) {
        if (responseData.code === 200 && responseData.data) {
          const data = responseData.data
          console.log('ApiResponse格式的data:', data)

          if (data.timeline) {
            // 如果响应包含timeline字段
            console.log('响应包含timeline字段:', data.timeline)

            // 将后端字段名称映射到前端期望的字段名称
            const timeline = data.timeline
            timelineData.id = timeline.id
            timelineData.title = timeline.name
            timelineData.status = timeline.status
            timelineData.eventCount = timeline.event_count
            timelineData.relationCount = timeline.relation_count
            timelineData.createTime = timeline.created_at
            timelineData.updateTime = timeline.updated_at
            timelineData.startTime = timeline.start_time
            timelineData.endTime = timeline.end_time
            timelineData.description = timeline.description

            // 设置regions
            timelineData.regions = data.regions || []

            // 设置空的nodes和relationships数组，因为后端没有返回这些数据
            timelineData.nodes = []
            timelineData.relationships = []
          } else {
            // 如果响应直接就是时间线数据
            console.log('响应直接就是时间线数据:', data)
            Object.assign(timelineData, data)
          }
        } else {
          throw new Error(responseData.message || responseData.msg || '获取时间线详情失败')
        }
      } else {
        // 如果不是ApiResponse格式，直接使用响应数据
        console.log('不是ApiResponse格式:', responseData)

        if (responseData.timeline) {
          // 如果响应包含timeline字段
          console.log('响应包含timeline字段:', responseData.timeline)

          // 将后端字段名称映射到前端期望的字段名称
          const timeline = responseData.timeline
          timelineData.id = timeline.id
          timelineData.title = timeline.name
          timelineData.status = timeline.status
          timelineData.eventCount = timeline.event_count
          timelineData.relationCount = timeline.relation_count
          timelineData.createTime = timeline.created_at
          timelineData.updateTime = timeline.updated_at
          timelineData.startTime = timeline.start_time
          timelineData.endTime = timeline.end_time
          timelineData.description = timeline.description

          // 设置regions
          timelineData.regions = responseData.regions || []

          // 设置空的nodes和relationships数组，因为后端没有返回这些数据
          timelineData.nodes = []
          timelineData.relationships = []
        } else {
          // 如果响应直接就是时间线数据
          console.log('响应直接就是时间线数据:', responseData)
          Object.assign(timelineData, responseData)
        }
      }
    }

    // 获取时间线事件
    try {
      const eventsUrl = `/api/timelines/${id}/events`
      console.log('发送请求到:', eventsUrl)

      const eventsResponse = await fetch(eventsUrl)
      console.log('收到事件响应:', eventsResponse.status)

      if (eventsResponse.ok) {
        const eventsResponseData = await eventsResponse.json()
        console.log('解析事件数据:', eventsResponseData)

        // 处理事件响应
        let eventsData = eventsResponseData

        // 检查是否是ApiResponse格式
        if (eventsResponseData.code !== undefined) {
          if (eventsResponseData.code === 200 && eventsResponseData.data) {
            eventsData = eventsResponseData.data
          } else {
            console.warn('事件API返回错误:', eventsResponseData.msg || eventsResponseData.message)
            eventsData = []
          }
        }

        // 如果数据中有events字段，使用events数组
        if (eventsData && eventsData.events && Array.isArray(eventsData.events)) {
          eventsData = eventsData.events
        }

        if (Array.isArray(eventsData)) {
          console.log('处理事件数据，数量:', eventsData.length)
          timelineData.nodes = eventsData.map((event, index) => {
            console.log(`处理事件 ${index + 1}:`, event)
            return {
              id: event.id || event.event_id || `event-${index}`,
              event_description: event.event_description || event.title || event.name || '未知事件',
              event_time: event.event_time || event.eventTime || event.time,
              event_location: event.event_location || event.location,
              intensity_level: event.intensity_level || event.importanceScore || 0.5,
              event: {
                title: event.event_description || event.title || event.name || '未知事件',
                eventTime: event.event_time || event.eventTime || event.time,
                location: event.event_location || event.location,
                description: event.event_description || event.description
              },
              nodeType: event.node_type || event.nodeType || 'normal',
              importanceScore: event.intensity_level || event.importance_score || event.importanceScore || 0.5
            }
          })
          console.log('处理后的节点数据:', timelineData.nodes)
        } else {
          console.warn('事件数据不是数组:', eventsData)
          timelineData.nodes = []
        }
      } else {
        console.warn('获取时间线事件失败:', eventsResponse.status)
        timelineData.nodes = []
      }
    } catch (eventsError) {
      console.warn('获取时间线事件失败', eventsError)
      timelineData.nodes = []
    }

    // 获取时间线图形数据
    try {
      const graphUrl = `/api/timelines/${id}/graph`
      console.log('发送请求到:', graphUrl)

      const graphResponse = await fetch(graphUrl)
      console.log('收到图形响应:', graphResponse.status)

      if (graphResponse.ok) {
        const graphResponseData = await graphResponse.json()
        console.log('解析图形数据:', graphResponseData)

        // 处理图形数据响应
        let graphData = graphResponseData

        // 检查是否是ApiResponse格式
        if (graphResponseData.code !== undefined) {
          if (graphResponseData.code === 200 && graphResponseData.data) {
            graphData = graphResponseData.data
          } else {
            console.warn('图形API返回错误:', graphResponseData.msg || graphResponseData.message)
            graphData = { nodes: [], links: [] }
          }
        }

        if (graphData && typeof graphData === 'object') {
          if (Array.isArray(graphData.links)) {
            timelineData.relationships = graphData.links.map((link: any) => ({
              id: link.id,
              sourceId: link.source || link.source_id,
              targetId: link.target || link.target_id,
              type: link.type || link.relation_type || 'related',
              description: link.description || ''
            }))
          } else if (Array.isArray(graphData.relationships)) {
            timelineData.relationships = graphData.relationships.map((rel: any) => ({
              id: rel.id,
              sourceId: rel.sourceId || rel.source_id,
              targetId: rel.targetId || rel.target_id,
              type: rel.type || rel.relation_type || 'related',
              description: rel.description || ''
            }))
          } else {
            console.warn('图形数据中没有links或relationships字段:', graphData)
            timelineData.relationships = []
          }

          // 如果graphData中有nodes字段，并且timelineData.nodes为空，则使用graphData.nodes
          if (Array.isArray(graphData.nodes) && timelineData.nodes.length === 0) {
            timelineData.nodes = graphData.nodes.map((node: any) => ({
              id: node.id,
              event: {
                title: node.title || node.name,
                eventTime: node.event_time || node.eventTime || node.time,
                location: node.location,
                description: node.description
              },
              nodeType: node.node_type || node.nodeType || 'normal',
              importanceScore: node.importance_score || node.importanceScore || 0.5
            }))
          }
        }
      } else {
        console.warn('获取时间线图形数据失败:', graphResponse.status)
        timelineData.relationships = []
      }
    } catch (graphError) {
      console.warn('获取时间线图形数据失败', graphError)
      timelineData.relationships = []
    }

    // 检查是否有足够的数据
    console.log('最终的时间线数据:', timelineData)

    // 确保timelineData有必要的字段
    if (!timelineData.id) {
      console.log('设置ID:', id)
      timelineData.id = id
    }

    if (!timelineData.title && timelineData.name) {
      console.log('使用name作为title:', timelineData.name)
      timelineData.title = timelineData.name
    }

    if (!timelineData.id || (!timelineData.title && !timelineData.name)) {
      console.warn('时间线数据不完整，将使用模拟数据')
      const mockData = generateMockTimelineDetail(id)
      console.log('生成的模拟数据:', mockData)
      timeline.value = mockData
    } else {
      // 设置时间线数据
      console.log('设置时间线数据:', timelineData)
      timeline.value = { ...timelineData }
      console.log('设置后的timeline.value:', timeline.value)
    }

  } catch (error) {
    console.error('加载时间线详情失败', error)

    // 使用模拟数据
    console.log('使用模拟数据')
    timeline.value = generateMockTimelineDetail(id)
    let errorMessage = '加载时间线详情失败'

    if (error instanceof Error) {
      // 检查是否是网络错误
      if (error.message.includes('Network Error') || error.message.includes('timeout')) {
        errorMessage = '网络连接失败，请检查网络连接后重试'
      }
      // 检查是否是业务逻辑错误
      else if ((error as any).businessError) {
        errorMessage = error.message
      }
      // 检查是否是权限错误
      else if (error.message.includes('401') || error.message.includes('Unauthorized')) {
        errorMessage = '登录已过期，请重新登录'
        // 可以在这里添加跳转到登录页面的逻辑
      }
      // 检查是否是服务器错误
      else if (error.message.includes('500') || error.message.includes('Internal Server Error')) {
        errorMessage = '服务器内部错误，请稍后重试'
      }
      // 其他错误
      else {
        errorMessage = error.message || '未知错误'
      }
    }

    ElMessage.error(errorMessage)

    // 如果API调用出错，使用模拟数据作为备用，但先询问用户
    ElMessageBox.confirm(
      '无法从服务器获取时间线详情，是否使用模拟数据查看示例？',
      '加载失败',
      {
        confirmButtonText: '使用模拟数据',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      timeline.value = generateMockTimelineDetail(id)
    }).catch(() => {
      // 用户取消，不做任何操作
    })
  } finally {
    loading.value = false
  }
}

const generateMockTimelineDetail = (id: string) => {
  return {
    id,
    title: `2024年中东地区冲突事件链 - 详情`,
    status: 'completed',
    eventCount: 15,
    relationCount: 23,
    createTime: '2024-01-15T10:30:00',
    updateTime: '2024-01-15T14:20:00',
    nodes: [
      {
        id: 'event-1',
        event: {
          title: '以色列空袭加沙',
          eventTime: '2024-01-10T08:00:00',
          location: '加沙地带',
          description: '以色列对加沙地带发动大规模空袭，造成严重人员伤亡和基础设施损毁。'
        },
        nodeType: 'source',
        importanceScore: 0.95
      },
      {
        id: 'event-2',
        event: {
          title: '哈马斯回应袭击',
          eventTime: '2024-01-10T14:30:00',
          location: '以色列南部',
          description: '哈马斯向以色列南部发射火箭弹，以回应以色列的空袭行动。'
        },
        nodeType: 'normal',
        importanceScore: 0.87
      },
      {
        id: 'event-3',
        event: {
          title: '国际社会调解',
          eventTime: '2024-01-11T09:15:00',
          location: '联合国总部',
          description: '联合国安理会召开紧急会议，讨论中东局势，呼吁双方克制。'
        },
        nodeType: 'hub',
        importanceScore: 0.72
      },
      {
        id: 'event-4',
        event: {
          title: '停火协议签署',
          eventTime: '2024-01-12T16:45:00',
          location: '开罗',
          description: '在埃及调解下，冲突双方签署临时停火协议，为期72小时。'
        },
        nodeType: 'terminal',
        importanceScore: 0.89
      },
      {
        id: 'event-5',
        event: {
          title: '人道主义援助',
          eventTime: '2024-01-13T11:20:00',
          location: '加沙地带',
          description: '国际人道主义组织开始向加沙地带运送紧急救援物资。'
        },
        nodeType: 'hot',
        importanceScore: 0.78
      },
      {
        id: 'event-6',
        event: {
          title: '联合国安理会紧急会议',
          eventTime: '2024-01-14T10:00:00',
          location: '纽约',
          description: '联合国安理会再次召开紧急会议，讨论停火协议执行情况。'
        },
        nodeType: 'normal',
        importanceScore: 0.65
      },
      {
        id: 'event-7',
        event: {
          title: '难民营设立',
          eventTime: '2024-01-15T08:30:00',
          location: '约旦边境',
          description: '国际组织在约旦边境设立难民营，接收来自冲突地区的难民。'
        },
        nodeType: 'normal',
        importanceScore: 0.70
      }
    ],
    relationships: [
      {
        id: 'rel-1',
        sourceId: 'event-1',
        targetId: 'event-2',
        type: 'cause',
        description: '以色列的空袭直接导致哈马斯的回应袭击'
      },
      {
        id: 'rel-2',
        sourceId: 'event-2',
        targetId: 'event-3',
        type: 'trigger',
        description: '哈马斯的回应袭击促使国际社会介入调解'
      },
      {
        id: 'rel-3',
        sourceId: 'event-3',
        targetId: 'event-4',
        type: 'lead_to',
        description: '国际社会的调解努力最终促成停火协议的签署'
      },
      {
        id: 'rel-4',
        sourceId: 'event-4',
        targetId: 'event-5',
        type: 'enable',
        description: '停火协议的签署使人道主义援助成为可能'
      },
      {
        id: 'rel-5',
        sourceId: 'event-5',
        targetId: 'event-7',
        type: 'related',
        description: '人道主义援助行动与难民营设立密切相关'
      },
      {
        id: 'rel-6',
        sourceId: 'event-4',
        targetId: 'event-6',
        type: 'follow_up',
        description: '联合国安理会紧急会议是对停火协议的后续跟进'
      }
    ]
  }
}

const getStatusType = (status: string) => {
  const typeMap: { [key: string]: string } = {
    'completed': 'success',
    'processing': 'warning',
    'failed': 'danger',
    'draft': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: { [key: string]: string } = {
    'completed': '已完成',
    'processing': '处理中',
    'failed': '失败',
    'draft': '草稿'
  }
  return textMap[status] || '未知'
}




const selectEvent = (event: TimelineEvent) => {
  console.log('第178行点击的event内容:', event)
  selectedEvent.value = event
  selectedEventId.value = event.id
}

const getNodeTypeTagType = (nodeType: string) => {
  const typeMap: { [key: string]: string } = {
    'source': 'success',
    'terminal': 'danger',
    'hub': 'warning',
    'hot': 'danger',
    'normal': 'info'
  }
  return typeMap[nodeType?.toLowerCase()] || 'info'
}

const getNodeTypeText = (nodeType: string) => {
  const textMap: { [key: string]: string } = {
    'source': '源事件',
    'terminal': '终端事件',
    'hub': '枢纽事件',
    'hot': '热点事件',
    'normal': '普通事件'
  }
  return textMap[nodeType?.toLowerCase()] || '普通事件'
}

// 图形控制方法
const zoomIn = () => {
  timelineZoom.value = Math.min(timelineZoom.value + 0.2, 3)
}

const zoomOut = () => {
  timelineZoom.value = Math.max(timelineZoom.value - 0.2, 0.5)
}

const resetZoom = () => {
  timelineZoom.value = 1
}

const handleZoomChange = (value: number) => {
  console.log('缩放级别变化:', value)
  // 缩放变化时重新计算时间标记
  // timeMarkers 是计算属性，会自动重新计算
}

// 根据ID获取事件标题
const getEventTitleById = (eventId: string) => {
  // 优先从时间线事件中查找（包含位置信息），如果没找到则从所有事件数据中查找
  let event = timelineEvents.value.find((e: TimelineEvent) => e.id === eventId)
  if (!event) {
    event = allEventsData.value.find((e: TimelineEvent) => e.id === eventId)
  }
  return event ? event.title : '未知事件'
}

// 根据ID选择事件
const selectEventById = (eventId: string) => {
  // 优先从时间线事件中查找，如果没找到则从所有事件数据中查找
  let event = timelineEvents.value.find((e: TimelineEvent) => e.id === eventId)
  if (!event) {
    event = allEventsData.value.find((e: TimelineEvent) => e.id === eventId)
  }
  if (event) {
    selectEvent(event)
  }
}

// 拖拽相关方法
const startDrag = (event: any, e: MouseEvent) => {
  console.log('开始拖拽:', event.title);

  // 如果是点击事件的内容区域，不启动拖拽（允许正常选择）
  if ((e.target as HTMLElement).closest('.timeline-item-content')) {
    console.log('点击内容区域，不启动拖拽');
    return;
  }

  e.preventDefault();
  e.stopPropagation();

  isDragging.value = true;
  draggedEvent.value = event;
  dragStartY.value = e.clientY;
  dragStartPosition.value = event.position;

  console.log('拖拽初始状态:', {
    clientY: e.clientY,
    startPosition: event.position,
    eventTitle: event.title
  });

  // 添加全局事件监听
  document.addEventListener('mousemove', handleDrag);
  document.addEventListener('mouseup', endDrag);

  // 改变鼠标样式
  document.body.style.cursor = 'grabbing';
}

const handleDrag = (e: MouseEvent) => {
  if (!isDragging.value || !draggedEvent.value || !timelineScrollRef.value) {
    console.log('拖拽条件不满足:', { isDragging: isDragging.value, hasEvent: !!draggedEvent.value, hasRef: !!timelineScrollRef.value });
    return;
  }

  // 计算拖拽距离
  const deltaY = e.clientY - dragStartY.value;
  console.log('拖拽距离(像素):', deltaY);

  // 计算时间线容器的高度
  const containerHeight = timelineScrollRef.value.clientHeight;
  console.log('容器高度:', containerHeight);

  // 将像素距离转换为百分比位置变化
  const percentageDelta = (deltaY / containerHeight) * 100;
  console.log('百分比变化:', percentageDelta);

  // 计算新位置
  let newPosition = dragStartPosition.value + percentageDelta;
  console.log('新位置(未限制):', newPosition);

  // 限制在5%到95%的范围内
  newPosition = Math.max(5, Math.min(95, newPosition));
  console.log('新位置(已限制):', newPosition);

  // 更新事件位置
  draggedEvent.value.position = newPosition;

  // 强制更新视图
  draggedEvent.value = { ...draggedEvent.value };
}

const endDrag = () => {
  console.log('结束拖拽');

  if (draggedEvent.value) {
    console.log('最终位置:', draggedEvent.value.position);
  }

  isDragging.value = false;
  draggedEvent.value = null;

  // 移除全局事件监听
  document.removeEventListener('mousemove', handleDrag);
  document.removeEventListener('mouseup', endDrag);

  // 恢复鼠标样式
  document.body.style.cursor = '';
}

// 定义关系类型接口
interface TimelineRelation {
  id: string
  sourceId: string
  targetId: string
  type: string
  description?: string
}

// 获取与指定事件相关的关系
const getRelatedEvents = (eventId: string) => {
  if (!eventId) return []

  // 查找所有以该事件为源或目标的关系
  return timelineRelations.value.filter((rel: TimelineRelation) =>
    rel.sourceId === eventId || rel.targetId === eventId
  ).map((rel: TimelineRelation) => {
    // 如果当前事件是源，则返回目标事件的关系
    if (rel.sourceId === eventId) {
      return {
        ...rel,
        targetId: rel.targetId
      }
    }
    // 如果当前事件是目标，则返回源事件的关系，并调整关系类型
    else {
      return {
        ...rel,
        type: getInverseRelationType(rel.type),
        targetId: rel.sourceId
      }
    }
  })
}

// 获取关系的反向类型
const getInverseRelationType = (type: string) => {
  const inverseMap: { [key: string]: string } = {
    'cause': '被导致',
    'trigger': '被触发',
    'lead_to': '由于',
    'enable': '被促成',
    'related': '相关',
    'follow_up': '前置'
  }
  return inverseMap[type] || type
}

// 获取关系类型文本
const getRelationTypeText = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'cause': '导致',
    'trigger': '触发',
    'lead_to': '引发',
    'enable': '促成',
    'related': '相关',
    'follow_up': '后续'
  }
  return typeMap[type] || type
}

/**
 * 导出时间线
 * @param format 导出格式
 */
const exportTimeline = async (format: string) => {
  if (!currentTimeline.value?.id) {
    ElMessage.warning('无法导出：时间线ID不存在')
    return
  }

  const id = currentTimeline.value.id
  // 使用导出选项中的includeDetails设置，而不是硬编码为true
  const includeDetails = exportOptions.includeDetails

  // 显示加载状态
  const loadingInstance = ElLoading.service({
    lock: true,
    text: `正在导出为${format.toUpperCase()}格式...`,
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 使用API函数导出
    const response = await timelineApi.exportTimeline(id, format.toLowerCase())

    // 处理响应数据
    let blob: Blob

    if (response instanceof Blob) {
      // 如果响应已经是Blob
      blob = response
    } else if (response.data instanceof Blob) {
      // 如果响应数据是Blob
      blob = response.data
    } else {
      // 其他情况，尝试创建Blob
      const responseData = response.data || response
      blob = new Blob([responseData], {
        type: format.toLowerCase() === 'json' ? 'application/json' :
          format.toLowerCase() === 'csv' ? 'text/csv' :
            'application/pdf'
      })
    }

    // 创建URL
    const url = URL.createObjectURL(blob)

    // 创建一个隐藏的a标签，模拟点击下载
    const link = document.createElement('a')
    link.href = url
    link.target = '_blank'
    link.download = `timeline_${id}.${format.toLowerCase()}`

    // 添加错误处理
    link.onerror = () => {
      ElMessage.error('导出失败，请稍后重试')
      loadingInstance.close()
    }

    // 添加加载完成处理
    link.onload = () => {
      loadingInstance.close()
      ElMessage.success(`成功导出为${format.toUpperCase()}格式`)
    }

    document.body.appendChild(link)
    link.click()

    // 设置超时，如果5秒后还没有完成，关闭加载状态
    setTimeout(() => {
      loadingInstance.close()
    }, 5000)

    document.body.removeChild(link)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
    loadingInstance.close()
  }
}

/**
 * 异步导出时间线
 */
const exportTimelineAsync = async () => {
  if (!currentTimeline.value?.id) {
    ElMessage.warning('无法导出：时间线ID不存在')
    return
  }

  const id = currentTimeline.value.id
  const { format, includeDetails } = exportOptions

  exportLoading.value = true
  exportStatus.value = '准备中...'
  exportProgress.value = 0

  try {
    // 调用异步导出API
    const response = await timelineApi.exportTimelineAsync(id, {
      format,
      includeDetails
    })

    // 处理不同的响应格式
    if (response) {
      // 类型断言：确保 response 是一个对象
      const responseData = response as any
      let taskId = null

      // 如果response已经被请求拦截器处理过
      if (responseData.taskId) {
        taskId = responseData.taskId
      }
      // 如果response是标准格式
      else if (responseData.code === 200 && responseData.data) {
        taskId = responseData.data.taskId
      }

      if (taskId) {
        exportTaskId.value = taskId

        // 开始轮询任务状态
        await pollExportTaskStatus()
      } else {
        throw new Error('创建导出任务失败：无法获取任务ID')
      }
    } else {
      throw new Error('创建导出任务失败：响应为空')
    }
  } catch (error) {
    console.error('导出失败:', error)

    // 改进的错误处理逻辑
    let errorMessage = '导出失败，请稍后重试'

    if (error instanceof Error) {
      // 检查是否是网络错误
      if (error.message.includes('Network Error') || error.message.includes('timeout')) {
        errorMessage = '网络连接失败，请检查网络连接后重试'
      }
      // 检查是否是业务逻辑错误
      else if ((error as any).businessError) {
        errorMessage = error.message
      }
      // 检查是否是权限错误
      else if (error.message.includes('401') || error.message.includes('Unauthorized')) {
        errorMessage = '登录已过期，请重新登录后再试'
      }
      // 检查是否是服务器错误
      else if (error.message.includes('500') || error.message.includes('Internal Server Error')) {
        errorMessage = '服务器内部错误，请稍后重试'
      }
      // 其他错误
      else {
        errorMessage = error.message || '导出失败，请稍后重试'
      }
    }

    ElMessage.error(errorMessage)
    exportLoading.value = false
  }
}

/**
 * 轮询导出任务状态
 */
const pollExportTaskStatus = async () => {
  if (!exportTaskId.value) return

  const maxAttempts = 30 // 最大尝试次数
  let attempts = 0

  const poll = async () => {
    attempts++

    try {
      const response = await timelineApi.getExportTaskStatus(exportTaskId.value)

      // 处理不同的响应格式
      let task = null

      // 类型断言：确保 response 是一个对象
      const responseData = response as any

      // 如果response已经被请求拦截器处理过
      if (responseData && typeof responseData === 'object') {
        if (responseData.status !== undefined) {
          // 直接返回了任务对象
          task = responseData
        } else if (responseData.code === 200 && responseData.data) {
          // 标准响应格式
          task = responseData.data
        } else if (responseData.data && responseData.data.status) {
          // 嵌套的数据格式
          task = responseData.data
        }
      }

      if (task) {
        // 更新状态和进度
        exportStatus.value = task.status || '处理中'

        // 确保进度是数字并且在0-100范围内
        const progress = task.progress !== undefined ? Number(task.progress) : 0
        exportProgress.value = isNaN(progress) ? 0 : Math.min(100, Math.max(0, progress))

        // 如果任务完成，提供下载链接
        if (task.status === 'COMPLETED' || task.status === 'SUCCESS') {
          exportLoading.value = false
          ElMessage.success('导出完成，准备下载')

          // 下载文件
          const downloadUrl = task.downloadUrl || `/api/timelines/export/download/${exportTaskId.value}`

          // 创建一个隐藏的a标签，模拟点击下载
          const link = document.createElement('a')
          link.href = downloadUrl
          link.target = '_blank'
          link.download = `timeline_export.${exportOptions.format.toLowerCase()}`
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)

          // 关闭导出对话框
          showExportDialog.value = false
          return
        }

        // 如果任务失败，显示错误信息
        if (task.status === 'FAILED' || task.status === 'ERROR') {
          exportLoading.value = false
          ElMessage.error(`导出失败: ${task.errorMessage || task.message || '未知错误'}`)
          return
        }

        // 如果任务仍在进行中且未达到最大尝试次数，继续轮询
        if (attempts < maxAttempts) {
          setTimeout(poll, 2000) // 每2秒轮询一次
        } else {
          exportLoading.value = false
          ElMessage.warning('导出任务超时，请稍后在导出历史中查看')
        }
      } else {
        throw new Error('获取导出任务状态失败：无效的响应格式')
      }
    } catch (error) {
      console.error('轮询导出任务状态失败', error)
      ElMessage.error(error instanceof Error ? error.message : '获取导出状态失败')
      exportLoading.value = false
    }
  }

  // 开始轮询
  await poll()
}

/**
 * 取消导出任务
 */
const cancelExportTask = async () => {
  if (!exportTaskId.value) {
    ElMessage.warning('没有正在进行的导出任务')
    return
  }

  try {
    const response = await timelineApi.cancelExportTask(exportTaskId.value)

    // 处理不同的响应格式
    let success = false

    // 类型断言：确保 response 是一个对象
    const responseData = response as any

    // 如果response已经被请求拦截器处理过
    if (responseData === true || responseData === 'success') {
      success = true
    }
    // 如果response是标准格式
    else if (responseData && responseData.code === 200) {
      success = true
    }

    if (success) {
      exportLoading.value = false
      exportProgress.value = 0
      exportStatus.value = '已取消'
      ElMessage.info('已取消导出任务')

      // 延迟关闭对话框，让用户看到"已取消"状态
      setTimeout(() => {
        showExportDialog.value = false
      }, 1500)
    } else {
      throw new Error(responseData?.message || '取消导出任务失败')
    }
  } catch (error) {
    console.error('取消导出任务失败:', error)

    // 改进的错误处理逻辑
    let errorMessage = '取消导出任务失败'

    if (error instanceof Error) {
      // 检查是否是网络错误
      if (error.message.includes('Network Error') || error.message.includes('timeout')) {
        errorMessage = '网络连接失败，无法取消导出任务'
      }
      // 检查是否是业务逻辑错误
      else if ((error as any).businessError) {
        errorMessage = error.message
      }
      // 其他错误
      else {
        errorMessage = error.message || '取消导出任务失败'
      }
    }

    ElMessage.error(errorMessage)

    // 即使API调用失败，也重置本地状态
    exportLoading.value = false
  }
}

// 事件列表过滤和排序
const eventsFilter = reactive({
  keyword: '',
  nodeType: ''
})

const eventsPagination = reactive({
  page: 1,
  size: 10
})

const eventsSortConfig = reactive({
  prop: 'eventTime',
  order: 'ascending'
})

// 过滤后的事件列表（不分页）
const filteredEventsAll = computed(() => {
  let result = [...allEventsData.value]

  // 关键词过滤
  if (eventsFilter.keyword) {
    const keyword = eventsFilter.keyword.toLowerCase()
    result = result.filter(event =>
      (event.title && event.title.toLowerCase().includes(keyword)) ||
      (event.location && event.location.toLowerCase().includes(keyword))
    )
  }

  // 类型过滤
  if (eventsFilter.nodeType) {
    result = result.filter(event =>
      event.nodeType && event.nodeType.toLowerCase() === eventsFilter.nodeType.toLowerCase()
    )
  }

  // 排序
  if (eventsSortConfig.prop) {
    result.sort((a, b) => {
      let valueA = a[eventsSortConfig.prop]
      let valueB = b[eventsSortConfig.prop]

      // 特殊处理日期类型
      if (eventsSortConfig.prop === 'eventTime') {
        valueA = valueA ? new Date(valueA).getTime() : 0
        valueB = valueB ? new Date(valueB).getTime() : 0
      }

      // 特殊处理字符串类型
      if (typeof valueA === 'string') {
        valueA = valueA.toLowerCase()
      }
      if (typeof valueB === 'string') {
        valueB = valueB.toLowerCase()
      }

      if (valueA < valueB) return eventsSortConfig.order === 'ascending' ? -1 : 1
      if (valueA > valueB) return eventsSortConfig.order === 'ascending' ? 1 : -1
      return 0
    })
  }

  return result
})

// 分页后的事件列表
const filteredEvents = computed(() => {
  const allEvents = filteredEventsAll.value
  const start = (eventsPagination.page - 1) * eventsPagination.size
  const end = start + eventsPagination.size
  return allEvents.slice(start, end)
})

// 过滤事件
const filterEvents = () => {
  eventsPagination.page = 1 // 重置到第一页
}

// 重置事件过滤器
const resetEventsFilter = () => {
  eventsFilter.keyword = ''
  eventsFilter.nodeType = ''
  eventsPagination.page = 1
}

// 处理事件排序变化
const handleEventsSortChange = (sort: { prop: string, order: string }) => {
  if (sort.prop) {
    eventsSortConfig.prop = sort.prop
    eventsSortConfig.order = sort.order
  } else {
    // 如果取消排序，恢复默认排序
    eventsSortConfig.prop = 'eventTime'
    eventsSortConfig.order = 'ascending'
  }
}

// 处理事件分页大小变化
const handleEventsSizeChange = (size: number) => {
  eventsPagination.size = size
}

// 处理事件页码变化
const handleEventsCurrentChange = (page: number) => {
  eventsPagination.page = page
}

// 关系列表过滤和排序
const relationsFilter = reactive({
  keyword: '',
  type: ''
})

const relationsPagination = reactive({
  page: 1,
  size: 10
})

const relationsSortConfig = reactive({
  prop: '',
  order: 'ascending'
})

// 过滤后的关系列表（不分页）
const filteredRelationsAll = computed(() => {
  let result = [...timelineRelations.value]

  // 关键词过滤
  if (relationsFilter.keyword) {
    const keyword = relationsFilter.keyword.toLowerCase()
    result = result.filter(relation =>
      (relation.description && relation.description.toLowerCase().includes(keyword))
    )
  }

  // 类型过滤
  if (relationsFilter.type) {
    result = result.filter(relation =>
      relation.type === relationsFilter.type
    )
  }

  // 排序
  if (relationsSortConfig.prop) {
    result.sort((a, b) => {
      let valueA, valueB

      // 特殊处理源事件和目标事件的排序
      if (relationsSortConfig.prop === 'sourceId') {
        valueA = getEventTitleById(a.sourceId).toLowerCase()
        valueB = getEventTitleById(b.sourceId).toLowerCase()
      } else if (relationsSortConfig.prop === 'targetId') {
        valueA = getEventTitleById(a.targetId).toLowerCase()
        valueB = getEventTitleById(b.targetId).toLowerCase()
      } else {
        valueA = a[relationsSortConfig.prop]
        valueB = b[relationsSortConfig.prop]

        // 特殊处理字符串类型
        if (typeof valueA === 'string') {
          valueA = valueA.toLowerCase()
        }
        if (typeof valueB === 'string') {
          valueB = valueB.toLowerCase()
        }
      }

      if (valueA < valueB) return relationsSortConfig.order === 'ascending' ? -1 : 1
      if (valueA > valueB) return relationsSortConfig.order === 'ascending' ? 1 : -1
      return 0
    })
  }

  return result
})

// 分页后的关系列表
const filteredRelations = computed(() => {
  const allRelations = filteredRelationsAll.value
  const start = (relationsPagination.page - 1) * relationsPagination.size
  const end = start + relationsPagination.size
  return allRelations.slice(start, end)
})

// 过滤关系
const filterRelations = () => {
  relationsPagination.page = 1 // 重置到第一页
}

// 重置关系过滤器
const resetRelationsFilter = () => {
  relationsFilter.keyword = ''
  relationsFilter.type = ''
  relationsPagination.page = 1
}

// 处理关系排序变化
const handleRelationsSortChange = (sort: { prop: string, order: string }) => {
  if (sort.prop) {
    relationsSortConfig.prop = sort.prop
    relationsSortConfig.order = sort.order
  } else {
    // 如果取消排序，恢复默认排序
    relationsSortConfig.prop = ''
    relationsSortConfig.order = 'ascending'
  }
}

// 处理关系分页大小变化
const handleRelationsSizeChange = (size: number) => {
  relationsPagination.size = size
}

// 处理关系页码变化
const handleRelationsCurrentChange = (page: number) => {
  relationsPagination.page = page
}

// 获取重要性颜色
const getImportanceColor = (score: number) => {
  if (score >= 0.8) return '#f56c6c' // 高重要性，红色
  if (score >= 0.6) return '#e6a23c' // 中高重要性，橙色
  if (score >= 0.4) return '#409eff' // 中等重要性，蓝色
  return '#67c23a' // 低重要性，绿色
}

// 关系图相关
const graphContainer = ref<HTMLElement | null>(null)
const graphInitialized = ref(false)
const graphLayout = ref('force')
const showNodeLabels = ref(true)
const graphInstance = ref<any>(null)

// 生命周期
onMounted(() => {
  console.log('TimelineDetailView组件已挂载')
  console.log('路由参数:', route.params)

  const timelineId = route.params.id as string
  console.log('时间线ID:', timelineId)

  if (timelineId && !props.timeline) {
    console.log('加载时间线详情:', timelineId)
    loadTimelineDetail(timelineId)
  } else if (props.timeline) {
    // 如果已经有 timeline 属性，直接使用
    console.log('使用props中的timeline:', props.timeline)
    timeline.value = props.timeline
  } else {
    // 如果没有 ID 也没有 timeline 属性，显示错误信息
    console.error('无法加载时间线详情：缺少时间线ID')
    ElMessage.error('无法加载时间线详情：缺少时间线ID')
  }

  // 确保DOM元素已经渲染
  nextTick(() => {
    console.log('时间线滚动容器引用:', timelineScrollRef.value);

    // 添加一个提示，告诉用户可以拖动事件
    if (timelineScrollRef.value) {
      ElMessage({
        message: '提示：您可以拖动事件气泡来调整它们的位置',
        type: 'info',
        duration: 3000
      });
    }
  });
})

// 监听路由变化
watch(() => route.params.id, (newId) => {
  if (newId && !props.timeline) {
    loadTimelineDetail(newId as string)
  }
})
</script>

<style scoped>
.timeline-detail-view {
  height: 100vh;
  /* 使用全屏高度 */
  min-height: 600px;
  /* 设置最小高度 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* 防止整体页面滚动 */
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.header-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.timeline-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.detail-content {
  flex: 1;
  overflow: hidden;
  height: calc(100vh - 250px);
  /* 确保内容区域有明确的高度 */
  min-height: 500px;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

/* 时间线视图样式 */
.timeline-view {
  display: flex;
  height: calc(100vh - 200px);
  /* 确保有足够的高度 */
  min-height: 600px;
  /* 设置最小高度 */
  overflow: hidden;
}

.timeline-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.timeline-header-controls {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.zoom-label {
  margin-left: 8px;
  color: #606266;
  font-size: 14px;
}

.zoom-detail {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
  font-style: italic;
}

.timeline-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  padding: 20px 0;
  max-height: calc(100vh - 300px);
  /* 确保有最大高度限制 */
  min-height: 500px;
  /* 设置最小高度 */
}

.timeline-track {
  position: relative;
  min-width: 100%;
  height: 550px;
  transform-origin: center center;
}

/* 垂直时间线样式 */
.timeline-track.vertical {
  height: auto;
  min-height: 800px;
  padding: 40px 0;
  transform-origin: center top;
  /* 确保内容可以完全显示 */
  padding-bottom: 100px;
}

.time-axis-vertical {
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 4px;
  margin-left: -2px;
  background-color: #e4e7ed;
  z-index: 1;
}

.time-markers-vertical {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 2;
}

.time-marker-vertical {
  position: absolute;
  left: 0;
  right: 0;
}

.marker-line-vertical {
  position: absolute;
  left: calc(50% - 40px);
  width: 80px;
  height: 2px;
  background-color: #e4e7ed;
}

.marker-label-vertical {
  position: absolute;
  left: calc(50% - 80px);
  width: 70px;
  text-align: right;
  font-size: 12px;
  color: #909399;
  transform: translateY(-50%);
}

.events-track-vertical {
  position: relative;
  width: 100%;
  height: 100%;
}

/* 时间线项目样式 */
.timeline-item {
  position: absolute;
  width: 45%;
  padding: 0;
  cursor: grab;
  /* 指示可拖动 */
  z-index: 3;
  transform: translateY(-50%);
  /* 垂直居中对齐 */
  margin-top: 0;
  /* 重置默认边距 */
  margin-bottom: 0;
  /* 重置默认边距 */
  min-height: 120px;
  /* 确保有最小高度 */
  user-select: none;
  /* 防止拖动时选择文本 */
  touch-action: none;
  /* 防止触摸设备上的默认行为 */
}

/* 拖动时的样式 */
.timeline-item.dragging {
  z-index: 100 !important;
  /* 确保拖动的项在最上层 */
  cursor: grabbing !important;
  transition: none !important;
  /* 拖动时禁用过渡效果，使移动更流畅 */
  opacity: 0.9;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  pointer-events: auto !important;
}

/* 拖动时的内容样式 */
.timeline-item.dragging .timeline-item-content {
  border-width: 2px;
  border-style: dashed;
  background-color: #f8f9fa;
}

.timeline-item-left {
  left: 0;
  padding-right: 20px;
}

.timeline-item-right {
  right: 0;
  padding-left: 20px;
}

.timeline-item-content {
  position: relative;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border-left: 4px solid #409eff;
  cursor: pointer;
  /* 内容区域使用普通指针，表示可点击 */
}

/* 时间线项目内容样式 */
.timeline-date {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.timeline-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.timeline-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.timeline-time {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.timeline-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

/* 时间线徽章 */
.timeline-badge {
  position: absolute;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  top: 50%;
  /* 改为50%，使徽章垂直居中 */
  transform: translateY(-50%);
  /* 确保徽章完全居中 */
  z-index: 1;
}

.timeline-item-left .timeline-badge {
  right: -15px;
}

.timeline-item-right .timeline-badge {
  left: -15px;
}

/* 连接线 */
.timeline-item::before {
  content: '';
  position: absolute;
  top: 50%;
  /* 改为50%，使连线垂直居中 */
  width: 20px;
  height: 2px;
  background-color: #e4e7ed;
}

.timeline-item-left::before {
  right: -20px;
}

.timeline-item-right::before {
  left: -20px;
}

/* 不同类型的徽章颜色 */
.badge-source {
  background-color: #67c23a;
}

.badge-terminal {
  background-color: #f56c6c;
}

.badge-hub {
  background-color: #e6a23c;
}

.badge-hot {
  background-color: #ff4757;
  animation: pulse 2s infinite;
}

.badge-normal {
  background-color: #409eff;
}

.node-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: #409eff;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
  position: absolute;
  left: 0;
  top: 0;
  transform: translate(-50%, -50%);
  z-index: 2;
  /* 确保点在连接线上方 */
}

/* 添加连接线 */
.event-node::after {
  content: '';
  position: absolute;
  left: 0;
  width: 2px;
  background-color: #e0e0e0;
  z-index: 1;
}

/* 上排事件的点在下方，连接线向下 */
.row-top .node-dot {
  top: 100%;
}

.row-top::after {
  top: 100%;
  height: 120px;
  /* 增加连接线长度 */
  background: linear-gradient(to bottom, #409eff, #e0e0e0);
}

/* 下排事件的点在上方，连接线向上 */
.row-bottom .node-dot {
  top: 0;
}

.row-bottom::after {
  bottom: 100%;
  height: 120px;
  /* 增加连接线长度 */
  background: linear-gradient(to top, #409eff, #e0e0e0);
}

.event-balloon {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  min-width: 200px;
  max-width: 280px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  z-index: 10;
  display: block;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

/* 上排事件的气球在下方 */
.row-top .event-balloon {
  top: 40px;
  /* 进一步增加距离 */
}

/* 下排事件的气球在上方 */
.row-bottom .event-balloon {
  bottom: 40px;
  /* 进一步增加距离 */
}

.balloon-content {
  padding: 14px 18px;
}

.balloon-arrow {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
}

/* 上排事件的气球箭头指向上方 */
.row-top .balloon-arrow {
  top: -10px;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 10px solid #fff;
}

.row-top .balloon-arrow::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -10px;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-bottom: 10px solid #ebeef5;
  z-index: -1;
}

/* 下排事件的气球箭头指向下方 */
.row-bottom .balloon-arrow {
  bottom: -10px;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 10px solid #fff;
}

.row-bottom .balloon-arrow::before {
  content: '';
  position: absolute;
  bottom: -1px;
  left: -10px;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 10px solid #ebeef5;
  z-index: -1;
}

.balloon-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 15px;
  color: #303133;
  line-height: 1.4;
}

.balloon-time {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.balloon-location {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}

.node-popup {
  display: none;
  /* 隐藏旧的弹出框 */
}

/* 时间线项目悬停效果 */
.timeline-item:hover .timeline-item-content {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

.timeline-item:hover .timeline-badge {
  transform: scale(1.2);
}

.timeline-item:hover::before {
  background-color: #409eff;
  height: 3px;
}

/* 活动状态 */
.timeline-item.active .timeline-item-content {
  border-left-width: 6px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.timeline-item.active .timeline-badge {
  transform: scale(1.3);
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.2);
}

/* 不同类型的项目边框颜色 */
.timeline-item.node-type-source .timeline-item-content {
  border-left-color: #67c23a;
}

.timeline-item.node-type-terminal .timeline-item-content {
  border-left-color: #f56c6c;
}

.timeline-item.node-type-hub .timeline-item-content {
  border-left-color: #e6a23c;
}

.timeline-item.node-type-hot .timeline-item-content {
  border-left-color: #ff4757;
}

.event-node.active .node-dot {
  transform: scale(1.3);
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.2);
}

.event-node.active .event-balloon {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
  border: 2px solid #409eff;
}

.event-node.node-type-source .node-dot {
  background-color: #67c23a;
}

.event-node.node-type-source .event-balloon {
  border-left: 3px solid #67c23a;
}

.event-node.node-type-terminal .node-dot {
  background-color: #f56c6c;
}

.event-node.node-type-terminal .event-balloon {
  border-left: 3px solid #f56c6c;
}

.event-node.node-type-hub .node-dot {
  background-color: #e6a23c;
}

.event-node.node-type-hub .event-balloon {
  border-left: 3px solid #e6a23c;
}

.event-node.node-type-hot .node-dot {
  background-color: #ff4757;
  animation: pulse 2s infinite;
}

.event-node.node-type-hot .event-balloon {
  border-left: 3px solid #ff4757;
}

/* 事件侧边栏样式 */
.event-sidebar {
  width: 300px;
  border-left: 1px solid #e4e7ed;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.sidebar-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.related-events {
  margin-top: 20px;
}

.related-events h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
}

.related-event-title {
  color: #409eff;
  cursor: pointer;
}

.related-event-title:hover {
  text-decoration: underline;
}

/* 关系图视图样式 */
.graph-view {
  height: 100%;
}

.graph-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.graph-controls {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.graph-canvas {
  flex: 1;
  position: relative;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.graph-loading {
  padding: 20px;
}

.loading-text {
  text-align: center;
  margin-top: 20px;
  color: #909399;
}

/* 表格视图样式 */
.table-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-view .el-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.table-view .el-tabs__content {
  flex: 1;
  overflow: hidden;
}

.table-view .el-tab-pane {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-view .el-table {
  flex: 1;
}

.table-view .el-table__body-wrapper {
  max-height: calc(100vh - 400px);
  overflow-y: auto;
}

.filter-container {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.clickable-cell {
  color: #409eff;
  cursor: pointer;
}

.clickable-cell:hover {
  text-decoration: underline;
}

/* 导出对话框样式 */
.export-progress {
  margin-top: 20px;
}

.progress-status {
  margin-bottom: 8px;
  font-weight: 500;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }

  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
