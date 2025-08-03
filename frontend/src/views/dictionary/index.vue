<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">字典管理</h2>
      <p class="page-description">管理系统字典数据，支持层级结构和自动扩展</p>
    </div>

    <!-- 工具栏 -->
    <el-card shadow="hover" class="toolbar-card">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-form :inline="true" :model="queryForm" class="query-form">
            <el-form-item label="字典类型">
              <el-select
                v-model="queryForm.dictType"
                placeholder="全部类型"
                clearable
                style="width: 150px"
                @change="handleQuery"
              >
                <el-option
                  v-for="type in dictTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="字典名称">
              <el-input
                v-model="queryForm.dictName"
                placeholder="请输入字典名称"
                clearable
                style="width: 200px"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="是否自动添加">
              <el-select
                v-model="queryForm.isAutoAdded"
                placeholder="全部"
                clearable
                style="width: 120px"
                @change="handleQuery"
              >
                <el-option label="是" :value="1" />
                <el-option label="否" :value="0" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="8" class="text-right">
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增字典
          </el-button>
          <el-button
            type="danger"
            :disabled="!selectedIds.length"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 字典表格 -->
    <el-card shadow="hover">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        stripe
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="字典名称" prop="dictName" min-width="200">
          <template #default="{ row }">
            <span class="dict-name">{{ row.dictName }}</span>
            <el-tag v-if="row.isAutoAdded" type="info" size="small" class="auto-tag">
              自动添加
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="字典编码" prop="dictCode" width="150" />
        <el-table-column label="字典类型" prop="dictType" width="120">
          <template #default="{ row }">
            <el-tag :type="getDictTypeColor(row.dictType)">
              {{ getDictTypeText(row.dictType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="dictDescription" min-width="200" show-overflow-tooltip />
        <el-table-column label="排序" prop="sortOrder" width="80" />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createdAt" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="(command: string) => handleCommand(command, row)" trigger="click">
              <el-button type="primary" size="small" @click.stop>
                操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="detail">
                    <el-icon><View /></el-icon>详情
                  </el-dropdown-item>
                  <el-dropdown-item v-if="!row.children || row.children.length === 0" command="addChild">
                    <el-icon><Plus /></el-icon>添加子项
                  </el-dropdown-item>
                  <el-dropdown-item divided command="delete">
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗组件 -->
    <CountryDetailDialog 
      v-model:visible="showCountryDetailDialog" 
      :country="countryDetailData" 
    />
    <OrganizationDetailDialog 
      v-model:visible="showOrganizationDetailDialog" 
      :organization="organizationDetailData" 
    />
    <PersonDetailDialog 
      v-model:visible="showPersonDetailDialog" 
      :person="personDetailData" 
    />
    <DictionaryDetailDialog
      v-model:visible="showDictionaryDetailDialog"
      :dictionary="dictionaryDetailData"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="字典类型" prop="dictType">
          <el-select
            v-model="formData.dictType"
            placeholder="请选择字典类型"
            style="width: 100%"
            :disabled="!!formData.id"
          >
            <el-option
              v-for="type in dictTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="字典编码" prop="dictCode">
          <el-input
            v-model="formData.dictCode"
            placeholder="请输入字典编码"
            :disabled="!!formData.id"
          />
        </el-form-item>
        
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        
        <el-form-item label="父级字典">
          <el-tree-select
            v-model="formData.parentId"
            :data="treeData"
            :props="{ value: 'id', label: 'dictName' }"
            placeholder="请选择父级字典（不选则为顶级）"
            style="width: 100%"
            clearable
            check-strictly
          />
        </el-form-item>
        
        <el-form-item label="字典描述">
          <el-input
            v-model="formData.dictDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入字典描述"
          />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getDictionaryList,
  getDictionaryTree,
  createDictionary,
  updateDictionary,
  deleteDictionary,
  deleteDictionariesBatch,
  getDictionaryTypes,
  getDictionaryDetail,
  type Dictionary,
  type DictionaryQuery,
  type DictionaryDetailResponse
} from '@/api/dictionary'
import dayjs from 'dayjs'
import CountryDetailDialog from './CountryDetailDialog.vue'
import OrganizationDetailDialog from './OrganizationDetailDialog.vue'
import PersonDetailDialog from './PersonDetailDialog.vue'
import DictionaryDetailDialog from './DictionaryDetailDialog.vue'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref<Dictionary[]>([])
const treeData = ref<Dictionary[]>([])
const selectedIds = ref<number[]>([])
const dictTypes = ref<Array<{ label: string; value: string }>>([])
const showCountryDetailDialog = ref(false)
const showOrganizationDetailDialog = ref(false)
const showPersonDetailDialog = ref(false)
const showDictionaryDetailDialog = ref(false)
const countryDetailData = ref<any>(null)
const organizationDetailData = ref<any>(null)
const personDetailData = ref<any>(null)
const dictionaryDetailData = ref<any>(null)

// 表单引用
const tableRef = ref()
const formRef = ref<FormInstance>()

// 查询表单
const queryForm = reactive<DictionaryQuery>({
  dictType: '',
  dictName: '',
  isAutoAdded: undefined
})

// 分页数据
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 表单数据
const formData = ref<Dictionary>({
  dictType: '',
  dictCode: '',
  dictName: '',
  dictDescription: '',
  parentId: undefined,
  sortOrder: 0,
  isAutoAdded: 0,
  status: 1
})

// 表单验证规则
const formRules: FormRules = {
  dictType: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
}

// 计算属性
const dialogTitle = computed(() => {
  return formData.value.id ? '编辑字典' : '新增字典'
})

// 加载字典类型列表
const loadDictTypes = async () => {
  try {
    const response = await getDictionaryTypes()
    console.log('字典类型API响应:', response)
    
    // 响应拦截器已经返回了data字段，所以response就是数组
    const types = response || []
    
    if (!Array.isArray(types)) {
      console.error('字典类型数据格式异常，期望数组:', types)
      ElMessage.error('字典类型数据格式异常')
      return
    }
    
    dictTypes.value = types.map((type: string) => ({
      label: type,
      value: type
    }))
    
    console.log('解析后的字典类型:', dictTypes.value)
  } catch (error) {
    console.error('获取字典类型失败:', error)
    ElMessage.error('获取字典类型失败')
  }
}

// 生命周期
onMounted(() => {
  loadDictTypes()
  loadDictionaryList()
  loadTreeData()
})

// 加载字典列表
const loadDictionaryList = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      current: pagination.current,
      size: pagination.size
    }
    
    const response: any = await getDictionaryList(params)
    tableData.value = response.records || []
    pagination.total = response.total || 0
  } catch (error) {
    ElMessage.error('加载字典列表失败')
  } finally {
    loading.value = false
  }
}

