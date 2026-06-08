<script setup lang="ts">
/**
 * 情绪统计页面。
 *
 * 当前页面使用 ECharts 展示近 7 天情绪强度趋势、情绪类型分布和强度分布，
 * 并基于日记关键词生成高频关键词区域。
 */
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getDiaryList } from '@/api/diary'
import {
  buildEmotionDistribution,
  buildKeywordFrequency,
  buildScoreDistribution,
  buildSevenDayTrend,
  countSevenDayDiaries,
  getAverageScoreText,
  normalizeDiaryList,
  type DiaryRecord,
  type Result,
} from '@/utils/emotion'

// 日记列表数据，用于前端汇总统计图表。
const diaryList = ref<DiaryRecord[]>([])

// 统计页加载状态。
const loading = ref(false)

// 趋势图 DOM 容器。
const trendChartRef = ref<HTMLDivElement | null>(null)

// 情绪类型分布图 DOM 容器。
const distributionChartRef = ref<HTMLDivElement | null>(null)

// 情绪强度分布图 DOM 容器。
const scoreChartRef = ref<HTMLDivElement | null>(null)

// ECharts 趋势图实例。
let trendChart: ReturnType<typeof echarts.init> | null = null

// ECharts 情绪类型分布图实例。
let distributionChart: ReturnType<typeof echarts.init> | null = null

// ECharts 情绪强度分布图实例。
let scoreChart: ReturnType<typeof echarts.init> | null = null

// 近 7 天趋势数据。
const trendData = computed(() => buildSevenDayTrend(diaryList.value))

// 情绪类型分布数据。
const distributionData = computed(() => buildEmotionDistribution(diaryList.value))

// 情绪强度分布数据。
const scoreData = computed(() => buildScoreDistribution(diaryList.value))

// 高频关键词数据。
const keywordData = computed(() => buildKeywordFrequency(diaryList.value, 10))

// 近 7 天记录数量。
const sevenDayCount = computed(() => countSevenDayDiaries(diaryList.value))

// 平均情绪强度。
const averageScoreText = computed(() => getAverageScoreText(diaryList.value))

// 是否有任何日记数据。
const hasDiaryData = computed(() => diaryList.value.length > 0)

// 关键词最高频次，用于计算条形宽度。
const keywordMaxCount = computed(() => Math.max(...keywordData.value.map((keyword) => keyword.count), 1))

/**
 * 加载统计页所需日记列表。
 */
async function loadStatisticsData() {
  loading.value = true

  try {
    const result = (await getDiaryList()) as Result<unknown>

    if (result.code === 200) {
      diaryList.value = normalizeDiaryList(result.data)
      renderCharts()
    } else {
      ElMessage.error(result.message || '统计数据加载失败')
    }
  } catch (error) {
    ElMessage.error('统计数据加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 渲染全部图表。
 *
 * 数据更新后调用；如果 DOM 还未挂载，会等待下一次渲染。
 */
async function renderCharts() {
  await nextTick()
  renderTrendChart()
  renderDistributionChart()
  renderScoreChart()
}

/**
 * 渲染近 7 天情绪强度趋势图。
 */
function renderTrendChart() {
  if (!trendChartRef.value) {
    return
  }

  trendChart = trendChart || echarts.init(trendChartRef.value)
  trendChart.setOption({
    color: ['#1f9d8a'],
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>平均强度：{c}',
    },
    grid: {
      top: 28,
      right: 18,
      bottom: 30,
      left: 36,
    },
    xAxis: {
      type: 'category',
      data: trendData.value.map((point) => point.label),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d7e5e2' } },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 5,
      splitNumber: 5,
      axisLabel: { color: '#6d807c' },
      splitLine: { lineStyle: { color: '#edf3f1' } },
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbolSize: 8,
        data: trendData.value.map((point) => point.score),
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(31, 157, 138, 0.28)' },
              { offset: 1, color: 'rgba(31, 157, 138, 0.04)' },
            ],
          },
        },
      },
    ],
  })
}

/**
 * 渲染情绪类型分布图。
 */
function renderDistributionChart() {
  if (!distributionChartRef.value) {
    return
  }

  const data = distributionData.value.length ? distributionData.value : [{ name: '暂无数据', value: 1 }]
  distributionChart = distributionChart || echarts.init(distributionChartRef.value)
  distributionChart.setOption({
    color: ['#1f9d8a', '#f2a65a', '#8f8bd8', '#77c67b', '#e5797b', '#74a7d7'],
    tooltip: {
      trigger: 'item',
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      textStyle: { color: '#607671' },
    },
    series: [
      {
        type: 'pie',
        radius: ['44%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        label: {
          color: '#415b56',
          formatter: '{b}',
        },
        data,
      },
    ],
  })
}

/**
 * 渲染情绪强度分布柱状图。
 */
function renderScoreChart() {
  if (!scoreChartRef.value) {
    return
  }

  scoreChart = scoreChart || echarts.init(scoreChartRef.value)
  scoreChart.setOption({
    color: ['#f2a65a'],
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>记录数：{c}',
    },
    grid: {
      top: 26,
      right: 14,
      bottom: 30,
      left: 32,
    },
    xAxis: {
      type: 'category',
      data: scoreData.value.map((item) => item.name),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#d7e5e2' } },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#edf3f1' } },
    },
    series: [
      {
        type: 'bar',
        barWidth: 26,
        data: scoreData.value.map((item) => item.value),
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
        },
      },
    ],
  })
}

