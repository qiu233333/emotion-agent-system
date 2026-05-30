/**
 * axios 请求基础配置模块。
 *
 * 前端所有后端接口请求建议统一从这里创建的 request 实例发出，
 * 这样后续添加 token、错误处理、超时时间等配置时，只需要改这一个文件。
 */
import axios from 'axios'

// baseURL 使用 /api，开发环境下会通过 Vite 代理转发到 Spring Boot 后端。
const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
})

export default request
