<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="flex-between">
        <div>
          <h2 class="page-title">{{ isEdit ? '编辑事件' : '录入事件' }}</h2>
          <p class="page-description">{{ isEdit ? '修改事件信息' : '录入新的热点事件信息' }}</p>
        </div>
        <div>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </div>
    </div>

    <!-- 操作模式切换 -->
    <el-card v-if="false" class="mode-card" shadow="hover">
      <el-radio-group>
        <el-radio-button label="single">单个录入</el-radio-button>
        <el-radio-button label="batch">批量录入</el-radio-button>
      </el-radio-group>
      <span class="mode-description">逐个录入事件信息</span>
    </el-card>

    <!-- 单个录入表单 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="event-form"
    >
      <el-card shadow="hover">
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
              <div class="flex items-center gap-2 mt-2">
                <el-button
                  type="primary"
                  :loading="generating"
                  @click="generateDescription"
                >
                  自动生成描述
                </el-button>
                <el-button @click="formData.eventDescription = ''">
                  <el-icon><Delete /></el-icon>清空描述
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

      <!-- 关联关系设置 - 已隐藏 -->
      <el-card v-if="false" shadow="hover" class="relation-card">
        <template #header>
          <div class="flex-between">
            <span class="card-title">关联关系设置</span>
            <el-button type="primary" size="small" @click="addRelation">
              <el-icon><Plus /></el-icon>
              添加关联
            </el-button>
          </div>
        </template>
        
        <div v-if="relations.length === 0" class="no-relation">
          暂无关联关系，点击上方按钮添加事件关联
        </div>
        
        <div v-for="(relation, index) in relations" :key="index" class="relation-item">
          <el-row :gutter="16" align="middle">
            <el-col :span="5">
              <el-select
                v-model="relation.targetEventId"
                placeholder="选择关联事件"
                filterable
                remote
                :remote-method="searchEvents"
                style="width: 100%"
              >
                <el-option
                  v-for="event in searchEventList"
                  :key="event.id"
                  :label="event.eventDescription"
                  :value="event.id"
                />
              </el-select>
            </el-col>
            
            <el-col :span="3">
              <el-select v-model="relation.relationType" placeholder="关系类型" style="width: 100%">
                <el-option
                  v-for="type in relationTypes"
                  :key="type.dictCode"
                  :label="type.dictName"
                  :value="type.dictCode"
                />
              </el-select>
            </el-col>
            
            <el-col :span="4">
              <el-input
                v-model="relation.relationName"
                placeholder="关系名称（必填）"
                style="width: 100%"
              />
            </el-col>
            
            <el-col :span="3">
              <el-input-number
                v-model="relation.intensityLevel"
                placeholder="强度级别"
                :min="1"
                :max="10"
                controls-position="right"
                style="width: 100%"
              />
            </el-col>
            
            <el-col :span="6">
              <el-input
                v-model="relation.relationDescription"
                placeholder="关系描述（可选）"
                style="width: 100%"
              />
            </el-col>
            
            <el-col :span="3">
              <el-button type="danger" @click="removeRelation(index)" circle>
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="resetForm">重置</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存修改' : '确认录入' }}
        </el-button>
      </div>
    </el-form>

    <!-- 批量录入表格 -->
    <el-card v-if="false" shadow="hover">
      <template #header>
        <div class="flex-between">
          <span class="card-title">批量录入事件</span>
          <div>
            <el-button @click="addBatchRow">
              <el-icon><Plus /></el-icon>
              添加行
            </el-button>
            <el-button type="primary" @click="submitBatch" :loading="submitting">
              批量提交
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="batchData" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column label="事件时间" width="180">
          <template #default="{ row, $index }">
            <el-date-picker
              v-model="row.eventTime"
              type="datetime"
              placeholder="事件时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              size="small"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="事件地点" width="150">
          <template #default="{ row }">
            <el-cascader
              v-model="row.locationCodes"
              :options="locationOptions"
              :props="{
                checkStrictly: true,
                label: 'dictName',
                value: 'dictCode',
                children: 'children'
              }"
              @change="(value: string[]) => handleBatchLocationChange(value, row)"
              placeholder="选择事件地点"
              size="small"
              style="width: 100%"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="事件类型" width="120">
          <template #default="{ row }">
            <el-select v-model="row.eventType" placeholder="类型" size="small" style="width: 100%">
              <el-option
                v-for="item in eventTypes"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="主体" width="120">
          <template #default="{ row }">
            <el-select v-model="row.subject" placeholder="主体" size="small" style="width: 100%">
              <el-option
                v-for="item in subjects"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="客体" width="120">
          <template #default="{ row }">
            <el-select v-model="row.object" placeholder="客体" size="small" style="width: 100%">
              <el-option
                v-for="item in objects"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="关系类型" width="120">
          <template #default="{ row }">
            <el-select v-model="row.relationType" placeholder="关系类型" size="small" style="width: 100%">
              <el-option
                v-for="item in relationTypes"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictName"
              />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="关系名称" width="120">
          <template #default="{ row }">
            <el-input
              v-model="row.relationName"
              placeholder="关系名称"
              size="small"
              maxlength="100"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="强度等级" width="120">
          <template #default="{ row }">
            <el-select v-model="row.intensityLevel" placeholder="强度等级" size="small" style="width: 100%">
              <el-option
                v-for="level in intensityLevels"
                :key="level.value"
                :label="level.label"
                :value="level.value"
              />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column label="事件描述" min-width="200">
          <template #default="{ row }">
            <el-input
              v-model="row.eventDescription"
              type="textarea"
              :rows="2"
              placeholder="事件描述"
              size="small"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ $index }">
            <el-button
              type="danger"
              size="small"
              @click="removeBatchRow($index)"
              circle
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useDictionaryStore } from '@/store/modules/dictionary'
import type { Event, EventRelation } from '@/api/event'
import { createEvent, updateEvent, getEventDetail, createEventsBatch, getEventList } from '@/api/event'
import dayjs from 'dayjs'
import type { Dictionary } from '@/api/dictionary'

