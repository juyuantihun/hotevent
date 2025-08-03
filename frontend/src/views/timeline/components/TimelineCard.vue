<template>
  <el-card class="timeline-card" shadow="hover" @click="$emit('view', timeline)">
    <template #header>
      <div class="card-header">
        <h3 class="timeline-title">{{ timeline.title }}</h3>
        <div class="card-actions">
          <el-button 
            type="primary" 
            size="small" 
            @click.stop="$emit('view', timeline)"
            data-test="view-detail"
          >
            <el-icon><View /></el-icon>
            查看
          </el-button>
          <el-button 
            type="warning" 
            size="small" 
            @click.stop="$emit('edit', timeline)"
            data-test="edit-timeline"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click.stop="$emit('delete', timeline.id)"
            data-test="delete-timeline"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </template>
    
    <div class="card-content">
      <p class="timeline-description">{{ timeline.description || '暂无描述' }}</p>
      
      <div class="timeline-meta">
        <div class="meta-item">
          <el-icon><Calendar /></el-icon>
          <span class="timeline-time">{{ formatDateTime(timeline.createdAt) }}</span>
        </div>
        
        <div class="meta-item" v-if="timeline.region">
          <el-icon><Location /></el-icon>
          <span>{{ timeline.region }}</span>
        </div>
        
        <div class="meta-item">
          <el-icon><User /></el-icon>
          <span class="event-count">{{ timeline.eventCount || 0 }} 个事件</span>
        </div>
      </div>
      
      <!-- 地理坐标信息显示 -->
      <div class="coordinate-section" v-if="hasCoordinates(timeline)">
        <div class="coordinate-title">
          <el-icon><Position /></el-icon>
          <span>地理坐标</span>
        </div>
        
        <div class="coordinate-list">
          <div v-if="timeline.coordinates" class="coordinate-info main-coordinate">
            <span class="coordinate-label">主坐标:</span>
            <span class="coordinate-value">{{ formatCoordinates(timeline.coordinates) }}</span>
          </div>
          
          <div v-if="timeline.subjectCoordinate" class="coordinate-info subject-coordinate">
            <span class="coordinate-label">主体:</span>
            <span class="coordinate-value">{{ formatCoordinates(timeline.subjectCoordinate) }}</span>
          </div>
          
          <div v-if="timeline.objectCoordinate" class="coordinate-info object-coordinate">
            <el-icon><Flag /></el-icon>
            <span class="coordinate-label">客体:</span>
            <span class="coordinate-value">{{ formatCoordinates(timeline.objectCoordinate) }}</span>
          </div>
        </div>
      </div>
      
      <div class="timeline-tags" v-if="timeline.tags && timeline.tags.length > 0">
        <el-tag 
          v-for="tag in timeline.tags" 
          :key="tag" 
          size="small" 
          class="timeline-tag"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'
import { ElCard, ElButton, ElIcon, ElTag } from 'element-plus'
import { 
  View, Edit, Delete, Calendar, Location, User, Position, Flag 
} from '@element-plus/icons-vue'

interface Timeline {
  id: string
  title: string
  description?: string
  createdAt: string
  updatedAt?: string
  eventCount?: number
  region?: string
  coordinates?: any
  subjectCoordinate?: any
  objectCoordinate?: any
  tags?: string[]
}

interface Props {
  timeline: Timeline
}

const props = defineProps<Props>()
const emit = defineEmits<{
  view: [timeline: Timeline]
  edit: [timeline: Timeline]
  delete: [id: string]
}>()

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return ''
  }
}

// 格式化坐标信息
const formatCoordinates = (coordinates: any) => {
  if (!coordinates) return ''
  
  if (typeof coordinates === 'object') {
    const lat = coordinates.latitude || coordinates.lat
    const lng = coordinates.longitude || coordinates.lng || coordinates.lon
    
    if (lat !== undefined && lng !== undefined) {
      return `${Number(lat).toFixed(4)}, ${Number(lng).toFixed(4)}`
    }
    
    // 如果有位置名称，也显示出来
    if (coordinates.locationName) {
      return coordinates.locationName
    }
  }
  
  return String(coordinates)
}

// 检查是否有坐标信息
const hasCoordinates = (timeline: Timeline) => {
  return timeline.coordinates || timeline.subjectCoordinate || timeline.objectCoordinate
}
</script>

<style scoped>
.timeline-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin-bottom: 16px;
}

.timeline-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timeline-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 10px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-content {
  padding-top: 10px;
}

.timeline-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.timeline-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.meta-item .el-icon {
  margin-right: 6px;
  color: #409eff;
}

.timeline-time {
  font-weight: 500;
}

.event-count {
  font-weight: 600;
  color: #67c23a;
}

.timeline-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.timeline-tag {
  font-size: 12px;
}

/* 地理坐标信息样式 */
.coordinate-section {
  margin-top: 15px;
  padding: 10px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.coordinate-title {
  display: flex;
  align-items: center;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.coordinate-title .el-icon {
  margin-right: 6px;
  color: #409eff;
}

.coordinate-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coordinate-info {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #606266;
}

.coordinate-info .el-icon {
  margin-right: 4px;
  color: #f56c6c;
  font-size: 12px;
}

.coordinate-label {
  margin-right: 6px;
  font-weight: 500;
  color: #303133;
  min-width: 40px;
}

.coordinate-value {
  font-family: 'Courier New', monospace;
  color: #409eff;
  font-weight: 500;
  font-size: 11px;
}

.main-coordinate .coordinate-value {
  color: #67c23a;
}

.subject-coordinate .coordinate-value {
  color: #e6a23c;
}

.object-coordinate .coordinate-value {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .card-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>