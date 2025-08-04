<template>
  <div class="manual-add-event-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="event-form"
    >
      <!-- 事件基本信息 -->
      <el-card shadow="hover" class="form-card">
        <template #header>
          <span class="card-title">事件基本信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="事件时间" prop="eventTime" required>
              <el-date-picker
                v-model="formData.eventTime"
                type="datetime"
                placeholder="选择事件时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="事件地点" prop="eventLocation" required>
              <el-cascader
                v-model="formData.locationCodes"
                :options="locationOptions"
                :props="{
                  checkStrictly: true,
                  label: 'dictName',
                  value: 'dictCode',
                  children: 'children'
                }"
                @change="handleLocationChange"
                placeholder="请选择事件地点"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="事件类型" prop="eventType" required>
              <el-select
                v-model="formData.eventType"
                placeholder="请选择事件类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in eventTypes"
                  :key="item.dictCode"
                  :label="item.dictName"
                  :value="item.dictName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="事件主体" prop="subject" required>
              <el-select
                v-model="formData.subject"
                placeholder="请选择事件主体"
                style="width: 100%"
              >
                <el-option
                  v-for="item in subjects"
                  :key="item.dictCode"
                  :label="item.dictName"
                  :value="item.dictName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="事件客体" prop="object" required>
              <el-select
                v-model="formData.object"
                placeholder="请选择事件客体"
                style="width: 100%"
              >
                <el-option
                  v-for="item in objects"
                  :key="item.dictCode"
                  :label="item.dictName"
                  :value="item.dictName"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="关系类型" prop="relationType">
              <el-select
                v-model="formData.relationType"
                placeholder="请选择关系类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in relationTypes"
                  :key="item.dictCode"
                  :label="item.dictName"
                  :value="item.dictName"
                />
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="关系名称" prop="relationName">
              <el-input
                v-model="formData.relationName"
                placeholder="请输入关系名称"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="强度等级" prop="intensityLevel">
              <el-select
                v-model="formData.intensityLevel"
                placeholder="请选择强度等级"
                style="width: 100%"
              >
                <el-option
                  v-for="level in intensityLevels"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="事件描述" prop="eventDescription">
              <el-input
                v-model="formData.eventDescription"
                type="textarea"
                :rows="4"
                placeholder="自动生成或手动输入事件描述"
                maxlength="500"
                show-word-limit
              />
              <div class="description-actions">
                <el-button
                  type="primary"
                  size="small"
                  :loading="generating"
                  @click="generateDescription"
                >
                  自动生成描述
                </el-button>
                <el-button size="small" @click="formData.eventDescription = ''">
                  <el-icon><Delete /></el-icon>
                  清空描述
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="关键词">
              <el-input
                v-model="formData.keywords"
                placeholder="多个关键词用逗号分隔"
                show-word-limit
                maxlength="200"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="经度">
              <el-input-number
                v-model="formData.longitude"
                :precision="6"
                :min="-180"
                :max="180"
                placeholder="经度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="纬度">
              <el-input-number
                v-model="formData.latitude"
                :precision="6"
                :min="-90"
                :max="90"
                placeholder="纬度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          
          <el-col :span="8">
            <el-form-item label="来源类型">
              <el-select v-model="formData.sourceType" style="width: 100%">
                <el-option label="人工录入" :value="2" />
                <el-option label="DeepSeek获取" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="resetForm">重置表单</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          <el-icon><Plus /></el-icon>
          添加到时间线
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useDictionaryStore } from '@/store/modules/dictionary'
import type { Dictionary } from '@/api/dictionary'
import dayjs from 'dayjs'

// Props
interface Props {
  timelineId?: string | number
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  success: []
}>()

// 字典存储
const dictionaryStore = useDictionaryStore()

// 响应式数据
const formRef = ref<FormInstance>()
const submitting = ref(false)
const generating = ref(false)

// 表单数据
const formData = reactive({
  eventTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  locationCodes: [] as string[],
  eventLocation: '',
  eventType: '',
  subject: '',
  object: '',
  relationType: '',
  relationName: '',
  intensityLevel: 1,
  eventDescription: '',
  keywords: '',
  longitude: undefined as number | undefined,
  latitude: undefined as number | undefined,
  sourceType: 2,
  status: 1
})

