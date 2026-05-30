<template>
  <!-- 登录页：展示基础账号密码表单，目前只做静态结构。 -->
  <el-card class="page-card">
    <h2 class="page-title">登录页</h2>
    <p class="page-description">用于用户输入账号和密码进入系统。当前仅搭建静态页面，不包含真实登录逻辑。</p>

    <el-form label-width="80px" class="form-preview">
      <el-form-item label="用户名">
        <el-input placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input placeholder="请输入密码" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">登录</el-button>
        <el-button>重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
/* 表单预览区域：限制宽度，避免输入框在大屏中过长。 */
.form-preview {
  max-width: 420px;
  margin-top: 24px;
}
</style>
