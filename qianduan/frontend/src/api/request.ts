/**
 * 兼容旧导入路径的请求模块。
 *
 * 早期 hello 接口从 src/api/request.ts 导入请求实例；现在统一复用
 * src/utils/request.js，确保所有请求都会自动携带 token 并处理 401。
 */
import request from '@/utils/request'

export default request