const router = useRouter()
const route = useRoute()
const dictionaryStore = useDictionaryStore()

// 响应式数据
const isEdit = computed(() => route.name === 'EventEdit')
const formRef = ref<FormInstance>()
const submitting = ref(false)
const generating = ref(false)

// 字典数据
const eventTypes = ref<Dictionary[]>([])
const subjects = ref<Dictionary[]>([])
const objects = ref<Dictionary[]>([])
const relationTypes = ref<Dictionary[]>([])
const locationOptions = ref<Dictionary[]>([])
const searchEventList = ref<Event[]>([])

// 强度等级选项
const intensityLevels = ref([
  { value: 1, label: '1级 - 极弱' },
  { value: 2, label: '2级 - 弱' },
  { value: 3, label: '3级 - 中等' },
  { value: 4, label: '4级 - 强' },
  { value: 5, label: '5级 - 极强' }
])

// 单个表单数据
const formData = ref<Event>({
  eventTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
  eventLocation: '',
  locationCodes: [], // 新增字段，用于存储级联选择的值
  eventType: '',
  eventDescription: '',
  subject: '',
  object: '',
  relationType: '',
  relationName: '',
  intensityLevel: 1,
  longitude: undefined,
  latitude: undefined,
  keywords: '',
  sourceType: 2,
  status: 1
})

// 关联关系数据
const relations = ref<Partial<EventRelation>[]>([])

// 批量数据
const batchData = ref<Event[]>([])

