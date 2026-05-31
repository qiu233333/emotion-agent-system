/**
 * Axios 请求工具模块。
 *
 * 该文件统一配置前端请求后端接口的基础地址、超时时间和拦截器。
 * 后续需要携带登录 token、统一处理错误提示时，都优先在这里扩展。
 */
import axios from 'axios'

/**
 * 创建 Axios 实例。
 *
 * baseURL 使用 /api，开发环境下会通过 Vite 代理转发到 Spring Boot 后端。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

/**
 * 请求拦截器。
 *
 * 当前先预留 token 自动携带逻辑：如果 localStorage 中存在 token，
 * 就把它放到 Authorization 请求头中。暂时不做登录注册时，不会影响普通请求。
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')

    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error),
)

/**
 * 响应拦截器。
 *
 * 后端统一返回 Result 对象时，response.data 就是 { code, message, data }。
 * 这里统一返回 response.data，页面和 API 调用方不用再手动取 response.data。
 */
request.interceptors.response.use(
  (response) => response.data,
  (error) => Promise.reject(error),
)

export default request
