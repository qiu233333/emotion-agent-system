/**
 * 后台管理 API 类型声明模块。
 *
 * admin.js 使用 JavaScript 编写；这里提供 TypeScript 声明，方便后台 Vue 页面
 * 使用 lang="ts" 时通过类型检查。
 */
declare module '@/api/admin' {
  /**
   * 管理员登录后台。
   *
   * @param data 管理员账号密码
   */
  export function adminLogin(data: Record<string, unknown>): Promise<unknown>

  /**
   * 分页查询后台用户列表。
   *
   * @param params 用户筛选和分页参数
   */
  export function getAdminUsers(params?: Record<string, unknown>): Promise<unknown>

  /**
   * 后台新增用户。
   *
   * @param data 新用户资料
   */
  export function createAdminUser(data: Record<string, unknown>): Promise<unknown>

  /**
   * 后台修改用户资料。
   *
   * @param id 用户 ID
   * @param data 待修改的用户资料
   */
  export function updateAdminUser(id: number | string, data: Record<string, unknown>): Promise<unknown>

  /**
   * 后台重置用户密码。
   *
   * @param id 用户 ID
   * @param data 新密码请求
   */
  export function resetAdminUserPassword(id: number | string, data: Record<string, unknown>): Promise<unknown>

  /**
   * 后台禁用用户。
   *
   * @param id 用户 ID
   */
  export function disableAdminUser(id: number | string): Promise<unknown>

  /**
   * 分页查询后台日记列表。
   *
   * @param params 日记筛选和分页参数
   */
  export function getAdminDiaries(params?: Record<string, unknown>): Promise<unknown>

  /**
   * 查询后台日记详情。
   *
   * @param id 日记 ID
   */
  export function getAdminDiaryDetail(id: number | string): Promise<unknown>

  /**
   * 后台新增日记。
   *
   * @param data 完整日记资料
   */
  export function createAdminDiary(data: Record<string, unknown>): Promise<unknown>

  /**
   * 后台修改日记。
   *
   * @param id 日记 ID
   * @param data 待修改的完整日记资料
   */
  export function updateAdminDiary(id: number | string, data: Record<string, unknown>): Promise<unknown>

  /**
   * 后台物理删除日记。
   *
   * @param id 日记 ID
   */
  export function deleteAdminDiary(id: number | string): Promise<unknown>
}
