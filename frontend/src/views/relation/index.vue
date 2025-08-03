<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="flex-between">
        <div>
          <h2 class="page-title">主体客体关系管理</h2>
          <p class="page-description">管理主体和客体之间的关系</p>
        </div>
        <div>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建关系
          </el-button>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" label-width="100px" :inline="true">
        <el-form-item label="主体">
          <el-select
            v-model="searchForm.subjectCode"
            placeholder="请选择主体"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in subjects"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="客体">
          <el-select
            v-model="searchForm.objectCode"
            placeholder="请选择客体"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in objects"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关系类型">
          <el-select
            v-model="searchForm.relationType"
            placeholder="请选择关系类型"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="item in relationTypes"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="强度级别">
          <el-select
            v-model="searchForm.intensityLevel"
            placeholder="请选择强度级别"
            clearable
            style="width: 150px"
          >
            <el-option label="非常弱" :value="1" />
            <el-option label="弱" :value="2" />
            <el-option label="中等" :value="3" />
            <el-option label="强" :value="4" />
            <el-option label="非常强" :value="5" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="hover" class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="subjectName" label="主体" min-width="120" />
        <el-table-column prop="objectName" label="客体" min-width="120" />
        <el-table-column prop="relationTypeName" label="关系类型" min-width="120" />
        <el-table-column prop="relationName" label="关系名称" min-width="150" />
        <el-table-column prop="intensityLevelName" label="强度级别" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getIntensityTagType(row.intensityLevel)">
              {{ row.intensityLevelName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="主体" prop="subjectCode">
          <el-select
            v-model="formData.subjectCode"
            placeholder="请选择主体"
            style="width: 100%"
          >
            <el-option
              v-for="item in subjects"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="客体" prop="objectCode">
          <el-select
            v-model="formData.objectCode"
            placeholder="请选择客体"
            style="width: 100%"
          >
            <el-option
              v-for="item in objects"
              :key="item.dictCode"
              :label="item.dictName"
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        
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
              :value="item.dictCode"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关系名称" prop="relationName">
          <el-input
            v-model="formData.relationName"
            placeholder="请输入关系名称"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="强度级别" prop="intensityLevel">
          <el-select
            v-model="formData.intensityLevel"
            placeholder="请选择强度级别"
            style="width: 100%"
          >
            <el-option label="非常弱" :value="1" />
            <el-option label="弱" :value="2" />
            <el-option label="中等" :value="3" />
            <el-option label="强" :value="4" />
            <el-option label="非常强" :value="5" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="关系描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入关系描述"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useDictionaryStore } from '@/store/modules/dictionary'
import type { Dictionary } from '@/api/dictionary'
import type { SubjectObjectRelation, RelationQuery } from '@/api/relation'
import {
  getRelationPage,
  createRelation,
  updateRelation,
  deleteRelation,
  deleteRelationsBatch
} from '@/api/relation'
import { getDictionaryByType } from '@/api/dictionary'

// Store
const dictionaryStore = useDictionaryStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

// 字典数据
const subjects = ref<Dictionary[]>([])
const objects = ref<Dictionary[]>([])
const relationTypes = ref<Dictionary[]>([])

// 搜索表单
const searchForm = reactive<RelationQuery>({
  subjectCode: '',
  objectCode: '',
  relationType: '',
  intensityLevel: undefined
})

// 表格数据
const tableData = ref<SubjectObjectRelation[]>([])
const selectedRows = ref<SubjectObjectRelation[]>([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表单数据
const formData = reactive<SubjectObjectRelation>({
  id: undefined,
  subjectCode: '',
  objectCode: '',
  relationType: '',
  relationName: '',
  intensityLevel: 1,
  description: ''
})

// 表单验证规则
const formRules = {
  subjectCode: [{ required: true, message: '请选择主体', trigger: 'change' }],
  objectCode: [{ required: true, message: '请选择客体', trigger: 'change' }],
  relationType: [{ required: true, message: '请选择关系类型', trigger: 'change' }],
  relationName: [{ required: true, message: '请输入关系名称', trigger: 'blur' }],
  intensityLevel: [{ required: true, message: '请选择强度级别', trigger: 'change' }]
}

const formRef = ref()

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑关系' : '新建关系')

// 方法
const loadDictionaries = async () => {
  try {
    const [subjectList, objectList, relationTypeList] = await Promise.all([
      dictionaryStore.getSubjects(),
      dictionaryStore.getObjects(),
      getDictionaryByType('关系类型')
    ])

    subjects.value = subjectList
    objects.value = objectList
    relationTypes.value = relationTypeList as unknown as Dictionary[]
  } catch (error) {
    console.error('加载字典数据失败:', error)
    ElMessage.error('加载字典数据失败')
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await getRelationPage(params) as any
    tableData.value = response.records || []
    pagination.total = response.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    subjectCode: '',
    objectCode: '',
    relationType: '',
    intensityLevel: undefined
  })
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadData()
}

const handleSelectionChange = (rows: SubjectObjectRelation[]) => {
  selectedRows.value = rows
}

const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: SubjectObjectRelation) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: SubjectObjectRelation) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除关系"${row.relationName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteRelation(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateRelation(formData)
      ElMessage.success('更新成功')
    } else {
      await createRelation(formData)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    subjectCode: '',
    objectCode: '',
    relationType: '',
    relationName: '',
    intensityLevel: 1,
    description: ''
  })
}

const getIntensityTagType = (level: number) => {
  const types = ['', 'info', 'warning', '', 'success', 'danger']
  return types[level] || ''
}

// 生命周期
onMounted(async () => {
  await loadDictionaries()
  await loadData()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #1f2937;
}

.page-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-card {
  margin-bottom: 24px;
}

.table-card {
  margin-bottom: 24px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 