<template>
  <div class="region-selector">
    <div class="region-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索地区"
        clearable
        @input="debounceSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="loadError" class="error-container">
      <el-empty description="加载地区数据失败" :image-size="100">
        <template #description>
          <p>无法加载地区数据，已使用模拟数据</p>
        </template>
        <el-button type="primary" @click="retryLoad">重新加载</el-button>
      </el-empty>
    </div>

    <div v-else-if="searchMode && searchResults.length > 0" class="search-results">
      <div class="result-title">搜索结果</div>
      <el-scrollbar height="300px">
        <div
          v-for="region in searchResults"
          :key="region.id"
          class="region-item"
          :class="{ 'is-selected': selectedRegion?.id === region.id }"
          @click="selectRegion(region)"
        >
          <div class="region-info">
            <span class="region-name">{{ region.name }}</span>
            <span class="region-path">{{ getRegionPath(region) }}</span>
          </div>
          <el-tag size="small" :type="getRegionTypeTag(region.type)">
            {{ getRegionTypeText(region.type) }}
          </el-tag>
        </div>
      </el-scrollbar>
      <div class="search-actions">
        <el-button text @click="clearSearch">返回树形结构</el-button>
      </div>
    </div>

    <div v-else-if="searchMode && searchResults.length === 0" class="empty-search">
      <el-empty description="未找到匹配的地区" :image-size="100">
        <el-button @click="clearSearch">返回树形结构</el-button>
      </el-empty>
    </div>

    <div v-else class="region-tree-container">
      <div class="breadcrumb-container" v-if="breadcrumbs.length > 0">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>
            <span class="breadcrumb-link" @click="navigateToBreadcrumb(-1)">根目录</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item 
            v-for="(crumb, index) in breadcrumbs" 
            :key="crumb.id"
          >
            <span class="breadcrumb-link" @click="navigateToBreadcrumb(index)">{{ crumb.name }}</span>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <el-scrollbar height="300px">
        <div class="region-list">
          <div
            v-for="region in currentRegions"
            :key="region.id"
            class="region-item"
            :class="{ 'is-selected': selectedRegion?.id === region.id }"
            @click="handleRegionClick(region)"
          >
            <div class="region-info">
              <span class="region-name">{{ region.name }}</span>
              <span class="region-count" v-if="region.children && region.children.length > 0">
                ({{ region.children.length }})
              </span>
            </div>
            <div class="region-actions">
              <el-tag size="small" :type="getRegionTypeTag(region.type)">
                {{ getRegionTypeText(region.type) }}
              </el-tag>
              <el-icon v-if="region.children && region.children.length > 0" class="expand-icon">
                <ArrowRight />
              </el-icon>
            </div>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <div class="region-selector-footer">
      <div class="selected-region" v-if="selectedRegion">
        <span class="label">已选择:</span>
        <el-tag class="region-tag" type="success">
          {{ selectedRegion.name }}
          <el-icon class="close-icon" @click.stop="clearSelection">
            <Close />
          </el-icon>
        </el-tag>
      </div>
      <div class="selector-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleConfirm"
          :disabled="!selectedRegion"
        >
          确认
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowRight, Close, Search } from '@element-plus/icons-vue'
import { regionApi } from '@/api/region'

// 定义 props
const props = defineProps({
  // 初始选中的地区ID
  initialRegionId: {
    type: Number,
    default: null
  }
})

// 定义 emits
const emit = defineEmits<{
  cancel: []
  confirm: [region: any]
}>()

// 响应式数据
const loading = ref(false)
const loadError = ref(false)
const regionTree = ref<any[]>([])
const currentRegions = ref<any[]>([])
const selectedRegion = ref<any>(null)
const breadcrumbs = ref<any[]>([])
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const searchMode = ref(false)

// 防抖定时器
let searchTimer: number | null = null

// 防抖搜索函数
const debounceSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = window.setTimeout(() => {
    handleSearch()
  }, 300) // 300ms延迟
}

// 生命周期
onMounted(() => {
  loadRegionTree()
})

