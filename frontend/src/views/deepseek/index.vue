<template>
  <div class="deepseek-container">
    <el-card class="page-header">
      <template #header>
        <div class="header-content">
          <span>DeepSeek 事件抓取管理</span>
          <el-button type="primary" @click="checkStatus" :loading="statusLoading">
            <el-icon><Refresh /></el-icon>
            检查连接状态
          </el-button>
        </div>
      </template>
      
      <!-- 连接状态展示 -->
      <div class="status-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover">
              <el-statistic
                title="连接状态"
                :value="status.connected ? '正常' : '使用模拟数据'"
                :value-style="{ color: status.connected ? '#67C23A' : '#E6A23C' }"
              >
                <template #prefix>
                  <el-icon :color="status.connected ? '#67C23A' : '#E6A23C'">
                    <component :is="status.connected ? 'SuccessFilled' : 'WarningFilled'" />
                  </el-icon>
                </template>
              </el-statistic>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <el-statistic title="最后检查时间" :value="formatTime(status.timestamp)" />
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card shadow="hover">
              <el-statistic title="状态消息" :value="status.message || '--'" />
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 操作区域 -->
    <div class="operation-section">
      <el-row :gutter="20">
        <!-- 手动抓取最新事件 -->
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>抓取最新事件</span>
                <el-icon><TrendCharts /></el-icon>
              </div>
            </template>
            
            <div class="operation-content">
              <el-form :model="fetchForm" label-width="80px">
                <el-form-item label="数量限制">
                  <el-input-number
                    v-model="fetchForm.limit"
                    :min="1"
                    :max="20"
                    controls-position="right"
                  />
                </el-form-item>
              </el-form>
              
              <el-button
                type="primary"
                @click="fetchLatestEvents"
                :loading="fetchLoading"
                style="width: 100%"
              >
                <el-icon><Download /></el-icon>
                抓取最新事件
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 关键词抓取 -->
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>关键词抓取</span>
                <el-icon><Search /></el-icon>
              </div>
            </template>
            
            <div class="operation-content">
              <el-form :model="keywordForm" label-width="80px">
                <el-form-item label="关键词">
                  <el-select
                    v-model="keywordForm.keywords"
                    multiple
                    filterable
                    allow-create
                    placeholder="输入关键词"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="item in keywordOptions"
                      :key="item"
                      :label="item"
                      :value="item"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="数量限制">
                  <el-input-number
                    v-model="keywordForm.limit"
                    :min="1"
                    :max="20"
                    controls-position="right"
                  />
                </el-form-item>
              </el-form>
              
              <el-button
                type="success"
                @click="fetchByKeywords"
                :loading="keywordLoading"
                :disabled="!keywordForm.keywords.length"
                style="width: 100%"
              >
                <el-icon><Search /></el-icon>
                关键词抓取
              </el-button>
            </div>
          </el-card>
        </el-col>

        <!-- 日期范围抓取 -->
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>日期范围抓取</span>
                <el-icon><Calendar /></el-icon>
              </div>
            </template>
            
            <div class="operation-content">
              <el-form :model="dateForm" label-width="80px">
                <el-form-item label="日期范围">
                  <el-date-picker
                    v-model="dateForm.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="数量限制">
                  <el-input-number
                    v-model="dateForm.limit"
                    :min="1"
                    :max="20"
                    controls-position="right"
                  />
                </el-form-item>
              </el-form>
              
              <el-button
                type="warning"
                @click="fetchByDateRange"
                :loading="dateLoading"
                :disabled="!dateForm.dateRange || dateForm.dateRange.length !== 2"
                style="width: 100%"
              >
                <el-icon><Calendar /></el-icon>
                日期范围抓取
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 定时任务管理 -->
    <el-card class="task-section">
      <template #header>
        <div class="header-content">
          <span>定时任务管理</span>
          <el-button type="danger" @click="triggerTask" :loading="taskLoading">
            <el-icon><VideoPlay /></el-icon>
            手动触发
          </el-button>
        </div>
      </template>
      
      <div class="task-info">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="定时任务状态">
            <el-tag type="success">已启用</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行间隔">
            每小时执行一次
          </el-descriptions-item>
          <el-descriptions-item label="批次大小">
            5个事件/次
          </el-descriptions-item>
          <el-descriptions-item label="执行时间" :span="2">
            每日凌晨2点执行关键词抓取
          </el-descriptions-item>
          <el-descriptions-item label="抓取关键词">
            中美关系、俄乌冲突、中东冲突、朝鲜、台海、伊朗、欧盟
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- API配置说明 -->
        <el-alert 
          v-if="!status.connected"
          title="API配置说明" 
          type="info" 
          :closable="false"
          style="margin-top: 20px"
        >
          <template #default>
            <p>当前未配置DeepSeek API密钥，系统将使用模拟数据进行演示。</p>
            <p>要启用真实数据抓取，请在应用配置中设置：</p>
            <el-code>app.deepseek.api-key = your_deepseek_api_key</el-code>
            <p style="margin-top: 10px; font-size: 12px; color: #909399;">
              模拟数据包含国际热点事件样例，可用于测试系统功能。
            </p>
          </template>
        </el-alert>
      </div>
    </el-card>

    <!-- 抓取结果 -->
    <el-card v-if="fetchResult" class="result-section">
      <template #header>
        <span>抓取结果</span>
      </template>
      
      <div class="result-summary">
        <el-alert
          :title="`共抓取 ${fetchResult.totalFetched} 个事件，成功保存 ${fetchResult.successSaved} 个`"
          :type="fetchResult.successSaved > 0 ? 'success' : 'warning'"
          show-icon
          :closable="false"
        />
      </div>
      
      <div v-if="fetchResult.events && fetchResult.events.length > 0" class="result-events">
        <el-table :data="fetchResult.events" stripe style="width: 100%">
          <el-table-column prop="eventType" label="事件类型" width="120" />
          <el-table-column prop="subject" label="事件主体" width="150" />
          <el-table-column prop="object" label="事件客体" width="150" />
          <el-table-column prop="eventLocation" label="事件地点" width="150" />
          <el-table-column prop="eventDescription" label="事件描述" show-overflow-tooltip />
          <el-table-column prop="eventTime" label="事件时间" width="160" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  SuccessFilled,
  WarningFilled,
  TrendCharts,
  Download,
  Search,
  Calendar,
  VideoPlay
} from '@element-plus/icons-vue'
import { deepseekApi } from '@/api/deepseek'
import dayjs from 'dayjs'

