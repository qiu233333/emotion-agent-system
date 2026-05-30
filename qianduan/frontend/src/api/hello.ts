/**
 * hello 接口模块。
 *
 * 当前用于验证前端 axios 与后端 Spring Boot 接口是否已经连通。
 */
import request from './request'

/**
 * 请求后端 /hello 测试接口。
 *
 * @returns 后端返回的字符串响应，正常情况下为 "Hello World"
 */
export function getHello() {
  return request.get<string>('/hello')
}
