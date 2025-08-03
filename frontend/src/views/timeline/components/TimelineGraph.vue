<template>
  <div class="timeline-graph-container" v-loading="loading">
    <!-- 图表控制区域 -->
    <div class="graph-controls">
      <div class="control-group">
        <el-button-group>
          <el-button size="small" @click="zoomIn" :disabled="zoomLevel >= 2">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button size="small" @click="zoomOut" :disabled="zoomLevel <= 0.5">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button size="small" @click="resetZoom">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </el-button-group>
      </div>

      <div class="control-group">
        <el-select v-model="graphLayout" placeholder="布局" size="small" style="width: 120px;">
          <el-option label="力导向图" value="force" />
          <el-option label="环形布局" value="circular" />
          <el-option label="层次布局" value="hierarchical" />
        </el-select>
      </div>

      <div class="control-group">
        <el-switch v-model="showNodeLabels" active-text="显示标签" />
      </div>

      <div class="control-group">
        <el-select v-model="nodeTypeFilter" placeholder="节点类型" clearable size="small" style="width: 120px;">
          <el-option label="全部" value="" />
          <el-option label="源事件" value="source" />
          <el-option label="终端事件" value="terminal" />
          <el-option label="枢纽事件" value="hub" />
          <el-option label="热点事件" value="hot" />
          <el-option label="普通事件" value="normal" />
        </el-select>
      </div>

      <div class="control-group">
        <el-tooltip content="导出为图片" placement="top">
          <el-button size="small" @click="exportImage">
            <el-icon><Download /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="graph-container" ref="graphContainer">
      <v-chart 
        v-if="!loading && graphData.nodes.length > 0" 
        class="chart" 
        :option="chartOption" 
        :autoresize="true"
        @click="handleChartClick"
      />
      <div v-else-if="!loading && graphData.nodes.length === 0" class="empty-graph">
        <el-empty description="暂无图表数据" />
      </div>
    </div>

    <!-- 事件详情侧边栏 -->
    <div class="event-sidebar" :class="{ 'sidebar-visible': selectedEvent }">
      <div class="sidebar-header">
        <h4>事件详情</h4>
        <el-button @click="selectedEvent = null" text size="small">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div v-if="selectedEvent" class="sidebar-content">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="标题">
            {{ selectedEvent.name }}
          </el-descriptions-item>
          <el-descriptions-item label="时间">
            {{ formatDateTime(selectedEvent.time) }}
          </el-descriptions-item>
          <el-descriptions-item label="地点" v-if="selectedEvent.location">
            {{ selectedEvent.location }}
          </el-descriptions-item>
          <el-descriptions-item label="类型" v-if="selectedEvent.type">
            <el-tag size="small" :type="getNodeTypeTagType(selectedEvent.type)">
              {{ getNodeTypeText(selectedEvent.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="重要性" v-if="selectedEvent.importanceScore !== undefined">
            <el-progress 
              :percentage="Math.round(selectedEvent.importanceScore * 100)" 
              :color="getImportanceColor(selectedEvent.importanceScore)" 
            />
          </el-descriptions-item>
          <el-descriptions-item label="描述" v-if="selectedEvent.description">
            {{ selectedEvent.description }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 相关事件 -->
        <div class="related-events" v-if="getRelatedEvents(selectedEvent.id).length > 0">
          <h5>相关事件</h5>
          <el-table :data="getRelatedEvents(selectedEvent.id)" size="small" style="width: 100%">
            <el-table-column label="关系" width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ getRelationTypeText(row.relation) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="事件" min-width="120">
              <template #default="{ row }">
                <span class="related-event-title" @click="selectNodeById(row.targetId)">
                  {{ getNodeNameById(row.targetId) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>
<
script setup lang="ts">
import { ref, computed, onMounted, watch, reactive } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GraphChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import {
  ZoomIn, ZoomOut, Refresh, Download, Close
} from '@element-plus/icons-vue'

// 注册必要的ECharts组件
use([
  CanvasRenderer,
  GraphChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  ToolboxComponent,
  DataZoomComponent
])

// 类型定义
interface GraphNode {
  id: string
  name: string
  symbolSize: number
  value?: number
  category?: number
  time?: string
  location?: string
  type?: string
  description?: string
  importanceScore?: number
  itemStyle?: {
    color?: string
  }
  label?: {
    show?: boolean
  }
}

interface GraphLink {
  source: string
  target: string
  relation: string
  value?: number
  lineStyle?: {
    width?: number
    color?: string
    curveness?: number
  }
  label?: {
    show?: boolean
    formatter?: string
  }
}

interface GraphData {
  nodes: GraphNode[]
  links: GraphLink[]
}

interface TimelineEvent {
  id: string
  title?: string
  eventTime?: string
  location?: string
  nodeType?: string
  importanceScore?: number
  description?: string
  event?: {
    title?: string
    eventTime?: string
    location?: string
    description?: string
  }
}

interface TimelineRelation {
  id: string
  sourceId: string
  targetId: string
  type: string
  description?: string
}

// 定义组件属性
const props = defineProps<{
  nodes?: TimelineEvent[]
  relationships?: TimelineRelation[]
  loading?: boolean
}>()

// 定义组件事件
const emit = defineEmits(['node-click', 'node-select'])

// 响应式数据
const loading = ref(props.loading || false)
const graphContainer = ref<HTMLElement | null>(null)
const zoomLevel = ref(1)
const graphLayout = ref('force')
const showNodeLabels = ref(true)
const nodeTypeFilter = ref('')
const selectedEvent = ref<GraphNode | null>(null)

// 图表数据
const graphData = reactive<GraphData>({
  nodes: [],
  links: []
})

// 监听属性变化
watch(() => props.nodes, (newNodes) => {
  if (newNodes) {
    updateGraphData()
  }
}, { deep: true })

watch(() => props.relationships, (newRelationships) => {
  if (newRelationships) {
    updateGraphData()
  }
}, { deep: true })

watch(() => props.loading, (newLoading) => {
  loading.value = newLoading
})

watch([() => graphLayout.value, () => showNodeLabels.value, () => nodeTypeFilter.value], () => {
  updateChartOption()
})
// 计算属性

const chartOption = computed(() => {
  // 根据过滤条件筛选节点
  let filteredNodes = [...graphData.nodes]
  let filteredLinks = [...graphData.links]
  
  if (nodeTypeFilter.value) {
    const nodeType = nodeTypeFilter.value
    filteredNodes = graphData.nodes.filter(node => node.type === nodeType)
    const nodeIds = new Set(filteredNodes.map(node => node.id))
    filteredLinks = graphData.links.filter(link => 
      nodeIds.has(link.source) && nodeIds.has(link.target)
    )
  }
  
  // 更新节点标签显示
  filteredNodes = filteredNodes.map(node => ({
    ...node,
    label: {
      show: showNodeLabels.value
    }
  }))
  
  // 根据布局类型生成不同的图表配置
  const layoutConfig = getLayoutConfig(graphLayout.value)
  
  return {
    title: {
      show: false
    },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        if (params.dataType === 'node') {
          const node = params.data
          let tooltip = `<div style="font-weight:bold">${node.name}</div>`
          if (node.time) tooltip += `<div>时间: ${formatDateTime(node.time)}</div>`
          if (node.location) tooltip += `<div>地点: ${node.location}</div>`
          if (node.type) tooltip += `<div>类型: ${getNodeTypeText(node.type)}</div>`
          return tooltip
        } else if (params.dataType === 'edge') {
          const link = params.data
          return `<div>${getNodeNameById(link.source)} <span style="color:#909399">${getRelationTypeText(link.relation)}</span> ${getNodeNameById(link.target)}</div>`
        }
        return ''
      }
    },
    legend: {
      data: ['源事件', '终端事件', '枢纽事件', '热点事件', '普通事件'],
      selected: {
        '源事件': true,
        '终端事件': true,
        '枢纽事件': true,
        '热点事件': true,
        '普通事件': true
      }
    },
    animationDuration: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        name: '事件关系图',
        type: 'graph',
        layout: layoutConfig.layout,
        data: filteredNodes,
        links: filteredLinks,
        categories: [
          { name: '源事件' },
          { name: '终端事件' },
          { name: '枢纽事件' },
          { name: '热点事件' },
          { name: '普通事件' }
        ],
        roam: true,
        draggable: true,
        label: {
          show: showNodeLabels.value,
          position: 'right',
          formatter: '{b}'
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        },
        ...layoutConfig.options
      }
    ]
  }
})

// 生命周期钩子
onMounted(() => {
  updateGraphData()
})

// 方法
const updateGraphData = () => {
  if (!props.nodes || !props.relationships) return
  
  // 转换节点数据
  const nodes: GraphNode[] = props.nodes.map(node => {
    const nodeType = node.nodeType?.toLowerCase() || 'normal'
    const categoryMap: { [key: string]: number } = {
      'source': 0,
      'terminal': 1,
      'hub': 2,
      'hot': 3,
      'normal': 4
    }
    
    // 计算节点大小，基于重要性或默认值
    const importanceScore = node.importanceScore || 0.5
    const symbolSize = 10 + importanceScore * 20
    
    return {
      id: node.id,
      name: node.title || node.event?.title || '未命名事件',
      symbolSize,
      category: categoryMap[nodeType],
      time: node.eventTime || node.event?.eventTime,
      location: node.location || node.event?.location,
      type: nodeType,
      description: node.description || node.event?.description,
      importanceScore,
      itemStyle: {
        color: getNodeColor(nodeType)
      },
      label: {
        show: showNodeLabels.value
      }
    }
  })
  
  // 转换关系数据
  const links: GraphLink[] = props.relationships.map(rel => {
    return {
      source: rel.sourceId,
      target: rel.targetId,
      relation: rel.type,
      value: 1,
      lineStyle: {
        width: 2,
        curveness: 0.2
      },
      label: {
        show: false,
        formatter: getRelationTypeText(rel.type)
      }
    }
  })
  
  // 更新图表数据
  graphData.nodes = nodes
  graphData.links = links
}const upda
teChartOption = () => {
  // 图表选项会通过计算属性自动更新
}

const getLayoutConfig = (layout: string) => {
  switch (layout) {
    case 'circular':
      return {
        layout: 'circular',
        options: {
          circular: {
            rotateLabel: true
          }
        }
      }
    case 'hierarchical':
      return {
        layout: 'force',
        options: {
          force: {
            layoutAnimation: true,
            gravity: 0.1,
            repulsion: 100,
            edgeLength: 100
          },
          orient: 'LR',
          initialTreeDepth: 2
        }
      }
    case 'force':
    default:
      return {
        layout: 'force',
        options: {
          force: {
            layoutAnimation: true,
            gravity: 0.05,
            repulsion: 200,
            edgeLength: 150
          }
        }
      }
  }
}

const handleChartClick = (params: any) => {
  if (params.dataType === 'node') {
    const node = params.data as GraphNode
    selectedEvent.value = node
    emit('node-click', node)
    emit('node-select', node.id)
  }
}

const selectNodeById = (id: string) => {
  const node = graphData.nodes.find(n => n.id === id)
  if (node) {
    selectedEvent.value = node
    emit('node-select', id)
  }
}

const getNodeNameById = (id: string) => {
  const node = graphData.nodes.find(n => n.id === id)
  return node ? node.name : '未知事件'
}

const getRelatedEvents = (nodeId: string) => {
  if (!graphData.links) return []
  
  return graphData.links
    .filter(link => link.source === nodeId || link.target === nodeId)
    .map(link => {
      // 确保目标ID不是当前事件ID
      const targetId = link.source === nodeId ? link.target : link.source
      return {
        ...link,
        targetId
      }
    })
}

const zoomIn = () => {
  zoomLevel.value = Math.min(zoomLevel.value + 0.2, 2)
  // 图表缩放需要通过ECharts API实现
}

const zoomOut = () => {
  zoomLevel.value = Math.max(zoomLevel.value - 0.2, 0.5)
  // 图表缩放需要通过ECharts API实现
}

const resetZoom = () => {
  zoomLevel.value = 1
  // 图表重置需要通过ECharts API实现
}

const exportImage = () => {
  try {
    // 获取图表实例
    const chartInstance = document.querySelector('.chart')?.__vue__?.chart
    if (chartInstance) {
      const dataURL = chartInstance.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff'
      })
      
      // 创建下载链接
      const link = document.createElement('a')
      link.download = `时间线关系图_${new Date().getTime()}.png`
      link.href = dataURL
      link.click()
      
      ElMessage.success('图片导出成功')
    } else {
      throw new Error('无法获取图表实例')
    }
  } catch (error) {
    console.error('导出图片失败:', error)
    ElMessage.error('导出图片失败')
  }
}

