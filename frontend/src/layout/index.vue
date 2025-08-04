<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="layout-header">
      <div class="header-left">
        <el-button
          :icon="isCollapse ? 'Expand' : 'Fold'"
          @click="toggleCollapse"
          circle
          size="small"
        />
        <h1 class="system-title">国际热点事件管理系统</h1>
      </div>
      <div class="header-right">
        <el-button :icon="'Refresh'" @click="refreshPage" circle size="small" />
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            <el-avatar :size="32" :src="authStore.userAvatar" :icon="'User'" />
            <span class="username">{{ authStore.displayName || '用户' }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="layout-main">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>仪表板</template>
          </el-menu-item>
          
          <el-sub-menu index="/event">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>事件管理</span>
            </template>
            <el-menu-item index="/event/list">
              <el-icon><List /></el-icon>
              <template #title>事件列表</template>
            </el-menu-item>
            <el-menu-item index="/event/create">
              <el-icon><Plus /></el-icon>
              <template #title>录入事件</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/timeline">
            <template #title>
              <el-icon><Connection /></el-icon>
              <span>时间线管理</span>
            </template>
            <el-menu-item index="/timeline/list">
              <el-icon><List /></el-icon>
              <template #title>时间线列表</template>
            </el-menu-item>
            <el-menu-item index="/timeline/event-management">
              <el-icon><EditPen /></el-icon>
              <template #title>事件管理</template>
            </el-menu-item>
          </el-sub-menu>

          <!-- 关联关系菜单已隐藏 -->
          <!-- <el-menu-item index="/relation">
            <el-icon><Share /></el-icon>
            <template #title>关联关系</template>
          </el-menu-item> -->

          <el-menu-item index="/dictionary">
            <el-icon><Collection /></el-icon>
            <template #title>字典管理</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Document, List, Plus, Collection, Connection, EditPen } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/modules/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const isCollapse = ref(false)

// 计算属性
const activeMenu = computed(() => {
  const { path } = route
  if (path.startsWith('/event/')) {
    return '/event/list'
  }
  return path
})

// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const refreshPage = () => {
  location.reload()
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '退出确认',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        // 调用认证store的退出登录方法
        await authStore.logoutAction()
        // logoutAction 方法内部已经处理了跳转到登录页面
      } catch (error) {
        // 用户取消退出或退出失败
        if (error !== 'cancel') {
          console.error('退出登录失败:', error)
          ElMessage.error('退出登录失败，请重试')
        }
      }
      break
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

.layout-header {
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.system-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.el-dropdown-link:hover {
  color: #409eff;
}

.username {
  font-size: 14px;
}

.layout-main {
  height: calc(100vh - 60px);
}

.layout-aside {
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s;
}

.sidebar-menu {
  border: none;
  height: 100%;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

.layout-content {
  background: #f5f7fa;
  padding: 0;
  overflow: auto;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 