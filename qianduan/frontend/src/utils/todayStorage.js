/**
 * 今日数据本地存储工具模块。
 *
 * 该模块只负责把“今日日记草稿”和“今日 AI 陪伴对话”保存到浏览器 localStorage。
 * 存储 key 按当前用户和本地日期隔离，避免不同用户或不同日期的数据互相覆盖。
 */

const DIARY_DRAFT_PREFIX = 'diary_draft'
const CHAT_HISTORY_PREFIX = 'chat_history'
const GUEST_USER_ID = 'guest'
const DAY_MILLISECONDS = 24 * 60 * 60 * 1000

/**
 * 安全获取浏览器 localStorage。
 *
 * @returns {Storage | null} localStorage 可用时返回实例，不可用时返回 null
 */
function getSafeStorage() {
  try {
    if (typeof window === 'undefined' || !window.localStorage) {
      return null
    }

    return window.localStorage
  } catch (error) {
    return null
  }
}

/**
 * 获取当前本地日期字符串。
 *
 * 使用浏览器本地时区拼接 yyyy-MM-dd，避免直接使用 toISOString 导致 UTC 日期偏移。
 *
 * @param {Date} date 待格式化日期
 * @returns {string} yyyy-MM-dd 格式的本地日期
 */
function formatLocalDate(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 把 yyyy-MM-dd 文本解析成本地日期零点。
 *
 * @param {string} dateText yyyy-MM-dd 格式日期文本
 * @returns {Date | null} 解析成功返回本地日期，失败返回 null
 */
function parseLocalDate(dateText) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(dateText)
  if (!match) {
    return null
  }

  const year = Number(match[1])
  const month = Number(match[2])
  const day = Number(match[3])
  const parsedDate = new Date(year, month - 1, day)

  if (
    parsedDate.getFullYear() !== year ||
    parsedDate.getMonth() !== month - 1 ||
    parsedDate.getDate() !== day
  ) {
    return null
  }

  return parsedDate
}

/**
 * 获取当前登录用户 ID。
 *
 * 优先从 localStorage.userInfo.userId 读取；缺失、解析失败或为空时使用 guest 兜底。
 *
 * @returns {string} 当前用户 ID 或 guest
 */
function getCurrentUserId() {
  const storage = getSafeStorage()
  if (!storage) {
    return GUEST_USER_ID
  }

  try {
    const userInfoText = storage.getItem('userInfo')
    if (!userInfoText) {
      return GUEST_USER_ID
    }

    const userInfo = JSON.parse(userInfoText)
    const userId = userInfo?.userId

    if (userId === undefined || userId === null || String(userId).trim() === '') {
      return GUEST_USER_ID
    }

    return String(userId)
  } catch (error) {
    return GUEST_USER_ID
  }
}

/**
 * 拼接今日存储 key。
 *
 * @param {string} prefix 存储类型前缀
 * @returns {string} 按 userId 和本地日期隔离的 localStorage key
 */
function buildTodayKey(prefix) {
  return `${prefix}_${getCurrentUserId()}_${formatLocalDate()}`
}

/**
 * 从存储 key 末尾读取日期。
 *
 * @param {string} key localStorage key
 * @param {string} prefix 存储类型前缀
 * @returns {Date | null} key 中的本地日期，无法解析时返回 null
 */
function getDateFromStorageKey(key, prefix) {
  const expectedPrefix = `${prefix}_`
  if (!key.startsWith(expectedPrefix)) {
    return null
  }

  const dateText = key.slice(-10)
  return parseLocalDate(dateText)
}

/**
 * 写入带保存时间的本地存储数据。
 *
 * localStorage 不可用、空间不足或浏览器拒绝写入时静默跳过，避免影响页面主流程。
 *
 * @param {string} key localStorage key
 * @param {unknown} data 需要保存的业务数据
 */
function writeStorageData(key, data) {
  const storage = getSafeStorage()
  if (!storage) {
    return
  }

  try {
    storage.setItem(
      key,
      JSON.stringify({
        data,
        savedAt: new Date().toISOString(),
      }),
    )
  } catch (error) {
    // localStorage 写入失败不阻断用户继续填写或聊天。
  }
}

