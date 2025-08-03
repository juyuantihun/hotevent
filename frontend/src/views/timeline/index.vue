<template>
  <div class="timeline-container">
    <!-- 顶部操作栏 -->
    <div class="timeline-header">
      <div class="header-left">
        <h2>事件时间线</h2>
        <span class="subtitle">基于事件关系的智能时间线生成</span>
      </div>
      
      <div class="header-right">
        <el-button 
          type="primary" 
          icon="Plus"
          @click="showCreateDialog = true"
        >
          生成时间线
        </el-button>
        
        <el-button 
          icon="Refresh" 
          @click="refreshTimelines"
        >
          刷新
        </el-button>
        
        <el-dropdown>
          <el-button icon="Setting">
            设置 <el-icon><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="showDuplicateDialog = true">
                事件去重
              </el-dropdown-item>
              <el-dropdown-item @click="showDictionaryDialog = true">
                字典管理
              </el-dropdown-item>
              <el-dropdown-item @click="showStatisticsDialog = true">
                统计信息
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 搜索和过滤区域 -->
    <div class="search-filter-container">
      <el-form :model="searchForm" label-width="80px" class="search-form" @submit.prevent="handleSearch">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="搜索时间线名称或描述" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select v-model="searchForm.statuses" multiple placeholder="选择状态" clearable>
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="处理中" value="PROCESSING" />
                <el-option label="失败" value="FAILED" />
                <el-option label="草稿" value="DRAFT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="事件数量">
              <el-input-number v-model="searchForm.minEventCount" :min="0" placeholder="最小" class="event-count-input" />
              <span class="separator">-</span>
              <el-input-number v-model="searchForm.maxEventCount" :min="0" placeholder="最大" class="event-count-input" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="排序字段">
              <el-select v-model="searchForm.sort">
                <el-option label="创建时间" value="createdAt" />
                <el-option label="更新时间" value="updatedAt" />
                <el-option label="事件数量" value="eventCount" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序方向">
              <el-select v-model="searchForm.direction">
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
        :closable="false"
        show-icon
      />
      
      <!-- 空状态 -->
      <el-empty
        v-if="!loading && timelineList.length === 0"
        description="暂无时间线数据"
      >
        <el-button type="primary" @click="showCreateDialog = true">
          创建一个时间线
        </el-button>
      </el-empty>
      
      <!-- 时间线列表 -->
      <div class="timeline-cards" v-if="!loading && timelineList.length > 0">
        <div class="timeline-card" v-for="item in timelineList" :key="item.id">
          <div class="card-header">
            <h3>{{ item.title }}</h3>
            <div class="card-actions">
              <el-tag :type="getTimelineStatusType(item.status)">
                {{ getTimelineStatusText(item.status) }}
              </el-tag>
              <el-dropdown trigger="click">
                <el-button icon="More" size="small"></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="editTimeline(item)">
                      编辑
                    </el-dropdown-item>
                    <el-dropdown-item @click="duplicateTimeline(item)">
                      复制
                    </el-dropdown-item>
                    <el-dropdown-item @click="deleteTimeline(item)">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="card-body" @click="viewTimeline(item)">
            <div class="stats">
              <div class="stat-item">
                <span class="stat-label">事件数量</span>
                <span class="stat-value">{{ item.eventCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">关系数量</span>
                <span class="stat-value">{{ item.relationCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">时间跨度</span>
                <span class="stat-value">{{ item.timeSpan }}</span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <span class="create-time">
              创建时间: {{ formatDate(item.createdAt) }}
            </span>
            <span class="update-time">
              更新时间: {{ formatDate(item.updatedAt) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container" v-if="!loading && timelineList.length > 0">
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

    <!-- 创建时间线对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="生成时间线"
      width="700px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <!-- 生成进度显示 -->
      <ProgressMonitor
        v-if="generationProgress.isGenerating"
        :title="progressMonitorTitle"
        :progress="generationProgress.progress"
        :status="generationProgress.status"
        :current-step="generationProgress.currentStep"
        :message="generationProgress.message"
        :start-time="generationProgress.startTime?.getTime() || 0"
        :elapsed-time="generationProgress.duration"
        :stats="progressStats"
        :error="generationProgress.error"
        :can-cancel="true"
        :cancelling="generationProgress.cancelling"
        @cancel="handleCancelGeneration"
        @close="handleProgressClose"
      />
      
      <!-- 创建表单 -->
      <div v-else class="create-form">
        <el-form 
          :model="createForm" 
          :rules="createFormRules"
          ref="createFormRef"
          label-width="100px"
        >
          <el-form-item label="时间线名称" prop="name">
            <el-input 
              v-model="createForm.name" 
              placeholder="请输入时间线名称"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="时间线描述" prop="description">
            <el-input 
              v-model="createForm.description" 
              type="textarea" 
              rows="3" 
              placeholder="请输入时间线描述，将用于智能事件检索"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="目标地区" prop="regionIds">
            <el-select 
              v-model="createForm.regionIds" 
              multiple 
              placeholder="请选择目标地区"
              style="width: 100%"
              collapse-tags
              collapse-tags-tooltip
            >
              <el-option 
                v-for="region in regionOptions" 
                :key="region.value" 
                :label="region.label" 
                :value="region.value" 
              />
            </el-select>
            <div class="form-tip">
              选择的地区将影响事件检索的范围和准确性
            </div>
          </el-form-item>
          
          <el-form-item label="时间范围" prop="timeRange">
            <el-date-picker
              v-model="createForm.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              :shortcuts="timeRangeShortcuts"
            />
            <div class="form-tip">
              时间范围过大可能影响生成速度，建议不超过1年
            </div>
          </el-form-item>
          
          <el-form-item label="生成选项">
            <el-checkbox-group v-model="createForm.options">
              <el-checkbox label="enableValidation">启用事件验证</el-checkbox>
              <el-checkbox label="enableDeduplication">启用事件去重</el-checkbox>
              <el-checkbox label="enableRelationAnalysis">启用关系分析</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button 
            @click="handleCloseCreateDialog"
            :disabled="generationProgress.isGenerating"
          >
            {{ generationProgress.isGenerating ? '生成中...' : '取消' }}
          </el-button>
          <EnhancedButton
            v-if="!generationProgress.isGenerating"
            type="primary"
            button-key="timeline-create-submit"
            :submission-key="'timeline-create'"
            :submission-data="createForm"
            :cooldown="5000"
            text="开始生成"
            loading-text="正在生成..."
            @click="handleCreateTimelineEnhanced"
            @blocked="handleSubmissionBlocked"
          >
            开始生成
          </EnhancedButton>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { optimizedTimelineApi } from '@/api/optimizedApi'
import { regionApi } from '@/api/region'
import { duplicationPrevention } from '@/utils/duplicationPrevention'
import { timelineFormValidator, showValidationErrors } from '@/utils/formValidationEnhancer'
import { timelineErrorHandler, showSuccessMessage, showWarningMessage } from '@/utils/errorHandlerEnhancer'
import EnhancedButton from '@/components/common/EnhancedButton.vue'
import ProgressMonitor from '@/components/common/ProgressMonitor.vue'

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
}

// 路由
const router = useRouter()

// 状态
const loading = ref(false)
const error = ref(null)
const timelineList = ref<TimelineItem[]>([])
const showCreateDialog = ref(false)
const showDuplicateDialog = ref(false)
const showDictionaryDialog = ref(false)
const showStatisticsDialog = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  statuses: [],
  minEventCount: null,
  maxEventCount: null,
  sort: 'createdAt',
  direction: 'desc'
})

// 创建表单
const createForm = reactive({
  name: '',
  description: '',
  regionIds: [] as number[],
  timeRange: [] as string[],
  options: ['enableValidation', 'enableDeduplication', 'enableRelationAnalysis'] as string[]
})

// 表单验证规则
const createFormRules = {
  name: [
    { required: true, message: '请输入时间线名称', trigger: 'blur' },
    { min: 2, max: 100, message: '名称长度应在2-100个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入时间线描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10-500个字符之间', trigger: 'blur' }
  ],
  regionIds: [
    { required: true, message: '请选择至少一个地区', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择时间范围', trigger: 'change' }
  ]
}

// 生成进度状态
const generationProgress = reactive({
  isGenerating: false,
  timelineId: null as number | null,
  progress: 0,
  status: '' as 'success' | 'exception' | 'warning' | '',
  currentStep: '',
  message: '',
  eventCount: 0,
  relationCount: 0,
  duration: 0,
  startTime: null as Date | null,
  error: '',
  cancelling: false
})

// 时间范围快捷选项
const timeRangeShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  },
  {
    text: '最近半年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 180)
      return [start, end]
    }
  }
]

// 地区选项 - 从后端API动态获取
const regionOptions = ref([])
const regionLoading = ref(false)

// 加载地区选项
const loadRegionOptions = async () => {
  regionLoading.value = true
  try {
    console.log('开始调用region API...')
    // 暂时使用分页API获取地区数据
    const response = await regionApi.getRegions({ page: 1, size: 100 })
    
    console.log('Region API响应:', response)
    console.log('响应类型:', typeof response)
    console.log('响应是否为数组:', Array.isArray(response))
    
    // 处理响应数据
    let regions = []
    if (response && response.records && Array.isArray(response.records)) {
      // 处理分页响应格式：{ records: [...], total: 49 }
      regions = response.records
    } else if (response && response.data) {
      if (response.data.code === 200 && response.data.data) {
        regions = Array.isArray(response.data.data) ? response.data.data : []
      } else if (Array.isArray(response.data)) {
        regions = response.data
      }
    } else if (Array.isArray(response)) {
      regions = response
    }
    
    console.log('处理后的regions数据:', regions)
    console.log('regions数量:', regions.length)
    
    // 获取地区层级函数（必须在使用前定义）
    const getRegionLevel = (type) => {
      const levelMap = {
        'CONTINENT': 1,
        'COUNTRY': 2, 
        'PROVINCE': 3,
        'CITY': 4,
        'CUSTOM': 5
      }
      return levelMap[type] || 5
    }
    
    // 直接转换为选项列表（不需要扁平化，因为是分页数据）
    const regionOptionsList = regions.map(region => ({
      value: region.id,
      label: region.name,
      type: region.type,
      level: getRegionLevel(region.type || 'CUSTOM')
    }))
    
    // 按层级和名称排序
    regionOptionsList.sort((a, b) => {
      if (a.level !== b.level) {
        return a.level - b.level
      }
      return a.label.localeCompare(b.label)
    })
    
    regionOptions.value = regionOptionsList
    
    console.log('加载地区选项成功:', regionOptions.value.length, '个地区')
    
  } catch (error) {
    console.error('加载地区选项失败:', error)
    ElMessage.warning('加载地区数据失败，使用默认选项')
    
    // 失败时使用默认选项（基于迁移文件中的数据）
    regionOptions.value = [
      { value: 1, label: '亚洲', type: 'CONTINENT', level: 1 },
      { value: 101, label: '中国', type: 'COUNTRY', level: 2 },
      { value: 102, label: '日本', type: 'COUNTRY', level: 2 },
      { value: 103, label: '韩国', type: 'COUNTRY', level: 2 },
      { value: 2, label: '欧洲', type: 'CONTINENT', level: 1 },
      { value: 201, label: '法国', type: 'COUNTRY', level: 2 },
      { value: 202, label: '德国', type: 'COUNTRY', level: 2 },
      { value: 203, label: '英国', type: 'COUNTRY', level: 2 },
      { value: 3, label: '北美洲', type: 'CONTINENT', level: 1 },
      { value: 301, label: '美国', type: 'COUNTRY', level: 2 },
      { value: 302, label: '加拿大', type: 'COUNTRY', level: 2 }
    ]
  } finally {
    regionLoading.value = false
  }
}

// 加载时间线列表
const loadTimelineList = async (forceRefresh = false) => {
  loading.value = true
  error.value = null
  
  try {
    // 构建请求参数
    const params = {
      page: pagination.page,
      size: pagination.size,
      sort: searchForm.sort,
      direction: searchForm.direction,
      keyword: searchForm.keyword || undefined,
      statuses: searchForm.statuses.length > 0 ? searchForm.statuses : undefined,
      minEventCount: searchForm.minEventCount,
      maxEventCount: searchForm.maxEventCount
    }
    
    // 使用优化的API服务获取时间线列表，强制刷新缓存
    const response = await optimizedTimelineApi.getTimelineList(params, forceRefresh)
    
    // 处理响应数据
    let content = []
    let total = 0
    
    // 处理不同的响应格式
    if (response) {
      // 处理标准响应格式：{ code: 200, data: { records: [...], total: 14 } }
      if (response.code === 200 && response.data) {
        if (response.data.records && Array.isArray(response.data.records)) {
          content = response.data.records
          total = response.data.total || content.length
        } else if (Array.isArray(response.data)) {
          content = response.data
          total = content.length
        }
      } 
      // 处理直接返回分页对象的格式：{ records: [...], total: 14, ... }
      else if (response.records) {
        content = response.records
        total = response.total || content.length
      } 
      // 处理直接返回数组的格式：[...]
      else if (Array.isArray(response)) {
        content = response
        total = content.length
      } 
      // 尝试从对象中提取数据
      else if (typeof response === 'object') {
        for (const key in response) {
          if (Array.isArray(response[key])) {
            content = response[key]
            total = content.length
            break
          }
        }
      }
    }
    
    // 转换数据格式
    const formattedList = content.map(item => ({
      id: item.id?.toString() || '',
      title: item.name || item.title || '未命名时间线',
      status: item.status || 'COMPLETED',
      eventCount: item.eventCount || 0,
      relationCount: item.relationCount || 0,
      timeSpan: calculateTimeSpan(item.startTime, item.endTime),
      createdAt: item.createdAt || new Date().toISOString(),
      updatedAt: item.updatedAt || new Date().toISOString()
    }))
    
    timelineList.value = formattedList
    pagination.total = total
    
    if (formattedList.length === 0) {
      ElMessage.info('没有找到符合条件的时间线')
    }
  } catch (err) {
    console.error('加载时间线列表失败:', err)
    error.value = '加载时间线列表失败'
    ElMessage.error('加载时间线列表失败')
  } finally {
    loading.value = false
  }
}

// 计算时间跨度
const calculateTimeSpan = (startTime: string, endTime: string): string => {
  if (!startTime || !endTime) return '未知'
  
  try {
    const start = new Date(startTime)
    const end = new Date(endTime)
    const diffTime = Math.abs(end.getTime() - start.getTime())
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    
    if (diffDays === 0) return '1天'
    return `${diffDays}天`
  } catch (e) {
    return '未知'
  }
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  if (!dateStr) return '-'
  try {
    return new Date(dateStr).toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch (e) {
    return '-'
  }
}

// 获取时间线状态类型
const getTimelineStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    'COMPLETED': 'success',
    'PROCESSING': 'warning',
    'FAILED': 'danger',
    'DRAFT': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取时间线状态文本
const getTimelineStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    'COMPLETED': '已完成',
    'PROCESSING': '处理中',
    'FAILED': '失败',
    'DRAFT': '草稿'
  }
  return textMap[status] || '未知'
}

