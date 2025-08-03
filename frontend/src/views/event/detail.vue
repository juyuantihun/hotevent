<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="flex-between">
        <div>
          <h2 class="page-title">事件详情</h2>
          <p class="page-description">查看事件的详细信息</p>
        </div>
        <div>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
          <el-button type="primary" @click="editEvent">
            <el-icon><Edit /></el-icon>
            编辑事件
          </el-button>
        </div>
      </div>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <!-- 左侧基本信息 -->
      <el-col :lg="16" :md="24">
        <!-- 基本信息卡片 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <el-tag :type="getSourceTypeColor(event.sourceType)">
                {{ getSourceTypeText(event.sourceType) }}
              </el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="事件编码">
              {{ event.eventCode || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="事件时间">
              {{ formatDateTime(event.eventTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="事件地点">
              {{ event.eventLocation }}
            </el-descriptions-item>
            <el-descriptions-item label="事件类型">
              <el-tag>{{ event.eventType }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="事件主体">
              <el-tag type="success">{{ event.subject }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="事件客体">
              <el-tag type="warning">{{ event.object }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="地理坐标" :span="2" v-if="event.longitude && event.latitude">
              {{ Number(event.longitude).toFixed(6) }}, {{ Number(event.latitude).toFixed(6) }}
              <el-button type="text" @click="showMap = true">
                <el-icon><Location /></el-icon>
                查看地图
              </el-button>
            </el-descriptions-item>
            <el-descriptions-item label="关键词" :span="2" v-if="keywordList.length > 0">
              <div class="keywords">
                <el-tag
                  v-for="keyword in keywordList"
                  :key="keyword"
                  size="small"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(event.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatDateTime(event.updatedAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 事件描述 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <span class="card-title">事件描述</span>
          </template>
          <div class="event-description">
            {{ event.eventDescription || '暂无描述' }}
          </div>
        </el-card>
      </el-col>

      <!-- 右侧相似事件 -->
      <el-col :lg="8" :md="24">
        <!-- 相似事件推荐 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">相似事件</span>
              <el-button size="small" @click="loadSimilarEvents">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <div v-if="similarEvents.length === 0" class="no-similar">
            <el-empty description="暂无相似事件" size="small" />
          </div>
          
          <div v-else class="similar-events">
            <div
              v-for="similarEvent in similarEvents"
              :key="similarEvent.id"
              class="similar-item"
              @click="viewSimilarEvent(similarEvent.id!)"
            >
              <div class="similar-title">{{ similarEvent.eventDescription }}</div>
              <div class="similar-meta">
                <span class="similar-time">{{ formatDate(similarEvent.eventTime) }}</span>
                <span class="similar-location">{{ similarEvent.eventLocation }}</span>
                <span class="similar-type">
                  <el-tag size="small">{{ similarEvent.eventType }}</el-tag>
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 地图弹窗 -->
    <el-dialog v-model="showMap" title="事件地理位置" width="80%" :before-close="closeMap">
      <div ref="mapRef" class="event-map"></div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Location, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import type { Event, PageResult } from '@/api/event'
import { getEventDetail, getEventList } from '@/api/event'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

// 响应式数据
const loading = ref(false)
const event = ref<Event>({} as Event)
const similarEvents = ref<Event[]>([])
const showMap = ref(false)

// DOM引用
const mapRef = ref<HTMLDivElement>()

// 图表实例
let eventMap: ECharts | null = null

// 计算属性
const keywordList = computed(() => {
  if (!event.value.keywords) return []
  
  // 如果是数组类型（从API返回）
  if (Array.isArray(event.value.keywords)) {
    return event.value.keywords.filter(k => k && k.trim())
  }
  
  // 如果是字符串类型（逗号分隔）
  if (typeof event.value.keywords === 'string') {
    return event.value.keywords.split(',').filter(k => k && k.trim())
  }
  
  return []
})

// 生命周期
onMounted(async () => {
  const id = Number(route.params.id)
  await loadEventDetail(id)
  await loadSimilarEvents()
})

// 监听地图显示状态
watch(showMap, (newVal) => {
  if (newVal) {
    nextTick(() => {
      initMap()
    })
  }
})

// 加载事件详情
const loadEventDetail = async (id: number) => {
  loading.value = true
  try {
    const data: Event = await getEventDetail(id)
    event.value = data
    console.log('Event detail loaded:', data)
  } catch (error) {
    console.error('加载事件详情失败:', error)
    ElMessage.error('加载事件详情失败')
    goBack()
  } finally {
    loading.value = false
  }
}

// 加载相似事件
const loadSimilarEvents = async () => {
  if (!event.value.eventType) return
  
  try {
    // 根据事件类型查找相似事件
    const response: PageResult<Event> = await getEventList({
      current: 1,
      size: 10,
      eventType: event.value.eventType
    })
    
    // 过滤掉当前事件，只保留其他相似事件
    similarEvents.value = (response.records || [])
      .filter(item => item.id !== event.value.id)
      .slice(0, 5) // 只显示前5个
    
    console.log('Similar events loaded:', similarEvents.value)
  } catch (error) {
    console.error('加载相似事件失败:', error)
  }
}

// 初始化地图
const initMap = () => {
  if (!mapRef.value || !event.value.longitude || !event.value.latitude) return
  
  eventMap = echarts.init(mapRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: `${event.value.eventLocation}<br/>经度: ${event.value.longitude}<br/>纬度: ${event.value.latitude}`
    },
    geo: {
      map: 'world',
      roam: true,
      zoom: 2,
      center: [Number(event.value.longitude), Number(event.value.latitude)],
      itemStyle: {
        areaColor: '#f0f0f0',
        borderColor: '#999'
      }
    },
    series: [{
      type: 'scatter',
      coordinateSystem: 'geo',
      data: [{
        name: event.value.eventLocation,
        value: [Number(event.value.longitude), Number(event.value.latitude), 1]
      }],
      symbolSize: 20,
      itemStyle: {
        color: '#ff4757'
      },
      emphasis: {
        itemStyle: {
          color: '#ff3838'
        }
      }
    }]
  }
  
  eventMap.setOption(option)
}

// 工具函数
const getSourceTypeText = (type: number) => {
  return type === 2 ? '人工录入' : 'AI获取'
}

const getSourceTypeColor = (type: number) => {
  return type === 2 ? 'primary' : 'success'
}

const formatDateTime = (datetime?: string) => {
  return datetime ? dayjs(datetime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatDate = (datetime?: string) => {
  return datetime ? dayjs(datetime).format('MM-DD') : '-'
}

// 事件处理
const goBack = () => {
  router.push('/event/list')
}

const editEvent = () => {
  router.push(`/event/edit/${event.value.id}`)
}

const viewSimilarEvent = (id: number) => {
  router.push(`/event/detail/${id}`)
}

const closeMap = () => {
  showMap.value = false
  if (eventMap) {
    eventMap.dispose()
    eventMap = null
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.event-description {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  margin: 0;
}

.no-similar {
  padding: 40px 20px;
}

.similar-events {
  max-height: 400px;
  overflow-y: auto;
}

.similar-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.similar-item:hover {
  background-color: #f8f9fa;
}

.similar-item:last-child {
  border-bottom: none;
}

.similar-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.similar-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  gap: 8px;
}

.similar-time,
.similar-location {
  flex-shrink: 0;
}

.similar-type {
  flex-shrink: 0;
}

.event-map {
  height: 500px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-container {
    padding: 10px;
  }
  
  .flex-between {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .event-map {
    height: 400px;
  }
  
  .similar-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style> 