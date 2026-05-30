<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const activePath = computed(() => route.path)

const menuItems = [
  { path: '/login', label: '登录页' },
  { path: '/register', label: '注册页' },
  { path: '/dashboard', label: '首页' },
  { path: '/diary', label: '情绪日记' },
  { path: '/history', label: '历史记录' },
  { path: '/chat', label: 'AI陪伴' },
  { path: '/statistics', label: '情绪统计' },
  { path: '/admin', label: '后台管理' },
]
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div>
        <h1>情绪记录与管理智能体系统</h1>
        <span>Vue 3 + Vue Router + Element Plus</span>
      </div>
    </el-header>

    <el-container class="app-body">
      <el-aside class="app-aside" width="220px">
        <el-menu :default-active="activePath" router class="side-menu">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f5f7fb;
}

.app-header {
  display: flex;
  align-items: center;
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

.app-body {
  min-height: calc(100vh - 64px);
}

.app-aside {
  border-right: 1px solid #e5e7eb;
  background: #ffffff;
}

.side-menu {
  min-height: 100%;
  border-right: 0;
}

.app-main {
  padding: 24px;
  background: #f5f7fb;
}
</style>
