<template>
  <div class="select-existing-events">
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Search /></el-icon>
            事件搜索与筛选
          </span>
          <div class="header-actions">
            <el-checkbox v-model="includeAssociated" @change="handleSearch">
              显示已关联事件
            </el-checkbox>
            <el-button type="primary" size="small" @click="toggleSearch">
              {{ showSearch ? '收起' : '展开' }}
              <el-icon><ArrowUp v-if="showSearch" /><ArrowDown v-else /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div v-show="showSearch" class="search-form">
        <el-form :model="searchForm" :inline="true" size="default">
          <el-form-item label="事件类型">
            <el-select v-model="searchForm.eventType" placeholder="请选择事件类型" clearable style="width: 180px">
              <el-option
                v-for="item in eventTypes"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="事件主体">
            <el-select v-model="searchForm.subject" placeholder="请选择事件主体" clearable style="width: 180px">
              <el-option
                v-for="item in subjects"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="事件客体">
            <el-select v-model="searchForm.object" placeholder="请选择事件客体" clearable style="width: 180px">
              <el-option
                v-for="item in objects"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 350px"
            />
          </el-form-item>

          <el-form-item label="来源类型">
            <el-select v-model="searchForm.sourceType" placeholder="请选择来源类型" clearable style="width: 180px">
              <el-option label="DeepSeek获取" :value="1" />
              <el-option label="人工录入" :value="2" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 事件列表 -->
    <el-card class="events-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">事件列表</span>
          <div class="header-info">
            <span class="event-count">共 {{ totalCount }} 个事件</span>
            <span class="selected-count">已选择 {{ selectedEvents.length }} 个</span>
          </div>
        </div>
      </template>

      <el-table
        :data="events"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        :row-class-name="getRowClassName"
        height="500"
        stripe
      >
        <el-table-column type="selection" width="55" :selectable="isEventSelectable" />
        
        <el-table-column prop="event_time" label="事件时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.event_time || row.eventTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="event_location" label="事件地点" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.event_location || row.eventLocation || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="event_type" label="事件类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.event_type || row.eventType || '未设置' }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="subject" label="事件主体" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.subject || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="object" label="事件客体" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.object || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="relation_type" label="关系类型" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.relation_type || row.relationType || '未设置' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="intensity_level" label="强度等级" width="100">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.round((row.intensity_level || row.intensityLevel || 0.5) * 100)"
              :color="getIntensityColor(row.intensity_level || row.intensityLevel || 0.5)"
              :show-text="false"
              :stroke-width="6"
            />
            <span class="intensity-text">{{ ((row.intensity_level || row.intensityLevel || 0.5) * 100).toFixed(0) }}%</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="event_description" label="事件描述" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.event_description || row.eventDescription || '无描述' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="source_type" label="来源" width="100">
          <template #default="{ row }">
            <el-tag :type="(row.source_type || row.sourceType) === 1 ? 'success' : 'warning'" size="small">
              {{ (row.source_type || row.sourceType) === 1 ? 'AI获取' : '人工录入' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="关联状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="isEventAssociated(row.id)" type="success" size="small">
              已关联
            </el-tag>
            <el-tag v-else type="info" size="small">
              可选择
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="totalCount"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <div class="selection-info">
        <span>已选择 {{ selectedEvents.length }} 个事件</span>
        <el-button v-if="selectedEvents.length > 0" text @click="clearSelection">
          清空选择
        </el-button>
      </div>
      <div class="action-buttons">
        <el-button
          type="primary"
          @click="handleAddSelected"
          :loading="adding"
          :disabled="selectedEvents.length === 0"
        >
          <el-icon><Plus /></el-icon>
          添加选中事件到时间线 ({{ selectedEvents.length }})
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, ArrowUp, ArrowDown, Refresh, Plus } from '@element-plus/icons-vue'
import { useDictionaryStore } from '@/store/modules/dictionary'

// Props
interface Props {
  timelineId?: string | number
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  success: []
}>()

// 字典存储
const dictionaryStore = useDictionaryStore()

// 响应式数据
const loading = ref(false)
const adding = ref(false)
const showSearch = ref(true)
const includeAssociated = ref(false)
const events = ref<any[]>([])
const selectedEvents = ref<any[]>([])
const associatedEventIds = ref<Set<string>>(new Set())
const totalCount = ref(0)

// 字典数据
const eventTypes = ref<any[]>([])
const subjects = ref<any[]>([])
const objects = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  eventType: '',
  subject: '',
  object: '',
  timeRange: [] as string[],
  sourceType: null as number | null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 方法
const loadDictionaryData = async () => {
  try {
    // 使用字典存储获取数据，与手动添加事件组件保持一致
    const [eventTypeList, subjectList, objectList] = await Promise.all([
      dictionaryStore.getEventTypes(),
      dictionaryStore.getSubjects(),
      dictionaryStore.getObjects()
    ])

    eventTypes.value = eventTypeList
    subjects.value = subjectList
    objects.value = objectList
    
    console.log('字典数据加载成功:', {
      eventTypes: eventTypes.value.length,
      subjects: subjects.value.length,
      objects: objects.value.length
    })
  } catch (error) {
    console.error('加载字典数据失败:', error)
    ElMessage.error('加载字典数据失败')
  }
}

const loadAssociatedEvents = async () => {
  if (!props.timelineId) return

  try {
    const response = await fetch(`/api/timelines/${props.timelineId}/events`)
    if (response.ok) {
      const result = await response.json()
      if (result.code === 200) {
        // 处理不同的数据结构
        const rawData = result.data
        let associatedEvents = []
        
        if (rawData.events && Array.isArray(rawData.events)) {
          associatedEvents = rawData.events
        } else if (rawData.nodes && Array.isArray(rawData.nodes)) {
          associatedEvents = rawData.nodes
        } else if (Array.isArray(rawData)) {
          associatedEvents = rawData
        }
        
        associatedEventIds.value = new Set(associatedEvents.map((event: any) => event.id?.toString()))
        console.log('已关联事件ID列表:', Array.from(associatedEventIds.value))
      }
    }
  } catch (error) {
    console.error('加载已关联事件失败:', error)
  }
}

const loadEvents = async () => {
  if (!props.timelineId) {
    ElMessage.error('缺少时间线ID')
    return
  }

  loading.value = true
  try {
    // 构建查询参数
    const params = new URLSearchParams({
      page: pagination.page.toString(),
      size: pagination.size.toString()
    })

    if (searchForm.eventType) params.append('eventType', searchForm.eventType)
    if (searchForm.subject) params.append('subject', searchForm.subject)
    if (searchForm.object) params.append('object', searchForm.object)
    if (searchForm.sourceType !== null) params.append('sourceType', searchForm.sourceType.toString())
    
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.append('startTime', searchForm.timeRange[0])
      params.append('endTime', searchForm.timeRange[1])
    }

    // 根据是否包含已关联事件选择不同的API
    let apiUrl = ''
    if (includeAssociated.value) {
      // 如果包含已关联事件，使用原来的全量事件API
      apiUrl = `/api/events?${params.toString()}`
    } else {
      // 如果不包含已关联事件，使用新的未关联事件API
      apiUrl = `/api/timelines/${props.timelineId}/available-events?${params.toString()}`
    }

    console.log('调用API:', apiUrl)
    console.log('搜索参数:', {
      eventType: searchForm.eventType,
      subject: searchForm.subject,
      object: searchForm.object,
      sourceType: searchForm.sourceType,
      timeRange: searchForm.timeRange
    })
    const response = await fetch(apiUrl)
    
    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 200) {
      const allEvents = result.data.records || result.data.list || []
      
      events.value = allEvents
      totalCount.value = result.data.total || allEvents.length
      
      console.log(`加载事件成功: 共 ${totalCount.value} 个事件`)
      console.log('API响应数据:', result.data)
      console.log('事件列表:', allEvents)
    } else {
      console.error('API返回错误:', result)
      throw new Error(result.message || '获取事件列表失败')
    }
  } catch (error: any) {
    console.error('加载事件列表失败:', error)
    ElMessage.error(error.message || '加载事件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadEvents()
}

const handleReset = () => {
  Object.assign(searchForm, {
    eventType: '',
    subject: '',
    object: '',
    timeRange: [],
    sourceType: null
  })
  pagination.page = 1
  loadEvents()
}

const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

const handleSelectionChange = (selection: any[]) => {
  selectedEvents.value = selection
}

const clearSelection = () => {
  selectedEvents.value = []
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadEvents()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadEvents()
}

const isEventAssociated = (eventId: string | number) => {
  return associatedEventIds.value.has(eventId?.toString())
}

const isEventSelectable = (row: any) => {
  // 如果事件已关联，则不可选择
  return !isEventAssociated(row.id)
}

const getRowClassName = ({ row }: { row: any }) => {
  return isEventAssociated(row.id) ? 'associated-event' : ''
}

const handleAddSelected = async () => {
  if (selectedEvents.value.length === 0) {
    ElMessage.warning('请先选择要添加的事件')
    return
  }

  if (!props.timelineId) {
    ElMessage.error('缺少时间线ID')
    return
  }

  adding.value = true

  try {
    const promises = selectedEvents.value.map(event =>
      fetch(`/api/timelines/${props.timelineId}/events`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ eventId: event.id })
      })
    )

    const responses = await Promise.all(promises)
    const results = await Promise.all(responses.map(response => response.json()))
    
    const successCount = results.filter(result => result.code === 200).length
    const failCount = results.length - successCount

    if (successCount > 0) {
      ElMessage.success(`成功添加 ${successCount} 个事件到时间线${failCount > 0 ? `，${failCount} 个失败` : ''}`)
      
      // 更新已关联事件列表
      selectedEvents.value.forEach(event => {
        associatedEventIds.value.add(event.id?.toString())
      })
      
      clearSelection()
      emit('success')
      
      // 重新加载数据
      await loadAssociatedEvents()
      await loadEvents()
    } else {
      ElMessage.error('所有事件添加失败')
    }
  } catch (error: any) {
    console.error('添加事件到时间线失败:', error)
    ElMessage.error(error.message || '添加事件失败，请稍后重试')
  } finally {
    adding.value = false
  }
}

