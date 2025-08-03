<template>
  <div class="deduplication-panel">
    <el-alert
      title="事件去重功能"
      description="此功能用于识别和处理重复的事件，基于主体、客体、类型、时间、地点进行匹配。"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <p>去重功能正在开发中，敬请期待...</p>
    
    <div style="text-align: right; margin-top: 20px;">
      <el-button @click="$emit('close')">关闭</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 定义 emits
defineEmits<{
  close: []
}>()
</script>

<style scoped>
.deduplication-panel {
  padding: 20px;
}
</style> 