/**
 * 加载地区树形结构
 */
const loadRegionTree = async () => {
  loading.value = true
  loadError.value = false
  
  try {
    try {
      // 添加超时处理
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('请求超时')), 5000)
      })
      
      // 使用Promise.race实现请求超时处理
      const response = await Promise.race([
        regionApi.getRegionTree(),
        timeoutPromise
      ]) as any
      
      // 处理响应数据 - 增强处理不同格式的响应能力
      if (response) {
        let processedData = null
        
        // 如果response本身就是数组（已经被请求拦截器处理过）
        if (Array.isArray(response)) {
          processedData = response
        }
        // 如果response包含data属性（标准格式）
        else if (response.data) {
          // 如果data是数组
          if (Array.isArray(response.data)) {
            processedData = response.data
          } 
          // 如果data包含其他数据结构
          else if (response.data.data && Array.isArray(response.data.data)) {
            processedData = response.data.data
          }
          // 如果data是对象但有content属性（分页格式）
          else if (response.data.content && Array.isArray(response.data.content)) {
            processedData = response.data.content
          }
        }
        // 如果response有content属性（某些API直接返回分页对象）
        else if (response.content && Array.isArray(response.content)) {
          processedData = response.content
        }
        
        // 如果成功获取并处理数据
        if (processedData && processedData.length > 0) {
          regionTree.value = processedData
          currentRegions.value = regionTree.value
          
          // 如果有初始选中的地区ID，尝试找到并选中
          if (props.initialRegionId) {
            await findAndSelectRegion(props.initialRegionId)
          }
          return
        } else {
          console.warn('API返回了空数据或格式无法识别:', response)
          throw new Error('数据格式无法识别')
        }
      }
    } catch (apiError) {
      console.warn('API调用失败，使用模拟数据:', apiError)
      throw apiError // 向上抛出错误，统一在外层catch处理
    }
  } catch (error) {
    console.error('加载地区树形结构失败:', error)
    ElMessage.warning('加载地区数据失败，使用模拟数据')
    loadError.value = true
    
    // 使用模拟数据
    const mockData = generateMockRegionTree()
    regionTree.value = mockData
    currentRegions.value = mockData
    
    // 如果有初始选中的地区ID，尝试在模拟数据中查找
    if (props.initialRegionId) {
      const findRegion = (regions: any[], id: number): any => {
        for (const region of regions) {
          if (region.id === id) return region
          if (region.children && region.children.length > 0) {
            const found = findRegion(region.children, id)
            if (found) return found
          }
        }
        return null
      }
      
      const region = findRegion(mockData, props.initialRegionId)
      if (region) {
        selectedRegion.value = region
      }
    }
  } finally {
    loading.value = false
  }
}

/**
 * 重试加载
 */
const retryLoad = () => {
  loadRegionTree()
}

/**
 * 查找并选中指定ID的地区
 */
const findAndSelectRegion = async (regionId: number) => {
  try {
    // 添加超时处理
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('请求超时')), 5000)
    })
    
    try {
      // 使用Promise.race实现请求超时处理
      const response = await Promise.race([
        regionApi.getRegionById(regionId),
        timeoutPromise
      ]) as any
      
      // 处理响应数据
      let region = null
      
      if (response) {
        // 如果response本身就是对象（已经被请求拦截器处理过）
        if (response.id) {
          region = response
        }
        // 如果response包含data属性（标准格式）
        else if (response.data) {
          region = response.data
        }
      }
      
      if (region && region.id) {
        selectedRegion.value = region
        
        // 如果有父级地区，加载其祖先地区作为面包屑
        if (region.parentId) {
          await loadAncestors(region.parentId)
        }
      } else {
        throw new Error('地区数据格式无法识别')
      }
    } catch (apiError) {
      console.warn('API调用失败，尝试在本地数据中查找:', apiError)
      
      // 在本地数据中查找地区
      const findRegion = (regions: any[], id: number): any => {
        for (const region of regions) {
          if (region.id === id) return region
          if (region.children && region.children.length > 0) {
            const found = findRegion(region.children, id)
            if (found) return found
          }
        }
        return null
      }
      
      const region = findRegion(regionTree.value, regionId)
      if (region) {
        selectedRegion.value = region
        
        // 构建面包屑
        const buildBreadcrumbs = (regions: any[], targetId: number, path: any[] = []): boolean => {
          for (const region of regions) {
            if (region.id === targetId) {
              return true
            }
            
            if (region.children && region.children.length > 0) {
              path.push(region)
              const found = buildBreadcrumbs(region.children, targetId, path)
              if (found) return true
              path.pop()
            }
          }
          return false
        }
        
        // 如果有父级地区，尝试构建面包屑
        if (region.parentId) {
          const path: any[] = []
          buildBreadcrumbs(regionTree.value, region.parentId, path)
          breadcrumbs.value = path
        }
      } else {
        ElMessage.warning('无法找到指定的地区')
      }
    }
  } catch (error) {
    console.error('查找地区失败:', error)
    ElMessage.error('查找地区失败')
  }
}

