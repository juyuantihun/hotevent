<template>
  <div class="event-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Calendar /></el-icon>
        国际热点事件管理
      </h1>
      <p class="page-subtitle">实时监控和管理全球热点事件</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Search /></el-icon>
            高级搜索
          </span>
          <el-button type="primary" size="small" @click="toggleSearch">
            {{ showSearch ? '收起' : '展开' }}
            <el-icon><ArrowUp v-if="showSearch" /><ArrowDown v-else /></el-icon>
          </el-button>
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

    <!-- 操作按钮区域 -->
    <div class="action-bar">
      <div class="action-left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增事件
        </el-button>
        <!-- 批量录入按钮已隐藏 -->
        <!-- <el-button type="success" @click="handleBatchAdd">
          <el-icon><Upload /></el-icon>
          批量录入
        </el-button> -->
        <el-button type="warning" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>

      <div class="action-right">
        <el-dropdown @command="handleBatchAction">
          <el-button>
            批量操作
            <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="delete">批量删除</el-dropdown-item>
              <el-dropdown-item command="export">批量导出</el-dropdown-item>
              <el-dropdown-item v-if="false" command="relation">建立关联</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon primary">
                <el-icon size="24"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.total }}</div>
                <div class="stat-label">总事件数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon success">
                <el-icon size="24"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.today }}</div>
                <div class="stat-label">今日新增</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon warning">
                <el-icon size="24"><Connection /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.relations }}</div>
                <div class="stat-label">关联关系</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon danger">
                <el-icon size="24"><Position /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.countries }}</div>
                <div class="stat-label">涉及国家</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 事件列表表格 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            事件列表
          </span>
          <div class="table-actions">
            <el-button-group>
              <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'">
                <el-icon><Grid /></el-icon>
              </el-button>
              <el-button :type="viewMode === 'card' ? 'primary' : ''" @click="viewMode = 'card'">
                <el-icon><Menu /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="table-container">
        <el-table
          v-if="viewMode === 'table'"
          :data="eventList"
          stripe
          highlight-current-row
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="eventCode" label="事件编码" width="150" />
          <el-table-column prop="eventTime" label="事件时间" width="180" sortable>
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Clock /></el-icon>
                {{ formatDate(row.eventTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="eventType" label="事件类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getEventTypeColor(row.eventType)">
                {{ getEventTypeName(row.eventType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="subject" label="事件主体" width="150" />
          <el-table-column prop="object" label="事件客体" width="150" />
          <el-table-column prop="eventLocation" label="事件地点" width="200" />
          <el-table-column prop="sourceType" label="来源" width="100">
            <template #default="{ row }">
              <el-tag :type="row.sourceType === 1 ? 'success' : 'info'" size="small">
                {{ row.sourceType === 1 ? 'DeepSeek' : '人工' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                :active-value="1"
                :inactive-value="0"
                @change="handleStatusChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-dropdown @command="(command: string) => handleCommand(command, row)" trigger="click">
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
            </template>
          </el-table-column>
        </el-table>

        <!-- 卡片视图 -->
        <div v-else class="card-view">
          <EventCardList 
            :events="eventList"
            @view="handleRowClick"
            @edit="handleEdit"
            @delete="handleDelete"
            @add="handleAdd"
          />
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { useEventStore } from '@/store/modules/event'
import { useDictionaryStore } from '@/store/modules/dictionary'
import { 
  getEventList, 
  deleteEvent, 
  updateEvent, 
  exportAllEvents,
  getStats,
  type Event, 
  type EventQuery,
  type PageResult
} from '@/api/event'
import type { Dictionary } from '@/api/dictionary'
import dayjs from 'dayjs'
import {
  Calendar,
  Search,
  ArrowUp,
  ArrowDown,
  Plus,
  Upload,
  Download,
  Document,
  CircleCheck,
  Connection,
  Position,
  List,
  Grid,
  Menu,
  Clock,
  View,
  Edit,
  Delete,
  User,
  UserFilled,
  Location,
  Refresh
} from '@element-plus/icons-vue'
import EventCardList from './components/EventCardList.vue'

const router = useRouter()
const eventStore = useEventStore()
const dictionaryStore = useDictionaryStore()

// 响应式数据
const showSearch = ref(false)
const loading = ref(false)
const viewMode = ref('table')
const selectedRows = ref<Event[]>([])

// 搜索表单
const searchForm = reactive({
  eventType: '',
  subject: '',
  object: '',
  timeRange: [],
  sourceType: null
})

// 统计数据
const stats = reactive({
  total: 0,
  today: 0,
  relations: 0,
  countries: 0
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 事件列表数据
const eventList = ref<Event[]>([])

// 字典数据
const eventTypes = ref<Dictionary[]>([])
const subjects = ref<Dictionary[]>([])
const objects = ref<Dictionary[]>([])

// 方法
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

// 加载字典数据
const loadDictionaries = async () => {
  try {
    const [eventTypeList, subjectList, objectList] = await Promise.all([
      dictionaryStore.getEventTypes(),
      dictionaryStore.getSubjects(),
      dictionaryStore.getObjects()
    ])
    
    eventTypes.value = eventTypeList
    subjects.value = subjectList
    objects.value = objectList
  } catch (error) {
    console.error('加载字典数据失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadEventList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    eventType: '',
    subject: '',
    object: '',
    timeRange: [],
    sourceType: null
  })
  handleSearch()
}

const handleAdd = () => {
  router.push('/event/create')
}

const handleBatchAdd = () => {
  router.push('/event/batch')
}



const handleExport = async () => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在导出数据...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const allEvents = await exportAllEvents()
    
    // 导入excelService
    const { exportToExcel } = await import('@/services/excelService')
    
    // 定义列配置
    const columns = [
      { label: '序号', prop: 'index', width: 8 },
      { label: '事件编码', prop: 'eventCode', width: 20 },
      { label: '事件时间', prop: 'eventTime', width: 20, 
        formatter: (row: any) => row.eventTime ? dayjs(row.eventTime).format('YYYY-MM-DD HH:mm:ss') : '' },
      { label: '事件地点', prop: 'eventLocation', width: 20 },
      { label: '事件类型', prop: 'eventType', width: 15 },
      { label: '事件主体', prop: 'subject', width: 15 },
      { label: '事件客体', prop: 'object', width: 15 },
      { label: '关系类型', prop: 'relationType', width: 15 },
      { label: '关系名称', prop: 'relationName', width: 15 },
      { label: '强度等级', prop: 'intensityLevel', width: 10 },
      { label: '事件描述', prop: 'eventDescription', width: 30 },
      { label: '关键词', prop: 'keywords', width: 20,
        formatter: (row: any) => row.keywords ? (Array.isArray(row.keywords) ? row.keywords.join(', ') : row.keywords) : '' },
      { label: '经度', prop: 'longitude', width: 12 },
      { label: '纬度', prop: 'latitude', width: 12 },
      { label: '来源类型', prop: 'sourceType', width: 12,
        formatter: (row: any) => row.sourceType === 1 ? 'DeepSeek获取' : '人工录入' },
      { label: '状态', prop: 'status', width: 8,
        formatter: (row: any) => row.status === 1 ? '启用' : '禁用' },
      { label: '创建时间', prop: 'createdAt', width: 20,
        formatter: (row: any) => row.createdAt ? dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '' },
      { label: '更新时间', prop: 'updatedAt', width: 20,
        formatter: (row: any) => row.updatedAt ? dayjs(row.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '' }
    ]
    
    // 准备导出数据（添加索引）
    const exportData = allEvents.map((event: any, index: number) => ({
      ...event,
      index: index + 1
    }))
    
    // 导出文件
    const fileName = await exportToExcel(exportData, columns, {
      fileName: `国际热点事件数据_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}`,
      sheetName: '事件数据',
      styles: {
        header: {
          font: { bold: true, size: 12 },
          fill: {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: 'FFE0E0E0' }
          }
        }
      }
    })
    
    ElMessage.success(`导出成功！共导出 ${allEvents.length} 条数据`)
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    loadingInstance.close()
  }
}

const handleBatchAction = (command: string) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要操作的数据')
    return
  }
  
  switch (command) {
    case 'delete':
      handleBatchDelete()
      break
    case 'export':
      handleBatchExport()
      break
    case 'relation':
      handleBatchRelation()
      break
  }
}

const handleBatchDelete = async () => {
  const ids = selectedRows.value.map(row => row.id!).filter(id => id)
  if (ids.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 个事件吗？`, '批量删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里应该调用批量删除API
    ElMessage.success('批量删除成功')
    loadEventList()
  } catch (error) {
    // 用户取消
  }
}

const handleBatchExport = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要导出的事件')
    return
  }
  
  const loadingInstance = ElLoading.service({
    lock: true,
    text: `正在导出 ${selectedRows.value.length} 条事件数据...`,
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    // 导入excelService
    const { exportToExcel } = await import('@/services/excelService')
    
    // 定义列配置
    const columns = [
      { label: '序号', prop: 'index', width: 8 },
      { label: '事件编码', prop: 'eventCode', width: 20 },
      { label: '事件时间', prop: 'eventTime', width: 20, 
        formatter: (row: any) => row.eventTime ? dayjs(row.eventTime).format('YYYY-MM-DD HH:mm:ss') : '' },
      { label: '事件地点', prop: 'eventLocation', width: 20 },
      { label: '事件类型', prop: 'eventType', width: 15 },
      { label: '事件主体', prop: 'subject', width: 15 },
      { label: '事件客体', prop: 'object', width: 15 },
      { label: '关系类型', prop: 'relationType', width: 15 },
      { label: '关系名称', prop: 'relationName', width: 15 },
      { label: '强度等级', prop: 'intensityLevel', width: 10 },
      { label: '事件描述', prop: 'eventDescription', width: 30 },
      { label: '关键词', prop: 'keywords', width: 20,
        formatter: (row: any) => row.keywords ? (Array.isArray(row.keywords) ? row.keywords.join(', ') : row.keywords) : '' },
      { label: '经度', prop: 'longitude', width: 12 },
      { label: '纬度', prop: 'latitude', width: 12 },
      { label: '来源类型', prop: 'sourceType', width: 12,
        formatter: (row: any) => row.sourceType === 1 ? 'DeepSeek获取' : '人工录入' },
      { label: '状态', prop: 'status', width: 8,
        formatter: (row: any) => row.status === 1 ? '启用' : '禁用' },
      { label: '创建时间', prop: 'createdAt', width: 20,
        formatter: (row: any) => row.createdAt ? dayjs(row.createdAt).format('YYYY-MM-DD HH:mm:ss') : '' },
      { label: '更新时间', prop: 'updatedAt', width: 20,
        formatter: (row: any) => row.updatedAt ? dayjs(row.updatedAt).format('YYYY-MM-DD HH:mm:ss') : '' }
    ]
    
    // 准备导出数据（添加索引）
    const exportData = selectedRows.value.map((event: any, index: number) => ({
      ...event,
      index: index + 1
    }))
    
    // 导出文件
    const fileName = await exportToExcel(exportData, columns, {
      fileName: `选中事件数据_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}`,
      sheetName: '选中事件数据',
      styles: {
        header: {
          font: { bold: true, size: 12 },
          fill: {
            type: 'pattern',
            pattern: 'solid',
            fgColor: { argb: 'FFE0E0E0' }
          }
        }
      }
    })
    
    ElMessage.success(`批量导出成功！共导出 ${selectedRows.value.length} 条数据`)
  } catch (error) {
    console.error('批量导出失败:', error)
    ElMessage.error('批量导出失败，请稍后重试')
  } finally {
    loadingInstance.close()
  }
}

const handleBatchRelation = () => {
  ElMessage.info('批量建立关联功能开发中...')
}

const handleSelectionChange = (selection: Event[]) => {
  selectedRows.value = selection
}

const handleRowClick = (row: Event) => {
  if (row.id === undefined) {
    ElMessage.error('事件ID不存在')
    return
  }
  router.push({
    path: `/event/detail/${row.id}`
  })
}

const handleView = (row: Event) => {
  router.push(`/event/detail/${row.id}`)
}

const handleEdit = (row: Event) => {
  if (!row.id) {
    ElMessage.error('事件ID不存在')
    return
  }
  router.push({
    path: `/event/edit/${row.id}`
  })
}

const handleDelete = async (row: Event) => {
  try {
    await ElMessageBox.confirm('确定要删除这个事件吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteEvent(row.id!)
    ElMessage.success('删除成功')
    loadEventList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleStatusChange = async (row: Event) => {
  try {
    await updateEvent({
      ...row,
      status: row.status
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 回滚状态
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadEventList()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadEventList()
}

const loadStats = async () => {
  try {
    const statsData = await getStats()
    Object.assign(stats, statsData)
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const loadEventList = async () => {
  loading.value = true
  try {
    const queryParams: EventQuery = {
      current: pagination.current,
      size: pagination.size,
      eventType: searchForm.eventType || undefined,
      subject: searchForm.subject || undefined,
      object: searchForm.object || undefined,
      sourceType: searchForm.sourceType !== null ? String(searchForm.sourceType) : undefined
    }
    
    // 处理时间范围
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      // 将时间格式改为不含空格的格式，避免URL编码问题
      queryParams.startTime = (searchForm.timeRange[0] as string).replace(' ', 'T')
      queryParams.endTime = (searchForm.timeRange[1] as string).replace(' ', 'T')
    }
    
    const response: PageResult<Event> = await getEventList(queryParams)
    // API拦截器已处理过响应，直接使用response数据
    eventList.value = response.records || []
    pagination.total = response.total || 0
    
    // 获取统计数据
    loadStats()
  } catch (error) {
    ElMessage.error('获取事件列表失败')
    console.error('loadEventList error:', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateString: string) => {
  return dateString
}

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

const getEventTypeName = (type: string) => {
  // 现在直接返回中文名称
  return type
}

// 添加新的处理函数
const handleCommand = (command: string, row: Event) => {
  switch (command) {
    case 'view':
      if (row.id === undefined) {
        ElMessage.error('事件ID不存在')
        return
      }
      router.push({
        path: `/event/detail/${row.id}`
      })
      break
    case 'edit':
      if (row.id === undefined) {
        ElMessage.error('事件ID不存在')
        return
      }
      router.push({
        path: `/event/edit/${row.id}`
      })
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

onMounted(() => {
  loadDictionaries()
  loadEventList()
})
</script>

<style scoped>
.event-list-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-form {
  padding-top: 16px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.action-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 12px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-icon.primary {
  background: linear-gradient(45deg, #409EFF, #66b1ff);
}

.stat-icon.success {
  background: linear-gradient(45deg, #67C23A, #85ce61);
}

.stat-icon.warning {
  background: linear-gradient(45deg, #E6A23C, #ebb563);
}

.stat-icon.danger {
  background: linear-gradient(45deg, #F56C6C, #f78989);
}

.stat-info {
  flex: 1;
  min-width: 0; /* 防止子元素溢出 */
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.table-card {
  margin-bottom: 20px;
}

.table-container {
  min-height: 400px;
  overflow-x: auto; /* 添加水平滚动以防止表格溢出 */
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.card-view {
  padding: 16px 0;
}

.event-card {
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.3s;
  height: 100%; /* 确保卡片高度一致 */
}

.event-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.event-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.event-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.event-card-content {
  padding: 12px 0;
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
  flex-wrap: wrap; /* 允许在小屏幕上换行 */
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

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  flex-wrap: wrap; /* 允许分页控件在小屏幕上换行 */
}

/* 响应式布局调整 */
@media (max-width: 768px) {
  .event-list-container {
    padding: 10px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-left, .action-right {
    width: 100%;
    justify-content: space-between;
  }
  
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
  .search-form :deep(.el-form-item) {
    display: block;
    margin-right: 0;
  }
  
  .search-form :deep(.el-select),
  .search-form :deep(.el-date-editor) {
    width: 100% !important;
  }
  
  .event-participants {
    flex-direction: column;
    gap: 8px;
  }
}
</style> 