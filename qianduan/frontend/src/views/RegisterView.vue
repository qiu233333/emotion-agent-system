<template>
<<<<<<< HEAD
  <!-- 注册页：展示用户注册需要填写的基础字段。 -->
=======
>>>>>>> c12f37e6a94c8b1bb7c85f65a53ba557c69edd43
  <el-card class="page-card">
    <h2 class="page-title">注册页</h2>
    <p class="page-description">用于新用户创建账号。当前页面只展示基础表单结构，不提交数据。</p>

    <el-form label-width="80px" class="form-preview">
      <el-form-item label="用户名">
        <el-input placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input placeholder="请输入密码" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary">注册</el-button>
        <el-button>返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
<<<<<<< HEAD
/* 表单预览区域：和登录页保持一致的宽度与间距。 */
=======
>>>>>>> c12f37e6a94c8b1bb7c85f65a53ba557c69edd43
.form-preview {
  max-width: 420px;
  margin-top: 24px;
}
</style>
