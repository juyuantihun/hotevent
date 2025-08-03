<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="flex-between">
        <div>
          <h2 class="page-title">批量录入事件</h2>
          <p class="page-description">同时录入多个事件信息，提高录入效率</p>
        </div>
        <div>
          <el-button @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>
      </div>
    </div>

    <!-- 录入方式选择 -->
    <el-card shadow="hover" class="method-card">
      <template #header>
        <span class="card-title">录入方式</span>
      </template>
      
      <el-radio-group v-model="inputMethod" @change="handleMethodChange">
        <el-radio-button label="manual">手动录入</el-radio-button>
        <el-radio-button label="import">文件导入</el-radio-button>
        <el-radio-button label="template">模板批量</el-radio-button>
      </el-radio-group>
      
      <div class="method-description">
        <span v-if="inputMethod === 'manual'">
          逐个添加多个事件，支持快速复制和编辑
        </span>
        <span v-else-if="inputMethod === 'import'">
          通过Excel或CSV文件批量导入事件数据
        </span>
        <span v-else>
          使用预设模板快速创建多个相关事件
        </span>
      </div>
    </el-card>

    <!-- 手动录入模式 -->
    <div v-if="inputMethod === 'manual'">
      <!-- 操作工具栏 -->
      <el-card shadow="hover" class="toolbar-card">
        <div class="toolbar">
          <div class="toolbar-left">
            <el-button type="primary" @click="addEvent">
              <el-icon><Plus /></el-icon>
              添加事件
            </el-button>
            <el-button @click="clearAll" :disabled="eventList.length === 0">
              <el-icon><Delete /></el-icon>
              清空所有
            </el-button>
            <el-button @click="importTemplate">
              <el-icon><Upload /></el-icon>
              导入模板
            </el-button>
          </div>
          <div class="toolbar-right">
            <span class="event-count">已添加 {{ eventList.length }} 个事件</span>
          </div>
        </div>
      </el-card>

      <!-- 事件列表 -->
      <el-card v-if="eventList.length > 0" shadow="hover" class="events-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">事件列表</span>
            <el-button type="success" @click="submitBatch" :loading="submitting">
              <el-icon><Check /></el-icon>
              批量提交 ({{ eventList.length }})
            </el-button>
          </div>
        </template>

        <!-- 事件关联管理 -->
        <el-card v-if="eventList.length > 1" shadow="hover" class="relation-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">事件关联设置</span>
              <el-button type="primary" @click="addRelation">
                <el-icon><Plus /></el-icon>
                添加关联
              </el-button>
            </div>
          </template>

          <div v-if="eventRelations.length === 0" class="no-relation">
            <el-empty description="暂无事件关联">
              <el-button type="primary" @click="addRelation">
                <el-icon><Plus /></el-icon>
                添加第一个关联
              </el-button>
            </el-empty>
          </div>

          <div v-else class="relations-list">
            <div 
              v-for="(relation, relationIndex) in eventRelations"
              :key="relationIndex"
              class="relation-item"
            >
              <el-row :gutter="20" align="middle">
                <el-col :span="6">
                  <el-form-item label="源事件">
                    <el-select 
                      v-model="relation.sourceIndex"
                      placeholder="选择源事件"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="(event, idx) in eventList"
                        :key="idx"
                        :label="`事件${idx + 1}: ${event.subject || '未填写'} ${event.eventType || ''} ${event.object || '未填写'}`"
                        :value="idx"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                
                <el-col :span="6">
                  <el-form-item label="关系类型">
                    <el-select 
                      v-model="relation.relationType"
                      placeholder="选择关系类型"
                      style="width: 100%"
                    >
                      <el-option label="导致" value="导致" />
                      <el-option label="影响" value="影响" />
                      <el-option label="触发" value="触发" />
                      <el-option label="关联" value="关联" />
                      <el-option label="报复" value="报复" />
                      <el-option label="支持" value="支持" />
                      <el-option label="反对" value="反对" />
                    </el-select>
                  </el-form-item>
                </el-col>
                
                <el-col :span="6">
                  <el-form-item label="目标事件">
                    <el-select 
                      v-model="relation.targetIndex"
                      placeholder="选择目标事件"
                      style="width: 100%"
                    >
                      <el-option
                        v-for="(event, idx) in eventList"
                        :key="idx"
                        :label="`事件${idx + 1}: ${event.subject || '未填写'} ${event.eventType || ''} ${event.object || '未填写'}`"
                        :value="idx"
                        :disabled="idx === relation.sourceIndex"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                
                <el-col :span="4">
                  <el-form-item label="置信度">
                    <el-slider 
                      v-model="relation.confidence"
                      :min="0"
                      :max="100"
                      :step="10"
                      show-tooltip
                    />
                  </el-form-item>
                </el-col>
                
                <el-col :span="2">
                  <el-button type="danger" @click="removeRelation(relationIndex)" circle>
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-col>
              </el-row>
              
              <el-row>
                <el-col :span="24">
                  <el-form-item label="关系描述">
                    <el-input
                      v-model="relation.relationDescription"
                      type="textarea"
                      :rows="2"
                      placeholder="描述事件之间的关系（可选）"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>

        <div class="events-list">
          <div 
            v-for="(event, index) in eventList" 
            :key="index"
            class="event-item"
          >
            <div class="event-header">
              <span class="event-number">事件 {{ index + 1 }}</span>
              <div class="event-actions">
                <el-button type="text" @click="copyEvent(index)">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
                <el-button type="text" @click="removeEvent(index)" style="color: #f56c6c">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>

                         <el-form :model="event" :rules="eventRules" :ref="(el: any) => eventRefs[index] = el" label-width="100px">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="事件时间" prop="eventTime" required>
                    <el-date-picker
                      v-model="event.eventTime"
                      type="datetime"
                      placeholder="选择时间"
                      format="YYYY-MM-DD HH:mm:ss"
                      value-format="YYYY-MM-DD HH:mm:ss"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                
                <el-col :span="8">
                  <el-form-item label="事件地点" prop="eventLocation" required>
                    <el-input v-model="event.eventLocation" placeholder="事件地点" />
                  </el-form-item>
                </el-col>
                
                <el-col :span="8">
                  <el-form-item label="事件类型" prop="eventType" required>
                    <el-select v-model="event.eventType" placeholder="选择类型" style="width: 100%">
                      <el-option label="袭击" value="ATTACK" />
                      <el-option label="冲突" value="CONFLICT" />
                      <el-option label="谈判" value="NEGOTIATE" />
                      <el-option label="抗议" value="PROTEST" />
                      <el-option label="制裁" value="SANCTION" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="事件主体" prop="subject" required>
                    <el-input v-model="event.subject" placeholder="事件主体" />
                  </el-form-item>
                </el-col>
                
                <el-col :span="8">
                  <el-form-item label="事件客体" prop="object" required>
                    <el-input v-model="event.object" placeholder="事件客体" />
                  </el-form-item>
                </el-col>
                
                <el-col :span="8">
                  <el-form-item label="来源类型">
                    <el-select v-model="event.sourceType" style="width: 100%">
                      <el-option label="人工录入" :value="2" />
                      <el-option label="DeepSeek获取" :value="1" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="24">
                  <el-form-item label="事件描述">
                    <el-input
                      v-model="event.eventDescription"
                      type="textarea"
                      :rows="2"
                      placeholder="事件描述（可自动生成）"
                    />
                    <div class="form-tip">
                      <el-button type="text" @click="generateDescription(index)">
                        <el-icon><Refresh /></el-icon>
                        自动生成
                      </el-button>
                    </div>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-card v-else shadow="hover" class="empty-card">
        <el-empty description="尚未添加任何事件">
          <el-button type="primary" @click="addEvent">
            <el-icon><Plus /></el-icon>
            添加第一个事件
          </el-button>
        </el-empty>
      </el-card>
    </div>

    <!-- 文件导入模式 -->
    <div v-else-if="inputMethod === 'import'">
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">文件导入</span>
        </template>
        
        <div class="import-section">
          <div class="import-tips">
            <h4>导入说明</h4>
            <ul>
              <li>支持Excel (.xlsx, .xls) 和CSV格式文件</li>
              <li>文件大小不超过10MB，最多支持1000条记录</li>
              <li>请确保文件包含必需字段：事件时间、事件地点、事件类型、事件主体、事件客体</li>
              <li>下载模板文件确保格式正确</li>
            </ul>
          </div>

          <div class="import-actions">
            <el-button type="info" @click="downloadTemplate">
              <el-icon><Download /></el-icon>
              下载模板
            </el-button>
            
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="false"
              accept=".xlsx,.xls,.csv"
              :on-change="handleFileChange"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择文件
              </el-button>
            </el-upload>
          </div>

          <div v-if="uploadFile" class="upload-preview">
            <div class="file-info">
              <el-icon><Document /></el-icon>
              <span>{{ uploadFile.name }}</span>
              <el-button type="text" @click="clearFile">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
            
            <el-button type="success" @click="parseFile" :loading="parsing">
              <el-icon><Check /></el-icon>
              解析文件
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 模板批量模式 -->
    <div v-else>
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">模板批量录入</span>
        </template>
        
        <div class="template-section">
          <p class="template-description">
            选择预设模板，快速创建一系列相关的事件。系统会根据模板自动生成多个相关事件。
          </p>
          
          <div class="template-list">
            <div 
              v-for="template in templates"
              :key="template.id"
              class="template-item"
              @click="selectTemplate(template)"
            >
              <div class="template-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="template-info">
                <h4>{{ template.name }}</h4>
                <p>{{ template.description }}</p>
                <span class="template-count">将生成 {{ template.eventCount }} 个事件</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createEventsBatch, type Event } from '@/api/event'