/**
 * 读取本地存储中的业务数据。
 *
 * 如果 JSON 解析失败或结构不符合预期，会删除异常 key 并返回 null。
 *
 * @param {string} key localStorage key
 * @returns {unknown | null} 读取到的业务数据；不存在或异常时返回 null
 */
function readStorageData(key) {
  const storage = getSafeStorage()
  if (!storage) {
    return null
  }

  try {
    const storageText = storage.getItem(key)
    if (!storageText) {
      return null
    }

    const parsedValue = JSON.parse(storageText)
    if (!parsedValue || typeof parsedValue !== 'object' || !Object.hasOwn(parsedValue, 'data')) {
      storage.removeItem(key)
      return null
    }

    return parsedValue.data
  } catch (error) {
    try {
      storage.removeItem(key)
    } catch (removeError) {
      // 删除异常数据失败时也不影响页面继续运行。
    }

    return null
  }
}

/**
 * 删除指定本地存储 key。
 *
 * 删除失败时静默处理，避免浏览器 localStorage 限制影响业务页面。
 *
 * @param {string} key localStorage key
 */
function removeStorageData(key) {
  const storage = getSafeStorage()
  if (!storage) {
    return
  }

  try {
    storage.removeItem(key)
  } catch (error) {
    // 删除失败不影响页面主流程。
  }
}

/**
 * 清理指定前缀下的过期本地数据。
 *
 * 只扫描并删除当前模块拥有的前缀 key，保留 localStorage 中其他业务数据。
 *
 * @param {string} prefix 存储类型前缀
 * @param {number} retentionDays 保留天数，超过该天数的日期 key 会被删除
 */
function cleanupExpiredEntries(prefix, retentionDays) {
  const storage = getSafeStorage()
  if (!storage) {
    return
  }

  const today = parseLocalDate(formatLocalDate())
  if (!today) {
    return
  }

  const keys = []
  for (let index = 0; index < storage.length; index += 1) {
    const key = storage.key(index)
    if (key?.startsWith(`${prefix}_`)) {
      keys.push(key)
    }
  }

  keys.forEach((key) => {
    const keyDate = getDateFromStorageKey(key, prefix)
    if (!keyDate) {
      removeStorageData(key)
      return
    }

    const ageDays = Math.floor((today.getTime() - keyDate.getTime()) / DAY_MILLISECONDS)
    if (ageDays > retentionDays) {
      removeStorageData(key)
    }
  })
}

/**
 * 读取今日未提交日记草稿。
 *
 * @returns {unknown | null} 当前用户今日草稿数据；不存在时返回 null
 */
export function readTodayDiaryDraft() {
  return readStorageData(buildTodayKey(DIARY_DRAFT_PREFIX))
}

/**
 * 保存今日未提交日记草稿。
 *
 * @param {unknown} draft 当前日记表单草稿
 */
export function saveTodayDiaryDraft(draft) {
  writeStorageData(buildTodayKey(DIARY_DRAFT_PREFIX), draft)
}

/**
 * 删除今日未提交日记草稿。
 */
export function removeTodayDiaryDraft() {
  removeStorageData(buildTodayKey(DIARY_DRAFT_PREFIX))
}

/**
 * 清理超过 7 天的日记草稿。
 */
export function cleanupExpiredDiaryDrafts() {
  cleanupExpiredEntries(DIARY_DRAFT_PREFIX, 7)
}

/**
 * 读取今日 AI 陪伴对话记录。
 *
 * @returns {unknown | null} 当前用户今日 AI 对话记录；不存在时返回 null
 */
export function readTodayChatHistory() {
  return readStorageData(buildTodayKey(CHAT_HISTORY_PREFIX))
}

/**
 * 保存今日 AI 陪伴对话记录。
 *
 * @param {unknown} messages 当前 AI 陪伴消息列表
 */
export function saveTodayChatHistory(messages) {
  writeStorageData(buildTodayKey(CHAT_HISTORY_PREFIX), messages)
}

/**
 * 删除今日 AI 陪伴对话记录。
 */
export function removeTodayChatHistory() {
  removeStorageData(buildTodayKey(CHAT_HISTORY_PREFIX))
}

/**
 * 清理超过 30 天的 AI 陪伴对话记录。
 */
export function cleanupExpiredChatHistories() {
  cleanupExpiredEntries(CHAT_HISTORY_PREFIX, 30)
}
