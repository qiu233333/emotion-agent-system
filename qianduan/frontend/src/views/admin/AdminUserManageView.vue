<script setup lang="ts">
/**
 * 后台用户管理页面。
 *
 * 管理员可以分页查询用户、新增用户、修改用户资料、重置密码和禁用用户。
 * 禁用用户使用 DELETE 接口，但后端实际只会把 status 改为 0。
 */
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createAdminUser,
  disableAdminUser,
  getAdminUsers,
  resetAdminUserPassword,
  updateAdminUser,
} from '@/api/admin'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * 后台分页响应结构。
 */
interface PageData<T = unknown> {
  records: T[]
  total: number
  page: number
  size: number
}

/**
 * 后台用户表格记录。
 */
interface AdminUserRecord {
  id: number
  username: string
  nickname?: string
  avatar?: string
  email?: string
  role?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户表单结构。
 */
interface UserForm {
  id?: number
  username: string
  password: string
  nickname: string
  avatar: string
  email: string
  role: string
  status: number
}

// 用户筛选条件。
const queryForm = reactive({
  username: '',
  role: '',
  status: '' as '' | number,
})

// 用户分页状态。
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 用户表格数据源。
const userList = ref<AdminUserRecord[]>([])

// 用户表格加载状态。
const loading = ref(false)

// 用户新增/编辑弹窗状态。
const userDialogVisible = ref(false)

// 用户弹窗模式。
const userDialogMode = ref<'create' | 'edit'>('create')

// 用户新增/编辑表单。
const userForm = reactive<UserForm>({
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  email: '',
  role: 'user',
  status: 1,
})

// 用户表单提交 loading 状态。
const userSubmitLoading = ref(false)

// 重置密码弹窗状态。
const passwordDialogVisible = ref(false)

// 当前正在重置密码的用户。
const passwordTarget = ref<AdminUserRecord | null>(null)

// 重置密码表单。
const passwordForm = reactive({
  password: '',
})

// 重置密码提交 loading 状态。
const passwordSubmitLoading = ref(false)

/**
 * 组装用户列表查询参数。
 *
 * @returns 后台用户分页接口参数
 */
function buildUserQueryParams() {
  const params: Record<string, string | number> = {
    page: pagination.page,
    size: pagination.size,
  }

  if (queryForm.username.trim()) {
    params.username = queryForm.username.trim()
  }
  if (queryForm.role) {
    params.role = queryForm.role
  }
  if (queryForm.status !== '') {
    params.status = queryForm.status
  }

  return params
}

/**
 * 查询后台用户列表。
 */
async function loadUsers() {
  loading.value = true

  try {
    const result = (await getAdminUsers(buildUserQueryParams())) as Result<PageData<AdminUserRecord>>

    if (result.code === 200) {
      userList.value = result.data?.records || []
      pagination.total = result.data?.total || 0
      pagination.page = result.data?.page || pagination.page
      pagination.size = result.data?.size || pagination.size
    } else {
      ElMessage.error(result.message || '查询用户失败')
    }
  } catch (error) {
    ElMessage.error('查询用户失败')
  } finally {
    loading.value = false
  }
}

/**
 * 重置用户筛选条件并重新查询。
 */
function resetQuery() {
  queryForm.username = ''
  queryForm.role = ''
  queryForm.status = ''
  pagination.page = 1
  loadUsers()
}

/**
 * 重置用户新增/编辑表单。
 */
function resetUserForm() {
  userForm.id = undefined
  userForm.username = ''
  userForm.password = ''
  userForm.nickname = ''
  userForm.avatar = ''
  userForm.email = ''
  userForm.role = 'user'
  userForm.status = 1
}

/**
 * 打开新增用户弹窗。
 */
function openCreateDialog() {
  userDialogMode.value = 'create'
  resetUserForm()
  userDialogVisible.value = true
}

/**
 * 打开编辑用户弹窗。
 *
 * @param row 当前表格行用户
 */
function openEditDialog(row: AdminUserRecord) {
  userDialogMode.value = 'edit'
  userForm.id = row.id
  userForm.username = row.username
  userForm.password = ''
  userForm.nickname = row.nickname || ''
  userForm.avatar = row.avatar || ''
  userForm.email = row.email || ''
  userForm.role = row.role || 'user'
  userForm.status = row.status ?? 1
  userDialogVisible.value = true
}

/**
 * 提交新增或编辑用户表单。
 */
async function submitUserForm() {
  if (userDialogMode.value === 'create' && (!userForm.username.trim() || !userForm.password.trim())) {
    ElMessage.warning('新增用户时用户名和密码不能为空')
    return
  }

  userSubmitLoading.value = true

  try {
    const payload = {
      username: userForm.username,
      password: userForm.password,
      nickname: userForm.nickname,
      avatar: userForm.avatar,
      email: userForm.email,
      role: userForm.role,
      status: userForm.status,
    }

    const result =
      userDialogMode.value === 'create'
        ? ((await createAdminUser(payload)) as Result<AdminUserRecord>)
        : ((await updateAdminUser(userForm.id as number, {
            nickname: userForm.nickname,
            avatar: userForm.avatar,
            email: userForm.email,
            role: userForm.role,
            status: userForm.status,
          })) as Result<AdminUserRecord>)

    if (result.code === 200) {
      ElMessage.success(userDialogMode.value === 'create' ? '用户新增成功' : '用户修改成功')
      userDialogVisible.value = false
      loadUsers()
    } else {
      ElMessage.error(result.message || '保存用户失败')
    }
  } catch (error) {
    ElMessage.error('保存用户失败')
  } finally {
    userSubmitLoading.value = false
  }
}

/**
 * 打开重置密码弹窗。
 *
 * @param row 当前表格行用户
 */
function openPasswordDialog(row: AdminUserRecord) {
  passwordTarget.value = row
  passwordForm.password = ''
  passwordDialogVisible.value = true
}

/**
 * 提交重置密码请求。
 */
async function submitPasswordForm() {
  if (!passwordTarget.value?.id || !passwordForm.password.trim()) {
    ElMessage.warning('请输入新密码')
    return
  }

  passwordSubmitLoading.value = true

  try {
    const result = (await resetAdminUserPassword(passwordTarget.value.id, {
      password: passwordForm.password,
    })) as Result

    if (result.code === 200) {
      ElMessage.success('密码重置成功')
      passwordDialogVisible.value = false
    } else {
      ElMessage.error(result.message || '密码重置失败')
    }
  } catch (error) {
    ElMessage.error('密码重置失败')
  } finally {
    passwordSubmitLoading.value = false
  }
}

/**
 * 禁用指定用户。
 *
 * @param row 当前表格行用户
 */
async function disableUser(row: AdminUserRecord) {
  if (row.status === 0) {
    ElMessage.info('该用户已是禁用状态')
    return
  }

  try {
    await ElMessageBox.confirm(`确认禁用用户 ${row.username} 吗？`, '禁用用户', {
      confirmButtonText: '确认禁用',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const result = (await disableAdminUser(row.id)) as Result
    if (result.code === 200) {
      ElMessage.success('用户已禁用')
      loadUsers()
    } else {
      ElMessage.error(result.message || '禁用用户失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('禁用用户失败')
    }
  }
}

/**
 * 根据角色返回中文标签。
 *
 * @param role 用户角色
 * @returns 中文角色名称
 */
function getRoleLabel(role?: string) {
  return role === 'admin' ? '管理员' : '普通用户'
}

/**
 * 根据用户状态返回中文标签。
 *
 * @param status 用户状态
 * @returns 中文状态名称
 */
function getStatusLabel(status?: number) {
  return status === 0 ? '禁用' : '正常'
}

/**
 * 页码变化时重新查询。
 *
 * @param page 新页码
 */
function handlePageChange(page: number) {
  pagination.page = page
  loadUsers()
}

/**
 * 每页数量变化时重新查询。
 *
 * @param size 新每页数量
 */
function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadUsers()
}

// 页面加载后查询用户列表。
onMounted(loadUsers)
</script>

<template>
  <!-- 后台用户管理页：提供用户完整基础资料管理。 -->
  <el-card class="page-card">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-description">新增用户、修改资料、重置密码，并通过禁用状态控制账号访问。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
    </div>

    <el-form class="query-form" :model="queryForm" inline @keyup.enter="loadUsers">
      <el-form-item label="用户名">
        <el-input v-model="queryForm.username" clearable placeholder="模糊搜索用户名" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="queryForm.role" clearable placeholder="全部角色" class="filter-select">
          <el-option label="普通用户" value="user" />
          <el-option label="管理员" value="admin" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" clearable placeholder="全部状态" class="filter-select">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadUsers">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList" border class="data-table">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="avatar" label="头像地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'warning' : 'info'" effect="plain">
            {{ getRoleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'danger' : 'success'" effect="plain">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" @click="openPasswordDialog(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="disableUser(row)">禁用</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog
      v-model="userDialogVisible"
      :title="userDialogMode === 'create' ? '新增用户' : '编辑用户'"
      width="560px"
    >
      <el-form label-width="92px" class="dialog-form">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" :disabled="userDialogMode === 'edit'" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="userDialogMode === 'create'" label="密码">
          <el-input v-model="userForm.password" type="password" show-password placeholder="请输入初始密码" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像地址">
          <el-input v-model="userForm.avatar" placeholder="请输入头像地址" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" class="full-control">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="userForm.status" class="full-control">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="userSubmitLoading" @click="submitUserForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="460px">
      <el-form label-width="92px" @keyup.enter="submitPasswordForm">
        <el-form-item label="用户">
          <el-input :model-value="passwordTarget?.username || ''" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSubmitLoading" @click="submitPasswordForm">确认重置</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
/* 页面标题区域。 */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

/* 查询条件区域。 */
.query-form {
  margin-top: 24px;
}

/* 筛选选择器固定宽度。 */
.filter-select {
  width: 150px;
}

/* 用户数据表格。 */
.data-table {
  margin-top: 8px;
}

/* 表格操作按钮区域。 */
.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.table-actions .el-button {
  margin-left: 0;
}

/* 分页区域。 */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

/* 弹窗表单控件。 */
.dialog-form {
  padding-right: 12px;
}

.full-control {
  width: 100%;
}
</style>
