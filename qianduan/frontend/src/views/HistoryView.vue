<script setup lang="ts">
/**
 * 历史记录页面。
 *
 * 当前页面进入时会调用 GET /api/diary/list 查询情绪日记列表，
 * 并用 Element Plus 表格展示标题、心情标签、情绪类型和日期。
 */
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { generateAiSuggestion } from '@/api/ai'
import { getDiaryList } from '@/api/diary'

/**
 * 情绪日记表格行数据。
 */
interface DiaryRecord {
  id?: number
  title?: string
  content?: string
  moodTag?: string
  emotionPolarity?: string
  emotionType?: string
  emotionScore?: number
  keywords?: string
  aiSummary?: string
  aiSuggestion?: string
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

/**
 * AI 建议接口返回数据。
 */
interface AiSuggestionData {
  diaryId?: number
  aiSuggestion?: string
  saved?: boolean
}

// 表格数据源，保存后端返回的日记列表。
const diaryList = ref<DiaryRecord[]>([])

// 表格加载状态，用于展示加载反馈。
const loading = ref(false)

// 正在生成 AI 建议的日记 ID，用于控制按钮 loading 状态。
const suggestionLoadingId = ref<number | null>(null)

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

/**
 * 获取情绪极性的中文展示文本。
 *
 * @param polarity 后端返回的情绪极性
 * @returns 中文极性名称
 */
function getPolarityLabel(polarity?: string) {
  const polarityMap: Record<string, string> = {
    positive: '积极',
    negative: '消极',
    neutral: '平静',
  }
  return polarity ? polarityMap[polarity] || polarity : '暂无'
}

/**
 * 获取情绪极性的标签类型。
 *
 * @param polarity 后端返回的情绪极性
 * @returns Element Plus 标签类型
 */
function getPolarityTagType(polarity?: string) {
  if (polarity === 'positive') {
    return 'success'
  }
  if (polarity === 'negative') {
    return 'danger'
  }
  return 'info'
}

/**
 * 格式化空文本。
 *
 * @param text 待展示文本
 * @returns 非空文本或占位符
 */
function formatText(text?: string) {
  return text || '暂无'
}

/**
 * 为指定日记生成 AI 情绪建议。
 *
 * @param row 当前表格行数据
 */
async function generateSuggestionForDiary(row: DiaryRecord) {
  if (!row.id) {
    ElMessage.warning('日记 ID 不存在')
    return
  }

  suggestionLoadingId.value = row.id

  try {
    const result = (await generateAiSuggestion({
      diaryId: row.id,
    })) as Result<AiSuggestionData>

    if (result.code === 200) {
      row.aiSuggestion = result.data?.aiSuggestion || ''
      ElMessage.success(result.data?.saved ? 'AI建议已生成并保存' : 'AI建议已生成')
    } else {
      ElMessage.error(result.message || '生成AI建议失败')
    }
  } catch (error) {
    ElMessage.error('生成AI建议失败')
  } finally {
    suggestionLoadingId.value = null
  }
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
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="analysis-detail">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="日记内容" :span="2">
                {{ formatText(row.content) }}
              </el-descriptions-item>
              <el-descriptions-item label="情绪极性">
                <el-tag :type="getPolarityTagType(row.emotionPolarity)" effect="plain">
                  {{ getPolarityLabel(row.emotionPolarity) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="情绪强度">
                <div class="score-cell">
                  <el-rate :model-value="row.emotionScore || 0" disabled />
                  <span>{{ row.emotionScore || 0 }}/5</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="关键词" :span="2">
                {{ formatText(row.keywords) }}
              </el-descriptions-item>
              <el-descriptions-item label="分析说明" :span="2">
                {{ formatText(row.aiSummary) }}
              </el-descriptions-item>
              <el-descriptions-item label="AI建议" :span="2">
                <div class="suggestion-detail">
                  <span>{{ formatText(row.aiSuggestion) }}</span>
                  <el-button
                    size="small"
                    type="primary"
                    :loading="suggestionLoadingId === row.id"
                    @click="generateSuggestionForDiary(row)"
                  >
                    生成AI建议
                  </el-button>
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="170" />
      <el-table-column prop="moodTag" label="心情标签" width="140" />
      <el-table-column label="情绪极性" width="120">
        <template #default="{ row }">
          <el-tag :type="getPolarityTagType(row.emotionPolarity)" effect="plain">
            {{ getPolarityLabel(row.emotionPolarity) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="情绪类型" width="120">
        <template #default="{ row }">
          <el-tag type="warning" effect="plain">{{ formatText(row.emotionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="强度" width="160">
        <template #default="{ row }">
          <div class="score-cell">
            <el-rate :model-value="row.emotionScore || 0" disabled />
            <span>{{ row.emotionScore || 0 }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="keywords" label="关键词" min-width="180" show-overflow-tooltip />
      <el-table-column label="日期" width="180" :formatter="formatDiaryDate" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="primary"
            :loading="suggestionLoadingId === row.id"
            @click="generateSuggestionForDiary(row)"
          >
            AI建议
          </el-button>
        </template>
      </el-table-column>
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

/* 展开行详情：展示日记内容、情绪分析说明和 AI 建议。 */
.analysis-detail {
  padding: 12px 24px;
  background: #fafafa;
}

/* 情绪强度展示：星级和数字并排显示。 */
.score-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* AI 建议详情：文本和按钮分列展示，方便用户补生成建议。 */
.suggestion-detail {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.suggestion-detail span {
  flex: 1;
  min-width: 0;
  line-height: 1.7;
}
</style>
