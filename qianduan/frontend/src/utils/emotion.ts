/**
 * 情绪数据前端整理工具模块。
 *
 * 该模块集中处理日记列表的日期格式化、情绪标签展示和统计汇总，
 * 让首页、历史记录页和统计页使用一致的数据口径。
 */

/**
 * 后端统一 Result 响应结构。
 */
export interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * 情绪日记记录结构。
 */
export interface DiaryRecord {
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
  createTime?: string
  updateTime?: string
}

/**
 * 趋势图单日数据结构。
 */
export interface TrendPoint {
  date: string
  label: string
  score: number
  count: number
}

/**
 * 分布图单项数据结构。
 */
export interface DistributionItem {
  name: string
  value: number
}

/**
 * 关键词频次结构。
 */
export interface KeywordItem {
  word: string
  count: number
}

/**
 * 把接口返回数据整理成日记数组。
 *
 * @param data 后端返回的 data 字段，可能是数组或分页对象
 * @returns 归一化后的日记数组
 */
export function normalizeDiaryList(data: unknown): DiaryRecord[] {
  if (Array.isArray(data)) {
    return data as DiaryRecord[]
  }

  if (data && typeof data === 'object' && Array.isArray((data as { records?: unknown }).records)) {
    return (data as { records: DiaryRecord[] }).records
  }

  return []
}

/**
 * 获取情绪极性的中文展示文本。
 *
 * @param polarity 后端返回的情绪极性
 * @returns 中文极性名称
 */
export function getPolarityLabel(polarity?: string) {
  const polarityMap: Record<string, string> = {
    positive: '积极',
    negative: '低落',
    neutral: '平静',
  }

  return polarity ? polarityMap[polarity] || polarity : '未识别'
}

/**
 * 获取情绪极性对应的 Element Plus 标签类型。
 *
 * @param polarity 后端返回的情绪极性
 * @returns 标签类型
 */
export function getPolarityTagType(polarity?: string) {
  if (polarity === 'positive') {
    return 'success'
  }
  if (polarity === 'negative') {
    return 'danger'
  }
  return 'info'
}

/**
 * 获取情绪类型展示文本。
 *
 * @param emotionType 后端返回的情绪类型
 * @returns 情绪类型或占位文本
 */
export function getEmotionTypeLabel(emotionType?: string) {
  return emotionType || '未识别'
}

/**
 * 获取情绪强度分数。
 *
 * @param score 后端返回的情绪强度
 * @returns 1 到 5 的有效分数；缺失时返回 0
 */
export function normalizeEmotionScore(score?: number) {
  if (typeof score !== 'number' || !Number.isFinite(score)) {
    return 0
  }

  return Math.max(0, Math.min(Number(score), 5))
}

/**
 * 格式化空文本。
 *
 * @param text 待展示文本
 * @param placeholder 空值占位符
 * @returns 可直接展示的文本
 */
export function formatText(text?: string, placeholder = '暂无') {
  return text && text.trim() ? text : placeholder
}

/**
 * 从日期时间文本中提取 yyyy-MM-dd。
 *
 * @param dateText 后端日期时间文本
 * @returns yyyy-MM-dd 或空字符串
 */
export function extractDateKey(dateText?: string) {
  return dateText ? dateText.slice(0, 10) : ''
}

/**
 * 获取浏览器本地日期字符串。
 *
 * @param date 待格式化日期
 * @returns yyyy-MM-dd 格式日期
 */
export function formatLocalDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 格式化日记日期展示。
 *
 * @param dateText 后端日期时间文本
 * @returns 更适合列表展示的日期文本
 */
export function formatDiaryDate(dateText?: string) {
  if (!dateText) {
    return '未记录日期'
  }

  const date = extractDateKey(dateText)
  const time = dateText.length >= 16 ? dateText.slice(11, 16) : ''
  return time ? `${date} ${time}` : date
}

/**
 * 判断日记是否属于今天。
 *
 * @param diary 待判断日记
 * @returns 今天的日记返回 true
 */
export function isTodayDiary(diary: DiaryRecord) {
  return extractDateKey(diary.diaryDate || diary.createTime) === formatLocalDate()
}

/**
 * 获取最近一篇日记。
 *
 * @param diaries 日记列表
 * @returns 最近一篇日记，没有数据时返回 null
 */
