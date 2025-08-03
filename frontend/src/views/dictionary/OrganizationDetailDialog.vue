<template>
  <el-dialog
    v-model="visible"
    title="组织详情"
    width="500px"
    :before-close="handleClose"
  >
    <div v-if="organization && Object.keys(organization).length">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="组织名称">{{ organization.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="组织类型">{{ organization.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="国家ID">{{ organization.countryId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ organization.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ organization.status === 1 ? '启用' : organization.status === 0 ? '禁用' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ organization.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ organization.updatedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ organization.createdBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新人">{{ organization.updatedBy || '-' }}</el-descriptions-item>
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
  organization: {
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