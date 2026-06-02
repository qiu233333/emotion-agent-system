/**
 * 前端路由模块。
 *
 * 这里集中配置普通用户端和后台管理端的页面路径。后台管理端使用 /admin/**
 * 独立路径、独立布局和独立登录状态，不再作为普通用户主页面的菜单模块。
 */
import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'
import AppLayout from '@/layouts/AppLayout.vue'
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue'
import AdminDiaryManageView from '@/views/admin/AdminDiaryManageView.vue'
import AdminLoginView from '@/views/admin/AdminLoginView.vue'
import AdminUserManageView from '@/views/admin/AdminUserManageView.vue'
import ChatView from '@/views/ChatView.vue'
import DashboardView from '@/views/DashboardView.vue'
import DiaryView from '@/views/DiaryView.vue'
import HistoryView from '@/views/HistoryView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import StatisticsView from '@/views/StatisticsView.vue'

/**
 * 业务页面路由表。
 *
 * path 是浏览器地址栏路径，component 是路径命中后展示的页面组件。
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { title: '登录页', public: true },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { title: '注册页', public: true },
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLoginView,
      meta: { title: '后台登录', adminPublic: true },
    },
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { title: '后台管理', adminRequired: true },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: AdminDashboardView,
          meta: { title: '后台首页', adminRequired: true },
        },
        {
          path: 'users',
          name: 'admin-users',
          component: AdminUserManageView,
          meta: { title: '用户管理', adminRequired: true },
        },
        {
          path: 'diaries',
          name: 'admin-diaries',
          component: AdminDiaryManageView,
          meta: { title: '日记管理', adminRequired: true },
        },
      ],
    },
    {
      path: '/',
      component: AppLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardView,
          meta: { title: '首页' },
        },
        {
          path: 'diary',
          name: 'diary',
          component: DiaryView,
          meta: { title: '情绪日记' },
        },
        {
          path: 'history',
          name: 'history',
          component: HistoryView,
          meta: { title: '历史记录' },
        },
        {
          path: 'chat',
          name: 'chat',
          component: ChatView,
          meta: { title: 'AI陪伴' },
        },
        {
          path: 'statistics',
          name: 'statistics',
          component: StatisticsView,
          meta: { title: '情绪统计' },
        },
      ],
    },
  ],
})

/**
 * 全局路由守卫。
 *
 * 普通用户端检查 token；后台管理端检查 adminToken。两个登录状态互不复用，
 * 避免普通用户登录后误进入后台页面。
 */
router.beforeEach((to) => {
  const isAdminPage = to.path.startsWith('/admin') || to.meta.adminRequired === true
  const isAdminPublicPage = to.meta.adminPublic === true

  if (isAdminPage) {
    const adminToken = localStorage.getItem('adminToken')

    if (!adminToken && !isAdminPublicPage) {
      return {
        path: '/admin/login',
        query: {
          redirect: to.fullPath,
        },
      }
    }

    if (adminToken && to.path === '/admin/login') {
      return '/admin/dashboard'
    }

    return true
  }

  const token = localStorage.getItem('token')
  const isPublicPage = to.meta.public === true

  if (!token && !isPublicPage) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (token && to.path === '/login') {
    return '/dashboard'
  }

  return true
})

export default router
