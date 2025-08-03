<template>
  <div class="create-timeline-form">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" size="default">
      <el-form-item label="时间线名称" prop="title">
        <el-input v-model="formData.title" placeholder="请输入时间线名称" maxlength="100" show-word-limit />
      </el-form-item>

      <!-- 地区选择 -->
      <el-form-item label="选择地区" prop="selectedRegions">
        <div class="region-selector-container">
          <div v-if="formData.selectedRegions && formData.selectedRegions.length > 0" class="selected-regions-info">
            <el-tag v-for="region in formData.selectedRegions" :key="region.id" type="success" class="region-tag"
              closable @close="handleRemoveRegion(region)">
              {{ region.name }}
              <span class="region-type">({{ getRegionTypeText(region.type) }})</span>
            </el-tag>
          </div>
          <div v-else class="no-region-selected">
            未选择地区
          </div>
          <el-button @click="showRegionSelector = true">
            {{ formData.selectedRegions && formData.selectedRegions.length > 0 ? '添加地区' : '选择地区' }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 时间范围选择 -->
      <el-form-item label="时间范围" prop="timeRange">
        <el-date-picker v-model="formData.timeRange" type="daterange" range-separator="至" start-placeholder="开始日期"
          end-placeholder="结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DDTHH:mm:ss"
          :default-time="['00:00:00', '23:59:59']" :shortcuts="dateShortcuts" :disabled-date="disabledDate" />
      </el-form-item>

      <!-- 高级设置 -->
      <el-divider content-position="left">高级设置</el-divider>

      <el-form-item label="去重设置">
        <el-switch v-model="formData.enableDeduplication" active-text="启用去重" inactive-text="禁用去重" />
      </el-form-item>

      <el-form-item label="字典管理">
        <el-switch v-model="formData.enableDictionary" active-text="自动提取" inactive-text="手动管理" />
      </el-form-item>

      <el-form-item label="关系分析">
        <el-switch v-model="formData.enableRelationAnalysis" active-text="启用分析" inactive-text="禁用分析" />
      </el-form-item>

      <el-form-item label="描述">
        <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入时间线描述（可选）" maxlength="500"
          show-word-limit />
      </el-form-item>
    </el-form>

    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        生成时间线
      </el-button>
    </div>

    <!-- 地区选择对话框 -->
    <el-dialog v-model="showRegionSelector" title="选择地区" width="650px" :close-on-click-modal="false">
      <RegionSelector @cancel="showRegionSelector = false" @confirm="handleRegionSelect" />
    </el-dialog>

    <!-- 生成进度对话框 -->
    <el-dialog v-model="showProgress" title="时间线生成进度" width="500px" :close-on-click-modal="false" :show-close="false"
      :close-on-press-escape="false">
      <TimelineProgress :status="generationStatus" :percentage="progressPercentage" :timeline-name="formData.title"
        :region-count="formData.selectedRegions.length" :regions="formData.selectedRegions.map(r => r.name)"
        :event-count="progressDetails.eventCount" :relation-count="progressDetails.relationCount"
        :current-step="progressDetails.currentStep" :error-message="errorMessage" @cancel="handleCancelGeneration"
        @view="handleViewTimeline" @close="handleCloseProgress" @retry="handleRetry" />
    </el-dialog>

    <!-- 生成结果反馈对话框 -->
    <el-dialog v-model="showResult" title="时间线生成结果" width="600px" :close-on-click-modal="false">
      <div class="generation-result">
        <div v-if="generationResult.success" class="result-success">
          <el-result icon="success" title="时间线生成成功" sub-title="您可以查看时间线详情或返回时间线列表">
            <template #extra>
              <div class="result-statistics">
                <el-descriptions title="时间线信息" :column="2" border>
                  <el-descriptions-item label="时间线名称">
                    {{ formData.title }}
                  </el-descriptions-item>
                  <el-descriptions-item label="地区">
                    {{formData.selectedRegions.map(r => r.name).join(', ')}}
                  </el-descriptions-item>
                  <el-descriptions-item label="事件数量">
                    {{ generationResult.eventCount || 0 }}
                  </el-descriptions-item>
                  <el-descriptions-item label="关系数量">
                    {{ generationResult.relationCount || 0 }}
                  </el-descriptions-item>
                  <el-descriptions-item label="生成时间" :span="2">
                    {{ generationResult.generationTime || '-' }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div class="result-actions">
                <el-button @click="showResult = false">关闭</el-button>
                <el-button type="primary" @click="handleViewTimeline">查看时间线</el-button>
              </div>
            </template>
          </el-result>
        </div>
        <div v-else class="result-error">
          <el-result icon="error" title="时间线生成失败" :sub-title="generationResult.errorMessage || '未知错误'">
            <template #extra>
              <div class="result-actions">
                <el-button @click="showResult = false">关闭</el-button>
                <el-button type="primary" @click="handleRetry">重试</el-button>
              </div>
            </template>
          </el-result>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { timelineApi } from '@/api/timeline'
import RegionSelector from './RegionSelector.vue'
import TimelineProgress from './TimelineProgress.vue'
import { useRouter } from 'vue-router'
import { time } from 'echarts'
import timeline from 'element-plus/es/components/timeline/index.mjs'
import { checkEnvironment } from '@/utils/envCheck'
import { directPost } from '@/utils/directRequest'

// 定义 emits
const emit = defineEmits<{
  success: [timeline: any]
  cancel: []
}>()

// 路由
const router = useRouter()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const formData = reactive({
  title: '',
  selectedRegions: [] as any[],
  timeRange: [] as string[],
  enableDeduplication: true,
  enableDictionary: true,
  enableRelationAnalysis: true,
  description: ''
})

// 对话框控制
const showRegionSelector = ref(false)
const showProgress = ref(false)
const showResult = ref(false)
const submitLoading = ref(false)

// 生成进度相关
const generationStatus = ref<'generating' | 'completed' | 'failed'>('generating')
const progressPercentage = ref(0)
const progressDetails = reactive({
  eventCount: 0,
  relationCount: 0,
  currentStep: ''
})
const errorMessage = ref('')
const generatedTimelineId = ref<number | null>(null)
const progressCheckInterval = ref<number | null>(null)

// 生成结果
const generationResult = reactive({
  success: false,
  timelineId: null as number | null,
  eventCount: 0,
  relationCount: 0,
  generationTime: '',
  errorMessage: ''
})

// 日期选择器快捷选项
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [start, end]
    },
  },
  {
    text: '最近半年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
  {
    text: '最近一年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setFullYear(start.getFullYear() - 1)
      return [start, end]
    },
  }
]

