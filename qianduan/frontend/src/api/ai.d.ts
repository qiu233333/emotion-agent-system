/**
 * AI 接口 API 的 TypeScript 声明模块。
 *
 * ai.js 使用 JavaScript 编写；这里补充声明，方便 Vue 单文件组件安全导入。
 */
declare module '@/api/ai' {
  /**
   * 调用 AI 情绪建议接口。
   *
   * @param data 建议生成参数
   * @returns 后端统一 Result 响应
   */
  export function generateAiSuggestion(data: Record<string, unknown>): Promise<unknown>

  /**
   * 调用 AI 陪伴对话接口。
   *
   * @param data 对话参数
   * @returns 后端统一 Result 响应
   */
  export function chatWithAi(data: Record<string, unknown>): Promise<unknown>
}
