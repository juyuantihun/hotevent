<template>
  <div class="timeline-list-container">
    <!-- 搜索和过滤区域 -->
    <div class="search-filter-container">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent="handleSearch">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="关键词">
              <el-input 
                v-model="searchForm.keyword" 
                placeholder="搜索时间线标题或描述" 
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select 
                v-model="searchForm.statuses" 
                placeholder="选择状态" 
                multiple 
                collapse-tags
                clearable
              >
                <el-option 
                  v-for="item in statusOptions" 
                  :key="item.value" 
                  :label="item.label" 
                  :value="item.value" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事件数量">
              <el-row :gutter="10">
                <el-col :span="11">
                  <el-input-number 
                    v-model="searchForm.minEventCount" 
                    :min="0" 
                    placeholder="最小" 
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-col>
                <el-col :span="2" class="text-center">-</el-col>
                <el-col :span="11">
                  <el-input-number 
                    v-model="searchForm.maxEventCount" 
                    :min="0" 
                    placeholder="最大" 
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-col>
              </el-row>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="排序">
              <el-select v-model="searchForm.sort" placeholder="排序字段">
                <el-option label="创建时间" value="createdAt" />
                <el-option label="更新时间" value="updatedAt" />
                <el-option label="事件数量" value="eventCount" />
                <el-option label="标题" value="title" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序方向">
              <el-select v-model="searchForm.direction" placeholder="排序方向">
                <el-option label="降序" value="desc" />
                <el-option label="升序" value="asc" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8" class="search-buttons">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 时间线列表 -->
    <div class="timeline-content" v-loading="loading">
      <!-- 错误提示 -->
      <el-alert
        v-if="error"
        :title="error"
        type="error"
        :closable="true"
        show-icon
        @close="error = null"
      />
      
      <!-- 空状态提示 -->
      <el-empty
        v-if="!loading && timelineList.length === 0"
        description="没有找到符合条件的时间线"
      >
        <el-button type="primary" @click="$emit('create')">
          创建时间线
        </el-button>
      </el-empty>
      
      <!-- 时间线列表 -->
      <div v-else class="timeline-grid">
        <el-card 
          v-for="timeline in timelineList" 
          :key="timeline.id" 
          class="timeline-card"
          shadow="hover"
        >
          <!-- 添加一个透明的覆盖层来捕获点击事件 -->
          <div class="card-overlay" @click="$emit('view', timeline)"></div>
          <template #header>
            <div class="card-header">
              <h3 class="timeline-title">{{ timeline.title }}</h3>
              <div class="card-actions">
                <el-tag :type="getTimelineStatusType(timeline.status)">
                  {{ getTimelineStatusText(timeline.status) }}
                </el-tag>
                <el-dropdown trigger="click" @command="handleCommand($event, timeline)">
                  <el-button text icon="MoreFilled" @click.stop />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="view">查看详情</el-dropdown-item>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="duplicate">复制</el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>
          
          <div class="timeline-info">
            <div class="timeline-stats" @click.stop="$emit('view', timeline)">
              <div class="stat-item">
                <span class="stat-label">事件数量</span>
                <span class="stat-value">{{ timeline.eventCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">关系数量</span>
                <span class="stat-value">{{ timeline.relationCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">时间跨度</span>
                <span class="stat-value">{{ formatTimeSpan(timeline.timeSpan) }}</span>
              </div>
            </div>
            
            <div class="timeline-regions" v-if="timeline.regions && timeline.regions.length > 0" @click.stop="$emit('view', timeline)">
              <span class="region-label">相关地区:</span>
              <el-tag 
                v-for="region in timeline.regions.slice(0, 3)" 
                :key="region.id" 
                size="small" 
                class="region-tag"
              >
                {{ region.name }}
              </el-tag>
              <el-tag v-if="timeline.regions.length > 3" size="small" type="info">
                +{{ timeline.regions.length - 3 }}
              </el-tag>
            </div>
            
            <div class="timeline-dates" @click.stop="$emit('view', timeline)">
              <div class="date-item">
                <span class="date-label">创建时间:</span>
                <span class="date-value">{{ formatDate(timeline.createdAt) }}</span>
              </div>
              <div class="date-item">
                <span class="date-label">更新时间:</span>
                <span class="date-value">{{ formatDate(timeline.updatedAt) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="timelineList.length > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { optimizedTimelineApi } from '@/api/optimizedApi'

// 类型定义
interface TimelineItem {
  id: string
  title: string
  status: string
  eventCount: number
  relationCount: number
  timeSpan: string
  createdAt: string
  updatedAt: string
  regions?: Array<{
    id: string
    name: string
  }>
}

// 搜索表单类型
interface SearchFormType {
  keyword: string
  statuses: string[]
  minEventCount: number | null
  maxEventCount: number | null
  sort: string
  direction: string
}

// 分页类型
interface PaginationType {
  page: number
  size: number
  total: number
}

// 定义组件属性
const props = withDefaults(defineProps<{
  initialKeyword?: string
}>(), {
  initialKeyword: ''
})

// 定义组件事件
const emit = defineEmits(['view', 'edit', 'duplicate', 'delete', 'create'])

// 响应式数据
const loading = ref(false)
const timelineList = ref<TimelineItem[]>([])
const error = ref<string | null>(null)

// 搜索表单
const searchForm = reactive<SearchFormType>({
  keyword: props.initialKeyword,
  statuses: [],
  minEventCount: null,
  maxEventCount: null,
  sort: 'createdAt',
  direction: 'desc'
})

// 状态选项
const statusOptions = [
  { value: 'COMPLETED', label: '已完成' },
  { value: 'PROCESSING', label: '处理中' },
  { value: 'FAILED', label: '失败' },
  { value: 'DRAFT', label: '草稿' }
]

// 分页数据
const pagination = reactive<PaginationType>({
  page: 1,
  size: 10,
  total: 0
})

// 监听初始关键词变化
watch(() => props.initialKeyword, (newValue) => {
  if (newValue !== searchForm.keyword) {
    searchForm.keyword = newValue
    handleSearch()
  }
})

// 生命周期钩子
onMounted(() => {
  loadTimelineList()
})

/**
 * 加载时间线列表
 */
const loadTimelineList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      page: Math.max(0, pagination.page - 1), // 后端从0开始计数
      size: pagination.size,
      sort: searchForm.sort || 'createdAt',
      direction: searchForm.direction || 'desc'
    }

    // 添加搜索条件
    if (searchForm.keyword && searchForm.keyword.trim()) {
      params.keyword = searchForm.keyword.trim()
    }
    
    if (searchForm.statuses.length > 0) {
      params.statuses = searchForm.statuses.join(',')
    }
    
    if (searchForm.minEventCount !== null && searchForm.minEventCount >= 0) {
      params.minEventCount = Math.floor(searchForm.minEventCount)
    }
    
    if (searchForm.maxEventCount !== null && searchForm.maxEventCount >= 0) {
      params.maxEventCount = Math.floor(searchForm.maxEventCount)
    }

    // 调用API
    const response = await optimizedTimelineApi.getTimelineList(params)
    
    // 处理响应数据
    if (response) {
      const content = response.content || []
      const total = response.totalElements || content.length || 0
      
      timelineList.value = content
      pagination.total = total
      
      if (content.length === 0) {
        ElMessage.info('没有找到符合条件的时间线')
      }
    } else {
      throw new Error('获取时间线列表失败')
    }
  } catch (error) {
    console.error('加载时间线列表失败:', error)
    
    let errorMessage = '加载时间线列表失败'
    if (error instanceof Error) {
      errorMessage = error.message
    }
    
    error.value = errorMessage
    ElMessage.error(errorMessage)
  } finally {
    loading.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  pagination.page = 1
  loadTimelineList()
}

/**
 * 重置搜索条件
 */
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.statuses = []
  searchForm.minEventCount = null
  searchForm.maxEventCount = null
  searchForm.sort = 'createdAt'
  searchForm.direction = 'desc'
  
  pagination.page = 1
  
  loadTimelineList()
}

/**
 * 处理每页数量变化
 */
const handleSizeChange = (size: number) => {
  pagination.size = size
  loadTimelineList()
}

/**
 * 处理页码变化
 */
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadTimelineList()
}

/**
 * 处理下拉菜单命令
 */
const handleCommand = (command: string, timeline: TimelineItem) => {
  switch (command) {
    case 'view':
      emit('view', timeline)
      break
    case 'edit':
      emit('edit', timeline)
      break
    case 'duplicate':
      emit('duplicate', timeline)
      break
    case 'delete':
      confirmDelete(timeline)
      break
  }
}

/**
 * 确认删除
 */
const confirmDelete = (timeline: TimelineItem) => {
  ElMessageBox.confirm(
    `确定要删除时间线 "${timeline.title}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      // 直接调用API删除时间线
      await optimizedTimelineApi.deleteTimeline(timeline.id)
      
      // 发出删除事件
      emit('delete', timeline)
      
      // 从本地列表中移除该时间线
      const index = timelineList.value.findIndex(item => item.id === timeline.id)
      if (index !== -1) {
        timelineList.value.splice(index, 1)
      }
      
      ElMessage.success('删除成功')
    } catch (error) {
      console.error('删除时间线失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消删除，不做任何操作
  })
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
 * 格式化时间跨度
 */
const formatTimeSpan = (timeSpan: string) => {
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
    return timeSpan
  }
}

/**
 * 格式化日期
 */
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  
  try {
    const date = new Date(dateStr)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      return dateStr
    }
    
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('格式化日期失败:', error)
    return dateStr
  }
}
</script>

<style scoped>
.timeline-list-container {
  width: 100%;
}

.search-filter-container {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-form {
  width: 100%;
}

.search-buttons {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.text-center {
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.timeline-content {
  min-height: 300px;
}

.timeline-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.timeline-card {
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.timeline-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* 添加一个覆盖层，确保整个卡片都能响应点击 */
.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0; /* 降低z-index，确保不会阻止其他交互元素 */
  cursor: pointer;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.timeline-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 10px;
  margin-top: 10px;
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
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.timeline-regions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-top: 5px;
}

.region-label {
  font-size: 12px;
  color: #909399;
}

.region-tag {
  margin-right: 5px;
}

.timeline-dates {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f2f5;
}

.date-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.date-label {
  color: #909399;
}

.date-value {
  color: #606266;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>