<script setup lang="ts">
/**
 * 历史记录页面。
 *
 * 页面以时间线方式展示用户自己的情绪日记，并提供关键词、日期和情绪筛选，
 * 让用户更自然地回看过去的记录和 AI 建议。
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { generateAiSuggestion } from '@/api/ai'
import { getDiaryList } from '@/api/diary'
import {
  formatDiaryDate,
  formatText,
  getEmotionTypeLabel,
  getPolarityLabel,
  getPolarityTagType,
  normalizeDiaryList,
  normalizeEmotionScore,
  splitKeywords,
  type DiaryRecord,
  type Result,
} from '@/utils/emotion'

/**
 * AI 建议接口返回数据。
 */
interface AiSuggestionData {
  diaryId?: number
  aiSuggestion?: string
  saved?: boolean
}

// 时间线数据源，保存后端返回的日记列表。
const diaryList = ref<DiaryRecord[]>([])

// 时间线加载状态，用于展示加载反馈。
const loading = ref(false)

// 正在生成 AI 建议的日记 ID，用于控制按钮 loading 状态。
const suggestionLoadingId = ref<number | null>(null)

// 当前展开详情的日记 ID。
const expandedDiaryId = ref<number | null>(null)

// 关键词搜索条件。
const keywordFilter = ref('')

// 情绪极性筛选条件。
const polarityFilter = ref('')

// 情绪类型筛选条件。
const emotionTypeFilter = ref('')

// 日期范围筛选条件，格式为 yyyy-MM-dd。
const dateRange = ref<string[]>([])

// 可筛选的情绪类型选项。
const emotionTypeOptions = computed(() => {
  const typeSet = new Set<string>()
  diaryList.value.forEach((diary) => {
    if (diary.emotionType) {
      typeSet.add(diary.emotionType)
    }
  })
  return Array.from(typeSet)
})

// 根据筛选条件得到的时间线日记列表。
const filteredDiaries = computed(() => {
  return diaryList.value.filter((diary) => {
    const dateText = (diary.diaryDate || '').slice(0, 10)
    const keyword = keywordFilter.value.trim().toLowerCase()
    const searchText = [diary.title, diary.content, diary.moodTag, diary.keywords, diary.aiSummary, diary.aiSuggestion]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    if (keyword && !searchText.includes(keyword)) {
      return false
    }

    if (polarityFilter.value && diary.emotionPolarity !== polarityFilter.value) {
      return false
    }

    if (emotionTypeFilter.value && diary.emotionType !== emotionTypeFilter.value) {
      return false
    }

    const [startDate, endDate] = dateRange.value
    if (startDate && endDate && (dateText < startDate || dateText > endDate)) {
      return false
    }

    return true
  })
})

/**
 * 查询日记列表。
 *
 * 页面挂载时自动调用；刷新按钮也复用该方法。
 */
