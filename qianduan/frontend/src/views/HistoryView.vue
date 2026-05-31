<script setup lang="ts">
/**
 * 历史记录页面。
 *
 * 当前页面进入时会调用 GET /api/diary/list 查询情绪日记列表，
 * 并用 Element Plus 表格展示标题、心情标签、情绪类型和日期。
 */
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getDiaryList } from '@/api/diary'

/**
 * 情绪日记表格行数据。
 */
interface DiaryRecord {
  id?: number
  title?: string
  moodTag?: string
  emotionType?: string
  diaryDate?: string
}

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

// 表格数据源，保存后端返回的日记列表。
const diaryList = ref<DiaryRecord[]>([])

// 表格加载状态，用于展示加载反馈。
const loading = ref(false)

/**
 * 查询日记列表。
 *
 * 页面挂载时自动调用；后续也可以复用到刷新按钮或筛选条件中。
 */
async function loadDiaryList() {
  loading.value = true

  try {
    const result = (await getDiaryList()) as Result<DiaryRecord[]>

    if (result.code === 200) {
      diaryList.value = Array.isArray(result.data) ? result.data : []
    } else {
      ElMessage.error(result.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

/**
 * 格式化日记日期。
 *
 * 后端当前返回 yyyy-MM-dd HH:mm:ss；这里做一次兜底处理，避免空值显示 undefined。
 *
 * @param {Object} row 当前行数据
 * @returns {string} 格式化后的日期文本
 */
function formatDiaryDate(row: DiaryRecord) {
  return row.diaryDate || ''
}

// 页面加载完成后立即查询历史记录。
onMounted(loadDiaryList)
</script>

<template>
  <!-- 历史记录页：展示当前用户的情绪日记列表。 -->
  <el-card class="page-card">
    <div class="page-header">
      <div>
        <h2 class="page-title">历史记录</h2>
        <p class="page-description">查看已经保存的情绪日记记录。</p>
      </div>

      <el-button @click="loadDiaryList">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="diaryList" border class="diary-table">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="moodTag" label="心情标签" width="140" />
      <el-table-column prop="emotionType" label="情绪类型" width="140" />
      <el-table-column label="日期" width="180" :formatter="formatDiaryDate" />
    </el-table>
  </el-card>
</template>

<style scoped>
/* 标题区域：左侧说明页面用途，右侧放刷新按钮。 */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

/* 日记表格和页面说明之间留出基础间距。 */
.diary-table {
  margin-top: 24px;
}
</style>