// 加载树形数据
const loadTreeData = async () => {
  try {
    const response: any = await getDictionaryTree()
    treeData.value = response || []
  } catch (error) {
    console.error('加载树形数据失败:', error)
  }
}

// 查询
const handleQuery = () => {
  pagination.current = 1
  loadDictionaryList()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    dictType: '',
    dictName: '',
    isAutoAdded: undefined
  })
  handleQuery()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 添加子项
const handleAddChild = (row: Dictionary) => {
  resetForm()
  formData.value.parentId = row.id
  formData.value.dictType = row.dictType
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Dictionary) => {
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: Dictionary) => {
  try {
    await ElMessageBox.confirm(`确定删除字典"${row.dictName}"吗？`, '删除确认', {
      type: 'warning'
    })
    
    await deleteDictionary(row.id!)
    ElMessage.success('删除成功')
    await loadDictionaryList()
    await loadTreeData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 个字典吗？`, '批量删除确认', {
      type: 'warning'
    })
    
    await deleteDictionariesBatch(selectedIds.value)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    await loadDictionaryList()
    await loadTreeData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// 状态切换
const handleStatusChange = async (row: Dictionary) => {
  try {
    await updateDictionary(row)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    row.status = row.status === 1 ? 0 : 1 // 回滚状态
  }
}

// 选择变化
const handleSelectionChange = (selection: Dictionary[]) => {
  selectedIds.value = selection.map(item => item.id!).filter(Boolean)
}

// 分页相关
const handleSizeChange = (size: number) => {
  pagination.size = size
  handleQuery()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadDictionaryList()
}

// 表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (formData.value.id) {
        await updateDictionary(formData.value)
        ElMessage.success('更新成功')
      } else {
        await createDictionary(formData.value)
        ElMessage.success('创建成功')
      }
      
      dialogVisible.value = false
      await loadDictionaryList()
      await loadTreeData()
    } catch (error) {
      ElMessage.error(formData.value.id ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

// 关闭弹窗
const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.value = {
    dictType: '',
    dictCode: '',
    dictName: '',
    dictDescription: '',
    parentId: undefined,
    isAutoAdded: 0,
    status: 1
  }
}

// 详情
const handleDetail = async (row: Dictionary) => {
  try {
    const response: DictionaryDetailResponse = await getDictionaryDetail(row.id!)
    const { entityType, data } = response
    if (entityType === 'country') {
      countryDetailData.value = data || {}
      showCountryDetailDialog.value = true
    } else if (entityType === 'organization') {
      organizationDetailData.value = data || {}
      showOrganizationDetailDialog.value = true
    } else if (entityType === 'person') {
      personDetailData.value = data || {}
      showPersonDetailDialog.value = true
    } else if (entityType === 'dictionary') {
      dictionaryDetailData.value = data || {}
      showDictionaryDetailDialog.value = true
    } else {
      ElMessage.warning('暂不支持该类型详情展示')
    }
  } catch (e) {
    ElMessage.error('获取详情异常')
  }
}

// 工具函数
const getDictTypeText = (type: string) => {
  return type // 现在直接返回类型，因为数据库中已经是中文了
}

const getDictTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    '国家': 'primary',
    '地区': 'success',
    '城市': 'warning',
    '事件类型': 'danger',
    '事件主体': 'info',
    '事件客体': '',
    '关联关系类型': 'primary',
    '来源类型': 'success',
    '事件状态': 'warning'
  }
  return colors[type] || ''
}

const formatDateTime = (datetime?: string) => {
  return datetime ? dayjs(datetime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 新增 handleCommand 方法
const handleCommand = (command: string, row: Dictionary) => {
  switch (command) {
    case 'edit':
      handleEdit(row)
      break
    case 'detail':
      handleDetail(row)
      break
    case 'addChild':
      handleAddChild(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}
</script>

<style scoped>
.toolbar-card {
  margin-bottom: 20px;
}

.query-form .el-form-item {
  margin-bottom: 0;
}

.text-right {
  text-align: right;
}

.dict-name {
  margin-right: 8px;
}

.auto-tag {
  margin-left: 4px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .text-right {
    text-align: left;
    margin-top: 16px;
  }
  
  .query-form .el-form-item {
    margin-bottom: 16px;
  }
}
</style> 