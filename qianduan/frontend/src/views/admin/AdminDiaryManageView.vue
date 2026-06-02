<script setup lang="ts">
/**
 * 后台日记管理页面。
 *
 * 管理员可以分页查询全部用户的情绪日记，新增任意用户日记，编辑完整日记字段，
 * 并物理删除指定日记。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createAdminDiary,
  deleteAdminDiary,
  getAdminDiaries,
  getAdminDiaryDetail,
  updateAdminDiary,
} from '@/api/admin'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * 后台分页响应结构。
 */
interface PageData<T = unknown> {
  records: T[]
  total: number
  page: number
  size: number
}

/**
 * 后台日记表格记录。
 */
interface DiaryRecord {
  id?: number
  userId?: number
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
 * 后台日记表单结构。
 */
interface DiaryForm {
  id?: number
  userId: number | null
  title: string
  content: string
  moodTag: string
  emotionPolarity: string
  emotionType: string
  emotionScore: number | null
  keywords: string
  aiSummary: string
  aiSuggestion: string
  diaryDate: string
  createTime: string
  updateTime: string
}

// 日记筛选条件。
const queryForm = reactive({
  userId: '',
  startDate: '',
  endDate: '',
  emotionType: '',
  emotionPolarity: '',
  keyword: '',
})

// 日记分页状态。
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 日记表格数据源。
const diaryList = ref<DiaryRecord[]>([])

// 日记表格加载状态。
const loading = ref(false)

// 日记新增/编辑弹窗状态。
const diaryDialogVisible = ref(false)

// 日记弹窗模式。
const diaryDialogMode = ref<'create' | 'edit'>('create')

// 日记新增/编辑表单。
const diaryForm = reactive<DiaryForm>({
  userId: null,
  title: '',
  content: '',
  moodTag: '',
  emotionPolarity: '',
  emotionType: '',
  emotionScore: null,
  keywords: '',
  aiSummary: '',
  aiSuggestion: '',
  diaryDate: '',
  createTime: '',
  updateTime: '',
})

// 日记表单提交 loading 状态。
const diarySubmitLoading = ref(false)

/**
 * 组装日记列表查询参数。
 *
 * @returns 后台日记分页接口参数
 */
function buildDiaryQueryParams() {
  const params: Record<string, string | number> = {
    page: pagination.page,
    size: pagination.size,
  }

  if (queryForm.userId.trim()) {
    params.userId = queryForm.userId.trim()
  }
  if (queryForm.startDate) {
    params.startDate = queryForm.startDate
  }
  if (queryForm.endDate) {
    params.endDate = queryForm.endDate
  }
  if (queryForm.emotionType.trim()) {
    params.emotionType = queryForm.emotionType.trim()
  }
  if (queryForm.emotionPolarity) {
    params.emotionPolarity = queryForm.emotionPolarity
  }
  if (queryForm.keyword.trim()) {
    params.keyword = queryForm.keyword.trim()
  }

  return params
}

/**
 * 查询后台日记列表。
 */
async function loadDiaries() {
  loading.value = true

  try {
    const result = (await getAdminDiaries(buildDiaryQueryParams())) as Result<PageData<DiaryRecord>>

    if (result.code === 200) {
      diaryList.value = result.data?.records || []
      pagination.total = result.data?.total || 0
      pagination.page = result.data?.page || pagination.page
      pagination.size = result.data?.size || pagination.size
    } else {
      ElMessage.error(result.message || '查询日记失败')
    }
  } catch (error) {
    ElMessage.error('查询日记失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置日记筛选条件并重新查询。
 */
function resetQuery() {
  queryForm.userId = ''
  queryForm.startDate = ''
  queryForm.endDate = ''
  queryForm.emotionType = ''
  queryForm.emotionPolarity = ''
  queryForm.keyword = ''
  pagination.page = 1
  loadDiaries()
}

/**
 * 重置日记新增/编辑表单。
 */
function resetDiaryForm() {
  diaryForm.id = undefined
  diaryForm.userId = null
  diaryForm.title = ''
  diaryForm.content = ''
  diaryForm.moodTag = ''
  diaryForm.emotionPolarity = ''
  diaryForm.emotionType = ''
  diaryForm.emotionScore = null
  diaryForm.keywords = ''
  diaryForm.aiSummary = ''
  diaryForm.aiSuggestion = ''
  diaryForm.diaryDate = ''
  diaryForm.createTime = ''
  diaryForm.updateTime = ''
}

/**
 * 把接口返回的日记复制到表单。
 *
 * @param diary 日记详情
 */
function copyDiaryToForm(diary: DiaryRecord) {
  diaryForm.id = diary.id
  diaryForm.userId = diary.userId || null
  diaryForm.title = diary.title || ''
  diaryForm.content = diary.content || ''
  diaryForm.moodTag = diary.moodTag || ''
  diaryForm.emotionPolarity = diary.emotionPolarity || ''
  diaryForm.emotionType = diary.emotionType || ''
  diaryForm.emotionScore = diary.emotionScore ?? null
  diaryForm.keywords = diary.keywords || ''
  diaryForm.aiSummary = diary.aiSummary || ''
  diaryForm.aiSuggestion = diary.aiSuggestion || ''
  diaryForm.diaryDate = diary.diaryDate || ''
  diaryForm.createTime = diary.createTime || ''
  diaryForm.updateTime = diary.updateTime || ''
}

/**
 * 打开新增日记弹窗。
 */
function openCreateDialog() {
  diaryDialogMode.value = 'create'
  resetDiaryForm()
  diaryDialogVisible.value = true
}

/**
 * 打开编辑日记弹窗。
 *
 * @param row 当前表格行日记
 */
async function openEditDialog(row: DiaryRecord) {
  if (!row.id) {
    ElMessage.warning('日记 ID 不存在')
    return
  }

  diaryDialogMode.value = 'edit'
  resetDiaryForm()

  try {
    const result = (await getAdminDiaryDetail(row.id)) as Result<DiaryRecord>
    if (result.code === 200 && result.data) {
      copyDiaryToForm(result.data)
      diaryDialogVisible.value = true
    } else {
      ElMessage.error(result.message || '查询日记详情失败')
    }
  } catch (error) {
    ElMessage.error('查询日记详情失败')
  }
}

/**
 * 组装日记保存请求体。
 *
 * @returns 后台日记新增或修改请求体
 */
function buildDiaryPayload() {
  return {
    userId: diaryForm.userId,
    title: diaryForm.title,
    content: diaryForm.content,
    moodTag: diaryForm.moodTag,
    emotionPolarity: diaryForm.emotionPolarity,
    emotionType: diaryForm.emotionType,
    emotionScore: diaryForm.emotionScore,
    keywords: diaryForm.keywords,
    aiSummary: diaryForm.aiSummary,
    aiSuggestion: diaryForm.aiSuggestion,
    diaryDate: diaryForm.diaryDate || null,
  }
}

/**
 * 提交新增或编辑日记表单。
 */
async function submitDiaryForm() {
  if (!diaryForm.userId || !diaryForm.title.trim() || !diaryForm.content.trim()) {
    ElMessage.warning('请填写用户 ID、标题和内容')
    return
  }

  diarySubmitLoading.value = true

  try {
    const result =
      diaryDialogMode.value === 'create'
        ? ((await createAdminDiary(buildDiaryPayload())) as Result<DiaryRecord>)
        : ((await updateAdminDiary(diaryForm.id as number, buildDiaryPayload())) as Result<DiaryRecord>)

    if (result.code === 200) {
      ElMessage.success(diaryDialogMode.value === 'create' ? '日记新增成功' : '日记修改成功')
      diaryDialogVisible.value = false
      loadDiaries()
    } else {
      ElMessage.error(result.message || '保存日记失败')
    }
  } catch (error) {
    ElMessage.error('保存日记失败')
  } finally {
    diarySubmitLoading.value = false
  }
}

/**
 * 删除指定日记。
 *
 * @param row 当前表格行日记
 */
async function removeDiary(row: DiaryRecord) {
  if (!row.id) {
    ElMessage.warning('日记 ID 不存在')
    return
  }

  try {
    await ElMessageBox.confirm(`确认删除日记「${row.title || row.id}」吗？`, '删除日记', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const result = (await deleteAdminDiary(row.id)) as Result
    if (result.code === 200) {
      ElMessage.success('日记已删除')
      loadDiaries()
    } else {
      ElMessage.error(result.message || '删除日记失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除日记失败')
    }
  }
}

/**
 * 获取情绪极性的中文展示文本。
 *
 * @param polarity 情绪极性
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
 * @param polarity 情绪极性
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
function formatText(text?: string | number | null) {
  return text === null || text === undefined || text === '' ? '暂无' : String(text)
}

/**
 * 页码变化时重新查询。
 *
 * @param page 新页码
 */
function handlePageChange(page: number) {
  pagination.page = page
  loadDiaries()
}

/**
 * 每页数量变化时重新查询。
 *
 * @param size 新每页数量
 */
function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadDiaries()
}

// 页面加载后查询日记列表。
onMounted(loadDiaries)
</script>

<template>
  <!-- 后台日记管理页：提供全部用户日记完整字段管理。 -->
  <el-card class="page-card">
    <div class="page-header">
      <div>
        <h2 class="page-title">日记管理</h2>
        <p class="page-description">查看、创建、编辑和删除所有用户的情绪日记完整信息。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增日记</el-button>
    </div>

    <el-form class="query-form" :model="queryForm" inline @keyup.enter="loadDiaries">
      <el-form-item label="用户ID">
        <el-input v-model="queryForm.userId" clearable placeholder="指定用户 ID" class="small-input" />
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker
          v-model="queryForm.startDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="开始日期"
          class="date-input"
        />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker
          v-model="queryForm.endDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="结束日期"
          class="date-input"
        />
      </el-form-item>
      <el-form-item label="情绪类型">
        <el-input v-model="queryForm.emotionType" clearable placeholder="例如：开心" class="small-input" />
      </el-form-item>
      <el-form-item label="情绪极性">
        <el-select v-model="queryForm.emotionPolarity" clearable placeholder="全部极性" class="small-input">
          <el-option label="积极" value="positive" />
          <el-option label="消极" value="negative" />
          <el-option label="平静" value="neutral" />
        </el-select>
      </el-form-item>
      <el-form-item label="关键词">
        <el-input v-model="queryForm.keyword" clearable placeholder="标题/内容/关键词" class="keyword-input" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadDiaries">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="diaryList" border class="data-table">
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="diary-detail">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="日记内容" :span="2">
                {{ formatText(row.content) }}
              </el-descriptions-item>
              <el-descriptions-item label="心情标签">
                {{ formatText(row.moodTag) }}
              </el-descriptions-item>
              <el-descriptions-item label="情绪分数">
                {{ formatText(row.emotionScore) }}
              </el-descriptions-item>
              <el-descriptions-item label="关键词" :span="2">
                {{ formatText(row.keywords) }}
              </el-descriptions-item>
              <el-descriptions-item label="AI 摘要" :span="2">
                {{ formatText(row.aiSummary) }}
              </el-descriptions-item>
              <el-descriptions-item label="AI 建议" :span="2">
                {{ formatText(row.aiSuggestion) }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatText(row.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ formatText(row.updateTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="moodTag" label="心情标签" width="130" show-overflow-tooltip />
      <el-table-column label="情绪极性" width="120">
        <template #default="{ row }">
          <el-tag :type="getPolarityTagType(row.emotionPolarity)" effect="plain">
            {{ getPolarityLabel(row.emotionPolarity) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="emotionType" label="情绪类型" width="120" />
      <el-table-column prop="emotionScore" label="分数" width="80" />
      <el-table-column prop="keywords" label="关键词" min-width="180" show-overflow-tooltip />
      <el-table-column prop="diaryDate" label="日记日期" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeDiary(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog
      v-model="diaryDialogVisible"
      :title="diaryDialogMode === 'create' ? '新增日记' : '编辑日记'"
      width="760px"
    >
      <el-form label-width="104px" class="dialog-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日记ID" v-if="diaryDialogMode === 'edit'">
              <el-input :model-value="diaryForm.id || ''" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户ID">
              <el-input-number v-model="diaryForm.userId" :min="1" :controls="false" class="full-control" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="标题">
          <el-input v-model="diaryForm.title" placeholder="请输入日记标题" />
        </el-form-item>

        <el-form-item label="内容">
          <el-input v-model="diaryForm.content" type="textarea" :rows="5" placeholder="请输入日记内容" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="心情标签">
              <el-input v-model="diaryForm.moodTag" placeholder="例如：开心、焦虑" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日记日期">
              <el-date-picker
                v-model="diaryForm.diaryDate"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择日记日期"
                class="full-control"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="情绪极性">
              <el-select v-model="diaryForm.emotionPolarity" clearable placeholder="选择极性" class="full-control">
                <el-option label="积极" value="positive" />
                <el-option label="消极" value="negative" />
                <el-option label="平静" value="neutral" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="情绪类型">
              <el-input v-model="diaryForm.emotionType" placeholder="例如：开心" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="情绪分数">
              <el-input-number v-model="diaryForm.emotionScore" :min="1" :max="5" class="full-control" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="关键词">
          <el-input v-model="diaryForm.keywords" placeholder="多个关键词可用逗号分隔" />
        </el-form-item>

        <el-form-item label="AI 摘要">
          <el-input v-model="diaryForm.aiSummary" type="textarea" :rows="3" placeholder="请输入 AI 摘要" />
        </el-form-item>

        <el-form-item label="AI 建议">
          <el-input v-model="diaryForm.aiSuggestion" type="textarea" :rows="3" placeholder="请输入 AI 建议" />
        </el-form-item>

        <el-row v-if="diaryDialogMode === 'edit'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="创建时间">
              <el-input :model-value="diaryForm.createTime" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="更新时间">
              <el-input :model-value="diaryForm.updateTime" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="diaryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="diarySubmitLoading" @click="submitDiaryForm">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
/* 页面标题区域。 */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

/* 查询条件区域。 */
.query-form {
  margin-top: 24px;
}

/* 小型筛选输入控件。 */
.small-input {
  width: 150px;
}

/* 日期筛选输入控件。 */
.date-input {
  width: 160px;
}

/* 关键词筛选输入控件。 */
.keyword-input {
  width: 190px;
}

/* 日记数据表格。 */
.data-table {
  margin-top: 8px;
}

/* 展开行详情。 */
.diary-detail {
  padding: 12px 24px;
  background: #fafafa;
}

/* 表格操作按钮区域。 */
.table-actions {
  display: flex;
  gap: 8px;
}

.table-actions .el-button {
  margin-left: 0;
}

/* 分页区域。 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

/* 弹窗表单布局。 */
.dialog-form {
  padding-right: 12px;
}

.full-control {
  width: 100%;
}
</style>
