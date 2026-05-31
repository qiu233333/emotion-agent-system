/**
 * 用户认证 API 的 TypeScript 声明模块。
 *
 * user.js 使用 JavaScript 编写；这里补充声明，方便 Vue 单文件组件安全导入。
 */
declare module '@/api/user' {
  /**
   * 调用登录接口。
   *
   * @param data 登录表单数据
   * @returns 后端统一 Result 响应
   */
  export function login(data: Record<string, unknown>): Promise<unknown>

  /**
   * 调用注册接口。
   *
   * @param data 注册表单数据
   * @returns 后端统一 Result 响应
   */
  export function register(data: Record<string, unknown>): Promise<unknown>
}
