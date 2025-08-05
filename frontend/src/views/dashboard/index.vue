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
            <el-icon size="28">
              <Document />
            </el-icon>
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
            <el-icon size="28">
              <Plus />
            </el-icon>
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
            <el-icon size="28">
              <Edit />
            </el-icon>
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
            <el-icon size="28">
              <Robot />
            </el-icon>
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
            <el-button text @click="refreshEventTypeData">
              <el-icon>
                <Refresh />
              </el-icon>
            </el-button>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 地理分布和统计 -->
    <el-row :gutter="20" class="bottom-row">
      <!-- 地理分布地图 -->
      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <div class="chart-header">
            <h3>地理分布</h3>
            <div class="geo-stats">
              <span class="stat-item">{{ geographicData.totalCountries }}个国家/地区</span>
              <span class="stat-item">{{ geographicData.totalEvents }}个事件</span>
            </div>
          </div>
          <div ref="mapChartRef" class="chart-container" style="height: 400px;"></div>
        </div>
      </el-col>

      <!-- 国家统计和热点事件 -->
      <el-col :xs="24" :lg="8">
        <!-- 国家统计 -->
        <div class="chart-card" style="height: 200px; margin-bottom: 20px;">
          <div class="chart-header">
            <h3>国家统计</h3>
            <el-button text @click="refreshGeographicData">
              <el-icon>
                <Refresh />
              </el-icon>
            </el-button>
          </div>
          <div class="country-stats">
            <div v-for="country in geographicData.countryStats.slice(0, 8)" :key="country.name" class="country-item">
              <div class="country-name">{{ country.name }}</div>
              <div class="country-bar">
                <div class="country-bar-fill" :style="{
                  width: `${(country.value / Math.max(...geographicData.countryStats.map((c: CountryStats) => c.value), 1)) * 100}%`
                }"></div>
              </div>
              <div class="country-value">{{ country.value }}</div>
            </div>
          </div>
        </div>

        <!-- 热点事件 -->
        <div class="chart-card" style="height: 200px;">
          <div class="chart-header">
            <h3>热点事件</h3>
            <el-button text @click="refreshHotEvents">
              <el-icon>
                <Refresh />
              </el-icon>
            </el-button>
          </div>
          <div class="hot-events">
            <div v-for="event in hotEvents.slice(0, 5)" :key="event.id" class="hot-event-item"
              @click="event.id && goToEventDetail(event.id)">
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
import { getEventList, getStats, getGeographicStats, getEventTypeStats } from '@/api/event'
import type { Event, PageResult, StatsData, GeographicStatsData, EventTypeStatsData } from '@/api/event'
import dayjs from 'dayjs'
// 导入世界地图数据
import worldJson from '@/assets/map/world.json'

// 定义地理数据类型
interface CountryStats {
  name: string
  value: number
}

interface MapDataPoint {
  name: string
  value: [number, number, number]
}

interface GeographicData {
  countryStats: CountryStats[]
  mapData: MapDataPoint[]
  totalCountries: number
  totalEvents: number
}

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
const geographicData = ref<GeographicData>({
  countryStats: [],
  mapData: [],
  totalCountries: 0,
  totalEvents: 0
})

const eventTypeData = ref<EventTypeStatsData>({
  typeDistribution: [],
  totalCount: 0,
  typeCount: 0
})

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
  // 数据加载完成后更新图表
  await nextTick()
  updateCharts()
})