async function loadDiaryList() {
  loading.value = true

  try {
    const result = (await getDiaryList()) as Result<unknown>

    if (result.code === 200) {
      diaryList.value = normalizeDiaryList(result.data)
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
 * 重置全部筛选条件。
 */
function resetFilters() {
  keywordFilter.value = ''
  polarityFilter.value = ''
  emotionTypeFilter.value = ''
  dateRange.value = []
}

/**
 * 切换指定日记的详情展开状态。
 *
 * @param diaryId 日记 ID
 */
function toggleDiaryDetail(diaryId?: number) {
  if (!diaryId) {
    return
  }

  expandedDiaryId.value = expandedDiaryId.value === diaryId ? null : diaryId
}

/**
 * 判断指定日记是否处于展开状态。
 *
 * @param diary 日记记录
 * @returns 当前记录展开时返回 true
 */
function isExpanded(diary: DiaryRecord) {
  return Boolean(diary.id && expandedDiaryId.value === diary.id)
}

/**
 * 获取日记关键词标签数组。
 *
 * @param diary 当前日记记录
 * @returns 关键词数组
 */
function getKeywordTags(diary: DiaryRecord) {
  return splitKeywords(diary.keywords).slice(0, 5)
}

/**
 * 为指定日记生成 AI 情绪建议。
 *
 * @param diary 当前日记记录
 */
async function generateSuggestionForDiary(diary: DiaryRecord) {
  if (!diary.id) {
    ElMessage.warning('日记 ID 不存在')
    return
  }

  suggestionLoadingId.value = diary.id

  try {
    const result = (await generateAiSuggestion({
      diaryId: diary.id,
    })) as Result<AiSuggestionData>

    if (result.code === 200) {
      diary.aiSuggestion = result.data?.aiSuggestion || ''
      ElMessage.success(result.data?.saved ? 'AI 建议已生成并保存' : 'AI 建议已生成')
    } else {
      ElMessage.error(result.message || '生成 AI 建议失败')
    }
  } catch (error) {
    ElMessage.error('生成 AI 建议失败')
  } finally {
    suggestionLoadingId.value = null
  }
}

// 页面加载完成后立即查询历史记录。
onMounted(loadDiaryList)
</script>

<template>
  <!-- 历史记录页：以时间线方式展示当前用户的情绪日记。 -->
  <section class="history-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">历史记录</h1>
        <p class="page-description">按时间回看过去的情绪、关键词和建议。</p>
      </div>
      <el-button :loading="loading" @click="loadDiaryList">刷新</el-button>
    </div>

    <section class="soft-panel filter-panel">
      <el-input v-model="keywordFilter" clearable placeholder="搜索标题、正文、关键词" />
      <el-select v-model="polarityFilter" clearable placeholder="情绪倾向">
        <el-option label="积极" value="positive" />
        <el-option label="平静" value="neutral" />
        <el-option label="低落" value="negative" />
      </el-select>
      <el-select v-model="emotionTypeFilter" clearable placeholder="情绪类型">
        <el-option v-for="emotionType in emotionTypeOptions" :key="emotionType" :label="emotionType" :value="emotionType" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
      />
      <el-button @click="resetFilters">重置</el-button>
    </section>

    <section v-loading="loading" class="soft-panel timeline-panel">
      <div class="timeline-summary">
        <strong>{{ filteredDiaries.length }}</strong>
        <span>条记录</span>
      </div>

      <div v-if="filteredDiaries.length" class="timeline-list">
        <article v-for="diary in filteredDiaries" :key="diary.id || diary.title" class="timeline-item">
          <div class="timeline-dot" />

          <div class="timeline-content">
            <div class="timeline-top">
              <div>
                <time>{{ formatDiaryDate(diary.diaryDate) }}</time>
                <h2>{{ formatText(diary.title, '未命名记录') }}</h2>
              </div>
              <div class="timeline-tags">
                <el-tag :type="getPolarityTagType(diary.emotionPolarity)" effect="plain">
                  {{ getPolarityLabel(diary.emotionPolarity) }}
                </el-tag>
                <el-tag type="warning" effect="plain">{{ getEmotionTypeLabel(diary.emotionType) }}</el-tag>
              </div>
            </div>

            <p class="timeline-excerpt">{{ formatText(diary.aiSummary || diary.content) }}</p>

            <div class="timeline-meta">
              <span>强度 {{ normalizeEmotionScore(diary.emotionScore) }}/5</span>
              <span v-if="diary.moodTag">标签 {{ diary.moodTag }}</span>
              <span v-if="getKeywordTags(diary).length">关键词 {{ getKeywordTags(diary).join('、') }}</span>
            </div>

            <div class="timeline-actions">
              <el-button size="small" text type="primary" @click="toggleDiaryDetail(diary.id)">
                {{ isExpanded(diary) ? '收起详情' : '展开详情' }}
              </el-button>
              <el-button
                size="small"
                type="primary"
                plain
                :loading="suggestionLoadingId === diary.id"
                @click="generateSuggestionForDiary(diary)"
              >
                AI 建议
              </el-button>
            </div>

            <div v-if="isExpanded(diary)" class="timeline-detail">
              <section>
                <h3>日记内容</h3>
                <p>{{ formatText(diary.content) }}</p>
              </section>
              <section>
                <h3>关键词</h3>
                <div v-if="getKeywordTags(diary).length" class="tag-row">
                  <el-tag v-for="keyword in getKeywordTags(diary)" :key="keyword" effect="plain">{{ keyword }}</el-tag>
                </div>
                <p v-else>暂无</p>
              </section>
              <section class="suggestion-block">
                <h3>AI 建议</h3>
                <p>{{ formatText(diary.aiSuggestion, '还没有生成建议，可以点击上方 AI 建议按钮。') }}</p>
              </section>
            </div>
          </div>
        </article>
      </div>

      <el-empty v-else description="没有符合条件的记录" :image-size="110" />
    </section>
  </section>
</template>

<style scoped>
/* 历史页面整体：筛选和时间线纵向排列。 */
.history-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* 筛选面板：输入、选择器和日期范围自适应换行。 */
.filter-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 150px 150px minmax(260px, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 16px;
}

.timeline-panel {
  padding: 22px;
}

.timeline-summary {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 18px;
  color: #607671;
}

.timeline-summary strong {
  color: #12342f;
  font-size: 28px;
  font-weight: 800;
}

/* 时间线列表：左侧竖线，右侧为每条日记内容。 */
.timeline-list {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.timeline-list::before {
  position: absolute;
  top: 8px;
  bottom: 8px;
  left: 8px;
  width: 2px;
  background: #d8e8e5;
  content: '';
}

.timeline-item {
  position: relative;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr);
  gap: 16px;
}

.timeline-dot {
  z-index: 1;
  width: 18px;
  height: 18px;
  margin-top: 18px;
  border: 4px solid #e6f6f3;
  border-radius: 50%;
  background: #1f9d8a;
}

.timeline-content {
  padding: 18px;
  border: 1px solid #e1ece9;
  border-radius: 8px;
  background: #ffffff;
}

.timeline-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.timeline-top time {
  color: #71837f;
  font-size: 13px;
}

.timeline-top h2 {
  margin: 5px 0 0;
  color: #173934;
  font-size: 20px;
  font-weight: 800;
}

.timeline-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.timeline-excerpt {
  display: -webkit-box;
  margin: 12px 0 0;
  overflow: hidden;
  color: #536963;
  line-height: 1.8;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.timeline-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  color: #6b817b;
  font-size: 13px;
}

.timeline-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

/* 展开详情：以小块内容展示全文、关键词和建议。 */
.timeline-detail {
  display: grid;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e8efed;
}

.timeline-detail h3 {
  margin: 0 0 6px;
  color: #173934;
  font-size: 15px;
  font-weight: 800;
}

.timeline-detail p {
  margin: 0;
  color: #4f635f;
  line-height: 1.8;
  white-space: pre-wrap;
}

.suggestion-block {
  padding: 14px;
  border-radius: 8px;
  background: #f2fbf8;
}

@media (max-width: 980px) {
  /* 中等屏下筛选控件两列排列。 */
  .filter-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  /* 小屏下筛选和时间线内容单列展示。 */
  .filter-panel {
    grid-template-columns: 1fr;
  }

  .timeline-panel {
    padding: 16px;
  }

  .timeline-top {
    flex-direction: column;
  }

  .timeline-tags {
    justify-content: flex-start;
  }
}
</style>