/**
 * 加载地区的祖先地区
 */
const loadAncestors = async (regionId: number) => {
  try {
    // 添加超时处理
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('请求超时')), 5000)
    })
    
    try {
      // 使用Promise.race实现请求超时处理
      const response = await Promise.race([
        regionApi.getRegionAncestors(regionId),
        timeoutPromise
      ]) as any
      
      // 处理响应数据
      let ancestors = null
      
      if (response) {
        // 如果response本身就是数组（已经被请求拦截器处理过）
        if (Array.isArray(response)) {
          ancestors = response
        }
        // 如果response包含data属性（标准格式）
        else if (response.data) {
          // 如果data是数组
          if (Array.isArray(response.data)) {
            ancestors = response.data
          }
          // 如果data包含其他数据结构
          else if (response.data.data && Array.isArray(response.data.data)) {
            ancestors = response.data.data
          }
        }
      }
      
      if (ancestors && ancestors.length > 0) {
        // 设置面包屑 - 确保正确的顺序（从根到叶）
        breadcrumbs.value = [...ancestors].reverse()
        
        // 如果有祖先地区，设置当前显示的地区列表为最后一个祖先的子地区
        if (breadcrumbs.value.length > 0) {
          const lastAncestor = breadcrumbs.value[breadcrumbs.value.length - 1]
          await navigateToRegion(lastAncestor.id)
        }
      } else {
        throw new Error('祖先地区数据格式无法识别或为空')
      }
    } catch (apiError) {
      console.warn('API调用失败，尝试在本地数据中构建祖先关系:', apiError)
      
      // 在本地数据中构建祖先关系
      const buildAncestors = (regions: any[], targetId: number, path: any[] = []): boolean => {
        for (const region of regions) {
          if (region.id === targetId) {
            return true
          }
          
          if (region.children && region.children.length > 0) {
            path.push(region)
            const found = buildAncestors(region.children, targetId, path)
            if (found) return true
            path.pop()
          }
        }
        return false
      }
      
      const path: any[] = []
      buildAncestors(regionTree.value, regionId, path)
      
      if (path.length > 0) {
        breadcrumbs.value = path
        
        // 设置当前显示的地区列表为最后一个祖先的子地区
        const lastAncestor = path[path.length - 1]
        if (lastAncestor.children && lastAncestor.children.length > 0) {
          currentRegions.value = lastAncestor.children
        }
      }
    }
  } catch (error) {
    console.error('加载祖先地区失败:', error)
    ElMessage.warning('加载祖先地区失败，使用本地数据')
  }
}

/**
 * 处理地区点击
 */
const handleRegionClick = async (region: any) => {
  if (region.children && region.children.length > 0) {
    // 如果有子地区，导航到子地区
    breadcrumbs.value.push(region)
    // 对于模拟数据，直接使用children
    currentRegions.value = region.children
  } else {
    // 如果没有子地区，直接选中
    selectRegion(region)
  }
}

