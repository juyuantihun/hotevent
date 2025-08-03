<template>
  <el-dialog
    v-model="visible"
    title="字典详情"
    width="500px"
    :before-close="handleClose"
  >
    <el-descriptions :column="1" border v-if="dictionary && Object.keys(dictionary).length">
      <el-descriptions-item label="字典名称">{{ dictionary.dictName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="字典编码">{{ dictionary.dictCode || '-' }}</el-descriptions-item>
      <el-descriptions-item label="字典类型">{{ dictionary.dictType || '-' }}</el-descriptions-item>
      <el-descriptions-item label="描述">{{ dictionary.dictDescription || '-' }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ dictionary.status === 1 ? '启用' : dictionary.status === 0 ? '禁用' : '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ dictionary.createdAt || '-' }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ dictionary.updatedAt || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建人">{{ dictionary.createdBy || '-' }}</el-descriptions-item>
      <el-descriptions-item label="更新人">{{ dictionary.updatedBy || '-' }}</el-descriptions-item>
    </el-descriptions>
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
  dictionary: {
    type: Object,
    default: () => ({})
  }
})
const emits = defineEmits(['update:visible'])
const visible = ref(props.visible)
watch(() => props.visible, (val) => { visible.value = val })
function handleClose() { emits('update:visible', false) }
</script>

<style scoped>
.el-descriptions {
  margin-bottom: 16px;
}
</style> 