import request from './request'

export function getHello() {
  return request.get<string>('/hello')
}
