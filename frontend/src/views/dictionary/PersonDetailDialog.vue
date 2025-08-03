<template>
  <el-dialog
    v-model="visible"
    title="人物详情"
    width="500px"
    :before-close="handleClose"
  >
    <div v-if="person && Object.keys(person).length">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="姓名">{{ person.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ person.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ person.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="国籍ID">{{ person.countryId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属组织ID">{{ person.organizationId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ person.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ person.status === 1 ? '启用' : person.status === 0 ? '禁用' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ person.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ person.updatedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ person.createdBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新人">{{ person.updatedBy || '-' }}</el-descriptions-item>
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
  person: {
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