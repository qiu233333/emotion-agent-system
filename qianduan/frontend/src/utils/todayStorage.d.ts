/**
 * 今日数据本地存储工具的 TypeScript 声明模块。
 *
 * todayStorage.js 使用 JavaScript 编写；这里为 Vue 单文件组件提供类型声明。
 */
declare module '@/utils/todayStorage' {
  /**
   * 今日未提交日记草稿数据。
   */
  export interface DiaryDraftData {
    title: string
    content: string
    moodTag: string
    selfScore?: number
  }

  /**
   * AI 陪伴对话消息数据。
   */
  export interface ChatHistoryMessage {
    role: 'user' | 'assistant'
    content: string
    emotionType?: string
    riskFlag?: boolean
  }

  /**
   * 读取当前用户今日未提交日记草稿。
   */
  export function readTodayDiaryDraft(): DiaryDraftData | null

  /**
   * 保存当前用户今日未提交日记草稿。
   *
   * @param draft 当前日记表单草稿
   */
  export function saveTodayDiaryDraft(draft: DiaryDraftData): void

  /**
   * 删除当前用户今日未提交日记草稿。
   */
  export function removeTodayDiaryDraft(): void

  /**
   * 清理超过 7 天的日记草稿。
   */
  export function cleanupExpiredDiaryDrafts(): void

  /**
   * 读取当前用户今日 AI 陪伴对话记录。
   */
  export function readTodayChatHistory(): ChatHistoryMessage[] | null

  /**
   * 保存当前用户今日 AI 陪伴对话记录。
   *
   * @param messages 当前 AI 陪伴消息列表
   */
  export function saveTodayChatHistory(messages: ChatHistoryMessage[]): void

  /**
   * 删除当前用户今日 AI 陪伴对话记录。
   */
  export function removeTodayChatHistory(): void

  /**
   * 清理超过 30 天的 AI 陪伴对话记录。
   */
  export function cleanupExpiredChatHistories(): void
}
