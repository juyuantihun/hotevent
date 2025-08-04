<template>
  <div class="select-event-list">
    <div class="filter-container">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索事件标题"
        clearable
        style="width: 300px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button @click="loadEvents" style="margin-left: 10px">搜索</el-button>
    </div>

    <div class="event-list-container">
      <el-table
        :data="events"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        height="400"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="事件标题" min-width="200" />
        <el-table-column prop="eventTime" label="事件时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.eventTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      </el-table>
    </div>

    <div class="form-actions">
      <div class="selection-info">
        已选择 {{ selectedEvents.length }} 个事件
      </div>
      <div class="action-buttons">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          @click="handleAddSelected"
          :loading="addingLoading"
          :disabled="selectedEvents.length === 0"
        >
          添加选中事件到时间线
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

interface Props {
  timelineId?: string | number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const loading = ref(false)
const addingLoading = ref(false)
const events = ref<any[]>([])
const selectedEvents = ref<any[]>([])
const searchKeyword = ref('')

const loadEvents = async () => {
  loading.value = true
  try {
    const response = await fetch('/api/events?page=1&size=50')
    
    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 200) {
      events.value = result.data.records || result.data.list || []
    } else {
      throw new Error(result.message || '获取事件列表失败')
    }
  } catch (error: any) {
    console.error('加载事件列表失败:', error)
    ElMessage.error(error.message || '加载事件列表失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedEvents.value = selection
}

const handleCancel = () => {
  emit('cancel')
}

const handleAddSelected = async () => {
  if (selectedEvents.value.length === 0) {
    ElMessage.warning('请先选择要添加的事件')
    return
  }

  if (!props.timelineId) {
    ElMessage.error('缺少时间线ID')
    return
  }

  addingLoading.value = true

  try {
    const promises = selectedEvents.value.map(event =>
      fetch(`/api/timelines/${props.timelineId}/events`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ eventId: event.id })
      })
    )

    const responses = await Promise.all(promises)
    const results = await Promise.all(responses.map(response => response.json()))
    
    const successCount = results.filter(result => result.code === 200).length
    const failCount = results.length - successCount

    if (successCount > 0) {
      ElMessage.success(`成功添加 ${successCount} 个事件到时间线${failCount > 0 ? `，${failCount} 个失败` : ''}`)
      emit('success')
    } else {
      ElMessage.error('所有事件添加失败')
    }
  } catch (error: any) {
    console.error('添加事件到时间线失败:', error)
    ElMessage.error(error.message || '添加事件失败，请稍后重试')
  } finally {
    addingLoading.value = false
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return dateStr
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.select-event-list {
  padding: 20px 0;
}

.filter-container {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.event-list-container {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.selection-info {
  color: #606266;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}
</style>