/**
 * 导航到指定地区的子地区
 */
const navigateToRegion = async (regionId: number) => {
  loading.value = true
  try {
    // 添加超时处理
    const timeoutPromise = new Promise((_, reject) => {
      setTimeout(() => reject(new Error('请求超时')), 5000)
    })
    
    try {
      // 使用Promise.race实现请求超时处理
      const response = await Promise.race([
        regionApi.getRegionDescendants(regionId),
        timeoutPromise
      ]) as any
      
      // 处理响应数据
      let descendants = null
      
      if (response) {
        // 如果response本身就是数组（已经被请求拦截器处理过）
        if (Array.isArray(response)) {
          descendants = response
        }
        // 如果response包含data属性（标准格式）
        else if (response.data) {
          // 如果data是数组
          if (Array.isArray(response.data)) {
            descendants = response.data
          }
          // 如果data包含其他数据结构
          else if (response.data.data && Array.isArray(response.data.data)) {
            descendants = response.data.data
          }
          // 如果data是对象但有content属性（分页格式）
          else if (response.data.content && Array.isArray(response.data.content)) {
            descendants = response.data.content
          }
        }
        // 如果response有content属性（某些API直接返回分页对象）
        else if (response.content && Array.isArray(response.content)) {
          descendants = response.content
        }
      }
      
      if (descendants && descendants.length > 0) {
        currentRegions.value = descendants
      } else {
        // 如果没有子地区，显示空列表
        currentRegions.value = []
        ElMessage.info('该地区没有子地区')
      }
    } catch (apiError) {
      console.warn('API调用失败，尝试在本地数据中查找子地区:', apiError)
      
      // 在本地数据中查找地区及其子地区
      const findRegionChildren = (regions: any[], id: number): any[] | null => {
        for (const region of regions) {
          if (region.id === id) {
            return region.children || []
          }
          
          if (region.children && region.children.length > 0) {
            const found = findRegionChildren(region.children, id)
            if (found) return found
          }
        }
        return null
      }
      
      const children = findRegionChildren(regionTree.value, regionId)
      if (children) {
        currentRegions.value = children
        if (children.length === 0) {
          ElMessage.info('该地区没有子地区')
        }
      } else {
        ElMessage.warning('无法找到指定地区的子地区')
        // 保持当前显示不变
      }
    }
  } catch (error) {
    console.error('加载子地区失败:', error)
    ElMessage.error('加载子地区失败')
  } finally {
    loading.value = false
  }
}

/**
 * 导航到面包屑指定位置
 */