// 处理搜索
const handleSearch = () => {
  pagination.page = 1
  loadTimelineList()
}

// 重置搜索
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

// 刷新时间线
const refreshTimelines = () => {
  // 强制刷新缓存
  loadTimelineList(true)
}

// 处理每页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  loadTimelineList()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadTimelineList()
}

// 查看时间线
const viewTimeline = (timeline: TimelineItem) => {
  // 跳转到时间线详情页面
  router.push(`/timeline/detail/${timeline.id}`)
}

// 编辑时间线
const editTimeline = (timeline: TimelineItem) => {
  ElMessage.info(`编辑时间线: ${timeline.title}`)
}

// 复制时间线
const duplicateTimeline = (timeline: TimelineItem) => {
  ElMessage.info(`复制时间线: ${timeline.title}`)
}

// 删除时间线
const deleteTimeline = (timeline: TimelineItem) => {
  ElMessageBox.confirm(`确定要删除时间线 "${timeline.title}" 吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 先从本地列表中移除该时间线（立即反馈给用户）
      const index = timelineList.value.findIndex(item => item.id === timeline.id)
      if (index !== -1) {
        timelineList.value.splice(index, 1)
      }
      
      // 然后调用API删除
      await optimizedTimelineApi.deleteTimeline(timeline.id)
      
      // 清除所有缓存
      optimizedTimelineApi.clearCache()
      
      ElMessage.success('删除成功')
    } catch (err) {
      // 如果删除失败，恢复列表
      ElMessage.error('删除失败')
      loadTimelineList()
    }
  }).catch(() => {
    // 取消删除
  })
}

// 增强的创建时间线方法
const handleCreateTimelineEnhanced = async () => {
  try {
    // 使用增强的表单验证
    const validationResult = timelineFormValidator.validateTimelineForm(createForm)
    
    if (!validationResult.valid) {
      showValidationErrors(validationResult)
      return
    }
    
    // 显示改进建议
    if (validationResult.suggestions && validationResult.suggestions.length > 0) {
      validationResult.suggestions.forEach(suggestion => {
        showWarningMessage(suggestion, 3000)
      })
    }
    
    // 调用原有的创建方法
    await handleCreateTimeline()
    
  } catch (error) {
    timelineErrorHandler.handleTimelineCreationError(error, createForm)
  }
}

// 处理提交被阻止
const handleSubmissionBlocked = (reason: string) => {
  showWarningMessage(`操作被阻止: ${reason}`)
}

// 原有的创建时间线方法
const handleCreateTimeline = async () => {
  // 防重复提交检查
  if (createLoading.value || generationProgress.isGenerating) {
    console.log('请求正在处理中，忽略重复提交')
    return
  }
  
  // 表单验证
  if (!createFormRef.value) return
  
  try {
    const valid = await createFormRef.value.validate()
    if (!valid) return
  } catch (error) {
    ElMessage.warning('请检查表单信息')
    return
  }
  
  // 验证时间范围
  if (createForm.timeRange.length !== 2) {
    ElMessage.warning('请选择完整的时间范围')
    return
  }
  
  const startTime = new Date(createForm.timeRange[0])
  const endTime = new Date(createForm.timeRange[1])
  const timeDiff = endTime.getTime() - startTime.getTime()
  const daysDiff = timeDiff / (1000 * 60 * 60 * 24)
  
  if (daysDiff > 365) {
    const result = await ElMessageBox.confirm(
      '时间范围超过1年，可能会影响生成速度和准确性。是否继续？',
      '时间范围提醒',
      {
        confirmButtonText: '继续生成',
        cancelButtonText: '重新选择',
        type: 'warning'
      }
    ).catch(() => false)
    
    if (!result) return
  }
  
  const data = {
    name: createForm.name,
    description: createForm.description,
    regionIds: createForm.regionIds,
    startTime: createForm.timeRange[0],
    endTime: createForm.timeRange[1],
    options: createForm.options
  }
  
  // 使用防重复提交工具检查
  const submissionKey = 'timeline-create'
  if (!duplicationPrevention.canSubmit(submissionKey, data)) {
    ElMessage.warning('请求过于频繁，请稍后再试')
    return
  }
  
  createLoading.value = true
  
  // 标记开始提交
  duplicationPrevention.markSubmitting(submissionKey, data)
  
  try {
    // 调用异步生成API
    const response = await optimizedTimelineApi.generateTimelineAsync(data)
    
    if (response && response.id) {
      // 检查是否为重复请求
      if (response.isDuplicate) {
        ElMessage.warning(`检测到重复请求: ${response.duplicateReason}`)
        ElMessage.info(`返回已存在的时间线: ${response.name}`)
        
        // 如果时间线已完成，直接关闭对话框并刷新列表
        if (response.status === 'COMPLETED') {
          handleGenerationComplete()
        } else if (response.status === 'GENERATING') {
          // 如果时间线正在生成，开始跟踪进度
          startProgressTracking(response.id, response.name)
        }
      } else {
        // 新的时间线生成任务
        startProgressTracking(response.id, createForm.name)
        ElMessage.success('时间线生成任务已启动')
      }
    } else {
      throw new Error('生成任务启动失败')
    }
    
    // 标记提交完成
    duplicationPrevention.markSubmitted(submissionKey, data)
    
  } catch (err) {
    console.error('创建时间线失败:', err)
    ElMessage.error('创建时间线失败: ' + (err.message || '未知错误'))
    generationProgress.isGenerating = false
    
    // 清除提交状态
    duplicationPrevention.clearSubmission(submissionKey, data)
  } finally {
    createLoading.value = false
  }
}

// 进度跟踪相关函数
let progressTimer: NodeJS.Timeout | null = null

// 开始进度跟踪
const startProgressTracking = (timelineId: number, timelineName: string) => {
  generationProgress.isGenerating = true
  generationProgress.timelineId = timelineId
  generationProgress.progress = 0
  generationProgress.status = ''
  generationProgress.currentStep = '正在初始化...'
  generationProgress.message = `开始生成时间线: ${timelineName}`
  generationProgress.eventCount = 0
  generationProgress.relationCount = 0
  generationProgress.duration = 0
  generationProgress.startTime = new Date()
  generationProgress.error = ''
  generationProgress.cancelling = false
  
  // 开始轮询进度
  startProgressPolling()
}

// 开始进度轮询
const startProgressPolling = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }
  
  progressTimer = setInterval(async () => {
    if (!generationProgress.isGenerating || !generationProgress.timelineId) {
      stopProgressPolling()
      return
    }
    
    try {
      const response = await optimizedTimelineApi.getGenerationProgress(generationProgress.timelineId)
      
      if (response && response.data) {
        updateProgressFromResponse(response.data)
      }
    } catch (error) {
      console.error('获取进度失败:', error)
      // 继续轮询，不中断
    }
  }, 2000) // 每2秒轮询一次
}

// 停止进度轮询
const stopProgressPolling = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

// 更新进度信息
const updateProgressFromResponse = (data: any) => {
  generationProgress.progress = data.progress || 0
  generationProgress.currentStep = data.currentStep || '处理中...'
  generationProgress.message = data.message || ''
  generationProgress.eventCount = data.eventCount || 0
  generationProgress.relationCount = data.relationCount || 0
  
  // 计算耗时
  if (generationProgress.startTime) {
    generationProgress.duration = Date.now() - generationProgress.startTime.getTime()
  }
  
  // 检查状态
  if (data.status === 'COMPLETED') {
    generationProgress.status = 'success'
    generationProgress.progress = 100
    generationProgress.currentStep = '生成完成'
    generationProgress.message = `时间线生成成功！共检索到 ${generationProgress.eventCount} 个事件，建立了 ${generationProgress.relationCount} 个关系。`
    
    ElMessage.success('时间线生成完成！')
    
    // 延迟关闭对话框和刷新列表
    setTimeout(() => {
      handleGenerationComplete()
    }, 2000)
    
  } else if (data.status === 'FAILED') {
    generationProgress.status = 'exception'
    generationProgress.error = data.errorMessage || '生成失败'
    generationProgress.currentStep = '生成失败'
    
    ElMessage.error('时间线生成失败: ' + generationProgress.error)
    stopProgressPolling()
    
  } else if (data.status === 'CANCELLED') {
    generationProgress.status = 'warning'
    generationProgress.currentStep = '已取消'
    generationProgress.message = '时间线生成已取消'
    
    ElMessage.info('时间线生成已取消')
    stopProgressPolling()
    
    setTimeout(() => {
      handleGenerationComplete()
    }, 1000)
  }
}

// 处理生成完成
const handleGenerationComplete = () => {
  generationProgress.isGenerating = false
  generationProgress.timelineId = null
  stopProgressPolling()
  
  // 重置表单
  resetCreateForm()
  
  // 关闭对话框
  showCreateDialog.value = false
  
  // 刷新列表
  loadTimelineList()
}

// 取消生成
const handleCancelGeneration = async () => {
  if (!generationProgress.timelineId) return
  
  try {
    generationProgress.cancelling = true
    
    const result = await ElMessageBox.confirm(
      '确定要取消时间线生成吗？已生成的数据将会丢失。',
      '取消确认',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '继续生成',
        type: 'warning'
      }
    )
    
    if (result) {
      await optimizedTimelineApi.cancelGeneration(generationProgress.timelineId)
      ElMessage.info('正在取消生成...')
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消生成失败:', error)
      timelineErrorHandler.handleCancelError(error, generationProgress.timelineId)
    }
  } finally {
    generationProgress.cancelling = false
  }
}

// 关闭创建对话框
const handleCloseCreateDialog = () => {
  if (generationProgress.isGenerating) {
    ElMessage.warning('时间线正在生成中，请等待完成或取消生成')
    return
  }
  
  showCreateDialog.value = false
  resetCreateForm()
}

// 重置创建表单
const resetCreateForm = () => {
  createForm.name = ''
  createForm.description = ''
  createForm.regionIds = []
  createForm.timeRange = []
  createForm.options = ['enableValidation', 'enableDeduplication', 'enableRelationAnalysis']
  
  // 清除表单验证状态
  if (createFormRef.value) {
    createFormRef.value.clearValidate()
  }
}

// 格式化持续时间
const formatDuration = (milliseconds: number): string => {
  if (milliseconds < 1000) return '< 1秒'
  
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes % 60}分钟`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds % 60}秒`
  } else {
    return `${seconds}秒`
  }
}

// 处理进度监控关闭
const handleProgressClose = () => {
  handleGenerationComplete()
}

// 进度监控相关计算属性
const progressMonitorTitle = computed(() => {
  if (generationProgress.status === 'success') {
    return '时间线生成完成'
  } else if (generationProgress.status === 'exception') {
    return '时间线生成失败'
  } else if (generationProgress.status === 'warning') {
    return '时间线生成已取消'
  } else {
    return '正在生成时间线...'
  }
})

const progressStats = computed(() => {
  return [
    {
      label: '已检索事件',
      value: generationProgress.eventCount,
      valueClass: 'stat-primary'
    },
    {
      label: '已建立关系',
      value: generationProgress.relationCount,
      valueClass: 'stat-success'
    },
    {
      label: '耗时',
      value: formatDuration(generationProgress.duration),
      valueClass: 'stat-info'
    }
  ]
})

// 进度监控相关计算属性
const progressMonitorTitle = computed(() => {
  if (generationProgress.status === 'success') {
    return '时间线生成完成'
  } else if (generationProgress.status === 'exception') {
    return '时间线生成失败'
  } else if (generationProgress.status === 'warning') {
    return '时间线生成已取消'
  } else {
    return '正在生成时间线...'
  }
})

// 生命周期钩子
onMounted(() => {
  // 初始化为空数组
  if (!timelineList.value) {
    timelineList.value = []
  }
  
  // 加载数据
  loadTimelineList()
  loadRegionOptions()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopProgressPolling()
})
</script>

<style scoped>
.timeline-container {
  padding: 20px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-left h2 {
  margin: 0;
  font-size: 24px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 10px;
}

.search-filter-container {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-buttons {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.event-count-input {
  width: 100px;
}

.separator {
  margin: 0 10px;
}

.timeline-content {
  min-height: 400px;
}

.timeline-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.timeline-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s;
  cursor: pointer;
}

.timeline-card:hover {
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.2);
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 15px;
}

.stat-item {
  text-align: center;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.create-form {
  padding: 10px;
}

/* 生成进度样式 */
.generation-progress {
  padding: 20px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-header h4 {
  margin: 0;
  color: #409eff;
  font-size: 16px;
}

.progress-content {
  margin-bottom: 20px;
}

.progress-details {
  margin-top: 15px;
}

.current-step {
  margin-bottom: 10px;
  font-size: 14px;
}

.step-label {
  color: #909399;
  margin-right: 8px;
}

.step-text {
  color: #303133;
  font-weight: 500;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin: 15px 0;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.progress-stats .stat-item {
  text-align: center;
}

.progress-stats .stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.progress-stats .stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.progress-message {
  padding: 10px;
  background-color: #ecf5ff;
  border-left: 4px solid #409eff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  margin-top: 10px;
}

.progress-error {
  margin-top: 15px;
}

/* 表单提示样式 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .progress-stats {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .progress-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .progress-header h4 {
    margin-bottom: 10px;
  }
}
</style>