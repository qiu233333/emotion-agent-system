<script setup lang="ts">
/**
 * 系统基础布局组件。
 *
 * 页面采用“顶部标题 + 左侧菜单 + 右侧内容区”的后台管理布局。
 * 左侧菜单点击后由 Vue Router 切换右侧 RouterView 中的页面内容。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 当前激活菜单项，和浏览器地址路径保持一致。
const activePath = computed(() => route.path)

// 当前登录用户展示名，从登录成功后保存的 userInfo 中读取。
const currentUserName = computed(() => {
  const userInfoText = localStorage.getItem('userInfo')
  if (!userInfoText) {
    return '已登录用户'
  }

  try {
    const userInfo = JSON.parse(userInfoText)
    return userInfo.nickname || userInfo.username || '已登录用户'
  } catch {
    return '已登录用户'
  }
})

/**
 * 左侧菜单配置。
 *
 * path 对应路由路径，label 对应菜单展示文字。
 */
const menuItems = [
  { path: '/dashboard', label: '首页' },
  { path: '/diary', label: '情绪日记' },
  { path: '/history', label: '历史记录' },
  { path: '/chat', label: 'AI陪伴' },
  { path: '/statistics', label: '情绪统计' },
]

/**
 * 退出登录。
 *
 * 清除本地保存的 token 和用户信息，并回到登录页。
 */
function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<template>
  <!-- Element Plus 容器布局：最外层控制整个应用的高度和背景。 -->
  <el-container class="app-shell">
    <!-- 顶部标题栏：展示系统名称和当前技术栈提示。 -->
    <el-header class="app-header">
      <div>
        <h1>情绪记录与管理智能体系统</h1>
        <span>Vue 3 + Vue Router + Element Plus</span>
      </div>

      <div class="header-user">
        <span>{{ currentUserName }}</span>
        <el-button size="small" @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="app-body">
      <!-- 左侧菜单：router 模式会在点击菜单时自动调用路由跳转。 -->
      <el-aside class="app-aside" width="220px">
        <el-menu :default-active="activePath" router class="side-menu">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区：当前路由命中的页面会渲染到这里。 -->
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* 应用外壳：占满整个浏览器窗口。 */
.app-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

/* 顶部标题栏：固定高度并使用白色背景，和内容区形成层级。 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.app-header h1 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  line-height: 1.2;
}

.app-header span {
  display: block;
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

/* 顶部右侧用户区域：显示当前用户和退出按钮。 */
.header-user {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #374151;
  font-size: 14px;
}

.header-user span {
  margin-top: 0;
  font-size: 14px;
}

/* 主体区域高度等于视口高度减去顶部标题栏高度。 */
.app-body {
  min-height: calc(100vh - 64px);
}

/* 左侧菜单栏：白色背景和右侧内容区分隔。 */
.app-aside {
  border-right: 1px solid #e5e7eb;
  background: #ffffff;
}

.side-menu {
  min-height: 100%;
  border-right: 0;
}

/* 右侧内容区：给页面卡片留出统一内边距。 */
.app-main {
  padding: 24px;
  background: #f5f7fb;
}
</style>