// 禁用日期函数 - 禁用未来日期
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

// 计算属性
const progressStatus = computed(() => {
  if (generationStatus.value === 'completed') return 'success'
  if (generationStatus.value === 'failed') return 'exception'
  return ''
})

// 表单验证规则
const formRules: FormRules = {
  title: [
    { required: true, message: '请输入时间线名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  selectedRegions: [
    {
      validator: (_, value, callback) => {
        if (!formData.selectedRegions || formData.selectedRegions.length === 0) {
          callback(new Error('请至少选择一个地区'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  timeRange: [
    {
      validator: (_, value, callback) => {
        if (!value || value.length !== 2) {
          callback(new Error('请选择时间范围'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 生命周期
onMounted(() => {
  // 初始化操作
  // 检查环境变量
  const env = checkEnvironment();
  console.log('当前环境变量状态:', env);
})

onBeforeUnmount(() => {
  // 清除定时器
  if (progressCheckInterval.value) {
    clearInterval(progressCheckInterval.value)
  }
})

/**
 * 处理地区选择
 */
const handleRegionSelect = (region: any) => {
  // 检查是否已经选择了该地区
  const existingIndex = formData.selectedRegions.findIndex(r => r.id === region.id)
  if (existingIndex === -1) {
    // 添加新地区
    formData.selectedRegions.push(region)
  }

  showRegionSelector.value = false

  // 自动生成时间线名称
  if (!formData.title && formData.selectedRegions.length === 1) {
    formData.title = `${formData.selectedRegions[0].name}地区事件时间线`
  } else if (!formData.title && formData.selectedRegions.length > 1) {
    const regionNames = formData.selectedRegions.map(r => r.name).join('、')
    formData.title = `${regionNames}多地区事件时间线`
  }
}

/**
 * 移除已选择的地区
 */
const handleRemoveRegion = (region: any) => {
  const index = formData.selectedRegions.findIndex(r => r.id === region.id)
  if (index !== -1) {
    formData.selectedRegions.splice(index, 1)
  }

  // 如果没有选择地区，清空时间线名称
  if (formData.selectedRegions.length === 0 && formData.title) {
    formData.title = ''
  } else if (formData.selectedRegions.length === 1) {
    // 如果只有一个地区，更新时间线名称
    formData.title = `${formData.selectedRegions[0].name}地区事件时间线`
  } else if (formData.selectedRegions.length > 1) {
    // 如果有多个地区，更新时间线名称
    const regionNames = formData.selectedRegions.map(r => r.name).join('、')
    formData.title = `${regionNames}多地区事件时间线`
  }
}

/**
 * 获取地区类型文本
 */
const getRegionTypeText = (type: string) => {
  const textMap: { [key: string]: string } = {
    'continent': '洲',
    'country': '国家',
    'province': '省份',
    'city': '城市'
  }
  return textMap[type] || type
}

/**
 * 获取状态文本
 */
const getStatusText = () => {
  if (generationStatus.value === 'generating') return '正在生成时间线...'
  if (generationStatus.value === 'completed') return '时间线生成完成'
  if (generationStatus.value === 'failed') return '时间线生成失败'
  return ''
}

/**
 * 处理提交
 */
const handleSubmit = async () => {
  console.log('开始提交表单...');
  if (!formRef.value) {
    console.error('表单引用不存在');
    return;
  }

  try {
    console.log('开始表单验证...');
    console.log('表单数据:', JSON.stringify(formData));

    // 手动触发表单验证
    const valid = await formRef.value.validate()
      .then(() => {
        console.log('表单验证通过');
        return true;
      })
      .catch((errors) => {
        console.error('表单验证失败:', errors);
        return false;
      });

    if (!valid) {
      // 如果验证失败，显示错误消息
      console.warn('表单验证失败，显示警告消息');
      ElMessage.warning('请完善表单信息');
      return;
    }

    // 验证通过，设置加载状态
    console.log('表单验证通过，设置加载状态');
    submitLoading.value = true;

    try {
      console.log('准备调用API生成时间线...');
      console.log('地区IDs:', formData.selectedRegions.map(r => r.id));
      console.log('时间线名称:', formData.title);
      console.log('时间线描述:', formData.description);
      console.log('时间范围:', formData.timeRange);

      // 检查环境变量
      const env = checkEnvironment();
      console.log('当前环境变量状态:', env);

      // 使用直接请求方式调用API，绕过模拟数据机制
      console.log('使用直接请求方式调用时间线生成API...');

      // 准备请求数据
      const requestData = {
        name: formData.title,
        description: formData.description,
        regionIds: formData.selectedRegions.map(r => r.id),
        startTime: formData.timeRange[0],
        endTime: formData.timeRange[1],
        enableDeduplication: formData.enableDeduplication,
        enableDictionary: formData.enableDictionary,
        enableRelationAnalysis: formData.enableRelationAnalysis
      };

      // 尝试使用直接请求方式
      const response = await directPost(
        `/timelines/generate/async`,
        requestData
      );
      console.log('API响应:', response);

      // 处理响应数据 - 修复：API响应格式可能不一致
      if (response && (
        (response.code === 200) ||
        (response.data && response.data.code === 200)
      )) {
        console.log('API调用成功，处理响应数据');
        // 获取时间线ID，处理不同的响应格式
        const timelineId = response.data ?
          (response.data.data ? response.data.data.id : response.data.id) :
          (response.id || response);

        console.log('获取到时间线ID:', timelineId);

        // 保存生成的时间线ID
        generatedTimelineId.value = timelineId;

        // 显示进度对话框
        showProgress.value = true;
        generationStatus.value = 'generating';
        progressPercentage.value = 0;

        // 开始定时检查进度
        console.log('开始检查进度...');
        startProgressCheck();
      } else {
        const errorMsg = response?.data?.message || response?.message || '时间线生成失败';
        console.error('API调用返回错误:', errorMsg);
        ElMessage.error(errorMsg);
      }
    } catch (error) {
      console.error('生成时间线失败:', error);
      // 显示更详细的错误信息
      if (error instanceof Error) {
        console.error('错误类型:', error.name);
        console.error('错误消息:', error.message);
        console.error('错误堆栈:', error.stack);
        ElMessage.error(`生成时间线失败: ${error.message}`);
      } else {
        ElMessage.error('生成时间线失败: 未知错误');
      }
    } finally {
      submitLoading.value = false;
    }
  } catch (error) {
    console.error('表单验证过程中出错:', error);
    if (error instanceof Error) {
      console.error('错误类型:', error.name);
      console.error('错误消息:', error.message);
      console.error('错误堆栈:', error.stack);
      ElMessage.warning(`表单验证失败: ${error.message}`);
    } else {
      ElMessage.warning('表单验证失败: 未知错误');
    }
  }
}

/**
 * 开始检查进度
 */
const startProgressCheck = () => {
  // 清除可能存在的定时器
  if (progressCheckInterval.value) {
    clearInterval(progressCheckInterval.value)
  }

  // 设置定时器，每5秒检查一次进度
  progressCheckInterval.value = window.setInterval(async () => {
    if (!generatedTimelineId.value) return

    try {
      const response = await timelineApi.getGenerationProgress(generatedTimelineId.value)

      // 处理响应数据 - 修复：API响应格式可能不一致
      let progressData = null

      if (response) {
        // 处理响应数据，考虑到可能的不同格式
        const responseData = response as any; // 使用类型断言解决TypeScript类型检查问题

        // 如果response本身就是数据对象（已经被请求拦截器处理过）
        if (responseData.percentage !== undefined) {
          progressData = responseData
        }
        // 如果response包含data属性（标准格式）
        else if (responseData.data) {
          if (responseData.data.code === 200) {
            progressData = responseData.data.data
          } else if (responseData.data.percentage !== undefined) {
            progressData = responseData.data
          }
        }
      }

      if (progressData) {
        // 更新进度信息
        progressPercentage.value = progressData.percentage || 0
        progressDetails.eventCount = progressData.eventCount || 0
        progressDetails.relationCount = progressData.relationCount || 0
        progressDetails.currentStep = progressData.currentStep || ''

        // 检查状态
        if (progressData.status === 'COMPLETED') {
          // 生成完成
          generationStatus.value = 'completed'
          progressPercentage.value = 100

          // 停止检查
          if (progressCheckInterval.value) {
            clearInterval(progressCheckInterval.value)
            progressCheckInterval.value = null
          }

          // 获取完整的时间线信息
          await getTimelineDetails()
        } else if (progressData.status === 'FAILED') {
          // 生成失败
          generationStatus.value = 'failed'
          errorMessage.value = progressData.errorMessage || '生成过程中发生错误'

          // 停止检查
          if (progressCheckInterval.value) {
            clearInterval(progressCheckInterval.value)
            progressCheckInterval.value = null
          }
        }
      }
    } catch (error) {
      console.error('获取生成进度失败:', error)
    }
  }, 5000)
}

/**
 * 获取时间线详情
 */
const getTimelineDetails = async () => {
  if (!generatedTimelineId.value) return

  try {
    const response = await timelineApi.getTimelineById(generatedTimelineId.value)

    // 处理响应数据 - 修复：API响应格式可能不一致
    let timelineData = null

    if (response) {
      // 处理响应数据，考虑到可能的不同格式
      const responseData = response as any; // 使用类型断言解决TypeScript类型检查问题

      // 如果response本身就是数据对象（已经被请求拦截器处理过）
      if (responseData.id) {
        timelineData = responseData
      }
      // 如果response包含data属性（标准格式）
      else if (responseData.data) {
        if (responseData.data.code === 200) {
          timelineData = responseData.data.data
        } else if (responseData.data.id) {
          timelineData = responseData.data
        }
      }
    }

    if (timelineData) {
      // 更新生成结果
      generationResult.success = true
      generationResult.timelineId = timelineData.id
      generationResult.eventCount = timelineData.eventCount || 0
      generationResult.relationCount = timelineData.relationCount || 0
      generationResult.generationTime = timelineData.createdAt || ''

      // 通知父组件生成成功
      emit('success', timelineData)
    } else {
      console.error('获取时间线详情失败: 无效的响应数据格式')
      generationResult.success = false
      generationResult.errorMessage = '获取时间线详情失败: 无效的响应数据格式'
    }
  } catch (error) {
    console.error('获取时间线详情失败:', error)
    generationResult.success = false
    generationResult.errorMessage = error instanceof Error ? error.message : '获取时间线详情失败'
  }
}

/**
 * 处理取消生成
 */
const handleCancelGeneration = async () => {
  if (!generatedTimelineId.value) return

  try {
    await timelineApi.cancelGeneration(generatedTimelineId.value)

    // 停止检查进度
    if (progressCheckInterval.value) {
      clearInterval(progressCheckInterval.value)
      progressCheckInterval.value = null
    }

    // 关闭进度对话框
    showProgress.value = false

    ElMessage.info('已取消时间线生成')
  } catch (error) {
    console.error('取消生成失败:', error)
    ElMessage.error('取消生成失败')
  }
}

/**
 * 处理查看时间线
 */
const handleViewTimeline = () => {
  if (!generatedTimelineId.value) return

  // 关闭所有对话框
  showProgress.value = false
  showResult.value = false

  // 跳转到时间线详情页
  router.push(`/timeline/detail/${generatedTimelineId.value}`)
}

/**
 * 处理关闭进度对话框
 */
const handleCloseProgress = () => {
  showProgress.value = false

  // 如果生成成功，显示结果对话框
  if (generationStatus.value === 'completed') {
    showResult.value = true
  }
}

/**
 * 处理重试
 */
const handleRetry = () => {
  // 关闭对话框
  showProgress.value = false
  showResult.value = false

  // 重新提交表单
  handleSubmit()
}

/**
 * 处理取消
 */
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.create-timeline-form {
  .region-selector-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;

    .selected-region-info {
      display: flex;
      align-items: center;
      gap: 8px;

      .region-type {
        color: #909399;
        font-size: 12px;
      }
    }

    .no-region-selected {
      color: #909399;
    }
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
  }

  .generation-progress {
    .progress-status {
      display: flex;
      align-items: center;
      margin-bottom: 16px;

      .rotating {
        animation: rotate 1.5s linear infinite;
      }

      .success-icon {
        color: #67c23a;
      }

      .error-icon {
        color: #f56c6c;
      }

      .status-text {
        margin-left: 8px;
        font-size: 16px;
        font-weight: 500;
      }
    }

    .progress-info {
      margin: 16px 0;
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;

      p {
        margin: 8px 0;
      }

      .error-message {
        color: #f56c6c;
      }
    }

    .progress-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 16px;
    }
  }

  .generation-result {
    .result-statistics {
      margin: 16px 0;
    }

    .result-actions {
      display: flex;
      justify-content: center;
      gap: 12px;
      margin-top: 24px;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>