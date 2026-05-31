<script setup lang="ts">
/**
 * 登录页面。
 *
 * 用户输入账号密码后调用后端 /api/user/login，登录成功会保存 token，
 * 后续 Axios 请求会自动把 token 放到 Authorization 请求头中。
 */
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { login } from '@/api/user'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * 登录成功后后端返回的数据结构。
 */
interface LoginData {
  token: string
  userId: number
  username: string
  nickname?: string
  role?: string
}

const route = useRoute()
const router = useRouter()

// 登录表单数据，字段名和后端登录请求对象保持一致。
const loginForm = reactive({
  username: '',
  password: '',
})

// 登录按钮 loading 状态，避免重复提交。
const loginLoading = ref(false)

/**
 * 清空登录表单。
 */
function resetLoginForm() {
  loginForm.username = ''
  loginForm.password = ''
}

/**
 * 获取登录成功后的跳转地址。
 *
 * @returns 登录前访问的地址；没有记录时返回首页
 */
function getRedirectPath() {
  const redirect = route.query.redirect
  return typeof redirect === 'string' && redirect ? redirect : '/dashboard'
}

/**
 * 提交登录请求。
 *
 * 登录成功后保存 token 和基础用户信息，再跳转到目标系统页面。
 */
async function submitLogin() {
  if (!loginForm.username.trim() || !loginForm.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loginLoading.value = true

  try {
    const result = (await login({
      username: loginForm.username,
      password: loginForm.password,
    })) as Result<LoginData>

    if (result.code === 200 && result.data?.token) {
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('userInfo', JSON.stringify(result.data))
      ElMessage.success('登录成功')
      router.push(getRedirectPath())
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error('登录失败')
  } finally {
    loginLoading.value = false
  }
}
</script>

<template>
  <!-- 登录页：用户登录入口，不进入后台布局。 -->
  <el-card class="auth-card">
    <h2 class="page-title">登录页</h2>
    <p class="page-description">输入账号和密码后进入情绪记录与管理智能体系统。</p>

    <el-form label-width="80px" class="form-preview" @keyup.enter="submitLogin">
      <el-form-item label="用户名">
        <el-input v-model="loginForm.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="loginForm.password" placeholder="请输入密码" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loginLoading" @click="submitLogin">登录</el-button>
        <el-button @click="resetLoginForm">重置</el-button>
        <el-button link type="primary" @click="router.push('/register')">去注册</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
/* 登录页背景：让认证表单在独立页面中居中展示。 */
.auth-card {
  width: min(460px, calc(100vw - 32px));
  margin: 96px auto 0;
  border-radius: 8px;
}

/* 表单预览区域：限制宽度，避免输入框在大屏中过长。 */
.form-preview {
  max-width: 420px;
  margin-top: 24px;
}
</style>