// 获取统计数据
const getStatsData = async () => {
  try {
    const statsResponse = await getStats()

    console.log('获取到统计数据:', statsResponse)

    // API拦截器已经处理了响应，直接使用数据
    if (statsResponse) {
      stats.value.totalEvents = statsResponse.totalEvents || 0
      stats.value.todayEvents = statsResponse.todayEvents || 0
      stats.value.manualEvents = statsResponse.manualEvents || 0
      stats.value.deepseekEvents = statsResponse.deepseekEvents || 0
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

    // 加载地理分布数据
    await loadGeographicData()

    // 加载事件类型分布数据
    await loadEventTypeData()

    // 如果没有统计数据，则通过事件列表计算
    if (!hasStats) {
      // 加载统计数据
      const response = await getEventList({ current: 1, size: 1000 })
      console.log('获取到事件列表响应:', response)

      // 检查响应格式并提取事件列表
      let events: Event[] = []
      if (response && response.records) {
        // 直接使用records字段
        events = response.records
      } else if (Array.isArray(response)) {
        // 直接是数组
        events = response as Event[]
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
      let events: Event[] = []
      if (response && response.records) {
        events = response.records
      } else if (Array.isArray(response)) {
        events = response as Event[]
      }

      if (events.length > 0) {
        hotEvents.value = events
      }
    }
  } catch (error) {
    console.error('加载仪表板数据失败:', error)
  }
}

// 加载地理分布数据
const loadGeographicData = async () => {
  try {
    const response = await getGeographicStats()
    console.log('获取到地理分布数据:', response)

    // API拦截器已经处理了响应，直接使用数据
    if (response) {
      geographicData.value = response
      console.log('地理数据已更新:', geographicData.value)
    } else {
      console.warn('地理分布数据为空:', response)
      // 设置默认值避免显示错误
      geographicData.value = {
        countryStats: [],
        mapData: [],
        totalCountries: 0,
        totalEvents: 0
      }
    }
  } catch (error) {
    console.error('加载地理分布数据失败:', error)
    // 设置默认值避免显示错误
    geographicData.value = {
      countryStats: [],
      mapData: [],
      totalCountries: 0,
      totalEvents: 0
    }
  }
}

// 加载事件类型分布数据
const loadEventTypeData = async () => {
  try {
    const response = await getEventTypeStats()
    console.log('获取到事件类型分布数据:', response)

    // API拦截器已经处理了响应，直接使用数据
    if (response) {
      eventTypeData.value = response
      console.log('事件类型数据已更新:', eventTypeData.value)
    } else {
      console.warn('事件类型分布数据为空:', response)
      // 设置默认值避免显示错误
      eventTypeData.value = {
        typeDistribution: [],
        totalCount: 0,
        typeCount: 0
      }
    }
  } catch (error) {
    console.error('加载事件类型分布数据失败:', error)
    // 设置默认值避免显示错误
    eventTypeData.value = {
      typeDistribution: [],
      totalCount: 0,
      typeCount: 0
    }
  }
}

// 初始化图表
const initCharts = () => {
  initTrendChart()
  initTypeChart()
  initMapChart()
}

// 更新所有图表
const updateCharts = () => {
  updateMapChart()
  updateTypeChart()
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
  updateTypeChart()
}

// 更新类型分布图
const updateTypeChart = () => {
  if (!typeChart) return

  // 定义美观的颜色配置
  const colors = [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452'
  ]

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}个 ({d}%)',
      backgroundColor: 'rgba(50, 50, 50, 0.8)',
      borderColor: '#333',
      textStyle: {
        color: '#fff'
      }
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      top: '8%',
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: {
        fontSize: 12
      }
    },
    color: colors,
    series: [{
      name: '事件类型',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '60%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        position: 'outside',
        formatter: '{b}\n{c}个',
        fontSize: 11,
        color: '#666'
      },
      labelLine: {
        show: true,
        length: 15,
        length2: 8,
        smooth: true
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        },
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: eventTypeData.value.typeDistribution || []
    }]
  }

  typeChart.setOption(option, true)
}

// 初始化地图
const initMapChart = () => {
  if (!mapChartRef.value) return

  // 注册世界地图数据
  echarts.registerMap('world', worldJson as any)

  mapChart = echarts.init(mapChartRef.value)

  updateMapChart()
}

// 中文到英文国家名称映射
const countryNameMapping: Record<string, string> = {
  '中国': 'China',
  '美国': 'United States of America',
  '英国': 'United Kingdom',
  '日本': 'Japan',
  '韩国': 'South Korea',
  '朝鲜': 'North Korea',
  '法国': 'France',
  '德国': 'Germany',
  '俄罗斯': 'Russia',
  '乌克兰': 'Ukraine',
  '印度': 'India',
  '巴西': 'Brazil',
  '加拿大': 'Canada',
  '澳大利亚': 'Australia',
  '以色列': 'Israel',
  '伊朗': 'Iran',
  '土耳其': 'Turkey',
  '意大利': 'Italy',
  '西班牙': 'Spain',
  '比利时': 'Belgium',
  '荷兰': 'Netherlands',
  '瑞士': 'Switzerland',
  '瑞典': 'Sweden',
  '挪威': 'Norway',
  '丹麦': 'Denmark',
  '芬兰': 'Finland',
  '泰国': 'Thailand',
  '新加坡': 'Singapore',
  '马来西亚': 'Malaysia',
  '柬埔寨': 'Cambodia',
  '老挝': 'Laos',
  '巴勒斯坦': 'Palestine',
  '伊拉克': 'Iraq',
  '叙利亚': 'Syria',
  '国际组织': 'International',
  '其他': 'Other'
}

