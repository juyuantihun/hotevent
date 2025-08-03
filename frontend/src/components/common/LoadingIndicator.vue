<template>
  <div class="loading-indicator" :class="{ 'is-fullscreen': fullscreen }">
    <div class="loading-spinner">
      <div class="loading-spinner-inner"></div>
    </div>
    <div v-if="text" class="loading-text">{{ text }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  text: {
    type: String,
    default: '加载中...'
  },
  fullscreen: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.loading-indicator.is-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 9999;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  position: relative;
}

.loading-spinner-inner {
  width: 100%;
  height: 100%;
  border: 3px solid rgba(24, 144, 255, 0.2);
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>