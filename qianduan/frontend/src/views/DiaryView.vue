<script setup lang="ts">
/**
 * 情绪日记页面。
 *
 * 当前页面承载用户端最核心的记录流程：填写日记、选择心情标签、
 * 做一次自我强度标记，保存后展示后端情绪分析结果和 AI 建议。
 */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

import { generateAiSuggestion } from '@/api/ai'
import { addDiary } from '@/api/diary'
import {
  cleanupExpiredDiaryDrafts,
  readTodayDiaryDraft,
  removeTodayDiaryDraft,
  saveTodayDiaryDraft,
} from '@/utils/todayStorage'
import {
  formatText,
  getEmotionTypeLabel,
  getPolarityLabel,
  getPolarityTagType,
  normalizeEmotionScore,
  type DiaryRecord,
  type Result,
} from '@/utils/emotion'

/**
 * 今日未提交日记草稿结构。
 */
interface DiaryDraft {
  title?: string
  content?: string
  moodTag?: string
  selfScore?: number
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
  selfScore: 3,
})

// 提交按钮 loading 状态，避免用户连续重复提交。
const submitLoading = ref(false)

// 最近一次保存成功的日记，用于展示后端自动生成的情绪分析结果。
const savedDiary = ref<DiaryRecord | null>(null)

// 生成 AI 建议按钮 loading 状态。
const suggestionLoading = ref(false)

// 常用心情标签，帮助用户快速开始记录。
const moodOptions = ['开心', '平静', '焦虑', '疲惫', '低落', '期待', '烦躁', '放松']

// 日记正文字数，用于给用户提供轻量反馈。
const contentLength = computed(() => diaryForm.content.trim().length)

// 当前表单是否有本地草稿内容。
const hasDraftContent = computed(() => hasDiaryDraftContent())

/**
 * 判断本地读取的对象是否是合法日记草稿。
 *
 * @param draft 待判断草稿对象
 * @returns 草稿字段结构合法时返回 true
 */
function isDiaryDraft(draft: unknown): draft is DiaryDraft {
  return Boolean(draft && typeof draft === 'object')
}

/**
 * 判断当前日记表单是否包含有效草稿内容。
 *
 * @returns 标题、内容、心情标签任意一项非空时返回 true
 */
function hasDiaryDraftContent() {
  return Boolean(diaryForm.title.trim() || diaryForm.content.trim() || diaryForm.moodTag.trim())
}

/**
 * 选择一个快捷心情标签。
 *
 * @param moodTag 用户选择的心情标签
 */
function selectMoodTag(moodTag: string) {
  diaryForm.moodTag = moodTag
}

/**
 * 恢复当前用户今日未提交日记草稿。
 *
 * 页面进入时调用；如果本地没有今日草稿，则保持表单为空。
 */
function restoreTodayDiaryDraft() {
  const draft = readTodayDiaryDraft()
  if (!isDiaryDraft(draft)) {
    return
  }

  diaryForm.title = draft.title || ''
  diaryForm.content = draft.content || ''
  diaryForm.moodTag = draft.moodTag || ''
  diaryForm.selfScore = draft.selfScore || 3
}

/**
 * 同步当前日记表单到今日草稿。
 *
 * 表单有内容时写入 localStorage；三项都为空时删除今日草稿。
 */
function syncTodayDiaryDraft() {
  if (!hasDiaryDraftContent()) {
    removeTodayDiaryDraft()
    return
  }

  saveTodayDiaryDraft({
    title: diaryForm.title,
    content: diaryForm.content,
    moodTag: diaryForm.moodTag,
    selfScore: diaryForm.selfScore,
  })
}

/**
 * 清空日记表单。
 *
 * 同时删除当前用户今日未提交草稿，避免刷新或切换页面后恢复旧内容。
 */
function resetDiaryForm() {
  diaryForm.title = ''
  diaryForm.content = ''
  diaryForm.moodTag = ''
  diaryForm.selfScore = 3
  removeTodayDiaryDraft()
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
      ElMessage.success(result.data?.saved ? 'AI 建议已生成并保存' : 'AI 建议已生成')
    } else {
      ElMessage.error(result.message || '生成 AI 建议失败')
    }
  } catch (error) {
    ElMessage.error('生成 AI 建议失败')
  } finally {
    suggestionLoading.value = false
  }
}

