<template>
  <div class="region-selector">
    <div class="region-selector-header">
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索地区名称"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="filter-container">
        <el-select
          v-model="filterType"
          placeholder="地区类型"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="自定义" value="CUSTOM" />
          <el-option label="洲" value="CONTINENT" />
          <el-option label="国家" value="COUNTRY" />
          <el-option label="省份" value="PROVINCE" />
          <el-option label="城市" value="CITY" />
        </el-select>
      </div>
    </div>

    <div class="region-list-container">
      <div v-loading="loading" class="region-list">
        <template v-if="regionList.length > 0">
          <div
            v-for="region in regionList"
            :key="region.id"
            class="region-item"
            :class="{ 'is-selected': selectedRegion && selectedRegion.id === region.id }"
            @click="handleSelectRegion(region)"
          >
            <div class="region-item-content">
              <div class="region-name">{{ region.name }}</div>
              <div class="region-type">
                <el-tag size="small" :type="getRegionTypeTag(region.type)">
                  {{ getRegionTypeText(region.type) }}
                </el-tag>
              </div>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无地区数据" />
      </div>
    </div>

    <div class="region-selector-footer">
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          small
        />
      </div>
      <div class="action-container">
        <el-button @click="handleCancel">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirm"
          :disabled="!selectedRegion"
        >
          确认
        </el-button>
        <el-button
          type="success"
          @click="handleCreateRegion"
        >
          新建地区
        </el-button>
      </div>
    </div>

    <!-- 地区表单对话框 -->
    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.title"
      width="700px"
      append-to-body
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

    <!-- 地区详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="地区详情"
      width="700px"
      append-to-body
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
          <!-- 描述字段已从数据库中移除
          <el-descriptions-item label="描述" :span="2">
            {{ detailDialog.region.description || '无' }}
          </el-descriptions-item>
          -->
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
          <el-button type="primary" @click="handleSelectConfirm(detailDialog.region)">
            选择此地区
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, defineProps, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { regionApi } from '@/api/region'
import { formatDateTime } from '@/utils/format'

const props = defineProps({
  initialRegionId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['cancel', 'confirm'])

// 搜索和筛选
const searchKeyword = ref('')
const filterType = ref('')

// 分页参数
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 地区列表
const regionList = ref([])
const loading = ref(false)
const selectedRegion = ref(null)

// 地区详情对话框
const detailDialog = reactive({
  visible: false,
  loading: false,
  region: {},
  items: []
})

// 地区表单对话框
const regionFormRef = ref(null)
const formDialog = reactive({
  visible: false,
  title: '新增地区',
  submitting: false,
  dictionaryLoading: false,
  dictionaryOptions: [],
  form: {
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
  
  // 如果有初始选中的地区ID，加载该地区信息
  if (props.initialRegionId) {
    loadInitialRegion()
  }
})

// 加载初始选中的地区
const loadInitialRegion = async () => {
  try {
    const res = await regionApi.getRegionDetail(props.initialRegionId)
    if (res && res.region) {
      selectedRegion.value = res.region
    }
  } catch (error) {
    console.error('加载初始地区失败:', error)
  }
}

// 获取地区列表
const fetchRegionList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      name: searchKeyword.value || undefined,
      type: filterType.value || undefined
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

// 筛选变化
const handleFilterChange = () => {
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

// 选择地区
const handleSelectRegion = (region) => {
  // 如果点击的是当前选中的地区，则查看详情
  if (selectedRegion.value && selectedRegion.value.id === region.id) {
    handleViewRegionDetail(region)
    return
  }
  
  selectedRegion.value = region
}

// 查看地区详情
const handleViewRegionDetail = async (region) => {
  detailDialog.region = { ...region }
  detailDialog.visible = true
  detailDialog.loading = true
  
  try {
    const res = await regionApi.getRegionDetail(region.id)
    detailDialog.region = res.region || {}
    detailDialog.items = res.dictionaryItems || []
  } catch (error) {
    console.error('获取地区详情失败:', error)
    ElMessage.error('获取地区详情失败')
  } finally {
    detailDialog.loading = false
  }
}

// 从详情对话框中选择地区
const handleSelectConfirm = (region) => {
  selectedRegion.value = region
  detailDialog.visible = false
}

// 取消选择
const handleCancel = () => {
  emit('cancel')
}

// 确认选择
const handleConfirm = () => {
  if (!selectedRegion.value) {
    ElMessage.warning('请先选择一个地区')
    return
  }
  
  emit('confirm', selectedRegion.value)
}

// 创建新地区
const handleCreateRegion = () => {
  formDialog.title = '新增地区'
  formDialog.form = {
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
      
      // 创建新地区
      const res = await regionApi.createRegion(formData)
      ElMessage.success('创建成功')
      
      // 关闭对话框
      formDialog.visible = false
      
      // 刷新地区列表
      fetchRegionList()
      
      // 选中新创建的地区
      if (res && res.id) {
        selectedRegion.value = res
      }
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
.region-selector {
  display: flex;
  flex-direction: column;
  height: 500px;
  
  .region-selector-header {
    display: flex;
    gap: 10px;
    margin-bottom: 15px;
    
    .search-container {
      flex: 1;
    }
    
    .filter-container {
      width: 150px;
    }
  }
  
  .region-list-container {
    flex: 1;
    overflow: hidden;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    
    .region-list {
      height: 100%;
      overflow-y: auto;
      padding: 10px;
      
      .region-item {
        padding: 10px 15px;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s;
        margin-bottom: 8px;
        border: 1px solid #ebeef5;
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        &.is-selected {
          background-color: #ecf5ff;
          border-color: #409eff;
        }
        
        .region-item-content {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .region-name {
            font-weight: 500;
          }
          
          .region-type {
            font-size: 12px;
          }
        }
      }
    }
  }
  
  .region-selector-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 15px;
    
    .action-container {
      display: flex;
      gap: 10px;
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