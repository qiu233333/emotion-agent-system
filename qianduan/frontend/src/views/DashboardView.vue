<script setup lang="ts">
/**
 * 用户工作台页面。
 *
 * 首页集中展示今日记录状态、最近一次情绪、近 7 天记录概览和核心快捷入口，
 * 帮助用户从“记录情绪”自然进入“分析和回看”的闭环。
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getDiaryList } from '@/api/diary'
import {
  buildKeywordFrequency,
  buildSevenDayTrend,
  countSevenDayDiaries,
  extractDateKey,
  formatDiaryDate,
  formatText,
  getAverageScoreText,
  getEmotionTypeLabel,
  getLatestDiary,
  getPolarityLabel,
  getPolarityTagType,
  isTodayDiary,
  normalizeDiaryList,
  type DiaryRecord,
  type Result,
} from '@/utils/emotion'

const router = useRouter()

// 首页日记列表数据，用于推导今日状态和近 7 天摘要。
const diaryList = ref<DiaryRecord[]>([])

// 首页加载状态，用于避免首屏数据请求时界面突兀。
const loading = ref(false)

// 最近一篇日记。
const latestDiary = computed(() => getLatestDiary(diaryList.value))

// 今日已记录的第一篇日记。
const todayDiary = computed(() => diaryList.value.find(isTodayDiary) || null)

// 近 7 天趋势数据。
const sevenDayTrend = computed(() => buildSevenDayTrend(diaryList.value))

// 近 7 天记录数量。
const sevenDayCount = computed(() => countSevenDayDiaries(diaryList.value))

// 最近记录的平均情绪强度。
const averageScoreText = computed(() => getAverageScoreText(diaryList.value))

// 近 7 天内出现次数最多的关键词。
const topKeywords = computed(() => buildKeywordFrequency(diaryList.value, 5))

// 今日记录状态文案。
const todayStatusText = computed(() => (todayDiary.value ? '今天已经留下记录' : '今天还没有记录'))

/**
 * 获取当前用户展示名。
 *
 * @returns 当前用户昵称、用户名或默认称呼
 */
function getCurrentUserName() {
  const userInfoText = localStorage.getItem('userInfo')
  if (!userInfoText) {
    return '同学'
  }

  try {
    const userInfo = JSON.parse(userInfoText)
    return userInfo.nickname || userInfo.username || '同学'
  } catch {
    return '同学'
  }
}

/**
 * 获取趋势柱高度。
 *
 * @param score 单日平均情绪强度
 * @returns CSS 百分比高度
 */
function getTrendBarHeight(score: number) {
  return `${Math.max(score / 5, 0.08) * 100}%`
}

/**
 * 跳转到指定用户端页面。
 *
 * @param path 目标路由路径
 */
function goTo(path: string) {
  router.push(path)
}

/**
 * 加载首页所需的日记列表。
 */
async function loadDashboardData() {
  loading.value = true

  try {
    const result = (await getDiaryList()) as Result<unknown>

    if (result.code === 200) {
      diaryList.value = normalizeDiaryList(result.data)
    } else {
      ElMessage.error(result.message || '工作台数据加载失败')
    }
  } catch (error) {
    ElMessage.error('工作台数据加载失败')
  } finally {
    loading.value = false
  }
}

// 页面进入后加载当前用户日记数据。
onMounted(loadDashboardData)
</script>

