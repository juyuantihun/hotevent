<template>
  <div class="error-tester">
    <h3>错误测试工具</h3>
    <p>用于测试全局错误处理机制</p>
    
    <div class="button-group">
      <button @click="triggerComponentError" class="test-button">
        触发组件渲染错误
      </button>
      
      <button @click="triggerPromiseError" class="test-button">
        触发Promise错误
      </button>
      
      <button @click="triggerRuntimeError" class="test-button">
        触发运行时错误
      </button>
      
      <button @click="triggerResourceError" class="test-button">
        触发资源加载错误
      </button>
      
      <button @click="triggerApiError" class="test-button">
        触发API请求错误
      </button>
      
      <button @click="triggerGlobalError" class="test-button">
        设置全局错误状态
      </button>
    </div>
    
    <div v-if="showErrorComponent" class="error-component">
      <!-- 这里会触发错误 -->
      {{ nonExistentProperty.value }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '@/store'
import { handleError, ErrorType, ErrorSeverity } from '@/services/errorHandler'

// 应用状态
const appStore = useAppStore()

// 控制是否显示会触发错误的组件
const showErrorComponent = ref(false)

/**
 * 触发组件渲染错误
 */
function triggerComponentError() {
  showErrorComponent.value = true
}

/**
 * 触发Promise错误
 */
function triggerPromiseError() {
  // 创建一个会被拒绝的Promise
  new Promise((resolve, reject) => {
    setTimeout(() => {
      reject(new Error('这是一个未处理的Promise错误'))
    }, 100)
  })
}

/**
 * 触发运行时错误
 */
function triggerRuntimeError() {
  // 故意调用不存在的函数
  const nonExistentFunction = undefined as any
  nonExistentFunction()
}

/**
 * 触发资源加载错误
 */
function triggerResourceError() {
  // 创建一个不存在的图片元素
  const img = document.createElement('img')
  img.src = '/non-existent-image.jpg'
  document.body.appendChild(img)
  
  // 稍后移除图片元素
  setTimeout(() => {
    if (document.body.contains(img)) {
      document.body.removeChild(img)
    }
  }, 1000)
}

/**
 * 触发API请求错误
 */
function triggerApiError() {
  // 发起一个会失败的请求
  fetch('/api/non-existent-endpoint')
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP错误: ${response.status}`)
      }
      return response.json()
    })
    .catch(error => {
      // 手动处理错误
      handleError(error, ErrorType.API, ErrorSeverity.ERROR, {
        userAction: '测试API错误'
      })
    })
}

/**
 * 设置全局错误状态
 */
function triggerGlobalError() {
  appStore.setGlobalError('这是一个全局错误状态测试')
  
  // 5秒后自动清除
  setTimeout(() => {
    appStore.setGlobalError(null)
  }, 5000)
}
</script>

<style scoped>
.error-tester {
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 20px;
  background-color: #f8f9fa;
}

h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #303133;
}

p {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.test-button {
  padding: 8px 16px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.test-button:hover {
  background-color: #f78989;
}

.error-component {
  padding: 10px;
  border: 1px dashed #f56c6c;
  border-radius: 4px;
  margin-top: 10px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .error-tester {
    background-color: #1a1a1a;
    border-color: #4c4c4c;
  }
  
  h3 {
    color: #e0e0e0;
  }
  
  p {
    color: #c0c0c0;
  }
}
</style>