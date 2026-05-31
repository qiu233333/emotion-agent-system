/**
 * 情绪日记 API 模块。
 *
 * 该文件只负责封装情绪日记相关后端接口，页面组件不需要直接写 URL。
 */
import request from '../utils/request'

/**
 * 新增情绪日记。
 *
 * @param {Object} data 日记表单数据
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function addDiary(data) {
  return request.post('/diary', data)
}

/**
 * 查询情绪日记列表。
 *
 * @param {Object} params 查询参数，当前后端暂时可不传
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function getDiaryList(params) {
  return request.get('/diary/list', { params })
}

/**
 * 查询情绪日记详情。
 *
 * @param {number|string} id 日记 ID
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function getDiaryDetail(id) {
  return request.get(`/diary/${id}`)
}

/**
 * 修改情绪日记。
 *
 * @param {number|string} id 日记 ID
 * @param {Object} data 修改后的日记数据
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function updateDiary(id, data) {
  return request.put(`/diary/${id}`, data)
}

/**
 * 删除情绪日记。
 *
 * @param {number|string} id 日记 ID
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function deleteDiary(id) {
  return request.delete(`/diary/${id}`)
}