// 工具方法
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return dateStr
  }
}

const getIntensityColor = (intensity: number) => {
  if (intensity >= 0.8) return '#f56c6c'
  if (intensity >= 0.6) return '#e6a23c'
  if (intensity >= 0.4) return '#409eff'
  return '#67c23a'
}

// 生命周期
onMounted(async () => {
  await loadDictionaryData()
  await loadAssociatedEvents()
  await loadEvents()
})
</script>

<style scoped>
.select-existing-events {
  padding: 20px 0;
}

.search-card {
  margin-bottom: 20px;
}

.events-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.event-count {
  color: #909399;
}

.selected-count {
  color: #409eff;
  font-weight: 500;
}

.search-form {
  padding-top: 16px;
}

.intensity-text {
  font-size: 12px;
  color: #606266;
  margin-left: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #e4e7ed;
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #606266;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

:deep(.el-card__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-table__header) {
  background-color: #fafafa;
}

:deep(.el-progress-bar__outer) {
  height: 6px;
}

/* 已关联事件的行样式 */
:deep(.el-table__row) {
  &.associated-event {
    background-color: #f5f7fa;
    opacity: 0.7;
  }
}

:deep(.el-table__row.associated-event .el-checkbox) {
  opacity: 0.5;
  pointer-events: none;
}
</style>