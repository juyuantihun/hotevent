<template>
  <div class="timeline-card-demo">
    <div class="demo-header">
      <h2>时间线卡片组件演示</h2>
      <p>展示带有地理坐标信息的时间线卡片</p>
    </div>
    
    <div class="demo-content">
      <div class="cards-grid">
        <TimelineCard
          v-for="timeline in demoTimelines"
          :key="timeline.id"
          :timeline="timeline"
          @view="handleView"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import TimelineCard from '../components/TimelineCard.vue'

// 演示数据
const demoTimelines = ref([
  {
    id: '1',
    title: '北京冬奥会时间线',
    description: '2022年北京冬季奥运会相关事件的时间线，包含开幕式、比赛项目、闭幕式等重要节点。',
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T12:00:00Z',
    eventCount: 15,
    region: '北京',
    coordinates: { latitude: 39.9042, longitude: 116.4074 },
    subjectCoordinate: { lat: 39.9042, lng: 116.4074 },
    objectCoordinate: { lat: 40.3769, lng: 116.6533 }, // 延庆赛区
    tags: ['体育', '奥运会', '国际赛事']
  },
  {
    id: '2',
    title: '上海疫情防控时间线',
    description: '2022年上海新冠疫情防控措施和重要事件的完整记录。',
    createdAt: '2024-01-02T14:30:00Z',
    updatedAt: '2024-01-02T16:30:00Z',
    eventCount: 28,
    region: '上海',
    coordinates: { latitude: 31.2304, longitude: 121.4737 },
    tags: ['疫情', '防控', '公共卫生']
  },
  {
    id: '3',
    title: '深圳科技创新发展',
    description: '深圳市科技创新政策和重大科技项目发展历程。',
    createdAt: '2024-01-03T09:15:00Z',
    updatedAt: '2024-01-03T11:45:00Z',
    eventCount: 12,
    region: '深圳',
    subjectCoordinate: { lat: 22.3193, lng: 114.1694 },
    objectCoordinate: { locationName: '深圳湾科技生态园' },
    tags: ['科技', '创新', '政策']
  },
  {
    id: '4',
    title: '一带一路倡议推进',
    description: '一带一路倡议的提出、发展和重要合作项目的时间线记录。',
    createdAt: '2024-01-04T16:20:00Z',
    eventCount: 35,
    region: '全球',
    coordinates: { latitude: 39.9042, longitude: 116.4074 }, // 北京
    subjectCoordinate: { lat: 41.9028, lng: 12.4964 }, // 罗马
    objectCoordinate: { lat: 55.7558, lng: 37.6176 }, // 莫斯科
    tags: ['国际合作', '经济', '基础设施']
  }
])

// 事件处理函数
const handleView = (timeline: any) => {
  ElMessage.success(`查看时间线: ${timeline.title}`)
  console.log('查看时间线:', timeline)
}

const handleEdit = (timeline: any) => {
  ElMessage.info(`编辑时间线: ${timeline.title}`)
  console.log('编辑时间线:', timeline)
}

const handleDelete = (id: string) => {
  ElMessage.warning(`删除时间线 ID: ${id}`)
  console.log('删除时间线 ID:', id)
}
</script>

<style scoped>
.timeline-card-demo {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.demo-header {
  text-align: center;
  margin-bottom: 30px;
}

.demo-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.demo-header p {
  color: #606266;
  font-size: 14px;
}

.demo-content {
  width: 100%;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .timeline-card-demo {
    padding: 10px;
  }
  
  .cards-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
}
</style>