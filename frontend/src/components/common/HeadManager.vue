<template>
  <div style="display: none;">
    <!-- 该组件不渲染任何可见内容 -->
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 添加关键CSS
const addCriticalCSS = () => {
  const style = document.createElement('style')
  style.id = 'critical-css'
  style.textContent = `
    /* 关键渲染路径所需的最小CSS */
    body { opacity: 1; transition: opacity 0.3s; }
    .app-loading { position: fixed; top: 0; left: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background-color: #f5f7fa; z-index: 9999; }
    .app-loading-spinner { width: 50px; height: 50px; border: 3px solid rgba(0, 0, 0, 0.1); border-radius: 50%; border-top-color: #409eff; animation: spin 1s ease-in-out infinite; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `
  document.head.appendChild(style)
}

// 添加资源预加载提示
const addResourceHints = () => {
  try {
    // 预加载关键CSS - 使用相对路径
    const cssPreload = document.createElement('link')
    cssPreload.rel = 'preload'
    cssPreload.as = 'style'
    cssPreload.href = './style/fixed-index.css'
    document.head.appendChild(cssPreload)
    
    // 预加载字体 - 使用相对路径
    const fontPreload = document.createElement('link')
    fontPreload.rel = 'preload'
    fontPreload.as = 'font'
    fontPreload.href = './assets/fonts/element-icons.woff'
    fontPreload.crossOrigin = 'anonymous'
    document.head.appendChild(fontPreload)
    
    // 添加DNS预解析
    const dnsPreconnect = document.createElement('link')
    dnsPreconnect.rel = 'dns-prefetch'
    dnsPreconnect.href = window.location.origin
    document.head.appendChild(dnsPreconnect)
  } catch (error) {
    console.warn('添加资源预加载提示失败:', error)
  }
}

// 更新页面元数据
const updateMetadata = (title: string, description: string) => {
  // 更新标题
  document.title = title ? `${title} - TimeFlow事件管理系统` : 'TimeFlow事件管理系统'
  
  // 更新描述
  let metaDescription = document.querySelector('meta[name="description"]')
  if (!metaDescription) {
    metaDescription = document.createElement('meta')
    metaDescription.setAttribute('name', 'description')
    document.head.appendChild(metaDescription)
  }
  metaDescription.setAttribute('content', description || '国际热点事件管理系统')
}

// 清理资源
const cleanup = () => {
  // 移除关键CSS
  const criticalCSS = document.getElementById('critical-css')
  if (criticalCSS) {
    criticalCSS.remove()
  }
  
  // 移除预加载提示
  document.querySelectorAll('link[rel="preload"]').forEach(el => {
    el.remove()
  })
}

// 监听路由变化，更新元数据
watch(
  () => route.meta,
  (meta) => {
    if (meta) {
      updateMetadata(meta.title as string, meta.description as string)
    }
  },
  { immediate: true }
)

onMounted(() => {
  addCriticalCSS()
  addResourceHints()
  
  // 初始化元数据
  updateMetadata(route.meta.title as string, route.meta.description as string)
  
  // 在页面完全加载后移除关键CSS
  window.addEventListener('load', () => {
    setTimeout(() => {
      const criticalCSS = document.getElementById('critical-css')
      if (criticalCSS) {
        criticalCSS.remove()
      }
    }, 1000)
  })
})

onUnmounted(() => {
  cleanup()
})
</script>