const navigateToBreadcrumb = async (index: number) => {
  loading.value = true
  
  try {
    // 截取到指定索引的面包屑
    breadcrumbs.value = breadcrumbs.value.slice(0, index + 1)
    
    if (index === -1) {
      // 返回根级别
      currentRegions.value = regionTree.value
      breadcrumbs.value = []
    } else {
      // 导航到指定面包屑的子地区
      const targetCrumb = breadcrumbs.value[index]
      
      if (!targetCrumb || !targetCrumb.id) {
        throw new Error('面包屑数据无效')
      }
      
      if (targetCrumb.children && Array.isArray(targetCrumb.children) && targetCrumb.children.length > 0) {
        // 如果面包屑对象中已包含子地区数据，直接使用
        currentRegions.value = targetCrumb.children
      } else {
        // 如果没有子地区数据，尝试通过API获取
        try {
          // 添加超时处理
          const timeoutPromise = new Promise((_, reject) => {
            setTimeout(() => reject(new Error('请求超时')), 5000)
          })
          
          // 使用Promise.race实现请求超时处理
          const response = await Promise.race([
            regionApi.getRegionDescendants(targetCrumb.id),
            timeoutPromise
          ]) as any
          
          // 处理响应数据
          let descendants = null
          
          if (response) {
            // 如果response本身就是数组（已经被请求拦截器处理过）
            if (Array.isArray(response)) {
              descendants = response
            }
            // 如果response包含data属性（标准格式）
            else if (response.data) {
              // 如果data是数组
              if (Array.isArray(response.data)) {
                descendants = response.data
              }
              // 如果data包含其他数据结构
              else if (response.data.data && Array.isArray(response.data.data)) {
                descendants = response.data.data
              }
              // 如果data是对象但有content属性（分页格式）
              else if (response.data.content && Array.isArray(response.data.content)) {
                descendants = response.data.content
              }
            }
            // 如果response有content属性（某些API直接返回分页对象）
            else if (response.content && Array.isArray(response.content)) {
              descendants = response.content
            }
          }
          
          if (descendants && descendants.length > 0) {
            currentRegions.value = descendants
            // 更新面包屑中的子地区数据，避免重复请求
            targetCrumb.children = descendants
          } else {
            // 如果没有子地区，显示空列表
            currentRegions.value = []
            ElMessage.info('该地区没有子地区')
          }
        } catch (apiError) {
          console.warn('API调用失败，尝试在本地数据中查找子地区:', apiError)
          
          // 在本地数据中查找地区及其子地区
          const findRegionChildren = (regions: any[], id: number): any[] | null => {
            for (const region of regions) {
              if (region.id === id) {
                return region.children || []
              }
              
              if (region.children && region.children.length > 0) {
                const found = findRegionChildren(region.children, id)
                if (found) return found
              }
            }
            return null
          }
          
          const children = findRegionChildren(regionTree.value, targetCrumb.id)
          if (children) {
            currentRegions.value = children
            // 更新面包屑中的子地区数据，避免重复请求
            targetCrumb.children = children
            
            if (children.length === 0) {
              ElMessage.info('该地区没有子地区')
            }
          } else {
            ElMessage.warning('无法找到指定地区的子地区')
            // 保持当前显示不变
          }
        }
      }
    }
  } catch (error) {
    console.error('导航到面包屑位置失败:', error)
    ElMessage.error('导航失败')
    
    // 恢复到根级别作为降级处理
    breadcrumbs.value = []
    currentRegions.value = regionTree.value
  } finally {
    loading.value = false
  }
}

/**
 * 选中地区
 */
const selectRegion = (region: any) => {
  selectedRegion.value = region
}

/**
 * 清除选择
 */
const clearSelection = () => {
  selectedRegion.value = null
}

/**
 * 处理搜索
 */
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    clearSearch()
    return
  }
  
  searchMode.value = true
  loading.value = true
  
  try {
    try {
      // 添加超时处理
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('搜索请求超时')), 5000)
      })
      
      // 使用Promise.race实现请求超时处理
      const response = await Promise.race([
        regionApi.searchRegions(searchKeyword.value, {
          page: 0,
          size: 50 // 增加搜索结果数量
        }),
        timeoutPromise
      ]) as any
      
      // 增强处理不同格式的响应能力
      let processedResults = null
      
      if (response) {
        // 如果response本身就是数组（已经被请求拦截器处理过）
        if (Array.isArray(response)) {
          processedResults = response
        }
        // 如果response包含data属性（标准格式）
        else if (response.data) {
          // 如果data是数组
          if (Array.isArray(response.data)) {
            processedResults = response.data
          } 
          // 如果data是分页对象
          else if (response.data.content && Array.isArray(response.data.content)) {
            processedResults = response.data.content
          }
          // 如果data包含其他数据结构
          else if (response.data.data && Array.isArray(response.data.data)) {
            processedResults = response.data.data
          }
        }
        // 如果response有content属性（某些API直接返回分页对象）
        else if (response.content && Array.isArray(response.content)) {
          processedResults = response.content
        }
      }
      
      if (processedResults && processedResults.length > 0) {
        searchResults.value = processedResults
        return
      } else {
        console.warn('API搜索返回了空数据或格式无法识别:', response)
        throw new Error('搜索结果格式无法识别')
      }
    } catch (apiError) {
      console.warn('API搜索调用失败，使用本地搜索:', apiError)
      throw apiError // 向上抛出错误，统一在外层catch处理
    }
  } catch (error) {
    console.error('搜索地区失败，使用本地搜索:', error)
    
    // 如果API调用失败，使用本地数据进行搜索
    const keyword = searchKeyword.value.toLowerCase()
    const mockResults: any[] = []
    
    // 递归搜索函数 - 增强搜索逻辑
    const searchInRegions = (regions: any[]) => {
      if (!regions || !Array.isArray(regions)) return
      
      for (const region of regions) {
        // 检查名称是否包含关键词
        if (region.name && region.name.toLowerCase().includes(keyword)) {
          // 避免重复添加
          if (!mockResults.some(r => r.id === region.id)) {
            mockResults.push({...region}) // 使用浅拷贝避免引用问题
          }
        }
        
        // 递归搜索子地区
        if (region.children && Array.isArray(region.children) && region.children.length > 0) {
          searchInRegions(region.children)
        }
      }
    }
    
    // 在当前加载的地区树中搜索
    searchInRegions(regionTree.value)
    
    // 如果没有找到结果，显示提示信息
    if (mockResults.length === 0) {
      ElMessage.info(`未找到包含"${searchKeyword.value}"的地区`)
    } else {
      ElMessage.success(`找到 ${mockResults.length} 个匹配地区`)
    }
    
    searchResults.value = mockResults
  } finally {
    loading.value = false
  }
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
  searchMode.value = false
}

