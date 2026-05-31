/**
 * 前端路由模块。
 *
 * 这里集中配置系统全部页面路径。页面访问时，Vue Router 会根据 URL
 * 找到对应组件，并渲染到布局组件中的 RouterView 区域。
 */
import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '@/layouts/AppLayout.vue'
import AdminView from '@/views/AdminView.vue'
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
        {
          path: 'admin',
          name: 'admin',
          component: AdminView,
          meta: { title: '后台管理' },
        },
      ],
    },
  ],
})

/**
 * 全局路由守卫。
 *
 * 访问登录和注册页不需要 token；访问其他系统页面时，如果本地没有 token，
 * 就跳转到登录页，并把原目标地址放到 redirect 参数里。
 */
router.beforeEach((to) => {
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