/**
 * 获取关键词条形宽度。
 *
 * @param count 关键词出现次数
 * @returns CSS 宽度百分比
 */
function getKeywordBarWidth(count: number) {
  return `${Math.max((count / keywordMaxCount.value) * 100, 8)}%`
}

/**
 * 统一调整所有图表尺寸。
 */
function resizeCharts() {
  trendChart?.resize()
  distributionChart?.resize()
  scoreChart?.resize()
}

/**
 * 释放 ECharts 实例。
 */
function disposeCharts() {
  trendChart?.dispose()
  distributionChart?.dispose()
  scoreChart?.dispose()
  trendChart = null
  distributionChart = null
  scoreChart = null
}

/**
 * 初始化统计页面。
 *
 * 页面挂载后加载数据，并监听窗口尺寸变化以重排图表。
 */
onMounted(() => {
  loadStatisticsData()
  window.addEventListener('resize', resizeCharts)
})

/**
 * 清理统计页面图表资源。
 *
 * 页面离开时移除监听并销毁图表实例，避免内存占用。
 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  disposeCharts()
})
</script>

<template>
  <!-- 情绪统计页：图表展示趋势、分布和关键词。 -->
  <section v-loading="loading" class="statistics-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">情绪统计</h1>
        <p class="page-description">从记录里看见最近的变化和反复出现的关键词。</p>
      </div>
      <el-button :loading="loading" @click="loadStatisticsData">刷新</el-button>
    </div>

    <div class="metric-grid">
      <div class="metric-card">
        <span>全部记录</span>
        <strong>{{ diaryList.length }}</strong>
        <small>条日记</small>
      </div>
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
    </div>

    <div class="chart-grid">
      <section class="soft-panel chart-panel chart-panel-wide">
        <div class="chart-title">
          <h2>近 7 天情绪强度趋势</h2>
          <span>按日记平均强度汇总</span>
        </div>
        <div ref="trendChartRef" class="chart-box" />
      </section>

      <section class="soft-panel chart-panel">
        <div class="chart-title">
          <h2>情绪类型分布</h2>
          <span>按系统识别类型统计</span>
        </div>
        <div ref="distributionChartRef" class="chart-box" />
      </section>

      <section class="soft-panel chart-panel">
        <div class="chart-title">
          <h2>情绪强度分布</h2>
          <span>1 到 5 分记录数量</span>
        </div>
        <div ref="scoreChartRef" class="chart-box" />
      </section>
    </div>

    <section class="soft-panel keyword-panel">
      <div class="chart-title">
        <h2>高频关键词</h2>
        <span>来自日记分析关键词</span>
      </div>

      <div v-if="keywordData.length" class="keyword-list">
        <div v-for="keyword in keywordData" :key="keyword.word" class="keyword-item">
          <span>{{ keyword.word }}</span>
          <div class="keyword-track">
            <div class="keyword-bar" :style="{ width: getKeywordBarWidth(keyword.count) }" />
          </div>
          <strong>{{ keyword.count }}</strong>
        </div>
      </div>

      <el-empty v-else :description="hasDiaryData ? '暂无关键词' : '还没有日记数据'" :image-size="100" />
    </section>
  </section>
</template>

<style scoped>
/* 统计页整体：指标、图表和关键词纵向排列。 */
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* 指标区：三项核心统计并排。 */
.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

/* 图表网格：趋势图独占上方，分布图并排。 */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.chart-panel,
.keyword-panel {
  padding: 20px;
}

.chart-panel-wide {
  grid-column: 1 / -1;
}

.chart-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.chart-title h2 {
  margin: 0;
  color: #173934;
  font-size: 18px;
  font-weight: 800;
}

.chart-title span {
  color: #71837f;
  font-size: 13px;
  line-height: 1.6;
}

.chart-box {
  width: 100%;
  height: 320px;
}

/* 关键词列表：条形宽度体现出现频次。 */
.keyword-list {
  display: grid;
  gap: 12px;
}

.keyword-item {
  display: grid;
  grid-template-columns: minmax(70px, 120px) minmax(0, 1fr) 36px;
  gap: 12px;
  align-items: center;
}

.keyword-item span {
  overflow: hidden;
  color: #314d47;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keyword-track {
  height: 12px;
  overflow: hidden;
  border-radius: 8px;
  background: #e8efed;
}

.keyword-bar {
  height: 100%;
  border-radius: 8px;
  background: linear-gradient(90deg, #1f9d8a 0%, #79c9b9 100%);
}

.keyword-item strong {
  color: #1f9d8a;
  font-weight: 800;
  text-align: right;
}

@media (max-width: 860px) {
  /* 中等屏幕下统计卡片和图表改为单列。 */
  .metric-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  /* 小屏下图表高度和关键词布局收紧。 */
  .chart-box {
    height: 280px;
  }

  .keyword-item {
    grid-template-columns: 1fr 34px;
  }

  .keyword-track {
    grid-column: 1 / -1;
    order: 3;
  }
}
</style>