/**
 * 初始化今日草稿本地存储。
 *
 * 进入页面时先清理超过 7 天的旧草稿，再恢复当前用户当天草稿。
 */
onMounted(() => {
  cleanupExpiredDiaryDrafts()
  restoreTodayDiaryDraft()
})

/**
 * 监听日记表单变化并自动保存今日草稿。
 *
 * 用户填写标题、内容、心情标签或自评分后，切换页面或刷新仍可恢复当天内容。
 */
watch(diaryForm, () => {
  syncTodayDiaryDraft()
})
</script>

<template>
  <!-- 情绪日记页：围绕“记录 -> 保存 -> 分析 -> 建议”构建输入闭环。 -->
  <section class="diary-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">写一篇情绪日记</h1>
        <p class="page-description">把当下写清楚一点，之后回看时会更容易理解自己的变化。</p>
      </div>
      <el-tag :type="hasDraftContent ? 'warning' : 'info'" effect="plain">
        {{ hasDraftContent ? '今日草稿已保存' : '今日草稿为空' }}
      </el-tag>
    </div>

    <div class="diary-grid">
      <section class="soft-panel diary-form-panel">
        <el-form class="diary-form" label-position="top">
          <el-form-item label="标题">
            <el-input v-model="diaryForm.title" size="large" placeholder="给今天的感受起一个名字" />
          </el-form-item>

          <el-form-item label="心情标签">
            <div class="mood-picker">
              <button
                v-for="mood in moodOptions"
                :key="mood"
                class="mood-chip"
                :class="{ active: diaryForm.moodTag === mood }"
                type="button"
                @click="selectMoodTag(mood)"
              >
                {{ mood }}
              </button>
            </div>
            <el-input v-model="diaryForm.moodTag" placeholder="也可以自己输入一个标签" />
          </el-form-item>

          <el-form-item label="自我感受强度">
            <div class="score-picker">
              <el-rate v-model="diaryForm.selfScore" />
              <span>{{ diaryForm.selfScore }}/5</span>
            </div>
          </el-form-item>

          <el-form-item label="内容">
            <el-input
              v-model="diaryForm.content"
              class="diary-textarea"
              placeholder="发生了什么？你当时最明显的感受是什么？"
              type="textarea"
              :rows="10"
            />
            <div class="form-helper">
              <span>{{ contentLength }} 字</span>
              <span>系统会在保存后整理情绪类型、强度和关键词</span>
            </div>
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" size="large" :loading="submitLoading" @click="submitDiary">保存并分析</el-button>
            <el-button size="large" @click="resetDiaryForm">清空</el-button>
          </div>
        </el-form>
      </section>

      <aside class="soft-panel writing-side">
        <h2>今日小结</h2>
        <dl>
          <div>
            <dt>当前标签</dt>
            <dd>{{ formatText(diaryForm.moodTag, '未选择') }}</dd>
          </div>
          <div>
            <dt>自评强度</dt>
            <dd>{{ diaryForm.selfScore }}/5</dd>
          </div>
          <div>
            <dt>正文长度</dt>
            <dd>{{ contentLength }} 字</dd>
          </div>
        </dl>
        <p>不用一次写完整，先把最明确的一句话写下来也可以。</p>
      </aside>
    </div>

    <section v-if="savedDiary" class="soft-panel analysis-panel">
      <div class="analysis-header">
        <div>
          <h2>本次情绪分析</h2>
          <p>{{ formatText(savedDiary.aiSummary, '分析结果已生成。') }}</p>
        </div>
        <div class="analysis-actions">
          <el-tag :type="getPolarityTagType(savedDiary.emotionPolarity)" effect="plain">
            {{ getPolarityLabel(savedDiary.emotionPolarity) }}
          </el-tag>
          <el-button size="small" type="primary" :loading="suggestionLoading" @click="generateSuggestionForSavedDiary">
            生成 AI 建议
          </el-button>
        </div>
      </div>

      <div class="analysis-grid">
        <div class="analysis-item">
          <span>情绪类型</span>
          <strong>{{ getEmotionTypeLabel(savedDiary.emotionType) }}</strong>
        </div>
        <div class="analysis-item">
          <span>系统强度</span>
          <strong>{{ normalizeEmotionScore(savedDiary.emotionScore) }}/5</strong>
        </div>
        <div class="analysis-item wide">
          <span>关键词</span>
          <p>{{ formatText(savedDiary.keywords) }}</p>
        </div>
        <div class="analysis-item wide calm">
          <span>AI 建议</span>
          <p>{{ formatText(savedDiary.aiSuggestion, '可以点击右上角按钮生成建议。') }}</p>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
