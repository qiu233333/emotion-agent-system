/**
 * 后台管理 API 模块。
 *
 * 该文件集中封装管理员登录、用户管理和日记管理接口，页面组件不需要直接拼接 URL。
 */
import adminRequest from '@/utils/adminRequest'

/**
 * 管理员登录后台。
 *
 * @param {Object} data 管理员账号密码
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function adminLogin(data) {
  return adminRequest.post('/admin/auth/login', data)
}

/**
 * 分页查询后台用户列表。
 *
 * @param {Object} params 用户筛选和分页参数
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function getAdminUsers(params) {
  return adminRequest.get('/admin/users', { params })
}

/**
 * 后台新增用户。
 *
 * @param {Object} data 新用户资料
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function createAdminUser(data) {
  return adminRequest.post('/admin/users', data)
}

/**
 * 后台修改用户资料。
 *
 * @param {number|string} id 用户 ID
 * @param {Object} data 待修改的用户资料
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function updateAdminUser(id, data) {
  return adminRequest.put(`/admin/users/${id}`, data)
}

/**
 * 后台重置用户密码。
 *
 * @param {number|string} id 用户 ID
 * @param {Object} data 新密码请求
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function resetAdminUserPassword(id, data) {
  return adminRequest.put(`/admin/users/${id}/password`, data)
}

/**
 * 后台禁用用户。
 *
 * @param {number|string} id 用户 ID
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function disableAdminUser(id) {
  return adminRequest.delete(`/admin/users/${id}`)
}

/**
 * 分页查询后台日记列表。
 *
 * @param {Object} params 日记筛选和分页参数
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function getAdminDiaries(params) {
  return adminRequest.get('/admin/diaries', { params })
}

/**
 * 查询后台日记详情。
 *
 * @param {number|string} id 日记 ID
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function getAdminDiaryDetail(id) {
  return adminRequest.get(`/admin/diaries/${id}`)
}

/**
 * 后台新增日记。
 *
 * @param {Object} data 完整日记资料
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function createAdminDiary(data) {
  return adminRequest.post('/admin/diaries', data)
}

/**
 * 后台修改日记。
 *
 * @param {number|string} id 日记 ID
 * @param {Object} data 待修改的完整日记资料
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function updateAdminDiary(id, data) {
  return adminRequest.put(`/admin/diaries/${id}`, data)
}

/**
 * 后台物理删除日记。
 *
 * @param {number|string} id 日记 ID
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function deleteAdminDiary(id) {
  return adminRequest.delete(`/admin/diaries/${id}`)
}
