<script setup lang="ts">
/**
 * 注册页面。
 *
 * 用户填写基础信息后调用后端 /api/user/register，注册成功后跳转到登录页。
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { register } from '@/api/user'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

const router = useRouter()

// 注册表单数据，字段名和后端注册请求对象保持一致。
const registerForm = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

// 注册按钮 loading 状态，避免重复提交。
const registerLoading = ref(false)

/**
 * 清空注册表单。
 */
function resetRegisterForm() {
  registerForm.username = ''
  registerForm.nickname = ''
  registerForm.email = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
}

/**
 * 提交注册请求。
 *
 * 这里只做基础非空和两次密码一致校验，复杂规则后续再扩展。
 */
async function submitRegister() {
  if (!registerForm.username.trim() || !registerForm.password.trim()) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  registerLoading.value = true

  try {
    const result = (await register({
      username: registerForm.username,
      nickname: registerForm.nickname,
      email: registerForm.email,
      password: registerForm.password,
    })) as Result

    if (result.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (error) {
    ElMessage.error('注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <!-- 注册页：新用户创建账号入口。 -->
  <el-card class="auth-card">
    <h2 class="page-title">注册页</h2>
    <p class="page-description">创建账号后即可登录系统，日记数据会按用户隔离保存。</p>

    <el-form label-width="90px" class="form-preview" @keyup.enter="submitRegister">
      <el-form-item label="用户名">
        <el-input v-model="registerForm.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="registerForm.nickname" placeholder="请输入昵称，可不填" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="registerForm.email" placeholder="请输入邮箱，可不填" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="registerForm.password" placeholder="请输入密码" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
          v-model="registerForm.confirmPassword"
          placeholder="请再次输入密码"
          type="password"
          show-password
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="registerLoading" @click="submitRegister">注册</el-button>
        <el-button @click="resetRegisterForm">重置</el-button>
        <el-button link type="primary" @click="router.push('/login')">返回登录</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
/* 注册页背景：让认证表单在独立页面中居中展示。 */
.auth-card {
  width: min(500px, calc(100vw - 32px));
  margin: 72px auto 0;
  border-radius: 8px;
}

/* 表单预览区域：和登录页保持一致的宽度与间距。 */
.form-preview {
  max-width: 460px;
  margin-top: 24px;
}
</style>