import {
  ArrowLeft,
  Plus,
  Delete,
  Upload,
  Check,
  CopyDocument,
  Refresh,
  Download,
  Document,
  Close
} from '@element-plus/icons-vue'

const router = useRouter()

// 录入方式
const inputMethod = ref('manual')

// 事件列表
const eventList = ref<Event[]>([])
const eventRefs = ref<any[]>([])
const submitting = ref(false)

// 事件关联列表
const eventRelations = ref<EventRelation[]>([])

// 事件关联接口定义
interface EventRelation {
  sourceIndex: number | null
  targetIndex: number | null
  relationType: string
  relationDescription: string
  confidence: number
}

// 文件导入
const uploadFile = ref<File | null>(null)
const parsing = ref(false)

// 模板数据
const templates = ref([
  {
    id: 1,
    name: '地区冲突模板',
    description: '创建一系列地区冲突相关事件',
    eventCount: 5
  },
  {
    id: 2,
    name: '外交谈判模板',
    description: '创建外交谈判进程相关事件',
    eventCount: 3
  },
  {
    id: 3,
    name: '制裁回应模板',
    description: '创建制裁与反制裁相关事件',
    eventCount: 4
  }
])

// 表单验证规则
const eventRules = {
  eventTime: [{ required: true, message: '请选择事件时间', trigger: 'change' }],
  eventLocation: [{ required: true, message: '请输入事件地点', trigger: 'blur' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  subject: [{ required: true, message: '请输入事件主体', trigger: 'blur' }],
  object: [{ required: true, message: '请输入事件客体', trigger: 'blur' }]
}

/**
 * 返回列表页
 */
const goBack = () => {
  router.push('/event/list')
}

/**
 * 切换录入方式
 */
const handleMethodChange = () => {
  // 清空已有数据
  eventList.value = []
  uploadFile.value = null
}

/**
 * 添加事件
 */
const addEvent = () => {
  const newEvent: Event = {
    eventTime: '',
    eventLocation: '',
    eventType: '',
    eventDescription: '',
    subject: '',
    object: '',
    sourceType: 2,
    status: 1
  }
  eventList.value.push(newEvent)
}

/**
 * 复制事件
 */
const copyEvent = (index: number) => {
  const event = eventList.value[index]
  const copiedEvent = { ...event }
  eventList.value.splice(index + 1, 0, copiedEvent)
  ElMessage.success('事件已复制')
}

/**
 * 删除事件
 */
const removeEvent = (index: number) => {
  eventList.value.splice(index, 1)
  ElMessage.success('事件已删除')
}

/**
 * 清空所有事件
 */
const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有事件吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    eventList.value = []
    ElMessage.success('已清空所有事件')
  } catch {
    // 用户取消
  }
}

