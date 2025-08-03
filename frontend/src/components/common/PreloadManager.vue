<template>
  <div class="preload-manager">
    <!-- 预加载管理器不渲染任何内容 -->
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { preloadRoutes, preloadImages, preloadComponents, preconnectResources } from '@/config/preload'

const router = useRouter()

// 预加载路由组件
const preloadRouteComponents = () => {
  try {
    preloadRoutes.forEach(route => {
      try {
        const matchedRoute = router.resolve(route)
        if (matchedRoute && matchedRoute.matched && matchedRoute.matched.length > 0) {
          const routeComponent = matchedRoute.matched[0].components?.default
          if (routeComponent && typeof routeComponent === 'function') {
            routeComponent()
          }
        }
      } catch (error) {
        console.warn(`预加载路由 ${route} 失败:`, error)
      }
    })
  } catch (error) {
    console.warn('预加载路由组件失败:', error)
  }
}

// 预加载组件
const preloadComponentResources = () => {
  preloadComponents.forEach(loader => {
    loader()
  })
}

// 预加载图片
const preloadImageResources = () => {
  preloadImages.forEach(src => {
    const img = new Image()
    img.src = src
  })
}

// 添加资源提示
const addResourceHints = () => {
  // DNS预解析
  const dnsPreconnect = document.createElement('link')
  dnsPreconnect.rel = 'dns-prefetch'
  dnsPreconnect.href = window.location.origin
  document.head.appendChild(dnsPreconnect)
  
  // 预连接API服务器
  const preconnect = document.createElement('link')
  preconnect.rel = 'preconnect'
  preconnect.href = window.location.origin
  document.head.appendChild(preconnect)
  
  // 添加其他预连接资源
  preconnectResources.forEach(url => {
    const link = document.createElement('link')
    link.rel = 'preconnect'
    link.href = url
    document.head.appendChild(link)
  })
}

// 移除资源提示
const removeResourceHints = () => {
  document.querySelectorAll('link[rel="dns-prefetch"], link[rel="preconnect"]').forEach(el => {
    el.remove()
  })
}

// 分阶段预加载资源
const preloadResourcesInStages = () => {
  // 第一阶段：关键资源
  addResourceHints()
  
  // 第二阶段：路由组件
  if ('requestIdleCallback' in window) {
    window.requestIdleCallback(() => {
      preloadRouteComponents()
    }, { timeout: 1000 })
  } else {
    setTimeout(preloadRouteComponents, 300)
  }
  
  // 第三阶段：其他资源
  if ('requestIdleCallback' in window) {
    window.requestIdleCallback(() => {
      preloadImageResources()
      preloadComponentResources()
    }, { timeout: 2000 })
  } else {
    setTimeout(() => {
      preloadImageResources()
      preloadComponentResources()
    }, 1000)
  }
}

onMounted(() => {
  // 在首次渲染完成后开始预加载资源
  setTimeout(preloadResourcesInStages, 0)
})

onUnmounted(() => {
  removeResourceHints()
})
</script>