<template>
  <!-- 用户工作台：展示最重要的个人情绪记录入口和摘要。 -->
  <section v-loading="loading" class="dashboard-page">
    <div class="hero-panel soft-panel">
      <div class="hero-copy">
        <span class="hero-kicker">{{ todayStatusText }}</span>
        <h1>{{ getCurrentUserName() }}，今天想先记录哪一刻？</h1>
        <p>
          先写下一段真实感受，再让系统帮你整理情绪类型、强度、关键词和温和建议。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="goTo('/diary')">写一篇日记</el-button>
          <el-button size="large" @click="goTo('/chat')">找 AI 聊聊</el-button>
        </div>
      </div>

      <div class="today-card">
        <span>今日状态</span>
        <strong>{{ todayDiary ? getEmotionTypeLabel(todayDiary.emotionType) : '待记录' }}</strong>
        <p>{{ todayDiary ? formatText(todayDiary.aiSummary, '已经记录，稍后可以回看。') : '写几句话也可以，重要的是给当下留一个位置。' }}</p>
        <el-tag v-if="todayDiary" :type="getPolarityTagType(todayDiary.emotionPolarity)" effect="plain">
          {{ getPolarityLabel(todayDiary.emotionPolarity) }}
        </el-tag>
      </div>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <span>近 7 天记录</span>
        <strong>{{ sevenDayCount }}</strong>
        <small>条日记</small>
      </div>
      <div class="metric-card">
        <span>平均强度</span>
        <strong>{{ averageScoreText }}</strong>
        <small>满分 5 分</small>
      </div>
      <div class="metric-card">
        <span>最近情绪</span>
        <strong>{{ latestDiary ? getEmotionTypeLabel(latestDiary.emotionType) : '暂无' }}</strong>
        <small>{{ latestDiary ? formatDiaryDate(latestDiary.diaryDate) : '还没有历史记录' }}</small>
      </div>
    </div>

    <div class="dashboard-grid">
      <section class="soft-panel trend-panel">
        <div class="section-title-row">
          <div>
            <h2>近 7 天趋势</h2>
            <p>按每天日记的情绪强度汇总</p>
          </div>
          <el-button text type="primary" @click="goTo('/statistics')">查看统计</el-button>
        </div>

        <div class="mini-trend">
          <div v-for="point in sevenDayTrend" :key="point.date" class="trend-day">
            <div class="trend-track">
              <span class="trend-bar" :style="{ height: getTrendBarHeight(point.score) }" />
            </div>
            <small>{{ point.label }}</small>
            <em>{{ point.count }}</em>
          </div>
        </div>
      </section>

      <section class="soft-panel latest-panel">
        <div class="section-title-row">
          <div>
            <h2>最近一次记录</h2>
            <p>{{ latestDiary ? extractDateKey(latestDiary.diaryDate) : '暂无记录' }}</p>
          </div>
          <el-button text type="primary" @click="goTo('/history')">看历史</el-button>
        </div>

        <div v-if="latestDiary" class="latest-diary">
          <div class="tag-row">
            <el-tag :type="getPolarityTagType(latestDiary.emotionPolarity)" effect="plain">
              {{ getPolarityLabel(latestDiary.emotionPolarity) }}
            </el-tag>
            <el-tag type="warning" effect="plain">{{ getEmotionTypeLabel(latestDiary.emotionType) }}</el-tag>
            <el-tag v-if="latestDiary.moodTag" type="info" effect="plain">{{ latestDiary.moodTag }}</el-tag>
          </div>
          <h3>{{ formatText(latestDiary.title, '未命名记录') }}</h3>
          <p>{{ formatText(latestDiary.aiSuggestion || latestDiary.aiSummary || latestDiary.content) }}</p>
        </div>

        <el-empty v-else description="还没有日记记录" :image-size="96" />
      </section>
    </div>

    <section class="shortcut-row">
      <button class="shortcut-card" type="button" @click="goTo('/diary')">
        <span>写日记</span>
        <strong>记录当下</strong>
      </button>
      <button class="shortcut-card" type="button" @click="goTo('/history')">
        <span>看历史</span>
        <strong>回看变化</strong>
      </button>
      <button class="shortcut-card" type="button" @click="goTo('/chat')">
        <span>找 AI 聊聊</span>
        <strong>整理片刻</strong>
      </button>
      <button class="shortcut-card" type="button" @click="goTo('/statistics')">
        <span>看趋势</span>
        <strong>理解规律</strong>
      </button>
    </section>

    <section v-if="topKeywords.length" class="soft-panel keyword-panel">
      <div class="section-title-row">
        <div>
          <h2>最近常出现的关键词</h2>
          <p>来自历史日记的关键词整理</p>
        </div>
      </div>
      <div class="tag-row">
        <el-tag v-for="keyword in topKeywords" :key="keyword.word" effect="plain">
          {{ keyword.word }} × {{ keyword.count }}
        </el-tag>
      </div>
    </section>
  </section>
</template>

