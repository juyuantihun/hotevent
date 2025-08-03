<template>
  <div 
    class="base-card"
    :class="{ 'card-visible': true, 'is-clickable': clickable }"
    @click="handleClick"
  >
    <div class="card-header" v-if="$slots.header">
      <slot name="header"></slot>
    </div>
    
    <div class="card-content">
      <slot></slot>
    </div>
    
    <div class="card-footer" v-if="$slots.footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  // 是否可点击
  clickable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['click'])

// 处理点击事件
const handleClick = (event: MouseEvent) => {
  if (props.clickable) {
    emit('click', event)
  }
}
</script>

<style scoped>
.base-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 16px;
  transition: all 0.3s ease;
  margin: 10px;
  opacity: 0;
  transform: translateY(10px);
  animation: fadeIn 0.3s forwards;
}

.base-card.card-visible {
  opacity: 1;
  transform: translateY(0);
}

.base-card.is-clickable {
  cursor: pointer;
}

.base-card.is-clickable:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.card-header {
  margin-bottom: 12px;
}

.card-content {
  padding: 8px 0;
}

.card-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f2f5;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .base-card {
    background: #1a1a1a;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
  
  .card-footer {
    border-top-color: #333;
  }
}

/* 响应式布局调整 */
@media (max-width: 768px) {
  .base-card {
    padding: 12px;
  }
}
</style>