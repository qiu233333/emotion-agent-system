/**
 * 用户认证 API 模块。
 *
 * 该文件只封装登录注册相关接口，页面组件不需要直接拼接后端 URL。
 */
import request from '../utils/request'

/**
 * 用户登录。
 *
 * @param {Object} data 登录表单数据
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function login(data) {
  return request.post('/user/login', data)
}

/**
 * 用户注册。
 *
 * @param {Object} data 注册表单数据
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function register(data) {
  return request.post('/user/register', data)
}
