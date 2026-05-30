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

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppLayout,
      redirect: '/dashboard',
      children: [
        {
          path: 'login',
          name: 'login',
          component: LoginView,
          meta: { title: '登录页' },
        },
        {
          path: 'register',
          name: 'register',
          component: RegisterView,
          meta: { title: '注册页' },
        },
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

export default router
