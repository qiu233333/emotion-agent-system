<script setup lang="ts">
/**
 * 后台首页页面。
 *
 * 页面进入时通过后台用户和日记分页接口读取总数，展示后台管理核心数据概览。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { getAdminDiaries, getAdminUsers } from '@/api/admin'

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

const router = useRouter()

// 后台首页统计数据。
const stats = reactive({
  userTotal: 0,
  activeUserTotal: 0,
  disabledUserTotal: 0,
  diaryTotal: 0,
})

// 统计卡片加载状态。
const loading = ref(false)

/**
 * 读取后台首页统计数据。
 */
async function loadDashboardStats() {
  loading.value = true

  try {
    const [usersResult, activeUsersResult, disabledUsersResult, diariesResult] = (await Promise.all([
      getAdminUsers({ page: 1, size: 1 }),
      getAdminUsers({ page: 1, size: 1, status: 1 }),
      getAdminUsers({ page: 1, size: 1, status: 0 }),
      getAdminDiaries({ page: 1, size: 1 }),
    ])) as [Result<PageData>, Result<PageData>, Result<PageData>, Result<PageData>]

    if (
      usersResult.code === 200 &&
      activeUsersResult.code === 200 &&
      disabledUsersResult.code === 200 &&
      diariesResult.code === 200
    ) {
      stats.userTotal = usersResult.data?.total || 0
      stats.activeUserTotal = activeUsersResult.data?.total || 0
      stats.disabledUserTotal = disabledUsersResult.data?.total || 0
      stats.diaryTotal = diariesResult.data?.total || 0
    } else {
      ElMessage.error('后台统计加载失败')
    }
  } catch (error) {
    ElMessage.error('后台统计加载失败')
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到指定后台管理页面。
 *
 * @param path 后台页面路径
 */
function goTo(path: string) {
  router.push(path)
}

// 页面加载后读取统计数据。
onMounted(loadDashboardStats)
</script>

<template>
  <!-- 后台首页：展示核心数据概览和管理入口。 -->
  <section v-loading="loading" class="admin-dashboard">
    <div class="page-header">
      <div>
        <h2 class="page-title">后台首页</h2>
        <p class="page-description">集中查看用户和情绪日记数据概览，并快速进入管理页面。</p>
      </div>
      <el-button @click="loadDashboardStats">刷新</el-button>
    </div>

    <div class="stats-grid">
      <el-card class="stat-card">
        <span>用户总数</span>
        <strong>{{ stats.userTotal }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>正常用户</span>
        <strong>{{ stats.activeUserTotal }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>禁用用户</span>
        <strong>{{ stats.disabledUserTotal }}</strong>
      </el-card>
      <el-card class="stat-card">
        <span>日记总数</span>
        <strong>{{ stats.diaryTotal }}</strong>
      </el-card>
    </div>

    <div class="quick-actions">
      <el-button type="primary" @click="goTo('/admin/users')">进入用户管理</el-button>
      <el-button type="primary" plain @click="goTo('/admin/diaries')">进入日记管理</el-button>
    </div>
  </section>
</template>

<style scoped>
/* 后台首页布局。 */
.admin-dashboard {
  min-height: calc(100vh - 112px);
}

/* 页面标题行。 */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

/* 统计卡片网格。 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(160px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

/* 单个统计卡片。 */
.stat-card {
  border-radius: 8px;
}

.stat-card span {
  display: block;
  color: #6b7280;
  font-size: 14px;
}

.stat-card strong {
  display: block;
  margin-top: 10px;
  color: #111827;
  font-size: 30px;
  line-height: 1;
}

/* 快捷操作按钮区域。 */
.quick-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(160px, 1fr));
  }
}

@media (max-width: 560px) {
  .page-header,
  .quick-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
