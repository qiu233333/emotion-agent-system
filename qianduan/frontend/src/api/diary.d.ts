/**
 * 情绪日记 API 类型声明模块。
 *
 * diary.js 是 JavaScript 文件；这里提供 TypeScript 声明，方便 .vue 页面
 * 使用 lang="ts" 时通过类型检查。
 */
declare module '@/api/diary' {
  /**
   * 新增情绪日记。
   *
   * @param data 日记表单数据
   */
  export function addDiary(data: Record<string, unknown>): Promise<unknown>

  /**
   * 查询情绪日记列表。
   *
   * @param params 查询参数
   */
  export function getDiaryList(params?: Record<string, unknown>): Promise<unknown>

  /**
   * 查询情绪日记详情。
   *
   * @param id 日记 ID
   */
  export function getDiaryDetail(id: number | string): Promise<unknown>

  /**
   * 修改情绪日记。
   *
   * @param id 日记 ID
   * @param data 修改后的日记数据
   */
  export function updateDiary(id: number | string, data: Record<string, unknown>): Promise<unknown>

  /**
   * 删除情绪日记。
   *
   * @param id 日记 ID
   */
  export function deleteDiary(id: number | string): Promise<unknown>
}
