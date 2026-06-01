/**
 * AI 接口 API 模块。
 *
 * 该文件封装后端大语言模型相关接口，页面组件只需要调用函数，
 * 不需要直接关心后端 URL 和请求细节。
 */
import request from '../utils/request'

/**
 * 根据日记内容或日记 ID 生成 AI 情绪建议。
 *
 * @param {Object} data 建议生成参数，常用格式为 { diaryId }
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function generateAiSuggestion(data) {
  return request.post('/ai/suggestion', data)
}

/**
 * 发送 AI 陪伴对话消息。
 *
 * @param {Object} data 对话参数，格式为 { message }
 * @returns {Promise<Object>} 后端统一 Result 响应
 */
export function chatWithAi(data) {
  return request.post('/ai/chat', data)
}