/**
 * 获取地区类型标签样式
 */
const getRegionTypeTag = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'continent': '',
    'country': 'success',
    'province': 'warning',
    'city': 'danger'
  }
  return typeMap[type] || ''
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
 * 获取地区路径
 */
const getRegionPath = (region: any) => {
  // 这里简化处理，实际应该通过API获取完整路径
  if (region.level === 1) return '洲级地区'
  if (region.level === 2) return '国家级地区'
  if (region.level === 3) return '省级地区'
  if (region.level === 4) return '市级地区'
  return ''
}

/**
 * 生成模拟地区树数据
 */
const generateMockRegionTree = () => {
  return [
    {
      id: 1,
      name: '亚洲',
      type: 'continent',
      level: 1,
      parentId: null,
      children: [
        {
          id: 101,
          name: '中国',
          type: 'country',
          level: 2,
          parentId: 1,
          children: [
            {
              id: 10101,
              name: '北京市',
              type: 'province',
              level: 3,
              parentId: 101,
              children: [
                {
                  id: 1010101,
                  name: '海淀区',
                  type: 'city',
                  level: 4,
                  parentId: 10101,
                  children: []
                },
                {
                  id: 1010102,
                  name: '朝阳区',
                  type: 'city',
                  level: 4,
                  parentId: 10101,
                  children: []
                }
              ]
            },
            {
              id: 10102,
              name: '上海市',
              type: 'province',
              level: 3,
              parentId: 101,
              children: [
                {
                  id: 1010201,
                  name: '浦东新区',
                  type: 'city',
                  level: 4,
                  parentId: 10102,
                  children: []
                },
                {
                  id: 1010202,
                  name: '黄浦区',
                  type: 'city',
                  level: 4,
                  parentId: 10102,
                  children: []
                }
              ]
            },
            {
              id: 10103,
              name: '广东省',
              type: 'province',
              level: 3,
              parentId: 101,
              children: [
                {
                  id: 1010301,
                  name: '广州市',
                  type: 'city',
                  level: 4,
                  parentId: 10103,
                  children: []
                },
                {
                  id: 1010302,
                  name: '深圳市',
                  type: 'city',
                  level: 4,
                  parentId: 10103,
                  children: []
                }
              ]
            }
          ]
        },
        {
          id: 102,
          name: '日本',
          type: 'country',
          level: 2,
          parentId: 1,
          children: [
            {
              id: 10201,
              name: '东京都',
              type: 'province',
              level: 3,
              parentId: 102,
              children: []
            },
            {
              id: 10202,
              name: '大阪府',
              type: 'province',
              level: 3,
              parentId: 102,
              children: []
            }
          ]
        }
      ]
    },
    {
      id: 2,
      name: '欧洲',
      type: 'continent',
      level: 1,
      parentId: null,
      children: [
        {
          id: 201,
          name: '法国',
          type: 'country',
          level: 2,
          parentId: 2,
          children: [
            {
              id: 20101,
              name: '巴黎',
              type: 'province',
              level: 3,
              parentId: 201,
              children: []
            }
          ]
        },
        {
          id: 202,
          name: '德国',
          type: 'country',
          level: 2,
          parentId: 2,
          children: [
            {
              id: 20201,
              name: '柏林',
              type: 'province',
              level: 3,
              parentId: 202,
              children: []
            }
          ]
        }
      ]
    },
    {
      id: 3,
      name: '北美洲',
      type: 'continent',
      level: 1,
      parentId: null,
      children: [
        {
          id: 301,
          name: '美国',
          type: 'country',
          level: 2,
          parentId: 3,
          children: [
            {
              id: 30101,
              name: '纽约州',
              type: 'province',
              level: 3,
              parentId: 301,
              children: [
                {
                  id: 3010101,
                  name: '纽约市',
                  type: 'city',
                  level: 4,
                  parentId: 30101,
                  children: []
                }
              ]
            },
            {
              id: 30102,
              name: '加利福尼亚州',
              type: 'province',
              level: 3,
              parentId: 301,
              children: [
                {
                  id: 3010201,
                  name: '洛杉矶',
                  type: 'city',
                  level: 4,
                  parentId: 30102,
                  children: []
                },
                {
                  id: 3010202,
                  name: '旧金山',
                  type: 'city',
                  level: 4,
                  parentId: 30102,
                  children: []
                }
              ]
            }
          ]
        },
        {
          id: 302,
          name: '加拿大',
          type: 'country',
          level: 2,
          parentId: 3,
          children: []
        }
      ]
    }
  ]
}

