<template>
  <div class="statistics-panel">
    <el-alert
      title="时间线统计信息"
      description="展示时间线生成的各种统计数据和分析结果。"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <p>统计功能正在开发中，敬请期待...</p>
    
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
.statistics-panel {
  padding: 20px;
}
</style> 