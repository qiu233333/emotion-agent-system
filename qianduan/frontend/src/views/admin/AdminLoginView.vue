<script setup lang="ts">
/**
 * 后台登录页面。
 *
 * 管理员在该页面调用 /api/admin/auth/login 登录后台，登录成功后保存 adminToken
 * 和 adminInfo，并进入后台首页。
 */
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { adminLogin } from '@/api/admin'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * 管理员登录成功后的返回数据。
 */
interface AdminLoginData {
  token: string
  userId: number
  username: string
  nickname?: string
  avatar?: string
  email?: string
  role?: string
  status?: number
}

const route = useRoute()
const router = useRouter()

// 后台登录表单数据，字段名和后端 UserLoginRequest 保持一致。
const loginForm = reactive({
  username: '',
  password: '',
})

// 登录按钮 loading 状态，避免重复提交。
const loginLoading = ref(false)

/**
 * 清空后台登录表单。
 */
function resetLoginForm() {
  loginForm.username = ''
  loginForm.password = ''
}

/**
 * 获取后台登录成功后的跳转地址。
 *
 * @returns 登录前访问的后台地址；没有记录时返回后台首页
 */
function getRedirectPath() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/admin') && redirect !== '/admin/login') {
    return redirect
  }
  return '/admin/dashboard'
}

/**
 * 提交后台登录请求。
 *
 * 登录成功后保存后台专用 token 和管理员信息，再跳转到目标后台页面。
 */
async function submitLogin() {
  if (!loginForm.username.trim() || !loginForm.password.trim()) {
    ElMessage.warning('请输入管理员账号和密码')
    return
  }

  loginLoading.value = true

  try {
    const result = (await adminLogin({
      username: loginForm.username,
      password: loginForm.password,
    })) as Result<AdminLoginData>

    if (result.code === 200 && result.data?.token) {
      localStorage.setItem('adminToken', result.data.token)
      localStorage.setItem('adminInfo', JSON.stringify(result.data))
      ElMessage.success('后台登录成功')
      router.push(getRedirectPath())
    } else {
      ElMessage.error(result.message || '后台登录失败')
    }
  } catch (error) {
    ElMessage.error('后台登录失败')
  } finally {
    loginLoading.value = false
  }
}
</script>

<template>
  <!-- 后台登录页：独立于普通用户登录入口。 -->
  <main class="admin-login-page">
    <section class="login-panel">
      <div class="login-heading">
        <h1>后台管理登录</h1>
        <p>管理员账号登录后可管理用户与情绪日记数据。</p>
      </div>

      <el-form label-width="86px" class="login-form" @keyup.enter="submitLogin">
        <el-form-item label="管理员">
          <el-input v-model="loginForm.username" placeholder="请输入管理员用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" placeholder="请输入密码" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loginLoading" @click="submitLogin">登录后台</el-button>
          <el-button @click="resetLoginForm">重置</el-button>
          <el-button link type="primary" @click="router.push('/login')">返回用户登录</el-button>
        </el-form-item>
      </el-form>
    </section>
  </main>
</template>

<style scoped>
/* 后台登录页背景：使用独立深色标题区，和普通用户端登录页区分。 */
.admin-login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #111827;
}

/* 登录面板：固定最大宽度，保证表单在桌面和移动端都易读。 */
.login-panel {
  width: min(480px, 100%);
  padding: 32px;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 24px 64px rgb(0 0 0 / 24%);
}

/* 登录页标题区域。 */
.login-heading h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
  line-height: 1.25;
}

.login-heading p {
  margin: 10px 0 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.7;
}

/* 后台登录表单。 */
.login-form {
  margin-top: 28px;
}
</style>