// 格式化方法
const formatDateTime = (date: string | undefined) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取节点类型标签类型和文本
const getNodeTypeTagType = (nodeType: string | undefined) => {
  const typeMap: { [key: string]: string } = {
    'source': 'success',
    'terminal': 'danger',
    'hub': 'warning',
    'hot': 'danger',
    'normal': 'info'
  }
  return typeMap[nodeType?.toLowerCase() || 'normal'] || 'info'
}

const getNodeTypeText = (nodeType: string | undefined) => {
  const textMap: { [key: string]: string } = {
    'source': '源事件',
    'terminal': '终端事件',
    'hub': '枢纽事件',
    'hot': '热点事件',
    'normal': '普通事件'
  }
  return textMap[nodeType?.toLowerCase() || 'normal'] || '普通事件'
}

// 获取节点颜色
const getNodeColor = (nodeType: string) => {
  const colorMap: { [key: string]: string } = {
    'source': '#67c23a',
    'terminal': '#f56c6c',
    'hub': '#e6a23c',
    'hot': '#ff4757',
    'normal': '#409eff'
  }
  return colorMap[nodeType] || '#409eff'
}

// 获取关系类型文本
const getRelationTypeText = (type: string) => {
  const textMap: { [key: string]: string } = {
    'cause': '导致',
    'trigger': '触发',
    'lead_to': '引发',
    'enable': '促成',
    'related': '相关',
    'follow_up': '后续'
  }
  return textMap[type] || type
}

// 获取重要性颜色
const getImportanceColor = (score: number) => {
  if (score >= 0.8) return '#f56c6c'
  if (score >= 0.6) return '#e6a23c'
  if (score >= 0.4) return '#409eff'
  return '#67c23a'
}
</script><style 
scoped>
.timeline-graph-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.graph-controls {
  display: flex;
  align-items: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 10px;
}

.control-group {
  display: flex;
  align-items: center;
}

.graph-container {
  flex: 1;
  position: relative;
  min-height: 400px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.chart {
  width: 100%;
  height: 100%;
}

.empty-graph {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.event-sidebar {
  position: absolute;
  top: 0;
  right: -350px;
  width: 350px;
  height: 100%;
  background-color: #fff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  transition: right 0.3s ease;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.event-sidebar.sidebar-visible {
  right: 0;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid #ebeef5;
}

.sidebar-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.sidebar-content {
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.related-events {
  margin-top: 20px;
}

.related-events h5 {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.related-event-title {
  color: #409eff;
  cursor: pointer;
}

.related-event-title:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .graph-controls {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .event-sidebar {
    width: 100%;
    right: -100%;
  }
}
</style>