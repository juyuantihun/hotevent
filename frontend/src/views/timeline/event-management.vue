<template>
  <div class="timeline-event-management">
    <div class="page-header">
      <div class="header-content">
        <h1>时间线事件管理</h1>
        <p class="description">为时间线添加、编辑和管理事件</p>
      </div>
    </div>

    <div class="main-content">
      <!-- 时间线选择器 -->
      <el-card class="timeline-selector-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>选择时间线</span>
          </div>
        </template>
        
        <div class="timeline-selector">
          <el-select
            v-model="selectedTimelineId"
            placeholder="请选择要管理的时间线"
            filterable
            clearable
            style="width: 400px"
            @change="handleTimelineChange"
          >
            <el-option
              v-for="timeline in timelines"
              :key="timeline.id"
              :label="timeline.title || timeline.name"
              :value="timeline.id"
            >
              <div class="timeline-option">
                <span class="timeline-title">{{ timeline.title || timeline.name }}</span>
                <span class="timeline-info">{{ timeline.eventCount || 0 }} 个事件</span>
              </div>
            </el-option>
          </el-select>
          
          <el-button type="primary" @click="loadTimelines" :loading="loadingTimelines">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </div>


      </el-card>

      <!-- 事件管理Tab -->
      <div v-if="selectedTimelineId" class="event-management-section">
        <el-card shadow="never">
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 手动添加事件 -->
            <el-tab-pane label="手动添加事件" name="manual">
              <div class="tab-content">
                <ManualAddEventForm 
                  :timeline-id="selectedTimelineId"
                  @success="handleAddEventSuccess"
                />
              </div>
            </el-tab-pane>

            <!-- 从已有事件选择 -->
            <el-tab-pane label="从已有事件选择" name="select">
              <div class="tab-content">
                <SelectExistingEvents 
                  :timeline-id="selectedTimelineId"
                  @success="handleSelectEventSuccess"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <!-- 当前时间线的事件列表 -->
        <el-card class="events-list-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>当前时间线事件 ({{ timelineEvents.length }})</span>
              <el-button text @click="loadTimelineEvents">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div v-if="timelineEvents.length > 0">
            <el-table :data="timelineEvents" v-loading="loadingEvents" style="width: 100%" 
              :default-sort="{ prop: 'eventTime', order: 'ascending' }">
              <el-table-column prop="title" label="事件描述" min-width="300" sortable="custom" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.title || '未设置描述' }}
                </template>
              </el-table-column>
              <el-table-column prop="eventTime" label="事件时间" width="180" sortable="custom">
                <template #default="{ row }">
                  {{ formatDateTime(row.eventTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="location" label="地点" width="200" sortable="custom" show-overflow-tooltip>
                <template #default="{ row }">
                  {{ row.location || '未设置' }}
                </template>
              </el-table-column>
              <el-table-column prop="nodeType" label="类型" width="120" sortable="custom">
                <template #default="{ row }">
                  <el-tag size="small" :type="getNodeTypeTagType(row.nodeType || row.node_type || row.type)">
                    {{ getNodeTypeText(row.nodeType || row.node_type || row.type) }}
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
              <el-table-column label="操作" width="160" fixed="right" align="center">
                <template #default="{ row }">
                  <div class="action-buttons">
                    <el-button @click="viewEventDetail(row)" text size="small" type="primary">
                      查看详情
                    </el-button>
                    <el-button @click="removeEventFromTimeline(row)" text size="small" type="danger">
                      移除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="no-events">
            <el-empty description="当前时间线暂无事件" :image-size="80">
              <el-button type="primary" @click="activeTab = 'manual'">添加事件</el-button>
            </el-empty>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <el-empty description="请先选择一个时间线">
          <el-button type="primary" @click="loadTimelines">加载时间线列表</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 事件详情对话框 -->
    <el-dialog
      v-model="showEventDetailDialog"
      title="事件详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedEventDetail" class="event-detail-content">
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="事件ID" :span="1">
            <el-tag size="small" type="info">{{ selectedEventDetail.id }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="事件类型" :span="1">
            <el-tag size="small" :type="getNodeTypeTagType(selectedEventDetail.nodeType)">
              {{ getNodeTypeText(selectedEventDetail.nodeType) }}
            </el-tag>
          </el-descriptions-item>
          
          <el-descriptions-item label="事件时间" :span="2">
            <div class="detail-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(selectedEventDetail.eventTime) }}
            </div>
          </el-descriptions-item>
          
          <el-descriptions-item label="事件地点" :span="2">
            <div class="detail-location">
              <el-icon><Location /></el-icon>
              {{ selectedEventDetail.location || '未设置' }}
            </div>
          </el-descriptions-item>
          
          <el-descriptions-item label="重要性评分" :span="1">
            <div class="importance-score">
              <el-progress 
                :percentage="Math.round(selectedEventDetail.importanceScore * 100)"
                :color="getImportanceColor(selectedEventDetail.importanceScore)"
                :show-text="true"
              />
              <span class="score-text">{{ (selectedEventDetail.importanceScore * 100).toFixed(1) }}%</span>
            </div>
          </el-descriptions-item>
          
          <el-descriptions-item label="关联创建时间" :span="1" v-if="selectedEventDetail.relationCreatedAt">
            {{ formatDateTime(selectedEventDetail.relationCreatedAt) }}
          </el-descriptions-item>
          
          <el-descriptions-item label="事件描述" :span="2">
            <div class="event-description">
              {{ selectedEventDetail.description || selectedEventDetail.title || '无描述信息' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 额外信息 -->
        <div class="extra-info" v-if="selectedEventDetail.sourceType !== undefined">
          <h4>额外信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="是否为源事件">
              <el-tag :type="selectedEventDetail.sourceType ? 'success' : 'info'" size="small">
                {{ selectedEventDetail.sourceType ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="强度等级">
              {{ selectedEventDetail.intensityLevel || '未设置' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEventDetailDialog = false">关闭</el-button>
          <el-button type="primary" @click="editEvent(selectedEventDetail)" disabled>
            编辑事件
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, Clock, Location
} from '@element-plus/icons-vue'
import ManualAddEventForm from './components/ManualAddEventForm.vue'
import SelectExistingEvents from './components/SelectExistingEvents.vue'

// 响应式数据
const selectedTimelineId = ref<string | number>('')
const currentTimeline = ref<any>(null)
const timelines = ref<any[]>([])
const timelineEvents = ref<any[]>([])
const loadingTimelines = ref(false)
const loadingEvents = ref(false)
const activeTab = ref('manual')
const showEventDetailDialog = ref(false)
const selectedEventDetail = ref<any>(null)

// 方法
const loadTimelines = async () => {
  loadingTimelines.value = true
  try {
    const response = await fetch('/api/timelines?page=1&size=100')
    
    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 200) {
      timelines.value = result.data.records || result.data.list || []
    } else {
      throw new Error(result.message || '获取时间线列表失败')
    }
  } catch (error: any) {
    console.error('加载时间线列表失败:', error)
    ElMessage.error(error.message || '加载时间线列表失败')
  } finally {
    loadingTimelines.value = false
  }
}

const handleTimelineChange = async (timelineId: string | number) => {
  if (!timelineId) {
    currentTimeline.value = null
    timelineEvents.value = []
    return
  }

  await loadTimelineInfo(timelineId)
}

const loadTimelineInfo = async (timelineId: string | number) => {
  try {
    // 并行获取时间线详情和事件列表
    const [detailResponse, eventsResponse] = await Promise.all([
      fetch(`/api/timelines/${timelineId}`),
      fetch(`/api/timelines/${timelineId}/events`)
    ])
    
    // 处理时间线详情
    if (detailResponse.ok) {
      const detailResult = await detailResponse.json()
      if (detailResult.code === 200) {
        currentTimeline.value = detailResult.data
      }
    }
    
    // 处理事件列表
    if (eventsResponse.ok) {
      const eventsResult = await eventsResponse.json()
      if (eventsResult.code === 200) {
        // 处理时间线详情数据结构，提取nodes数组
        const rawData = eventsResult.data
        let events = []
        
        if (rawData.events && Array.isArray(rawData.events)) {
          // 处理事件管理接口返回的数据格式
          events = rawData.events.map((event: any) => ({
            id: event.id,
            title: event.event_description || '未知事件',
            eventTime: event.event_time,
            location: event.event_location || '未设置',
            nodeType: event.source_type ? 'source' : 'normal',
            importanceScore: event.intensity_level || 0.5,
            description: event.event_description,
            // 保存原始数据用于详情显示
            source_type: event.source_type,
            intensity_level: event.intensity_level,
            relation_created_at: event.relation_created_at
          }))
        } else if (rawData.nodes && Array.isArray(rawData.nodes)) {
          // 如果是时间线详情格式，从nodes中提取事件数据
          events = rawData.nodes.map((node: any, index: number) => ({
            id: node.id || `event-${index}`,
            title: node.event_description || node.event?.title || node.title || '未知事件',
            eventTime: node.event_time || node.eventTime || node.event?.eventTime,
            location: node.event_location || node.event?.location || node.location,
            nodeType: node.nodeType || 'normal',
            importanceScore: node.intensity_level || node.importanceScore || 0.5,
            description: node.event?.description || node.description
          }))
        } else if (Array.isArray(rawData)) {
          // 如果是直接的事件数组
          events = rawData.map((event: any) => ({
            id: event.id,
            title: event.event_description || event.title || '未知事件',
            eventTime: event.event_time || event.eventTime,
            location: event.event_location || event.location || '未设置',
            nodeType: event.source_type ? 'source' : (event.nodeType || 'normal'),
            importanceScore: event.intensity_level || event.importanceScore || 0.5,
            description: event.event_description || event.description
          }))
        }
        
        timelineEvents.value = events
        console.log('加载的事件数据:', timelineEvents.value)
        console.log('原始数据结构:', rawData)
        console.log('处理后的第一个事件:', events[0])
        
        // 更新事件数量
        if (currentTimeline.value) {
          currentTimeline.value.eventCount = timelineEvents.value.length
        }
      }
    }
    
  } catch (error: any) {
    console.error('获取时间线信息失败:', error)
    ElMessage.error(error.message || '获取时间线信息失败')
  }
}



const loadTimelineEvents = async () => {
  if (!selectedTimelineId.value) return

  loadingEvents.value = true
  try {
    const response = await fetch(`/api/timelines/${selectedTimelineId.value}/events`)
    
    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 200) {
      // 处理时间线详情数据结构，提取nodes数组
      const rawData = result.data
      let events = []
      
      if (rawData.events && Array.isArray(rawData.events)) {
        // 处理事件管理接口返回的数据格式
        events = rawData.events.map((event: any) => ({
          id: event.id,
          title: event.event_description || '未知事件',
          eventTime: event.event_time,
          location: event.event_location || '未设置',
          nodeType: event.source_type ? 'source' : 'normal',
          importanceScore: event.intensity_level || 0.5,
          description: event.event_description,
          // 保存原始数据用于详情显示
          source_type: event.source_type,
          intensity_level: event.intensity_level,
          relation_created_at: event.relation_created_at
        }))
      } else if (rawData.nodes && Array.isArray(rawData.nodes)) {
        // 如果是时间线详情格式，从nodes中提取事件数据
        events = rawData.nodes.map((node: any, index: number) => ({
          id: node.id || `event-${index}`,
          title: node.event_description || node.event?.title || node.title || '未知事件',
          eventTime: node.event_time || node.eventTime || node.event?.eventTime,
          location: node.event_location || node.event?.location || node.location,
          nodeType: node.nodeType || 'normal',
          importanceScore: node.intensity_level || node.importanceScore || 0.5,
          description: node.event?.description || node.description
        }))
      } else if (Array.isArray(rawData)) {
        // 如果是直接的事件数组
        events = rawData.map((event: any) => ({
          id: event.id,
          title: event.event_description || event.title || '未知事件',
          eventTime: event.event_time || event.eventTime,
          location: event.event_location || event.location || '未设置',
          nodeType: event.source_type ? 'source' : (event.nodeType || 'normal'),
          importanceScore: event.intensity_level || event.importanceScore || 0.5,
          description: event.event_description || event.description
        }))
      }
      
      timelineEvents.value = events
      console.log('刷新的事件数据:', timelineEvents.value)
      console.log('原始数据结构:', rawData)
      console.log('处理后的第一个事件:', events[0])
    } else {
      throw new Error(result.message || '获取时间线事件失败')
    }
  } catch (error: any) {
    console.error('加载时间线事件失败:', error)
    ElMessage.error(error.message || '加载时间线事件失败')
  } finally {
    loadingEvents.value = false
  }
}

const handleAddEventSuccess = () => {
  ElMessage.success('事件添加成功')
  loadTimelineEvents()
  handleTimelineChange(selectedTimelineId.value) // 刷新时间线信息
}

const handleSelectEventSuccess = () => {
  ElMessage.success('事件添加成功')
  loadTimelineEvents()
  handleTimelineChange(selectedTimelineId.value) // 刷新时间线信息
}



const viewEventDetail = (event: any) => {
  // 获取原始事件数据以显示更完整的信息
  const originalEvent = timelineEvents.value.find(e => e.id === event.id)
  if (originalEvent) {
    selectedEventDetail.value = {
      ...originalEvent,
      // 添加一些可能存在的原始字段
      sourceType: event.source_type,
      intensityLevel: event.intensity_level,
      relationCreatedAt: event.relation_created_at
    }
    showEventDetailDialog.value = true
  } else {
    ElMessage.error('无法找到事件详情')
  }
}

const removeEventFromTimeline = async (event: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要从时间线中移除事件"${event.title?.substring(0, 50)}${event.title?.length > 50 ? '...' : ''}"吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const response = await fetch(`/api/timelines/${selectedTimelineId.value}/events/${event.id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    })

    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 200) {
      ElMessage.success('事件已从时间线中移除')
      loadTimelineEvents()
      handleTimelineChange(selectedTimelineId.value) // 刷新时间线信息
    } else {
      throw new Error(result.message || '移除事件失败')
    }
  } catch (error: any) {
    if (error.message !== 'cancel') {
      console.error('移除事件失败:', error)
      ElMessage.error(error.message || '移除事件失败')
    }
  }
}

// 工具方法
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '未设置'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return dateStr
  }
}






const getNodeTypeTagType = (nodeType: string) => {
  const typeMap: { [key: string]: string } = {
    'source': 'success',
    'terminal': 'danger',
    'hub': 'warning',
    'hot': 'danger',
    'normal': 'info'
  }
  return typeMap[nodeType] || 'info'
}

const getNodeTypeText = (nodeType: string) => {
  const textMap: { [key: string]: string } = {
    'source': '源事件',
    'terminal': '终端事件',
    'hub': '枢纽事件',
    'hot': '热点事件',
    'normal': '普通事件'
  }
  return textMap[nodeType] || '普通事件'
}

const getImportanceColor = (score: number) => {
  if (score >= 0.8) return '#f56c6c' // 红色 - 高重要性
  if (score >= 0.6) return '#e6a23c' // 橙色 - 中高重要性
  if (score >= 0.4) return '#409eff' // 蓝色 - 中等重要性
  if (score >= 0.2) return '#67c23a' // 绿色 - 中低重要性
  return '#909399' // 灰色 - 低重要性
}

const editEvent = () => {
  // 编辑事件功能待实现
  ElMessage.info('编辑事件功能待实现')
}

// 生命周期
onMounted(() => {
  loadTimelines()
})
</script>

<style scoped>
.timeline-event-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.main-content {
  max-width: 1200px;
}

.timeline-selector-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.timeline-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.timeline-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.timeline-title {
  font-weight: 500;
}

.timeline-info {
  color: #909399;
  font-size: 12px;
}

.current-timeline-info {
  margin-top: 16px;
}



.event-management-section {
  margin-bottom: 24px;
}

.tab-content {
  padding: 20px 0;
}

.events-list-card {
  margin-top: 24px;
}

.no-events {
  padding: 40px 20px;
  text-align: center;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

:deep(.el-card__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}

/* 事件详情对话框样式 */
.event-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.detail-time,
.detail-location {
  display: flex;
  align-items: center;
  gap: 8px;
}

.importance-score {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-text {
  font-weight: 500;
  color: #606266;
}

.event-description {
  line-height: 1.6;
  color: #303133;
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.extra-info {
  margin-top: 20px;
}

.extra-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  margin: 0;
  min-width: 60px;
}

/* 表格操作列居中对齐 */
:deep(.el-table .el-table__cell) {
  padding: 8px 0;
}

:deep(.el-table th.el-table__cell:last-child) {
  text-align: center;
}

:deep(.el-table td.el-table__cell:last-child) {
  text-align: center;
}
</style>