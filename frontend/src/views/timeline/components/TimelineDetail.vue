<template>
  <div class="timeline-detail-container" v-loading="loading">
    <!-- 无数据提示 -->
    <div v-if="!timeline && !loading" class="no-data">
      <el-empty description="暂无时间线数据">
        <el-button type="primary" @click="$emit('close')">返回列表</el-button>
      </el-empty>
    </div>

    <!-- 时间线详情 -->
    <template v-if="timeline">
      <!-- 头部信息 -->
      <div class="detail-header">
        <div class="header-info">
          <h2>{{ timeline.title }}</h2>
          <div class="timeline-meta">
            <el-tag :type="getStatusType(timeline.status)" size="small">
              {{ getStatusText(timeline.status) }}
            </el-tag>
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDate(timeline.createdAt || timeline.createTime) }}
            </span>
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              {{ timeline.eventCount || 0 }} 个事件
            </span>
            <span class="meta-item">
              <el-icon><Connection /></el-icon>
              {{ timeline.relationCount || 0 }} 个关系
            </span>
          </div>
        </div>

        <div class="header-actions">
          <el-button-group>
            <el-button type="primary" @click="$emit('view-graph', timeline)" size="small">
              <el-icon><Share /></el-icon>
              查看关系图
            </el-button>
            <el-button @click="$emit('edit', timeline)" size="small">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button @click="$emit('export', timeline)" size="small">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </el-button-group>

          <el-button @click="$emit('close')" size="small">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </div>

      <!-- 详情内容 -->
      <div class="detail-content">
        <el-tabs v-model="activeTab">
          <!-- 基本信息标签页 -->
          <el-tab-pane label="基本信息" name="info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="时间线ID">{{ timeline.id }}</el-descriptions-item>
              <el-descriptions-item label="标题">{{ timeline.title }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(timeline.status)">
                  {{ getStatusText(timeline.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(timeline.createdAt || timeline.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ formatDateTime(timeline.updatedAt || timeline.updateTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="事件数量">{{ timeline.eventCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="关系数量">{{ timeline.relationCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="时间跨度">{{ formatTimeSpan(timeline.timeSpan) }}</el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">
                {{ timeline.description || '暂无描述' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>

          <!-- 地区信息标签页 -->
          <el-tab-pane label="相关地区" name="regions">
            <div v-if="!timeline.regions || timeline.regions.length === 0" class="empty-data">
              <el-empty description="暂无相关地区数据" />
            </div>
            <el-table v-else :data="timeline.regions" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="地区名称" />
              <el-table-column prop="type" label="地区类型" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ getRegionTypeText(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button @click="viewRegion(row)" text size="small">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <!-- 事件列表标签页 -->
          <el-tab-pane label="事件列表" name="events">
            <!-- 事件列表过滤器 -->
            <div class="filter-container">
              <el-input v-model="eventsFilter.keyword" placeholder="搜索事件标题或地点" clearable @input="filterEvents"
                style="width: 220px; margin-right: 10px;">
                <template #prefix>
                  <el-icon><Search /></el-icon>
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

            <div v-if="!timeline.nodes || timeline.nodes.length === 0" class="empty-data">
              <el-empty description="暂无事件数据" />
            </div>
            <el-table v-else :data="filteredEvents" style="width: 100%"
              :default-sort="{ prop: 'eventTime', order: 'ascending' }" @sort-change="handleEventsSortChange">
              <el-table-column prop="title" label="事件标题" min-width="200" sortable="custom" />
              <el-table-column prop="eventTime" label="时间" width="180" sortable="custom">
                <template #default="{ row }">
                  {{ formatDateTime(row.eventTime || row.event?.eventTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="location" label="地点" width="150" sortable="custom">
                <template #default="{ row }">
                  {{ row.location || row.event?.location || '-' }}
                </template>
              </el-table-column>
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
                  <el-button @click="viewEvent(row)" text size="small">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container" v-if="filteredEvents.length > 10">
              <el-pagination v-model:current-page="eventsPagination.page" v-model:page-size="eventsPagination.size"
                :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" :total="filteredEvents.length"
                @size-change="handleEventsSizeChange" @current-change="handleEventsCurrentChange" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <!-- 事件详情对话框 -->
    <el-dialog v-model="showEventDialog" title="事件详情" width="600px">
      <div v-if="selectedEvent" class="event-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">{{ selectedEvent.title || selectedEvent.event?.title }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatDateTime(selectedEvent.eventTime || selectedEvent.event?.eventTime) }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ selectedEvent.location || selectedEvent.event?.location || '-' }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small" :type="getNodeTypeTagType(selectedEvent.nodeType)">
              {{ getNodeTypeText(selectedEvent.nodeType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="重要性" v-if="selectedEvent.importanceScore !== undefined">
            <el-rate v-model="selectedEvent.importanceScore" :max="1" :step="0.01" :show-score="true"
              :score-template="(selectedEvent.importanceScore * 100).toFixed(0) + '%'" disabled />
          </el-descriptions-item>
          <el-descriptions-item label="描述">
            {{ selectedEvent.description || selectedEvent.event?.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 相关事件 -->
        <div class="related-events" v-if="getRelatedEvents(selectedEvent.id).length > 0">
          <h4>相关事件</h4>
          <el-table :data="getRelatedEvents(selectedEvent.id)" size="small" style="width: 100%">
            <el-table-column label="关系" width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ getRelationTypeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="事件" min-width="120">
              <template #default="{ row }">
                <span class="related-event-title" @click="viewEventById(row.targetId)">
                  {{ getEventTitleById(row.targetId) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 地区详情对话框 -->
    <el-dialog v-model="showRegionDialog" title="地区详情" width="600px">
      <div v-if="selectedRegion" class="region-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="ID">{{ selectedRegion.id }}</el-descriptions-item>
          <el-descriptions-item label="名称">{{ selectedRegion.name }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag size="small">{{ getRegionTypeText(selectedRegion.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ selectedRegion.description || '暂无描述' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 地区项目 -->
        <div class="region-items" v-if="selectedRegion.items && selectedRegion.items.length > 0">
          <h4>包含的地区项目</h4>
          <el-table :data="selectedRegion.items" size="small" style="width: 100%">
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script se
tup lang="ts">
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Calendar, Document, Connection, Share, Edit, Download, Close, Search
} from '@element-plus/icons-vue'
import { timelineApi } from '@/api/optimizedApi'

// 类型定义
interface TimelineEvent {
  id: string
  title?: string
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
}

interface TimelineRelation {
  id: string
  sourceId: string
  targetId: string
  type: string
  description?: string
}

interface RegionItem {
  id: string
  name: string
  type: string
}

interface Region {
  id: string
  name: string
  type: string
  description?: string
  items?: RegionItem[]
}

interface Timeline {
  id: string
  title: string
  status: string
  description?: string
  eventCount?: number
  relationCount?: number
  timeSpan?: string
  createdAt?: string
  createTime?: string
  updatedAt?: string
  updateTime?: string
  nodes?: TimelineEvent[]
  relationships?: TimelineRelation[]
  regions?: Region[]
}

// 定义组件属性
const props = defineProps<{
  timelineId?: string | number
  timeline?: Timeline
}>()

// 定义组件事件
const emit = defineEmits(['close', 'edit', 'export', 'view-graph'])

// 响应式数据
const loading = ref(false)
const timeline = ref<Timeline | null>(null)
const activeTab = ref('info')
const selectedEvent = ref<TimelineEvent | null>(null)
const selectedRegion = ref<Region | null>(null)
const showEventDialog = ref(false)
const showRegionDialog = ref(false)

// 事件过滤器
const eventsFilter = reactive({
  keyword: '',
  nodeType: ''
})

// 事件分页
const eventsPagination = reactive({
  page: 1,
  size: 10
})

// 计算属性
const filteredEvents = computed(() => {
  if (!timeline.value?.nodes) return []
  
  let events = timeline.value.nodes.map(node => ({
    ...node,
    title: node.title || node.event?.title || '未命名事件'
  }))
  
  // 应用过滤条件
  if (eventsFilter.keyword) {
    const keyword = eventsFilter.keyword.toLowerCase()
    events = events.filter(event => {
      const title = (event.title || event.event?.title || '').toLowerCase()
      const location = (event.location || event.event?.location || '').toLowerCase()
      return title.includes(keyword) || location.includes(keyword)
    })
  }
  
  if (eventsFilter.nodeType) {
    events = events.filter(event => 
      event.nodeType && event.nodeType.toLowerCase() === eventsFilter.nodeType.toLowerCase()
    )
  }
  
  return events
})

// 监听属性变化
watch(() => props.timeline, (newTimeline) => {
  if (newTimeline) {
    timeline.value = newTimeline
  } else if (props.timelineId) {
    loadTimelineDetail(props.timelineId.toString())
  }
}, { immediate: true })

watch(() => props.timelineId, (newId) => {
  if (newId && (!props.timeline || props.timeline.id !== newId.toString())) {
    loadTimelineDetail(newId.toString())
  }
}, { immediate: true })

// 生命周期钩子
onMounted(() => {
  if (props.timeline) {
    timeline.value = props.timeline
  } else if (props.timelineId) {
    loadTimelineDetail(props.timelineId.toString())
  }
})

// 方法
const loadTimelineDetail = async (id: string) => {
  loading.value = true
  try {
    const response = await timelineApi.getTimelineWithDetails(id)
    
    if (response) {
      // 处理响应数据
      if (response.id) {
        timeline.value = response
      } else if (response.data && response.data.id) {
        timeline.value = response.data
      } else {
        throw new Error('无效的响应数据格式')
      }
    } else {
      throw new Error('获取时间线详情失败：响应为空')
    }
  } catch (error) {
    console.error('加载时间线详情失败', error)
    
    let errorMessage = '加载时间线详情失败'
    if (error instanceof Error) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

// 事件过滤方法
const filterEvents = () => {
  eventsPagination.page = 1
}

const resetEventsFilter = () => {
  eventsFilter.keyword = ''
  eventsFilter.nodeType = ''
  eventsPagination.page = 1
}

// 事件排序方法
const handleEventsSortChange = (column: { prop: string, order: string }) => {
  // 实现排序逻辑
}

// 事件分页方法
const handleEventsSizeChange = (size: number) => {
  eventsPagination.size = size
}

const handleEventsCurrentChange = (page: number) => {
  eventsPagination.page = page
}

// 查看事件详情
const viewEvent = (event: TimelineEvent) => {
  selectedEvent.value = event
  showEventDialog.value = true
}

const viewEventById = (id: string) => {
  if (!timeline.value?.nodes) return
  
  const event = timeline.value.nodes.find(node => node.id === id)
  if (event) {
    viewEvent(event)
  }
}

// 查看地区详情
const viewRegion = (region: Region) => {
  selectedRegion.value = region
  showRegionDialog.value = true
}

// 获取相关事件
const getRelatedEvents = (eventId: string) => {
  if (!timeline.value?.relationships) return []
  
  return timeline.value.relationships.filter(rel => 
    rel.sourceId === eventId || rel.targetId === eventId
  ).map(rel => {
    // 确保目标ID不是当前事件ID
    const targetId = rel.sourceId === eventId ? rel.targetId : rel.sourceId
    return {
      ...rel,
      targetId
    }
  })
}

// 获取事件标题
const getEventTitleById = (id: string) => {
  if (!timeline.value?.nodes) return '未知事件'
  
  const event = timeline.value.nodes.find(node => node.id === id)
  return event?.title || event?.event?.title || '未知事件'
}

// 格式化方法
const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatDateTime = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatTimeSpan = (timeSpan: string | undefined) => {
  if (!timeSpan) return '-'
  
  try {
    // 处理ISO 8601持续时间格式
    if (timeSpan.startsWith('P') || timeSpan.startsWith('PT')) {
      return timeSpan
        .replace('PT', '')
        .replace('P', '')
        .replace('T', ' ')
        .replace('H', '小时')
        .replace('M', '分钟')
        .replace('S', '秒')
        .replace('D', '天')
        .replace('W', '周')
        .replace('Y', '年')
    }
    
    return timeSpan
  } catch (error) {
    console.error('格式化时间跨度失败:', error)
    return timeSpan || '-'
  }
}

// 获取状态类型和文本
const getStatusType = (status: string) => {
  const typeMap: { [key: string]: string } = {
    'COMPLETED': 'success',
    'completed': 'success',
    'PROCESSING': 'warning',
    'processing': 'warning',
    'FAILED': 'danger',
    'failed': 'danger',
    'DRAFT': 'info',
    'draft': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: { [key: string]: string } = {
    'COMPLETED': '已完成',
    'completed': '已完成',
    'PROCESSING': '处理中',
    'processing': '处理中',
    'FAILED': '失败',
    'failed': '失败',
    'DRAFT': '草稿',
    'draft': '草稿'
  }
  return textMap[status] || '未知'
}

// 获取节点类型标签类型和文本
const getNodeTypeTagType = (nodeType: string | undefined) => {
  const typeMap: { [key: string]: string } = {
    'source': 'success',
    'terminal': 'danger',
    'hub': 'warning',
    'hot': 'danger',
    'normal': 'info'
  }
  return typeMap[nodeType?.toLowerCase() || 'normal'] || 'info'
}

const getNodeTypeText = (nodeType: string | undefined) => {
  const textMap: { [key: string]: string } = {
    'source': '源事件',
    'terminal': '终端事件',
    'hub': '枢纽事件',
    'hot': '热点事件',
    'normal': '普通事件'
  }
  return textMap[nodeType?.toLowerCase() || 'normal'] || '普通事件'
}

// 获取地区类型文本
const getRegionTypeText = (type: string) => {
  const textMap: { [key: string]: string } = {
    'CUSTOM': '自定义',
    'CONTINENT': '洲',
    'COUNTRY': '国家',
    'PROVINCE': '省份',
    'CITY': '城市'
  }
  return textMap[type] || type
}

// 获取关系类型文本
const getRelationTypeText = (type: string) => {
  const textMap: { [key: string]: string } = {
    'cause': '导致',
    'trigger': '触发',
    'lead_to': '引发',
    'enable': '促成',
    'related': '相关',
    'follow_up': '后续'
  }
  return textMap[type] || type
}

// 获取重要性颜色
const getImportanceColor = (score: number) => {
  if (score >= 0.8) return '#f56c6c'
  if (score >= 0.6) return '#e6a23c'
  if (score >= 0.4) return '#409eff'
  return '#67c23a'
}
</script>
<sty
le scoped>
.timeline-detail-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.header-info {
  flex: 1;
}

.header-info h2 {
  margin: 0 0 10px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.timeline-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  color: #606266;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.detail-content {
  flex: 1;
  overflow: auto;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.filter-container {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.event-detail, .region-detail {
  padding: 10px;
}

.related-events, .region-items {
  margin-top: 20px;
}

.related-events h4, .region-items h4 {
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.related-event-title {
  color: #409eff;
  cursor: pointer;
}

.related-event-title:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .timeline-meta {
    gap: 10px;
  }
}
</style>