<script setup lang="ts">
/**
 * 情绪日记页面。
 *
 * 当前页面提供新增日记表单，用户填写标题、内容和心情标签后，
 * 点击提交会调用后端 POST /api/diary 接口保存数据。
 */
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { generateAiSuggestion } from '@/api/ai'
import { addDiary } from '@/api/diary'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * 情绪日记保存后返回的数据结构。
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
 * AI 建议接口返回数据。
 */
interface AiSuggestionData {
  diaryId?: number
  aiSuggestion?: string
  saved?: boolean
}

// 日记表单数据，字段名和后端 EmotionDiary 实体保持一致。
const diaryForm = reactive({
  title: '',
  content: '',
  moodTag: '',
})

// 提交按钮 loading 状态，避免用户连续重复提交。
const submitLoading = ref(false)

// 最近一次保存成功的日记，用于展示后端自动生成的情绪分析结果。
const savedDiary = ref<DiaryRecord | null>(null)

// 生成 AI 建议按钮 loading 状态。
const suggestionLoading = ref(false)

/**
 * 清空日记表单。
 *
 * 保存成功后调用，方便用户继续新增下一篇日记。
 */
function resetDiaryForm() {
  diaryForm.title = ''
  diaryForm.content = ''
  diaryForm.moodTag = ''
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
 * 提交新增日记请求。
 *
 * 这里只做最基础的非空判断，不加入复杂业务规则。
 */
async function submitDiary() {
  if (!diaryForm.title.trim() || !diaryForm.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  submitLoading.value = true

  try {
    const result = (await addDiary({
      title: diaryForm.title,
      content: diaryForm.content,
      moodTag: diaryForm.moodTag,
    })) as Result<DiaryRecord>

    if (result.code === 200) {
      savedDiary.value = result.data || null
      ElMessage.success('保存成功')
      resetDiaryForm()
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 为最近保存的日记生成 AI 情绪建议。
 *
 * 后端会根据 diaryId 查询当前用户日记，调用大语言模型生成建议，
 * 并把建议保存到 emotion_diary.ai_suggestion 字段。
 */
async function generateSuggestionForSavedDiary() {
  if (!savedDiary.value?.id) {
    ElMessage.warning('请先保存一篇日记')
    return
  }

  suggestionLoading.value = true

  try {
    const result = (await generateAiSuggestion({
      diaryId: savedDiary.value.id,
    })) as Result<AiSuggestionData>

    if (result.code === 200) {
      savedDiary.value.aiSuggestion = result.data?.aiSuggestion || ''
      ElMessage.success(result.data?.saved ? 'AI建议已生成并保存' : 'AI建议已生成')
    } else {
      ElMessage.error(result.message || '生成AI建议失败')
    }
  } catch (error) {
    ElMessage.error('生成AI建议失败')
  } finally {
    suggestionLoading.value = false
  }
}
</script>

<template>
  <!-- 情绪日记页：提供新增日记的基础表单。 -->
  <el-card class="page-card">
    <h2 class="page-title">情绪日记</h2>
    <p class="page-description">记录今天发生的事情、心情标签和情绪感受，提交后会保存到后端数据库。</p>

    <el-form class="diary-form" label-width="90px">
      <el-form-item label="标题">
        <el-input v-model="diaryForm.title" placeholder="请输入日记标题" />
      </el-form-item>

      <el-form-item label="内容">
        <el-input
          v-model="diaryForm.content"
          placeholder="请输入今天的情绪日记内容"
          type="textarea"
          :rows="6"
        />
      </el-form-item>

      <el-form-item label="心情标签">
        <el-input v-model="diaryForm.moodTag" placeholder="例如：开心、焦虑、平静" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitLoading" @click="submitDiary">提交</el-button>
        <el-button @click="resetDiaryForm">清空</el-button>
      </el-form-item>
    </el-form>

    <div v-if="savedDiary" class="analysis-panel">
      <div class="analysis-header">
        <h3>本次情绪分析</h3>
        <div class="analysis-actions">
          <el-tag :type="getPolarityTagType(savedDiary.emotionPolarity)" effect="plain">
            {{ getPolarityLabel(savedDiary.emotionPolarity) }}
          </el-tag>
          <el-button size="small" type="primary" :loading="suggestionLoading" @click="generateSuggestionForSavedDiary">
            生成AI建议
          </el-button>
        </div>
      </div>

      <el-descriptions :column="2" border class="analysis-descriptions">
        <el-descriptions-item label="情绪类型">
          <el-tag type="warning" effect="plain">{{ formatText(savedDiary.emotionType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="情绪强度">
          <div class="score-cell">
            <el-rate :model-value="savedDiary.emotionScore || 0" disabled />
            <span>{{ savedDiary.emotionScore || 0 }}/5</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="关键词" :span="2">
          {{ formatText(savedDiary.keywords) }}
        </el-descriptions-item>
        <el-descriptions-item label="分析说明" :span="2">
          {{ formatText(savedDiary.aiSummary) }}
        </el-descriptions-item>
        <el-descriptions-item label="AI建议" :span="2">
          {{ formatText(savedDiary.aiSuggestion) }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </el-card>
</template>

<style scoped>
/* 日记表单保持适中的宽度，方便阅读和填写。 */
.diary-form {
  max-width: 720px;
  margin-top: 24px;
}

/* 分析结果区域：展示后端保存日记时自动生成的 NLP 情绪分析结果。 */
.analysis-panel {
  margin-top: 28px;
  max-width: 880px;
}

/* 分析标题行：左侧标题，右侧展示情绪极性标签。 */
.analysis-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.analysis-header h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 600;
}

/* 分析操作区：放置情绪极性标签和生成 AI 建议按钮。 */
.analysis-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 情绪强度展示：星级和数字并排显示。 */
.score-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.analysis-descriptions {
  max-width: 880px;
}
</style>
