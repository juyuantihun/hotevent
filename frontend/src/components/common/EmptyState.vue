<template>
  <div class="empty-state" :class="{ 'is-small': small }">
    <div class="empty-image">
      <slot name="image">
        <img v-if="image" :src="image" :alt="description" class="custom-image" />
        <svg v-else class="default-image" viewBox="0 0 79 86" xmlns="http://www.w3.org/2000/svg">
          <path d="M78.0686 30.1318C77.9304 29.7697 77.6065 29.5983 77.2443 29.5983H26.8791L25.9721 26.5H79.3686C79.8929 26.5 80.2551 26.1379 80.2551 25.6136C80.2551 25.0894 79.8929 24.7273 79.3686 24.7273H25.0652C24.6136 24.7273 24.2514 25.0894 24.1136 25.5379L22.9652 29.5983H1.75573C1.25 29.5983 0.887787 30.0983 0.887787 30.6225C0.887787 30.7604 0.887787 30.8983 0.956878 31.0361L10.4304 78.2719C10.5686 78.6341 10.8929 78.8056 11.2551 78.8056H67.7443C68.1065 78.8056 68.4304 78.6341 68.5686 78.2719L78.0421 31.0361C78.1799 30.7604 78.1799 30.4939 78.0686 30.1318ZM66.9929 77.0328H12.0065L2.87787 31.371H76.1215L66.9929 77.0328Z" fill="currentColor" />
          <path d="M25.9721 26.5H79.3686C79.8929 26.5 80.2551 26.1379 80.2551 25.6136C80.2551 25.0894 79.8929 24.7273 79.3686 24.7273H25.0652C24.6136 24.7273 24.2514 25.0894 24.1136 25.5379L22.9652 29.5983H1.75573C1.25 29.5983 0.887787 30.0983 0.887787 30.6225C0.887787 30.7604 0.887787 30.8983 0.956878 31.0361L10.4304 78.2719C10.5686 78.6341 10.8929 78.8056 11.2551 78.8056H67.7443C68.1065 78.8056 68.4304 78.6341 68.5686 78.2719L78.0421 31.0361C78.1799 30.7604 78.1799 30.4939 78.0686 30.1318C77.9304 29.7697 77.6065 29.5983 77.2443 29.5983H26.8791L25.9721 26.5Z" stroke="currentColor" stroke-width="1.5" />
          <path d="M29.6631 43.9773C29.6631 43.4531 30.0252 43.0909 30.5495 43.0909H48.4495C48.9737 43.0909 49.3359 43.4531 49.3359 43.9773C49.3359 44.5015 48.9737 44.8636 48.4495 44.8636H30.5495C30.0252 44.8636 29.6631 44.5015 29.6631 43.9773Z" fill="currentColor" />
          <path d="M28.7764 51.9773C28.7764 51.4531 29.1385 51.0909 29.6628 51.0909H49.3359C49.8602 51.0909 50.2223 51.4531 50.2223 51.9773C50.2223 52.5015 49.8602 52.8636 49.3359 52.8636H29.6628C29.1385 52.8636 28.7764 52.5015 28.7764 51.9773Z" fill="currentColor" />
          <path d="M27.8896 59.9773C27.8896 59.4531 28.2518 59.0909 28.776 59.0909H50.2223C50.7465 59.0909 51.1087 59.4531 51.1087 59.9773C51.1087 60.5015 50.7465 60.8636 50.2223 60.8636H28.776C28.2518 60.8636 27.8896 60.5015 27.8896 59.9773Z" fill="currentColor" />
        </svg>
      </slot>
    </div>
    <div class="empty-description">
      <slot name="description">{{ description }}</slot>
    </div>
    <div v-if="$slots.default" class="empty-actions">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps({
  // 描述文本
  description: {
    type: String,
    default: '暂无数据'
  },
  // 自定义图片
  image: {
    type: String,
    default: ''
  },
  // 是否使用小尺寸
  small: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.empty-state.is-small {
  padding: 20px 0;
}

.empty-image {
  margin-bottom: 20px;
}

.default-image {
  width: 90px;
  height: 90px;
  color: var(--text-secondary, #909399);
}

.custom-image {
  max-width: 100%;
  max-height: 120px;
}

.empty-state.is-small .default-image {
  width: 60px;
  height: 60px;
}

.empty-state.is-small .custom-image {
  max-height: 80px;
}

.empty-description {
  margin-bottom: 16px;
  color: var(--text-secondary, #909399);
  font-size: 14px;
  text-align: center;
  line-height: 1.5;
}

.empty-state.is-small .empty-description {
  font-size: 12px;
  margin-bottom: 12px;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 暗色模式支持 */
@media (prefers-color-scheme: dark) {
  .default-image {
    color: var(--text-secondary, #a0a0a0);
  }
  
  .empty-description {
    color: var(--text-secondary, #a0a0a0);
  }
}
</style>