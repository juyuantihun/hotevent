<template>
  <div class="region-container">
    <div class="region-header">
      <h2 class="region-title">地区管理</h2>
      <div class="region-actions">
        <el-button type="primary" @click="handleCreateRegion">
          <el-icon><Plus /></el-icon>新增地区
        </el-button>
      </div>
    </div>

    <div class="region-search">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="地区名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入地区名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="地区类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择地区类型"
            clearable
          >
            <el-option label="自定义" value="CUSTOM" />
            <el-option label="洲" value="CONTINENT" />
            <el-option label="国家" value="COUNTRY" />
            <el-option label="省份" value="PROVINCE" />
            <el-option label="城市" value="CITY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="region-table">
      <el-table
        v-loading="loading"
        :data="regionList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="地区名称" min-width="150" />
        <el-table-column label="地区类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getRegionTypeTag(row.type)">
              {{ getRegionTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="handleViewRegion(row)"
            >
              查看
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleEditRegion(row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteRegion(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 地区详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      :title="detailDialog.title"
      width="700px"
    >
      <div v-loading="detailDialog.loading" class="region-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="地区名称">{{ detailDialog.region.name }}</el-descriptions-item>
          <el-descriptions-item label="地区类型">
            <el-tag :type="getRegionTypeTag(detailDialog.region.type)">
              {{ getRegionTypeText(detailDialog.region.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(detailDialog.region.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ formatDateTime(detailDialog.region.updatedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ detailDialog.region.description || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="region-items">
          <div class="region-items-header">
            <h3>包含的地区项目</h3>
          </div>
          <el-table
            :data="detailDialog.items"
            border
            style="width: 100%"
            max-height="300"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="dict_code" label="名称" min-width="150" />
            <el-table-column prop="dict_type" label="类型" width="120" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
          <el-button type="primary" @click="handleEditRegion(detailDialog.region)">
            编辑
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 地区表单对话框 -->
    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.title"
      width="700px"
    >
      <el-form
        ref="regionFormRef"
        :model="formDialog.form"
        :rules="formDialog.rules"
        label-width="100px"
      >
        <el-form-item label="地区名称" prop="name">
          <el-input v-model="formDialog.form.name" placeholder="请输入地区名称" />
        </el-form-item>
        <el-form-item label="地区类型" prop="type">
          <el-select v-model="formDialog.form.type" placeholder="请选择地区类型">
            <el-option label="自定义" value="CUSTOM" />
            <el-option label="洲" value="CONTINENT" />
            <el-option label="国家" value="COUNTRY" />
            <el-option label="省份" value="PROVINCE" />
            <el-option label="城市" value="CITY" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formDialog.form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入地区描述"
          />
        </el-form-item>
        <el-form-item label="包含地区" prop="dictionaryIds">
          <el-select
            v-model="formDialog.form.dictionaryIds"
            multiple
            filterable
            remote
            reserve-keyword
            placeholder="请输入关键词搜索地区"
            :remote-method="handleDictionarySearch"
            :loading="formDialog.dictionaryLoading"
          >
            <el-option
              v-for="item in formDialog.dictionaryOptions"
              :key="item.id"
              :label="item.dict_code"
              :value="item.id"
            >
              <span>{{ item.dict_code }}</span>
              <span class="option-type">({{ item.dict_type }})</span>
            </el-option>
          </el-select>
          <div class="form-tip">
            可以选择国家、地区、城市等，支持搜索
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="formDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitForm" :loading="formDialog.submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { regionApi } from '@/api/region'
import { formatDateTime } from '@/utils/format'

// 搜索表单
const searchForm = reactive({
  name: '',
  type: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 地区列表
const regionList = ref([])
const loading = ref(false)

// 地区详情对话框
const detailDialog = reactive({
  visible: false,
  title: '地区详情',
  loading: false,
  region: {},
  items: []
})

// 地区表单对话框
const regionFormRef = ref(null)
const formDialog = reactive({
  visible: false,
  title: '新增地区',
  isEdit: false,
  submitting: false,
  dictionaryLoading: false,
  dictionaryOptions: [],
  form: {
    id: null,
    name: '',
    type: '',
    description: '',
    dictionaryIds: []
  },
  rules: {
    name: [
      { required: true, message: '请输入地区名称', trigger: 'blur' },
      { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
    ],
    type: [
      { required: true, message: '请选择地区类型', trigger: 'change' }
    ],
    description: [
      { max: 500, message: '长度不能超过 500 个字符', trigger: 'blur' }
    ],
    dictionaryIds: [
      { required: true, message: '请选择包含的地区', trigger: 'change' },
      { type: 'array', min: 1, message: '至少选择一个地区', trigger: 'change' }
    ]
  }
})

// 生命周期钩子
onMounted(() => {
  fetchRegionList()
})

// 获取地区列表
const fetchRegionList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined
    }
    
    const res = await regionApi.getRegions(params)
    regionList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取地区列表失败:', error)
    ElMessage.error('获取地区列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchRegionList()
}

// 重置搜索
const handleReset = () => {
  searchForm.name = ''
  searchForm.type = ''
  pagination.page = 1
  fetchRegionList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  fetchRegionList()
}

// 页码变化
const handleCurrentChange = (page) => {
  pagination.page = page
  fetchRegionList()
}

// 获取地区类型文本
const getRegionTypeText = (type) => {
  const typeMap = {
    'CUSTOM': '自定义',
    'CONTINENT': '洲',
    'COUNTRY': '国家',
    'PROVINCE': '省份',
    'CITY': '城市'
  }
  return typeMap[type] || type
}

// 获取地区类型标签类型
const getRegionTypeTag = (type) => {
  const tagMap = {
    'CUSTOM': '',
    'CONTINENT': 'danger',
    'COUNTRY': 'primary',
    'PROVINCE': 'success',
    'CITY': 'warning'
  }
  return tagMap[type] || ''
}

// 查看地区详情
const handleViewRegion = async (row) => {
  detailDialog.region = { ...row }
  detailDialog.visible = true
  detailDialog.loading = true
  
  try {
    const res = await regionApi.getRegionDetail(row.id)
    detailDialog.region = res.region || {}
    detailDialog.items = res.dictionaryItems || []
  } catch (error) {
    console.error('获取地区详情失败:', error)
    ElMessage.error('获取地区详情失败')
  } finally {
    detailDialog.loading = false
  }
}

// 新增地区
const handleCreateRegion = () => {
  formDialog.isEdit = false
  formDialog.title = '新增地区'
  formDialog.form = {
    id: null,
    name: '',
    type: '',
    description: '',
    dictionaryIds: []
  }
  formDialog.visible = true
  
  // 重置表单校验
  if (regionFormRef.value) {
    regionFormRef.value.resetFields()
  }
}

// 编辑地区
const handleEditRegion = async (row) => {
  formDialog.isEdit = true
  formDialog.title = '编辑地区'
  formDialog.visible = true
  formDialog.submitting = true
  
  try {
    const res = await regionApi.getRegionDetail(row.id)
    const region = res.region || {}
    const items = res.dictionaryItems || []
    
    formDialog.form = {
      id: region.id,
      name: region.name,
      type: region.type,
      description: region.description,
      dictionaryIds: items.map(item => item.id)
    }
    
    // 加载字典选项
    formDialog.dictionaryOptions = items
  } catch (error) {
    console.error('获取地区详情失败:', error)
    ElMessage.error('获取地区详情失败')
  } finally {
    formDialog.submitting = false
  }
  
  // 重置表单校验
  if (regionFormRef.value) {
    regionFormRef.value.resetFields()
  }
}

// 删除地区
const handleDeleteRegion = (row) => {
  ElMessageBox.confirm(
    '确定要删除该地区吗？删除后无法恢复。',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await regionApi.deleteRegion(row.id)
      ElMessage.success('删除成功')
      fetchRegionList()
    } catch (error) {
      console.error('删除地区失败:', error)
      if (error.message && error.message.includes('引用')) {
        ElMessage.error('该地区被时间线引用，无法删除')
      } else {
        ElMessage.error('删除地区失败')
      }
    }
  }).catch(() => {
    // 取消删除
  })
}

// 搜索字典项
const handleDictionarySearch = async (query) => {
  if (query.length < 1) return
  
  formDialog.dictionaryLoading = true
  try {
    // 调用字典搜索API
    const res = await fetch(`/api/dictionary/search?keyword=${encodeURIComponent(query)}&types=国家,地区,城市`)
    const data = await res.json()
    
    if (data.code === 200) {
      formDialog.dictionaryOptions = data.data || []
    } else {
      formDialog.dictionaryOptions = []
    }
  } catch (error) {
    console.error('搜索字典项失败:', error)
    formDialog.dictionaryOptions = []
  } finally {
    formDialog.dictionaryLoading = false
  }
}

// 提交表单
const handleSubmitForm = async () => {
  if (!regionFormRef.value) return
  
  await regionFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    formDialog.submitting = true
    try {
      const formData = { ...formDialog.form }
      
      if (formDialog.isEdit) {
        // 编辑地区
        await regionApi.updateRegion(formData.id, formData)
        ElMessage.success('更新成功')
      } else {
        // 新增地区
        await regionApi.createRegion(formData)
        ElMessage.success('创建成功')
      }
      
      formDialog.visible = false
      fetchRegionList()
    } catch (error) {
      console.error('保存地区失败:', error)
      ElMessage.error('保存地区失败')
    } finally {
      formDialog.submitting = false
    }
  })
}
</script>

<style scoped lang="scss">
.region-container {
  padding: 20px;
  
  .region-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .region-title {
      margin: 0;
      font-size: 20px;
    }
  }
  
  .region-search {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }
  
  .region-table {
    background-color: #fff;
    border-radius: 4px;
    
    .pagination-container {
      padding: 15px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .region-detail {
    .region-items {
      margin-top: 20px;
      
      .region-items-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        
        h3 {
          margin: 0;
          font-size: 16px;
        }
      }
    }
  }
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
  
  .option-type {
    color: #909399;
    margin-left: 5px;
    font-size: 12px;
  }
}
</style>