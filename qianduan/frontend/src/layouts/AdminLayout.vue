<script setup lang="ts">
/**
 * 后台管理基础布局组件。
 *
 * 该布局只服务 /admin/** 路由，提供后台侧边栏、顶部管理员信息和退出后台功能，
 * 不复用普通用户端的 AppLayout。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 当前激活的后台菜单项，和浏览器地址路径保持一致。
const activePath = computed(() => route.path)

// 当前登录管理员展示名，从 adminInfo 中读取。
const adminName = computed(() => {
  const adminInfoText = localStorage.getItem('adminInfo')
  if (!adminInfoText) {
    return '后台管理员'
  }

  try {
    const adminInfo = JSON.parse(adminInfoText)
    return adminInfo.nickname || adminInfo.username || '后台管理员'
  } catch {
    return '后台管理员'
  }
})

/**
 * 后台侧边栏菜单配置。
 *
 * path 对应后台路由路径，label 对应菜单展示文字。
 */
const adminMenuItems = [
  { path: '/admin/dashboard', label: '后台首页' },
  { path: '/admin/users', label: '用户管理' },
  { path: '/admin/diaries', label: '日记管理' },
]

/**
 * 退出后台登录。
 *
 * 清除后台专用 token 和管理员信息，并回到后台登录页。
 */
function logoutAdmin() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  router.push('/admin/login')
}
</script>

<template>
  <!-- 后台管理外壳：和普通用户端分离的独立布局。 -->
  <el-container class="admin-shell">
    <el-header class="admin-header">
      <div>
        <h1>后台管理系统</h1>
        <span>用户与情绪日记数据管理</span>
      </div>

      <div class="admin-user">
        <span>{{ adminName }}</span>
        <el-button size="small" @click="logoutAdmin">退出后台</el-button>
      </div>
    </el-header>

    <el-container class="admin-body">
      <el-aside class="admin-aside" width="220px">
        <el-menu :default-active="activePath" router class="admin-menu">
          <el-menu-item v-for="item in adminMenuItems" :key="item.path" :index="item.path">
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 后台外壳：占满浏览器窗口并使用独立背景。 */
.admin-shell {
  min-height: 100vh;
  background: #f3f4f6;
}

/* 后台顶部栏：展示系统名称和管理员操作。 */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  border-bottom: 1px solid #d1d5db;
  background: #111827;
  color: #ffffff;
}

.admin-header h1 {
  margin: 0;
  font-size: 20px;
  line-height: 1.2;
}

.admin-header span {
  display: block;
  margin-top: 4px;
  color: #d1d5db;
  font-size: 12px;
}

/* 顶部管理员信息区域。 */
.admin-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-user span {
  margin-top: 0;
  color: #ffffff;
  font-size: 14px;
}

/* 后台主体区域高度。 */
.admin-body {
  min-height: calc(100vh - 64px);
}

/* 后台侧边栏。 */
.admin-aside {
  border-right: 1px solid #d1d5db;
  background: #ffffff;
}

.admin-menu {
  min-height: 100%;
  border-right: 0;
}

/* 后台内容区。 */
.admin-main {
  padding: 24px;
  background: #f3f4f6;
}
</style>