// 响应式数据
const statusLoading = ref(false)
const fetchLoading = ref(false)
const keywordLoading = ref(false)
const dateLoading = ref(false)
const taskLoading = ref(false)

// 连接状态
const status = reactive({
  connected: false,
  message: '',
  timestamp: null as number | null
})

// 表单数据
const fetchForm = reactive({
  limit: 5
})

const keywordForm = reactive({
  keywords: [] as string[],
  limit: 5
})

const dateForm = reactive({
  dateRange: [] as string[],
  limit: 5
})

// 抓取结果
const fetchResult = ref<any>(null)

// 关键词选项
const keywordOptions = [
  '中美关系', '贸易战', '俄乌冲突', '乌克兰', '中东冲突',
  '以色列', '巴勒斯坦', '朝鲜', '核武器', '台海',
  '两岸关系', '伊朗', '核协议', '欧盟', '制裁'
]

// 检查连接状态
const checkStatus = async () => {
  statusLoading.value = true
  try {
    const response = await deepseekApi.checkStatus()
    Object.assign(status, response)
    
    ElMessage.success(response.connected ? '连接正常' : '使用模拟数据模式')
  } catch (error) {
    console.error('检查状态失败:', error)
    ElMessage.error('检查状态失败')
  } finally {
    statusLoading.value = false
  }
}

// 抓取最新事件
const fetchLatestEvents = async () => {
  fetchLoading.value = true
  try {
    const response = await deepseekApi.fetchLatestEvents(fetchForm.limit)
    fetchResult.value = response
    
    ElMessage.success(`抓取完成！共获取 ${response.totalFetched} 个事件，成功保存 ${response.successSaved} 个`)
  } catch (error) {
    console.error('抓取最新事件失败:', error)
    ElMessage.error('抓取失败')
  } finally {
    fetchLoading.value = false
  }
}

// 关键词抓取
const fetchByKeywords = async () => {
  keywordLoading.value = true
  try {
    const response = await deepseekApi.fetchByKeywords(keywordForm.keywords, keywordForm.limit)
    fetchResult.value = response
    
    ElMessage.success(`关键词抓取完成！共获取 ${response.totalFetched} 个事件，成功保存 ${response.successSaved} 个`)
  } catch (error) {
    console.error('关键词抓取失败:', error)
    ElMessage.error('抓取失败')
  } finally {
    keywordLoading.value = false
  }
}

// 日期范围抓取
const fetchByDateRange = async () => {
  dateLoading.value = true
  try {
    const [startDate, endDate] = dateForm.dateRange
    const response = await deepseekApi.fetchByDateRange(startDate, endDate, dateForm.limit)
    fetchResult.value = response
    
    ElMessage.success(`日期范围抓取完成！共获取 ${response.totalFetched} 个事件，成功保存 ${response.successSaved} 个`)
  } catch (error) {
    console.error('日期范围抓取失败:', error)
    ElMessage.error('抓取失败')
  } finally {
    dateLoading.value = false
  }
}

// 触发定时任务
const triggerTask = async () => {
  taskLoading.value = true
  try {
    await deepseekApi.triggerTask()
    ElMessage.success('定时任务已触发，正在后台执行')
  } catch (error) {
    console.error('触发定时任务失败:', error)
    ElMessage.error('触发失败')
  } finally {
    taskLoading.value = false
  }
}

// 格式化时间
const formatTime = (timestamp: number | null) => {
  if (!timestamp) return '--'
  return dayjs(timestamp).format('YYYY-MM-DD HH:mm:ss')
}

// 组件挂载时检查状态
onMounted(() => {
  checkStatus()
})
</script>

<style scoped>
.deepseek-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-section {
  padding: 20px 0;
}

.operation-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.operation-content {
  padding: 10px 0;
}

.task-section {
  margin-bottom: 20px;
}

.task-info {
  padding: 20px 0;
}

.result-section {
  margin-bottom: 20px;
}

.result-summary {
  margin-bottom: 20px;
}

.result-events {
  margin-top: 20px;
}

.el-card {
  border-radius: 8px;
}

.el-card :deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.el-statistic :deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

.el-button {
  border-radius: 6px;
}

.el-form-item {
  margin-bottom: 15px;
}
</style> 