/**
 * 自动生成描述
 */
const generateDescription = (index: number) => {
  const event = eventList.value[index]
  if (event.subject && event.eventType && event.object) {
    // 现在事件类型直接就是中文名称
    event.eventDescription = `${event.subject}${event.eventType}${event.object}`
    ElMessage.success('描述已生成')
  } else {
    ElMessage.warning('请先填写事件主体、类型和客体')
  }
}

/**
 * 获取事件类型名称
 */
const getEventTypeName = (type: string) => {
  // 现在直接返回中文名称
  return type
}

/**
 * 批量提交
 */
const submitBatch = async () => {
  // 验证所有表单
  let isValid = true
  for (let i = 0; i < eventRefs.value.length; i++) {
    const formRef = eventRefs.value[i]
    if (formRef) {
      try {
        await formRef.validate()
      } catch {
        isValid = false
        ElMessage.error(`事件 ${i + 1} 验证失败，请检查必填项`)
        break
      }
    }
  }

  if (!isValid) return

  submitting.value = true
  try {
    // 处理数据格式
    const submitData = eventList.value.map(({ keywords, ...item }) => ({
      ...item,
      keywords: keywords ? keywords.split(',').map(k => k.trim()).filter(k => k) : []
    })) as any
    
    await createEventsBatch(submitData)
    ElMessage.success(`成功提交 ${eventList.value.length} 个事件`)
    router.push('/event/list')
  } catch (error) {
    ElMessage.error('批量提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 文件选择
 */
const handleFileChange = (file: any) => {
  uploadFile.value = file.raw
}

/**
 * 清除文件
 */
const clearFile = () => {
  uploadFile.value = null
}

/**
 * 下载模板
 */
const downloadTemplate = () => {
  ElMessage.info('模板下载功能开发中...')
}

/**
 * 解析文件
 */
const parseFile = async () => {
  if (!uploadFile.value) return
  
  parsing.value = true
  try {
    // 这里应该调用文件解析API
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('文件解析成功')
    // 模拟解析结果
    addEvent()
    addEvent()
  } catch (error) {
    ElMessage.error('文件解析失败')
  } finally {
    parsing.value = false
  }
}

/**
 * 选择模板
 */
const selectTemplate = async (template: any) => {
  try {
    await ElMessageBox.confirm(`确定使用"${template.name}"模板吗？这将生成${template.eventCount}个事件。`, '确认使用模板', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 根据模板生成事件
    for (let i = 0; i < template.eventCount; i++) {
      addEvent()
    }
    ElMessage.success(`已根据模板生成 ${template.eventCount} 个事件`)
    inputMethod.value = 'manual'
  } catch {
    // 用户取消
  }
}

/**
 * 导入模板
 */
const importTemplate = () => {
  ElMessage.info('导入模板功能开发中...')
}

/**
 * 添加事件关联
 */
const addRelation = () => {
  if (eventList.value.length < 2) {
    ElMessage.warning('至少需要2个事件才能建立关联')
    return
  }
  
  const newRelation: EventRelation = {
    sourceIndex: null,
    targetIndex: null,
    relationType: '',
    relationDescription: '',
    confidence: 80
  }
  eventRelations.value.push(newRelation)
}

/**
 * 移除事件关联
 */
const removeRelation = (index: number) => {
  eventRelations.value.splice(index, 1)
  ElMessage.success('关联已删除')
}

onMounted(() => {
  // 默认添加一个事件
  addEvent()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.page-description {
  color: #909399;
  margin: 0;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.method-card {
  margin-bottom: 20px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.method-description {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  gap: 12px;
}

.event-count {
  color: #606266;
  font-size: 14px;
}

.events-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.event-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  background: #fafafa;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.event-number {
  font-weight: 600;
  color: #409EFF;
}

.event-actions {
  display: flex;
  gap: 8px;
}

.form-tip {
  margin-top: 8px;
}

.empty-card {
  margin-bottom: 20px;
}

.import-section {
  padding: 20px 0;
}

.import-tips {
  margin-bottom: 24px;
}

.import-tips h4 {
  color: #303133;
  margin-bottom: 12px;
}

.import-tips ul {
  color: #606266;
  padding-left: 20px;
}

.import-tips li {
  margin-bottom: 8px;
}

.import-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.upload-preview {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.template-section {
  padding: 20px 0;
}

.template-description {
  color: #606266;
  margin-bottom: 24px;
}

.template-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.template-item {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  gap: 16px;
}

.template-item:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.template-icon {
  color: #409EFF;
  font-size: 32px;
}

.template-info h4 {
  color: #303133;
  margin: 0 0 8px 0;
}

.template-info p {
  color: #606266;
  margin: 0 0 8px 0;
  font-size: 14px;
}

.template-count {
  color: #909399;
  font-size: 12px;
}
</style> 