<style scoped>
/* 工作台整体：各区块纵向排列。 */
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 首屏欢迎区：左侧引导记录，右侧展示今日状态。 */
.hero-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  padding: 28px;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(31, 157, 138, 0.14), rgba(255, 255, 255, 0.92)),
    #ffffff;
}

.hero-copy {
  max-width: 700px;
}

.hero-kicker {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 5px 10px;
  border-radius: 8px;
  background: rgba(31, 157, 138, 0.12);
  color: #126253;
  font-size: 13px;
  font-weight: 700;
}

.hero-copy h1 {
  margin: 0;
  color: #12342f;
  font-size: 34px;
  font-weight: 800;
  line-height: 1.25;
}

.hero-copy p {
  max-width: 620px;
  margin: 12px 0 0;
  color: #607671;
  font-size: 16px;
  line-height: 1.8;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 22px;
}

/* 今日状态卡片：固定宽度展示今日是否已记录。 */
.today-card {
  min-height: 220px;
  padding: 22px;
  border: 1px solid rgba(31, 157, 138, 0.16);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
}

.today-card span {
  color: #66817a;
  font-size: 13px;
}

.today-card strong {
  display: block;
  margin-top: 12px;
  color: #12342f;
  font-size: 30px;
  font-weight: 800;
}

.today-card p {
  min-height: 76px;
  margin: 12px 0;
  color: #607671;
  line-height: 1.8;
}

/* 指标区：三个核心数字并排展示。 */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

/* 首页主体网格：趋势和最近记录并排。 */
.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(300px, 0.95fr);
  gap: 16px;
}

.trend-panel,
.latest-panel,
.keyword-panel {
  padding: 20px;
}

.section-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.section-title-row h2 {
  margin: 0;
  color: #173934;
  font-size: 18px;
  font-weight: 800;
}

.section-title-row p {
  margin: 4px 0 0;
  color: #71837f;
  font-size: 13px;
}

/* 迷你趋势图：纯 CSS 柱状图用于工作台概览。 */
.mini-trend {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
  min-height: 180px;
  margin-top: 20px;
}

.trend-day {
  display: grid;
  grid-template-rows: minmax(110px, 1fr) auto auto;
  gap: 6px;
  justify-items: center;
  min-width: 0;
}

.trend-track {
  position: relative;
  width: 100%;
  max-width: 38px;
  overflow: hidden;
  border-radius: 8px;
  background: #e6efed;
}

.trend-bar {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  border-radius: 8px;
  background: linear-gradient(180deg, #45bda8 0%, #1f9d8a 100%);
}

.trend-day small {
  color: #70827e;
  font-size: 12px;
}

.trend-day em {
  color: #1f9d8a;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
}

/* 最近记录摘要：限制文本高度，保持版面稳定。 */
.latest-diary h3 {
  margin: 14px 0 8px;
  color: #173934;
  font-size: 20px;
  font-weight: 800;
}

.latest-diary p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #566b66;
  line-height: 1.8;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 5;
}

/* 快捷入口：四个核心动作的轻量按钮卡片。 */
.shortcut-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.shortcut-card {
  min-height: 104px;
  padding: 18px;
  border: 1px solid rgba(112, 139, 132, 0.16);
  border-radius: 8px;
  background: #ffffff;
  color: #173934;
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.18s ease,
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.shortcut-card:hover {
  border-color: rgba(31, 157, 138, 0.36);
  box-shadow: 0 14px 32px rgba(63, 95, 88, 0.1);
  transform: translateY(-2px);
}

.shortcut-card span {
  display: block;
  color: #70827e;
  font-size: 13px;
}

.shortcut-card strong {
  display: block;
  margin-top: 8px;
  font-size: 20px;
  font-weight: 800;
}

@media (max-width: 900px) {
  /* 中等屏下首屏和主体网格改为单列。 */
  .hero-panel,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid,
  .shortcut-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  /* 小屏下所有指标和入口单列展示。 */
  .hero-panel {
    padding: 20px;
  }

  .hero-copy h1 {
    font-size: 28px;
  }

  .metric-grid,
  .shortcut-row {
    grid-template-columns: 1fr;
  }
}
</style>
