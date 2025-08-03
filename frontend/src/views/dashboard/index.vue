<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">仪表板</h2>
      <p class="page-description">系统概览和数据统计</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6" :lg="6">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon size="28"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalEvents }}</div>
            <div class="stat-label">总事件数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <div class="stat-card">
          <div class="stat-icon today">
            <el-icon size="28"><Plus /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.todayEvents }}</div>
            <div class="stat-label">今日新增</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <div class="stat-card">
          <div class="stat-icon manual">
            <el-icon size="28"><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.manualEvents }}</div>
            <div class="stat-label">人工录入</div>
          </div>
        </div>
      </el-col>
      
      <el-col :xs="12" :sm="6" :lg="6">
        <div class="stat-card">
          <div class="stat-icon auto">
            <el-icon size="28"><Robot /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.deepseekEvents }}</div>
            <div class="stat-label">AI获取</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 事件趋势图 -->
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>事件趋势</h3>
            <el-button-group size="small">
              <el-button :type="trendPeriod === '7d' ? 'primary' : ''" @click="changeTrendPeriod('7d')">
                7天
              </el-button>
              <el-button :type="trendPeriod === '30d' ? 'primary' : ''" @click="changeTrendPeriod('30d')">
                30天
              </el-button>
              <el-button :type="trendPeriod === '90d' ? 'primary' : ''" @click="changeTrendPeriod('90d')">
                90天
              </el-button>
            </el-button-group>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </el-col>
      
      <!-- 事件类型分布 -->
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <h3>事件类型分布</h3>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 地理分布和热点事件 -->
    <el-row :gutter="20" class="bottom-row">
      <!-- 地理分布 -->
      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <div class="chart-header">
            <h3>地理分布</h3>
          </div>
          <div ref="mapChartRef" class="chart-container" style="height: 400px;"></div>
        </div>
      </el-col>
      
      <!-- 热点事件 -->
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="chart-header">
            <h3>热点事件</h3>
            <el-button text @click="refreshHotEvents">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
          <div class="hot-events">
            <div
              v-for="event in hotEvents"
              :key="event.id"
              class="hot-event-item"
              @click="goToEventDetail(event.id!)"
            >
              <div class="event-title">{{ event.eventDescription }}</div>
              <div class="event-meta">
                <span class="event-time">{{ formatTime(event.eventTime) }}</span>
                <span class="event-location">{{ event.eventLocation }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { getEventList, getStats } from '@/api/event'
import type { Event, PageResult } from '@/api/event'
import dayjs from 'dayjs'
// 导入世界地图数据
import worldJson from '@/assets/map/world.json'

const router = useRouter()

// 响应式数据
const stats = ref({
  totalEvents: 0,
  todayEvents: 0,
  manualEvents: 0,
  deepseekEvents: 0
})

const hotEvents = ref<Event[]>([])
const trendPeriod = ref('7d')

// 图表实例
const trendChartRef = ref<HTMLDivElement>()
const typeChartRef = ref<HTMLDivElement>()
const mapChartRef = ref<HTMLDivElement>()

let trendChart: ECharts | null = null
let typeChart: ECharts | null = null
let mapChart: ECharts | null = null

// 生命周期
onMounted(async () => {
  await loadDashboardData()
  await nextTick()
  initCharts()
})

// 获取统计数据
const getStatsData = async () => {
  try {
    const statsResponse = await getStats()
    
    console.log('获取到统计数据:', statsResponse)
    
    // 如果有统计数据，直接使用
    if (statsResponse && statsResponse.data) {
      const data = statsResponse.data
      stats.value.totalEvents = data.totalEvents || 0
      stats.value.todayEvents = data.todayEvents || 0
      stats.value.manualEvents = data.manualEvents || 0
      stats.value.deepseekEvents = data.deepseekEvents || 0
      return true
    }
    return false
  } catch (error) {
    console.error('获取统计数据失败:', error)
    return false
  }
}

// 加载仪表板数据
const loadDashboardData = async () => {
  try {
    // 先尝试获取统计数据
    const hasStats = await getStatsData()
    
    // 如果没有统计数据，则通过事件列表计算
    if (!hasStats) {
      // 加载统计数据
      const response = await getEventList({ current: 1, size: 1000 })
      console.log('获取到事件列表响应:', response)
      
      // 检查响应格式并提取事件列表
      let events = []
      if (response && response.records) {
        // 直接使用records字段
        events = response.records
      } else if (response && response.data && response.data.records) {
        // 嵌套在data字段中
        events = response.data.records
      } else if (Array.isArray(response)) {
        // 直接是数组
        events = response
      }
      
      console.log('提取的事件列表:', events)
      
      stats.value.totalEvents = events.length
      stats.value.todayEvents = events.filter(event => 
        dayjs(event.eventTime).isAfter(dayjs().startOf('day'))
      ).length
      stats.value.manualEvents = events.filter(event => 
        event.sourceType === 2
      ).length
      stats.value.deepseekEvents = events.filter(event => 
        event.sourceType === 1
      ).length
      
      // 加载热点事件（最新的10个）
      if (events.length > 0) {
        hotEvents.value = events
          .sort((a, b) => new Date(b.eventTime || '').getTime() - new Date(a.eventTime || '').getTime())
          .slice(0, 10)
      }
    } else {
      // 如果已经有统计数据，只加载热点事件
      const response = await getEventList({ current: 1, size: 10, sortField: 'eventTime', sortOrder: 'desc' })
      let events = []
      if (response && response.records) {
        events = response.records
      } else if (response && response.data && response.data.records) {
        events = response.data.records
      } else if (Array.isArray(response)) {
        events = response
      }
      
      if (events.length > 0) {
        hotEvents.value = events
      }
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

// 初始化图表
const initCharts = () => {
  initTrendChart()
  initTypeChart()
  initMapChart()
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  // 模拟数据
  const dates = []
  const values = []
  
  for (let i = 6; i >= 0; i--) {
    dates.push(dayjs().subtract(i, 'day').format('MM/DD'))
    values.push(Math.floor(Math.random() * 50) + 10)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '事件数量',
      type: 'line',
      smooth: true,
      data: values,
      itemStyle: {
        color: '#409eff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      }
    }]
  }
  
  trendChart.setOption(option)
}

// 初始化类型分布图
const initTypeChart = () => {
  if (!typeChartRef.value) return
  
  typeChart = echarts.init(typeChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    series: [{
      name: '事件类型',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        { value: 335, name: '政治事件' },
        { value: 310, name: '经济事件' },
        { value: 234, name: '军事事件' },
        { value: 135, name: '社会事件' },
        { value: 148, name: '自然灾害' }
      ]
    }]
  }
  
  typeChart.setOption(option)
}

// 初始化地图
const initMapChart = () => {
  if (!mapChartRef.value) return
  
  // 注册世界地图数据
  echarts.registerMap('world', worldJson)
  
  mapChart = echarts.init(mapChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    geo: {
      map: 'world',
      roam: true,
      zoom: 1.2,
      itemStyle: {
        areaColor: '#f0f0f0',
        borderColor: '#999'
      },
      emphasis: {
        itemStyle: {
          areaColor: '#409eff'
        }
      }
    },
    series: [{
      name: '事件分布',
      type: 'scatter',
      coordinateSystem: 'geo',
      data: [
        { name: '北京', value: [116.46, 39.92, 100] },
        { name: '华盛顿', value: [-77.04, 38.91, 80] },
        { name: '伦敦', value: [0.12, 51.51, 60] },
        { name: '东京', value: [139.69, 35.69, 90] },
        { name: '莫斯科', value: [37.62, 55.75, 70] }
      ],
      symbolSize: (val: number[]) => Math.sqrt(val[2]) / 2,
      itemStyle: {
        color: '#409eff'
      }
    }]
  }
  
  mapChart.setOption(option)
}

// 切换趋势图时间段
const changeTrendPeriod = (period: string) => {
  trendPeriod.value = period
  // 重新加载趋势图数据
  initTrendChart()
}

// 刷新热点事件
const refreshHotEvents = () => {
  loadDashboardData()
}

// 跳转到事件详情
const goToEventDetail = (id: number) => {
  router.push(`/event/detail/${id}`)
}

// 格式化时间
const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm')
}
</script>

<style scoped>
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #ffffff;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.today {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.manual {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.auto {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row,
.bottom-row {
  margin-bottom: 20px;
}

.chart-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.chart-container {
  flex: 1;
  min-height: 0;
}

.hot-events {
  flex: 1;
  overflow-y: auto;
}

.hot-event-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.hot-event-item:hover {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding-left: 8px;
  padding-right: 8px;
}

.hot-event-item:last-child {
  border-bottom: none;
}

.event-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.event-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.event-time,
.event-location {
  flex: 1;
}

.event-location {
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    margin-right: 12px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .chart-card {
    height: 300px;
  }
}
</style> 