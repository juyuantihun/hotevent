<template>
  <div class="dictionary-panel">
    <el-alert
      title="自动字典管理"
      description="此功能会自动从事件中提取人物、组织、国家等实体信息，并添加到字典管理系统中。"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <p>字典管理功能正在开发中，敬请期待...</p>
    
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
.dictionary-panel {
  padding: 20px;
}
</style> 