/**
 * 后台管理 Axios 请求工具模块。
 *
 * 该文件专门服务后台管理端接口，和普通用户端 request 分开维护。
 * 后台请求会自动携带 adminToken，避免和普通用户 token 混用。
 */
import axios from 'axios'

/**
 * 创建后台管理 Axios 实例。
 *
 * baseURL 使用 /api，开发环境下会通过 Vite 代理转发到 Spring Boot 后端。
 */
const adminRequest = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

/**
 * 后台请求拦截器。
 *
 * 如果 localStorage 中存在 adminToken，就把它放到 Authorization 请求头中。
 */
adminRequest.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('adminToken')

    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error),
)

/**
 * 后台响应拦截器。
 *
 * 后端返回 401 或 403 时说明后台登录状态无效或权限不足，前端会清理后台登录状态，
 * 并跳转到独立后台登录页。
 */
adminRequest.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminInfo')

      if (window.location.pathname !== '/admin/login') {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search)
        window.location.href = `/admin/login?redirect=${redirect}`
      }
    }

    return Promise.reject(error)
  },
)

export default adminRequest