// 更新地图数据
const updateMapChart = () => {
  if (!mapChart) return

  // 准备地图数据
  const mapData = geographicData.value.mapData || []
  const countryStats = geographicData.value.countryStats || []

  console.log('更新地图数据:', { mapData: mapData.length, countryStats: countryStats.length })

  // 为地图着色准备数据 - 将中文国家名转换为英文以匹配地图
  const mapColorData = countryStats.map((item: CountryStats) => ({
    name: countryNameMapping[item.name] || item.name, // 转换为英文名称
    value: item.value,
    chineseName: item.name // 保留中文名称用于显示
  }))

  const option = {
    title: {
      text: `全球事件分布 (${geographicData.value.totalCountries}个国家/地区)`,
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 14,
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params: any) {
        if (params.seriesType === 'map') {
          // 显示中文名称
          const chineseName = params.data?.chineseName || params.name
          return `${chineseName}<br/>事件数量: ${params.value || 0}`
        } else if (params.seriesType === 'scatter') {
          return `${params.name}<br/>坐标: [${params.value[0].toFixed(2)}, ${params.value[1].toFixed(2)}]`
        }
        return `${params.name}<br/>事件数量: ${params.value || 0}`
      }
    },
    visualMap: {
      min: 0,
      max: Math.max(...countryStats.map((item: CountryStats) => item.value), 10),
      left: 'left',
      top: 'bottom',
      text: ['高', '低'],
      calculable: true,
      inRange: {
        color: ['#e0f3ff', '#409eff', '#1f5582']
      }
    },
    series: [
      {
        name: '事件数量',
        type: 'map',
        map: 'world',
        roam: true,
        zoom: 1.2,
        center: [0, 20],
        data: mapColorData,
        itemStyle: {
          borderColor: '#999',
          borderWidth: 0.5
        },
        emphasis: {
          itemStyle: {
            borderColor: '#333',
            borderWidth: 1
          }
        }
      },
      {
        name: '事件位置',
        type: 'scatter',
        coordinateSystem: 'geo',
        data: mapData,
        symbolSize: (val: number[]) => Math.max(Math.sqrt(val[2]) * 3, 6),
        itemStyle: {
          color: '#ff6b6b',
          shadowBlur: 10,
          shadowColor: 'rgba(255, 107, 107, 0.5)'
        },
        emphasis: {
          itemStyle: {
            color: '#ff4757',
            shadowBlur: 15
          }
        }
      }
    ],
    geo: {
      map: 'world',
      roam: true,
      zoom: 1.2,
      center: [0, 20],
      itemStyle: {
        areaColor: 'transparent',
        borderColor: 'transparent'
      },
      emphasis: {
        itemStyle: {
          areaColor: 'transparent'
        }
      },
      silent: true
    }
  }

  mapChart.setOption(option, true)
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

// 刷新地理分布数据
const refreshGeographicData = async () => {
  await loadGeographicData()
  await nextTick()
  updateCharts()
}

// 刷新事件类型分布数据
const refreshEventTypeData = async () => {
  await loadEventTypeData()
  await nextTick()
  updateTypeChart()
}

// 跳转到事件详情
const goToEventDetail = (id: string | number) => {
  router.push(`/event/detail/${id}`)
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return '--'
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

.geo-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  font-size: 12px;
  color: #666;
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
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
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 4px;
  margin: 2px 0;
}

.hot-event-item:hover {
  background-color: #f8f9fa;
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hot-event-item:last-child {
  border-bottom: none;
}

.event-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.event-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  gap: 8px;
}

.event-time {
  flex: 0 0 auto;
  white-space: nowrap;
}

.event-location {
  flex: 1;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.country-stats {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

.country-item {
  display: flex;
  align-items: center;
  padding: 6px 0;
  gap: 8px;
}

.country-name {
  font-size: 12px;
  color: #303133;
  min-width: 60px;
  flex-shrink: 0;
}

.country-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.country-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.country-value {
  font-size: 12px;
  color: #666;
  font-weight: 600;
  min-width: 20px;
  text-align: right;
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