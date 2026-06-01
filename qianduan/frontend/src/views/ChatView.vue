<script setup lang="ts">
/**
 * AI 陪伴页面。
 *
 * 用户输入一句话后会调用后端 POST /api/ai/chat，由后端再调用大语言模型
 * 生成温和简短的陪伴式回复。
 */
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { chatWithAi } from '@/api/ai'
import {
  cleanupExpiredChatHistories,
  readTodayChatHistory,
  removeTodayChatHistory,
  saveTodayChatHistory,
} from '@/utils/todayStorage'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

/**
 * AI 陪伴接口返回数据。
 */
interface AiChatData {
  reply?: string
}

/**
 * 聊天消息数据。
 */
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

/**
 * 创建默认欢迎消息。
 *
 * 每次恢复默认对话时都返回新的数组，避免多个状态共用同一个引用。
 */
function createDefaultMessages(): ChatMessage[] {
  return [
    {
      role: 'assistant',
      content: '你可以把现在的感受告诉我，我会尽量用温和、简短的方式陪你梳理一下。',
    },
  ]
}

// 当前输入框内容。
const messageText = ref('')

// 消息列表，默认给一个轻量提示，说明可以直接倾诉。
const messages = ref<ChatMessage[]>(createDefaultMessages())

// 发送按钮 loading 状态，避免重复提交。
const sending = ref(false)

/**
 * 判断本地读取的对象是否是合法聊天消息。
 *
 * @param message 待检查的本地消息对象
 * @returns 角色合法且内容为字符串时返回 true
 */
function isValidChatMessage(message: unknown): message is ChatMessage {
  if (!message || typeof message !== 'object') {
    return false
  }

  const candidate = message as ChatMessage
  return (candidate.role === 'user' || candidate.role === 'assistant') && typeof candidate.content === 'string'
}

/**
 * 保存当前 AI 陪伴消息列表。
 *
 * 用户消息和 AI 回复都会调用该方法，保证刷新页面时能恢复尽量完整的今日对话。
 */
function saveCurrentChatHistory() {
  saveTodayChatHistory(messages.value)
}

/**
 * 恢复当前用户今日 AI 陪伴对话。
 *
 * 页面进入时调用；有合法本地记录则恢复记录，否则展示默认欢迎语。
 */
function restoreTodayChatHistory() {
  const history = readTodayChatHistory()
  if (Array.isArray(history) && history.length > 0 && history.every(isValidChatMessage)) {
    messages.value = history
    return
  }

  if (history) {
    removeTodayChatHistory()
  }
  messages.value = createDefaultMessages()
}

/**
 * 清空当前用户今日 AI 陪伴对话。
 *
 * 删除当天本地聊天记录，清空输入框，并把页面恢复到默认欢迎语。
 */
function clearTodayChatHistory() {
  removeTodayChatHistory()
  messageText.value = ''
  messages.value = createDefaultMessages()
  ElMessage.success('今日对话已清空')
}

/**
 * 发送 AI 陪伴对话消息。
 */
async function sendMessage() {
  const text = messageText.value.trim()
  if (!text) {
    ElMessage.warning('请输入想和 AI 聊的内容')
    return
  }

  messages.value.push({
    role: 'user',
    content: text,
  })
  saveCurrentChatHistory()
  messageText.value = ''
  sending.value = true

  try {
    const result = (await chatWithAi({ message: text })) as Result<AiChatData>

    if (result.code === 200) {
      messages.value.push({
        role: 'assistant',
        content: result.data?.reply || '我听到了。你可以先慢慢呼吸一下，再告诉我更多细节。',
      })
      saveCurrentChatHistory()
    } else {
      ElMessage.error(result.message || 'AI 回复失败')
    }
  } catch (error) {
    ElMessage.error('AI 回复失败')
  } finally {
    sending.value = false
  }
}

/**
 * 初始化 AI 陪伴本地对话记录。
 *
 * 进入页面时先清理超过 30 天的旧对话，再恢复当前用户当天记录。
 */
onMounted(() => {
  cleanupExpiredChatHistories()
  restoreTodayChatHistory()
})
</script>

<template>
  <!-- AI陪伴页：调用后端大语言模型接口进行简单单轮陪伴对话。 -->
  <el-card class="page-card">
    <div class="chat-page-header">
      <div>
        <h2 class="page-title">AI陪伴</h2>
        <p class="page-description">和 AI 简短聊聊当前的心情，回复会尽量温和、具体、可执行。</p>
      </div>

      <el-button size="small" @click="clearTodayChatHistory">清空今日对话</el-button>
    </div>

    <div class="chat-box">
      <div v-for="(message, index) in messages" :key="index" class="chat-row" :class="message.role">
        <div class="chat-bubble">
          {{ message.content }}
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="messageText"
        type="textarea"
        :rows="3"
        placeholder="输入你想和 AI 聊的内容"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
    </div>
  </el-card>
</template>

<style scoped>
/* 页面标题栏：左侧展示页面说明，右侧提供今日对话清空操作。 */
.chat-page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

/* 聊天消息区域：限制高度并允许滚动，避免长对话撑开页面。 */
.chat-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 280px;
  max-height: 460px;
  margin-top: 24px;
  padding: 16px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
}

/* 单条消息行：根据角色控制气泡左右位置。 */
.chat-row {
  display: flex;
}

.chat-row.user {
  justify-content: flex-end;
}

.chat-row.assistant {
  justify-content: flex-start;
}

/* 消息气泡：固定最大宽度，保证文本可读。 */
.chat-bubble {
  max-width: min(680px, 78%);
  padding: 10px 12px;
  border-radius: 8px;
  color: #1f2937;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-row.user .chat-bubble {
  background: #dbeafe;
}

.chat-row.assistant .chat-bubble {
  background: #ffffff;
  border: 1px solid #e5e7eb;
}

/* 输入区域：文本框占主要空间，按钮保持固定宽度。 */
.chat-input {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 16px;
}

@media (max-width: 640px) {
  /* 小屏标题栏：按钮换到标题下方，避免和说明文字挤压。 */
  .chat-page-header {
    flex-direction: column;
  }
}
</style>
