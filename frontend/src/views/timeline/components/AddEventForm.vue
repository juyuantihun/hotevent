<template>
  <div class="add-event-form">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="事件标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入事件标题" />
      </el-form-item>
      
      <el-form-item label="事件时间" prop="eventTime">
        <el-date-picker
          v-model="form.eventTime"
          type="datetime"
          placeholder="选择事件时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="事件地点" prop="location">
        <el-input v-model="form.location" placeholder="请输入事件地点" />
      </el-form-item>

      <el-form-item label="事件描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入事件描述"
        />
      </el-form-item>
    </el-form>

    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        添加到时间线
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  timelineId?: string | number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  success: []
  cancel: []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  title: '',
  eventTime: '',
  location: '',
  description: ''
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入事件标题', trigger: 'blur' }
  ],
  eventTime: [
    { required: true, message: '请选择事件时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入事件地点', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入事件描述', trigger: 'blur' }
  ]
}

const handleCancel = () => {
  emit('cancel')
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!props.timelineId) {
      ElMessage.error('缺少时间线ID')
      return
    }

    loading.value = true

    const eventData = {
      title: form.title,
      eventTime: form.eventTime,
      location: form.location,
      description: form.description,
      nodeType: 'normal',
      importanceScore: 0.5,
      credibilityScore: 0.8
    }

    const response = await fetch('/api/events', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(eventData)
    })

    if (!response.ok) {
      throw new Error(`HTTP错误! 状态: ${response.status}`)
    }

    const result = await response.json()
    
    if (result.code === 200) {
      const eventId = result.data.id
      
      const addToTimelineResponse = await fetch(`/api/timelines/${props.timelineId}/events`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ eventId })
      })

      if (!addToTimelineResponse.ok) {
        throw new Error(`添加到时间线失败! 状态: ${addToTimelineResponse.status}`)
      }

      const addResult = await addToTimelineResponse.json()
      
      if (addResult.code === 200) {
        ElMessage.success('事件创建并添加到时间线成功')
        emit('success')
      } else {
        throw new Error(addResult.message || '添加到时间线失败')
      }
    } else {
      throw new Error(result.message || '创建事件失败')
    }
  } catch (error: any) {
    console.error('添加事件失败:', error)
    ElMessage.error(error.message || '添加事件失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.add-event-form {
  padding: 20px 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>