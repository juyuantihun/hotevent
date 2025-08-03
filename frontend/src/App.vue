<template>
  <div id="app">
    <ErrorBoundary reportToGlobal>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <ErrorBoundary>
            <component :is="Component" />
          </ErrorBoundary>
        </transition>
      </router-view>
      <HeadManager />
      <PreloadManager />
    </ErrorBoundary>
    
    <!-- 全局错误显示 -->
    <GlobalErrorDisplay />
  </div>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import HeadManager from './components/common/HeadManager.vue'
import ErrorBoundary from './components/common/ErrorBoundary.vue'
import GlobalErrorDisplay from './components/common/GlobalErrorDisplay.vue'

// 使用异步组件加载预加载管理器，避免影响首屏渲染
const PreloadManager = defineAsyncComponent(() => 
  import('./components/common/PreloadManager.vue'))
</script>

<style>
#app {
  height: 100vh;
  overflow: hidden;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background-color: #f5f7fa;
}

.page-container {
  padding: 20px;
  background-color: #ffffff;
  margin: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 32px);
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.page-description {
  color: #606266;
  font-size: 14px;
}

/* 页面过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 