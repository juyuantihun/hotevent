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
      </div>
    </div>

    <!-- 时间线列表 -->
    <div class="timeline-list">
      <el-card v-if="loading" class="loading-card">
        <div class="loading-content">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在加载时间线数据...</span>
        </div>
      </el-card>

      <el-empty
        v-else-if="timelineList.length === 0"
        description="暂无时间线数据"
      >
        <el-button type="primary" @click="showCreateDialog = true">
          创建第一个时间线
        </el-button>
      </el-empty>

      <div v-else class="timeline-cards">
        <el-card 
          v-for="item in timelineList" 
          :key="item.id" 
          class="timeline-card"
          shadow="hover"
        >
          <div class="card-header">
            <h3>{{ item.name }}</h3>
            <el-tag :type="getStatusType(item.status)">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
          
          <div class="card-content">
            <p class="description">{{ item.description }}</p>
            <div class="stats">
              <div class="stat-item">
                <span class="label">事件数量:</span>
                <span class="value">{{ item.eventCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="label">创建时间:</span>
                <span class="value">{{ formatDate(item.createdAt) }}</span>
              </div>
            </div>
          </div>

          <div class="card-actions">
            <el-button size="small" @click="viewTimeline(item)">查看</el-button>
            <el-button size="small" type="primary" @click="editTimeline(item)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTimeline(item)">删除</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 创建时间线对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="生成时间线"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="100px"
      >
        <el-form-item label="时间线名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入时间线名称" />
        </el-form-item>
        
        <el-form-item label="时间线描述" prop="description">
          <el-input 
            v-model="createForm.description" 
            type="textarea" 
            rows="3" 
            placeholder="请输入时间线描述"
          />
        </el-form-item>
        
        <el-form-item label="地区选择" prop="regionIds">
          <el-select 
            v-model="createForm.regionIds" 
            multiple 
            placeholder="请选择地区"
            style="width: 100%"
          >
            <el-option
              v-for="region in regionOptions"
              :key="region.id"
              :label="region.name"
              :value="region.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围" prop="timeRange">
          <el-date-picker
            v-model="createForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="handleCreateTimeline" :loading="creating">
            生成时间线
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 路由实例
const router = useRouter()

// 响应式数据
const loading = ref(false)
const creating = ref(false)
const showCreateDialog = ref(false)
const timelineList = ref([])
const regionOptions = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 创建表单
const createForm = reactive({
  name: '',
  description: '',
  regionIds: [] as number[],
  timeRange: [] as string[]
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

// 获取地区数据
const loadRegions = async () => {
  try {
    console.log('开始获取地区数据...')
    const response = await axios.get('/api/regions', {
      params: {
        page: 1,
        size: 1000 // 获取所有地区
      }
    })
    
    console.log('地区API响应:', response.data)
    
    if (response.data && response.data.code === 200 && response.data.data) {
      regionOptions.value = response.data.data.records || []
      console.log('地区数据加载成功:', regionOptions.value.length, regionOptions.value)
    } else {
      console.warn('地区数据格式异常:', response.data)
      ElMessage.warning('地区数据加载异常')
      
      // 添加一些从数据库中看到的实际地区数据作为备用
      regionOptions.value = [
        { id: 1, name: '亚洲', type: 'continent' },
        { id: 2, name: '欧洲', type: 'continent' },
        { id: 3, name: '北美洲', type: 'continent' },
        { id: 4, name: '南美洲', type: 'continent' },
        { id: 5, name: '非洲', type: 'continent' },
        { id: 6, name: '大洋洲', type: 'continent' },
        { id: 7, name: '南极洲', type: 'continent' },
        { id: 8, name: '中国', type: 'country' },
        { id: 9, name: '美国', type: 'country' },
        { id: 10, name: '日本', type: 'country' },
        { id: 11, name: '韩国', type: 'country' },
        { id: 12, name: '朝鲜', type: 'country' },
        { id: 13, name: '伊朗', type: 'country' },
        { id: 14, name: '以色列', type: 'country' },
        { id: 15, name: '俄罗斯', type: 'country' },
        { id: 16, name: '德国', type: 'country' },
        { id: 17, name: '法国', type: 'country' },
        { id: 18, name: '英国', type: 'country' },
        { id: 19, name: '乌克兰', type: 'country' },
        { id: 20, name: '加拿大', type: 'country' }
      ]
    }
  } catch (error) {
    console.error('获取地区数据失败:', error)
    ElMessage.error('获取地区数据失败')
    
    // 添加一些测试数据作为备用
    regionOptions.value = [
      { id: 1, name: '北京', type: 'city' },
      { id: 2, name: '上海', type: 'city' },
      { id: 3, name: '广州', type: 'city' },
      { id: 4, name: '深圳', type: 'city' }
    ]
  }
}

// 获取时间线列表数据
const refreshTimelines = async () => {
  loading.value = true
  try {
    console.log('开始获取时间线列表数据...')
    const response = await axios.get('/api/timelines', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    
    console.log('时间线API响应:', response.data)
    
    if (response.data && response.data.code === 200 && response.data.data) {
      const pageData = response.data.data
      timelineList.value = pageData.records || []
      pagination.total = pageData.total || 0
      console.log('时间线数据加载成功:', timelineList.value.length, timelineList.value)
      ElMessage.success('刷新成功')
    } else {
      console.warn('时间线数据格式异常:', response.data)
      ElMessage.warning('时间线数据加载异常')
      timelineList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取时间线数据失败:', error)
    ElMessage.error('获取时间线数据失败')
    timelineList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleCreateTimeline = async () => {
  creating.value = true
  try {
    console.log('开始创建时间线...', createForm)
    
    // 构建请求数据
    const requestData = {
      name: createForm.name,
      description: createForm.description,
      regionIds: createForm.regionIds,
      startTime: createForm.timeRange[0],
      endTime: createForm.timeRange[1]
    }
    
    console.log('时间线创建请求数据:', requestData)
    
    // 调用异步生成时间线API
    const response = await axios.post('/api/timelines/generate/async', requestData)
    
    console.log('时间线创建API响应:', response.data)
    
    if (response.data && response.data.code === 200) {
      const result = response.data.data
      if (result.isDuplicate) {
        ElMessage.warning(`检测到重复请求: ${result.duplicateReason}`)
      } else {
        ElMessage.success('时间线生成任务已提交，正在后台处理...')
      }
      
      showCreateDialog.value = false
      
      // 重置表单
      createForm.name = ''
      createForm.description = ''
      createForm.regionIds = []
      createForm.timeRange = []
      
      // 刷新列表
      refreshTimelines()
    } else {
      console.warn('时间线创建响应异常:', response.data)
      ElMessage.error(response.data.msg || '时间线生成失败')
    }
  } catch (error) {
    console.error('创建时间线失败:', error)
    ElMessage.error('时间线生成失败')
  } finally {
    creating.value = false
  }
}

const viewTimeline = (item: any) => {
  console.log('跳转到时间线详情页面:', item)
  try {
    // 跳转到时间线详情页面
    router.push({
      name: 'TimelineDetail',
      params: {
        id: item.id
      }
    })
  } catch (error) {
    console.error('跳转到时间线详情页面失败:', error)
    ElMessage.error('跳转失败')
  }
}

const editTimeline = (item: any) => {
  ElMessage.info(`编辑时间线: ${item.name}`)
}

const deleteTimeline = async (item: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除时间线 "${item.name}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    console.log('开始删除时间线:', item)
    
    // 调用后端删除接口
    const response = await axios.delete(`/api/timelines/${item.id}`)
    
    console.log('删除时间线API响应:', response.data)
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('删除成功')
      // 刷新列表
      refreshTimelines()
    } else {
      console.warn('删除时间线响应异常:', response.data)
      ElMessage.error(response.data.msg || '删除失败')
    }
  } catch (error: any) {
    // 检查是否是用户取消操作
    if (error === 'cancel') {
      // 用户取消删除，不显示错误信息
      return
    }
    
    console.error('删除时间线失败:', error)
    
    // 处理HTTP错误
    if (error.response) {
      const errorMsg = error.response.data?.msg || `删除失败 (${error.response.status})`
      ElMessage.error(errorMsg)
    } else {
      ElMessage.error('删除失败，请检查网络连接')
    }
  }
}

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'COMPLETED': 'success',
    'PROCESSING': 'warning',
    'FAILED': 'danger',
    'DRAFT': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'COMPLETED': '已完成',
    'PROCESSING': '处理中',
    'FAILED': '失败',
    'DRAFT': '草稿'
  }
  return statusMap[status] || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  refreshTimelines()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  refreshTimelines()
}

// 初始化
onMounted(() => {
  loadRegions()
  refreshTimelines()
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

.loading-card {
  text-align: center;
  padding: 40px;
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 16px;
  color: #909399;
}

.timeline-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.timeline-card {
  transition: transform 0.2s;
}

.timeline-card:hover {
  transform: translateY(-2px);
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
}

.card-content {
  margin-bottom: 15px;
}

.description {
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.label {
  color: #909399;
}

.value {
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  gap: 10px;
}
</style>