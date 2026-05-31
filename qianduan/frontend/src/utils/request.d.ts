/**
 * Axios 请求工具的 TypeScript 声明模块。
 *
 * request.js 使用 JavaScript 编写；这里给 TypeScript 文件提供默认导出声明，
 * 避免旧的 TS API 文件导入统一请求实例时报隐式 any 错误。
 */
import type { AxiosInstance } from 'axios'

declare const request: AxiosInstance

export default request
