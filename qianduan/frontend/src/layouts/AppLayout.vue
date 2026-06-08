<script setup lang="ts">
/**
 * 用户端基础布局组件。
 *
 * 页面采用“顶部导航 + 中央内容区”的轻量工作台布局，
 * 让普通用户端和后台管理端在视觉气质上保持区分。
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 当前激活路径，用于顶部导航高亮。
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
 * 用户端顶部导航配置。
 *
 * path 对应路由路径，label 对应导航展示文字，hint 用于轻量说明入口用途。
 */
const menuItems = [
  { path: '/dashboard', label: '工作台', hint: '今日概览' },
  { path: '/diary', label: '写日记', hint: '记录当下' },
  { path: '/history', label: '回看', hint: '历史记录' },
  { path: '/chat', label: '陪伴', hint: 'AI 对话' },
  { path: '/statistics', label: '趋势', hint: '统计图表' },
]

/**
 * 判断导航项是否处于激活状态。
 *
 * @param path 导航项路径
 * @returns 当前路径命中该导航项时返回 true
 */
function isActivePath(path: string) {
  return activePath.value === path
}

/**
 * 跳转到指定业务页面。
 *
 * @param path 目标路由路径
 */
function navigateTo(path: string) {
  router.push(path)
}

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
  <!-- 用户端外壳：顶部固定品牌和导航，下方承载业务页面。 -->
  <div class="user-shell">
    <header class="user-header">
      <button class="brand-block" type="button" @click="navigateTo('/dashboard')">
        <span class="brand-mark">心</span>
        <span>
          <strong>情绪记录工作台</strong>
          <small>记录、整理、陪伴、趋势</small>
        </span>
      </button>

      <nav class="top-nav" aria-label="用户端导航">
        <button
          v-for="item in menuItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: isActivePath(item.path) }"
          type="button"
          @click="navigateTo(item.path)"
        >
          <span>{{ item.label }}</span>
          <small>{{ item.hint }}</small>
        </button>
      </nav>

      <div class="header-user">
        <span>{{ currentUserName }}</span>
        <el-button size="small" plain @click="logout">退出</el-button>
      </div>
    </header>

    <main class="user-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
/* 用户端外壳：使用柔和背景承载全部页面。 */
.user-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(108, 194, 176, 0.18), transparent 32rem),
    linear-gradient(180deg, #f4fbfa 0%, #f7f8fb 44%, #eef5f3 100%);
}

/* 顶部导航栏：轻量固定入口，降低后台管理感。 */
.user-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  padding: 14px 32px;
  border-bottom: 1px solid rgba(116, 140, 134, 0.18);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(14px);
}

/* 品牌区域：点击后回到用户工作台。 */
.brand-block {
  display: inline-flex;
  gap: 10px;
  align-items: center;
  min-width: 210px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #14312d;
  text-align: left;
  cursor: pointer;
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  border-radius: 8px;
  background: #1f9d8a;
  color: #ffffff;
  font-weight: 700;
}

.brand-block strong {
  display: block;
  font-size: 17px;
  font-weight: 700;
  line-height: 1.2;
}

.brand-block small {
  display: block;
  margin-top: 3px;
  color: #6a7f79;
  font-size: 12px;
}

/* 顶部导航：每个入口保持稳定尺寸，避免页面切换时跳动。 */
.top-nav {
  display: flex;
  justify-content: center;
  gap: 8px;
  min-width: 0;
}

.nav-item {
  min-width: 88px;
  padding: 8px 12px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  color: #4d625d;
  cursor: pointer;
  transition:
    background-color 0.18s ease,
    border-color 0.18s ease,
    color 0.18s ease;
}

.nav-item span,
.nav-item small {
  display: block;
}

.nav-item span {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.25;
}

.nav-item small {
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.25;
}

.nav-item:hover,
.nav-item.active {
  border-color: rgba(31, 157, 138, 0.22);
  background: rgba(31, 157, 138, 0.1);
  color: #146456;
}

/* 顶部用户区域：展示昵称和退出操作。 */
.header-user {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  color: #3b514d;
  font-size: 14px;
}

.header-user span {
  max-width: 120px;
  overflow: hidden;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主内容区：约束宽度，保留呼吸感。 */
.user-main {
  width: min(1180px, calc(100% - 32px));
  margin: 0 auto;
  padding: 28px 0 40px;
}

@media (max-width: 960px) {
  /* 中等屏幕下顶部区域换行，导航独占一行。 */
  .user-header {
    grid-template-columns: 1fr auto;
    padding: 12px 18px;
  }

  .top-nav {
    grid-column: 1 / -1;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 2px;
  }
}

@media (max-width: 640px) {
  /* 小屏幕下隐藏导航说明，保证入口文字不拥挤。 */
  .brand-block {
    min-width: 0;
  }

  .brand-block small,
  .nav-item small {
    display: none;
  }

  .top-nav {
    gap: 6px;
  }

  .nav-item {
    min-width: 70px;
    padding: 8px 10px;
  }

  .user-main {
    width: min(100% - 20px, 1180px);
    padding-top: 18px;
  }
}
</style>