/* 日记页面整体：表单和分析区纵向排列。 */
.diary-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 日记主体：大表单和右侧小结并排。 */
.diary-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 18px;
}

.diary-form-panel {
  padding: 24px;
}

.diary-form {
  max-width: 820px;
}

/* 心情快捷标签：按钮尺寸稳定，方便快速选择。 */
.mood-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.mood-chip {
  min-width: 64px;
  padding: 8px 12px;
  border: 1px solid #d7e5e2;
  border-radius: 8px;
  background: #ffffff;
  color: #415b56;
  cursor: pointer;
}

.mood-chip.active,
.mood-chip:hover {
  border-color: #1f9d8a;
  background: #e6f6f3;
  color: #126253;
  font-weight: 700;
}

/* 自评强度：星级和数值横向展示。 */
.score-picker {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 32px;
}

.score-picker span {
  color: #5d706c;
  font-weight: 700;
}

.diary-textarea {
  width: 100%;
}

.form-helper {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  margin-top: 8px;
  color: #768b86;
  font-size: 12px;
}

.form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

/* 右侧小结：实时展示用户输入状态。 */
.writing-side {
  align-self: start;
  padding: 22px;
}

.writing-side h2 {
  margin: 0 0 16px;
  color: #173934;
  font-size: 18px;
  font-weight: 800;
}

.writing-side dl {
  display: grid;
  gap: 12px;
  margin: 0;
}

.writing-side dl > div {
  padding-bottom: 12px;
  border-bottom: 1px solid #e8efed;
}

.writing-side dt {
  color: #738681;
  font-size: 13px;
}

.writing-side dd {
  margin: 5px 0 0;
  color: #173934;
  font-size: 20px;
  font-weight: 800;
}

.writing-side p {
  margin: 18px 0 0;
  color: #607671;
  line-height: 1.8;
}

/* 分析结果面板：保存成功后显示系统分析和 AI 建议。 */
.analysis-panel {
  padding: 24px;
}

.analysis-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.analysis-header h2 {
  margin: 0;
  color: #173934;
  font-size: 20px;
  font-weight: 800;
}

.analysis-header p {
  margin: 6px 0 0;
  color: #667a75;
  line-height: 1.7;
}

.analysis-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.analysis-item {
  min-height: 108px;
  padding: 16px;
  border: 1px solid #e5efec;
  border-radius: 8px;
  background: #ffffff;
}

.analysis-item.wide {
  grid-column: 1 / -1;
}

.analysis-item.calm {
  background: #f2fbf8;
}

.analysis-item span {
  display: block;
  color: #71837f;
  font-size: 13px;
}

.analysis-item strong {
  display: block;
  margin-top: 8px;
  color: #173934;
  font-size: 24px;
  font-weight: 800;
}

.analysis-item p {
  margin: 8px 0 0;
  color: #4e625d;
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  /* 中等屏下表单和右侧小结改为单列。 */
  .diary-grid {
    grid-template-columns: 1fr;
  }

  .writing-side {
    align-self: stretch;
  }
}

@media (max-width: 640px) {
  /* 小屏下表单间距和分析网格收紧。 */
  .diary-form-panel,
  .analysis-panel,
  .writing-side {
    padding: 18px;
  }

  .analysis-header {
    flex-direction: column;
  }

  .analysis-actions {
    justify-content: flex-start;
  }

  .analysis-grid {
    grid-template-columns: 1fr;
  }
}
</style>