/**
 * 处理取消
 */
const handleCancel = () => {
  emit('cancel')
}

/**
 * 处理确认
 */
const handleConfirm = () => {
  if (selectedRegion.value) {
    emit('confirm', selectedRegion.value)
  } else {
    ElMessage.warning('请先选择一个地区')
  }
}
</script>

<style scoped lang="scss">
.region-selector {
  display: flex;
  flex-direction: column;
  height: 450px;
  
  .region-search {
    margin-bottom: 16px;
  }
  
  .loading-container {
    flex: 1;
    padding: 20px;
  }
  
  .error-container {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .search-results {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .result-title {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
      padding: 0 8px;
    }
    
    .search-actions {
      margin-top: 12px;
      text-align: center;
    }
  }
  
  .empty-search {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .region-tree-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    .breadcrumb-container {
      margin-bottom: 12px;
      padding: 8px;
      background: #f5f7fa;
      border-radius: 4px;
      
      :deep(.el-breadcrumb__item) {
        cursor: pointer;
      }
      
      .breadcrumb-link {
        color: #409eff;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
    
    .region-list {
      padding: 4px 0;
    }
  }
  
  .region-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: #f5f7fa;
    }
    
    &.is-selected {
      background-color: #ecf5ff;
      color: #409eff;
    }
    
    .region-info {
      display: flex;
      align-items: center;
      
      .region-name {
        font-weight: 500;
      }
      
      .region-count {
        margin-left: 4px;
        color: #909399;
        font-size: 12px;
      }
      
      .region-path {
        margin-left: 8px;
        font-size: 12px;
        color: #909399;
      }
    }
    
    .region-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .expand-icon {
        color: #909399;
      }
    }
  }
  
  .region-selector-footer {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .selected-region {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .label {
        color: #606266;
      }
      
      .region-tag {
        display: flex;
        align-items: center;
        
        .close-icon {
          margin-left: 4px;
          cursor: pointer;
        }
      }
    }
    
    .selector-actions {
      display: flex;
      gap: 12px;
    }
  }
}
</style>