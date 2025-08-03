<template>
  <el-dialog
    v-model="visible"
    title="国家详情"
    width="500px"
    :before-close="handleClose"
  >
    <div v-if="country && Object.keys(country).length">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="国家名称">{{ country.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="简称">{{ country.shortName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="人口">{{ country.population || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面积">{{ country.area || '-' }}</el-descriptions-item>
        <el-descriptions-item label="首都">{{ country.capital || '-' }}</el-descriptions-item>
        <el-descriptions-item label="语言">{{ country.language || '-' }}</el-descriptions-item>
        <el-descriptions-item label="货币">{{ country.currency || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ country.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ country.updatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div v-else>
      <el-empty description="暂无数据" />
    </div>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, defineProps, defineEmits } from 'vue'

const props = defineProps({
  visible: Boolean,
  country: {
    type: Object,
    default: () => ({})
  }
})
const emits = defineEmits(['update:visible'])

const visible = ref(props.visible)

watch(() => props.visible, (val) => {
  visible.value = val
})

function handleClose() {
  emits('update:visible', false)
}
</script>

<style scoped>
.el-descriptions {
  margin-bottom: 16px;
}
</style> 