// 表单验证规则
const formRules: FormRules = {
  eventTime: [{ required: true, message: '请选择事件时间', trigger: 'change' }],
  eventLocation: [{ required: true, message: '请选择事件地点', trigger: 'change' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  subject: [{ required: true, message: '请选择事件主体', trigger: 'change' }],
  object: [{ required: true, message: '请选择事件客体', trigger: 'change' }]
}

// 生命周期
onMounted(async () => {
  await loadDictionaries()
  
  if (isEdit.value) {
    const id = Number(route.params.id)
    await loadEventDetail(id)
  }
})

// 监听主体、客体、类型变化自动生成描述
watch([() => formData.value.subject, () => formData.value.eventType, () => formData.value.object], () => {
  if (formData.value.subject && formData.value.eventType && formData.value.object) {
    if (!formData.value.eventDescription) {
      generateDescription()
    }
  }
})

// 处理地点选择变化
const handleLocationChange = (value: string[]) => {
  if (!value || value.length === 0) {
    formData.value.eventLocation = ''
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
  formData.value.eventLocation = value.map(code => getLocationName(code)).join(' - ')
}

// 处理批量地点选择变化
const handleBatchLocationChange = (value: string[], row: Event) => {
  if (!value || value.length === 0) {
    row.eventLocation = ''
    return
  }
  
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
  
  row.eventLocation = value.map(code => getLocationName(code)).join(' - ')
}

// 加载字典数据
const loadDictionaries = async () => {
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

// 加载事件详情
const loadEventDetail = async (id: number) => {
  try {
    const event = await getEventDetail(id)
    const codes = event.eventLocation?.split(' - ').map(location => {
      const findLocationCode = (options: Dictionary[]): string | undefined => {
        for (const option of options) {
          if (option.dictName === location) {
            return option.dictCode
          }
          if (option.children) {
            const found = findLocationCode(option.children)
            if (found) return found
          }
        }
        return undefined
      }
      return findLocationCode(locationOptions.value)
    }).filter((code): code is string => code !== undefined) || []

    // 转换字典编码为中文名称（兼容旧数据）
    const convertCodeToName = (code: string, dictList: Dictionary[]) => {
      const item = dictList.find(item => item.dictCode === code)
      return item ? item.dictName : code
    }

    formData.value = { 
      ...event,
      locationCodes: codes,
      eventType: convertCodeToName(event.eventType, eventTypes.value),
      subject: convertCodeToName(event.subject, subjects.value),
      object: convertCodeToName(event.object, objects.value),
      relationType: event.relationType ? convertCodeToName(event.relationType, relationTypes.value) : ''
    }
  } catch (error) {
    ElMessage.error('加载事件详情失败')
    goBack()
  }
}

// 自动生成事件描述
const generateDescription = () => {
  const { subject, eventType, object } = formData.value
  if (subject && eventType && object) {
    // 现在 subject、eventType、object 直接就是中文名称
    formData.value.eventDescription = `${subject}${eventType}${object}`
  }
}

// 地点建议查询
const queryLocationSuggestions = (queryString: string, callback: (suggestions: any[]) => void) => {
  const suggestions = [
    { value: '北京市' },
    { value: '上海市' },
    { value: '广州市' },
    { value: '深圳市' },
    { value: '华盛顿' },
    { value: '伦敦' },
    { value: '东京' },
    { value: '首尔' }
  ].filter(item => item.value.includes(queryString))
  
  callback(suggestions)
}

// 搜索事件
const searchEvents = async (query: string) => {
  if (!query) {
    searchEventList.value = []
    return
  }
  
  try {
    const response = await getEventList({
      current: 1,
      size: 20,
      keywords: query
    })
    searchEventList.value = response.records || []
  } catch (error) {
    console.error('搜索事件失败:', error)
  }
}

// 添加关联关系
const addRelation = () => {
  relations.value.push({
    targetEventId: undefined,
    relationType: '',
    relationName: '',
    intensityLevel: undefined,
    relationDescription: '',
    confidence: 80,
    status: 1
  })
}

// 移除关联关系
const removeRelation = (index: number) => {
  relations.value.splice(index, 1)
}

// 批量提交
const submitBatch = async () => {
  submitting.value = true
  try {
    // 处理数据格式
    const submitData = batchData.value.map(({ locationCodes, keywords, ...item }) => ({
      ...item,
      keywords: keywords ? keywords.split(',').map(k => k.trim()).filter(k => k) : []
    })) as any
    
    await createEventsBatch(submitData)
    ElMessage.success(`成功提交 ${batchData.value.length} 个事件`)
    router.push('/event/list')
  } catch (error) {
    ElMessage.error('批量提交失败')
  } finally {
    submitting.value = false
  }
}

// 返回
const goBack = () => {
  router.push('/event/list')
}

// 添加批量行
const addBatchRow = () => {
  batchData.value.push({
    eventTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    eventLocation: '',
    locationCodes: [],
    eventType: '',
    eventDescription: '',
    subject: '',
    object: '',
    relationType: '',
    relationName: '',
    intensityLevel: 1,
    sourceType: 2,
    status: 1
  })
}

// 移除批量行
const removeBatchRow = (index: number) => {
  batchData.value.splice(index, 1)
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  relations.value = []
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 过滤掉前端专用字段，并处理数据格式
      const { locationCodes, keywords, ...submitData } = formData.value
      
      // 处理关键词字段 - 将字符串转换为数组
      const processedData = {
        ...submitData,
        keywords: keywords ? keywords.split(',').map(k => k.trim()).filter(k => k) : []
      } as any
      
      if (isEdit.value) {
        await updateEvent(processedData)
        ElMessage.success('事件更新成功')
      } else {
        await createEvent(processedData)
        ElMessage.success('事件录入成功')
      }
      goBack()
    } catch (error) {
      ElMessage.error(isEdit.value ? '事件更新失败' : '事件录入失败')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.mode-card {
  margin-bottom: 20px;
}

.mode-description {
  margin-left: 16px;
  color: #909399;
  font-size: 14px;
}

.event-form {
  max-width: 1200px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.relation-card {
  margin-top: 20px;
}

.no-relation {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.relation-item {
  margin-bottom: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.relation-item:last-child {
  margin-bottom: 0;
}

.form-tip {
  margin-top: 8px;
  text-align: right;
}

.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
}

/* 批量录入样式 */
:deep(.el-table) {
  .el-input,
  .el-select,
  .el-date-picker {
    width: 100%;
  }
  
  .el-textarea .el-textarea__inner {
    resize: none;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .event-form {
    margin: 0;
  }
  
  .el-col {
    margin-bottom: 16px;
  }
  
  .relation-item .el-row .el-col {
    margin-bottom: 8px;
  }
}
</style> 