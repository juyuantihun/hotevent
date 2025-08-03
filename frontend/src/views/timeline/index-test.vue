<template>
  <div class="timeline-container">
    <h2>时间线测试页面</h2>
    <p>这是一个简单的测试页面，用于验证组件加载是否正常。</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const message = ref('Hello Timeline!')
</script>

<style scoped>
.timeline-container {
  padding: 20px;
}
</style>