export function getLatestDiary(diaries: DiaryRecord[]) {
  return diaries[0] || null
}

/**
 * 拆分关键词字符串。
 *
 * @param keywords 后端保存的关键词文本
 * @returns 关键词数组
 */
export function splitKeywords(keywords?: string) {
  if (!keywords) {
    return []
  }

  return keywords
    .split(/[、，,\s]+/)
    .map((word) => word.trim())
    .filter(Boolean)
}

/**
 * 获取近 7 天日期键列表。
 *
 * @returns 从 6 天前到今天的日期键数组
 */
export function getLastSevenDateKeys() {
  const keys: string[] = []
  const today = new Date()

  for (let offset = 6; offset >= 0; offset -= 1) {
    const date = new Date(today)
    date.setDate(today.getDate() - offset)
    keys.push(formatLocalDate(date))
  }

  return keys
}

/**
 * 构建近 7 天情绪强度趋势。
 *
 * @param diaries 日记列表
 * @returns 近 7 天趋势数据
 */
export function buildSevenDayTrend(diaries: DiaryRecord[]): TrendPoint[] {
  const dateKeys = getLastSevenDateKeys()

  return dateKeys.map((dateKey) => {
    const dayDiaries = diaries.filter((diary) => extractDateKey(diary.diaryDate || diary.createTime) === dateKey)
    const scores = dayDiaries.map((diary) => normalizeEmotionScore(diary.emotionScore)).filter((score) => score > 0)
    const averageScore = scores.length
      ? Number((scores.reduce((sum, score) => sum + score, 0) / scores.length).toFixed(1))
      : 0

    return {
      date: dateKey,
      label: dateKey.slice(5),
      score: averageScore,
      count: dayDiaries.length,
    }
  })
}

/**
 * 构建情绪类型分布数据。
 *
 * @param diaries 日记列表
 * @returns 情绪类型分布数组
 */
export function buildEmotionDistribution(diaries: DiaryRecord[]): DistributionItem[] {
  const countMap = new Map<string, number>()

  diaries.forEach((diary) => {
    const name = getEmotionTypeLabel(diary.emotionType)
    countMap.set(name, (countMap.get(name) || 0) + 1)
  })

  return Array.from(countMap.entries()).map(([name, value]) => ({
    name,
    value,
  }))
}

/**
 * 构建情绪强度分布数据。
 *
 * @param diaries 日记列表
 * @returns 1 到 5 分强度统计
 */
export function buildScoreDistribution(diaries: DiaryRecord[]): DistributionItem[] {
  return [1, 2, 3, 4, 5].map((score) => ({
    name: `${score}分`,
    value: diaries.filter((diary) => normalizeEmotionScore(diary.emotionScore) === score).length,
  }))
}

/**
 * 构建高频关键词数据。
 *
 * @param diaries 日记列表
 * @param limit 最大返回数量
 * @returns 高频关键词数组
 */
export function buildKeywordFrequency(diaries: DiaryRecord[], limit = 10): KeywordItem[] {
  const keywordMap = new Map<string, number>()

  diaries.forEach((diary) => {
    splitKeywords(diary.keywords).forEach((keyword) => {
      keywordMap.set(keyword, (keywordMap.get(keyword) || 0) + 1)
    })
  })

  return Array.from(keywordMap.entries())
    .map(([word, count]) => ({ word, count }))
    .sort((left, right) => right.count - left.count)
    .slice(0, limit)
}

/**
 * 计算近 7 天日记数量。
 *
 * @param diaries 日记列表
 * @returns 近 7 天日记总数
 */
export function countSevenDayDiaries(diaries: DiaryRecord[]) {
  const dateKeySet = new Set(getLastSevenDateKeys())
  return diaries.filter((diary) => dateKeySet.has(extractDateKey(diary.diaryDate || diary.createTime))).length
}

/**
 * 计算平均情绪强度。
 *
 * @param diaries 日记列表
 * @returns 平均强度文本
 */
export function getAverageScoreText(diaries: DiaryRecord[]) {
  const scores = diaries.map((diary) => normalizeEmotionScore(diary.emotionScore)).filter((score) => score > 0)
  if (!scores.length) {
    return '暂无'
  }

  const averageScore = scores.reduce((sum, score) => sum + score, 0) / scores.length
  return averageScore.toFixed(1)
}