// 字典数据
const locationOptions = ref<Dictionary[]>([])
const eventTypes = ref<Dictionary[]>([])
const subjects = ref<Dictionary[]>([])
const objects = ref<Dictionary[]>([])
const relationTypes = ref<Dictionary[]>([])

// 强度等级选项
const intensityLevels = [
  { value: 1, label: '1级 - 极弱' },
  { value: 2, label: '2级 - 弱' },
  { value: 3, label: '3级 - 中等' },
  { value: 4, label: '4级 - 强' },
  { value: 5, label: '5级 - 极强' }
]

// 表单验证规则
const formRules: FormRules = {
  eventTime: [
    { required: true, message: '请选择事件时间', trigger: 'change' }
  ],
  eventLocation: [
    { required: true, message: '请选择事件地点', trigger: 'change' }
  ],
  eventType: [
    { required: true, message: '请选择事件类型', trigger: 'change' }
  ],
  subject: [
    { required: true, message: '请选择事件主体', trigger: 'change' }
  ],
  object: [
    { required: true, message: '请选择事件客体', trigger: 'change' }
  ],
  eventDescription: [
    { required: true, message: '请输入事件描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10-500个字符之间', trigger: 'blur' }
  ]
}

// 方法
const loadDictionaryData = async () => {
  try {
    // 并行加载所有字典数据
    const [eventTypeList, subjectList, objectList, relationTypeList, locationTree] = await Promise.all([
      dictionaryStore.getEventTypes(),
      dictionaryStore.getSubjects(),
      dictionaryStore.getObjects(),
      dictionaryStore.getRelationTypes(),
      dictionaryStore.fetchLocationTree()
    ])

    eventTypes.value = eventTypeList
    subjects.value = subjectList
    objects.value = objectList
    relationTypes.value = relationTypeList
    locationOptions.value = locationTree
  } catch (error) {
    console.error('加载字典数据失败:', error)
    ElMessage.error('加载字典数据失败')
  }
}

const handleLocationChange = (value: string[]) => {
  if (!value || value.length === 0) {
    formData.eventLocation = ''
    return
  }
  
  // 根据选择的地点代码查找对应的名称
  const getLocationName = (code: string): string => {
    const findLocation = (options: Dictionary[]): string | undefined => {
      for (const option of options) {
        if (option.dictCode === code) {
          return option.dictName
        }
        if (option.children) {
          const found = findLocation(option.children)
          if (found) return found
        }
      }
      return undefined
    }
    return findLocation(locationOptions.value) || code
  }
  
  // 将选择的地点代码转换为名称并用 ' - ' 连接
  formData.eventLocation = value.map(code => getLocationName(code)).join(' - ')
}

const generateDescription = () => {
  const { subject, eventType, object } = formData
  if (subject && eventType && object) {
    // 现在 subject、eventType、object 直接就是中文名称
    formData.eventDescription = `${subject}${eventType}${object}`
    ElMessage.success('描述生成成功')
  } else {
    ElMessage.warning('请先填写事件主体、事件类型和事件客体')
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(formData, {
    eventTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    locationCodes: [],
    eventLocation: '',
    eventType: '',
    subject: '',
    object: '',
    relationType: '',
    relationName: '',
    intensityLevel: 1,
    eventDescription: '',
    keywords: '',
    longitude: undefined,
    latitude: undefined,
    sourceType: 2,
    status: 1
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!props.timelineId) {
      ElMessage.error('缺少时间线ID')
      return
    }

    submitting.value = true

    // 准备提交数据，过滤掉前端专用字段
    const { locationCodes, keywords, ...submitData } = formData
    
    // 处理关键词字段 - 将字符串转换为数组
    const eventData = {
      ...submitData,
      keywords: keywords ? keywords.split(',').map(k => k.trim()).filter(k => k) : []
    }

    // 创建事件
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
      
      // 将事件添加到时间线
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
        resetForm()
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
    submitting.value = false
  }
}

// 监听主体、客体、类型变化自动生成描述
watch([() => formData.subject, () => formData.eventType, () => formData.object], () => {
  if (formData.subject && formData.eventType && formData.object) {
    if (!formData.eventDescription) {
      generateDescription()
    }
  }
})

// 生命周期
onMounted(() => {
  loadDictionaryData()
})
</script>

<style scoped>
.manual-add-event-form {
  padding: 20px 0;
}

.form-card {
  margin-bottom: 20px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.description-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-card__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
</style>