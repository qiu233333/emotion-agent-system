<script setup lang="ts">
/**
 * AI 陪伴页面。
 *
 * 用户输入一句话后会调用后端 POST /api/ai/chat，由后端调用大语言模型生成回复。
 * 页面会保留今日对话，并支持把某条消息快速整理成今日日记草稿。
 */
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { chatWithAi } from '@/api/ai'
import {
  cleanupExpiredChatHistories,
  readTodayChatHistory,
  removeTodayChatHistory,
  saveTodayChatHistory,
  saveTodayDiaryDraft,
} from '@/utils/todayStorage'
import { formatText, getEmotionTypeLabel, type Result } from '@/utils/emotion'

/**
 * AI 陪伴接口返回数据。
 */
interface AiChatData {
  reply?: string
  emotionType?: string
  riskFlag?: boolean
}

/**
 * 聊天消息数据。
 */
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  emotionType?: string
  riskFlag?: boolean
}

const router = useRouter()

// 聊天滚动容器，用于新消息出现后自动滚动到底部。
const chatBoxRef = ref<HTMLElement | null>(null)

// 当前输入框内容。
const messageText = ref('')

// 消息列表，默认给一个轻量提示，说明可以直接倾诉。
const messages = ref<ChatMessage[]>(createDefaultMessages())

// 发送按钮 loading 状态，避免重复提交。
const sending = ref(false)

// 常用开场文本，帮助用户在不知道如何开始时快速输入。
const promptChips = ['今天有点累', '我想整理一下焦虑', '刚才发生了一件小事', '我想把心情写成日记']

/**
 * 创建默认欢迎消息。
 *
 * 每次恢复默认对话时都返回新的数组，避免多个状态共用同一个引用。
 *
 * @returns 默认消息数组
 */
function createDefaultMessages(): ChatMessage[] {
  return [
    {
      role: 'assistant',
      content: '你可以把现在的感受告诉我。我会尽量用温和、简短的方式陪你梳理一下。',
    },
  ]
}

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
    scrollToBottom()
    return
  }

  if (history) {
    removeTodayChatHistory()
  }
  messages.value = createDefaultMessages()
  scrollToBottom()
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
  scrollToBottom()
}

/**
 * 把快捷开场写入输入框。
 *
 * @param prompt 预设开场文本
 */
function usePrompt(prompt: string) {
  messageText.value = prompt
}

/**
 * 把指定消息内容保存为今日日记草稿。
 *
 * @param message 待整理的聊天消息
 */
function saveMessageAsDiaryDraft(message: ChatMessage) {
  saveTodayDiaryDraft({
    title: '来自 AI 陪伴的记录',
    content: message.content,
    moodTag: message.emotionType || '',
    selfScore: 3,
  })
  ElMessage.success('已整理为今日日记草稿')
  router.push('/diary')
}

/**
 * 滚动聊天窗口到底部。
 */
async function scrollToBottom() {
  await nextTick()
  if (chatBoxRef.value) {
    chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
  }
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
  scrollToBottom()

  try {
    const result = (await chatWithAi({ message: text })) as Result<AiChatData>

    if (result.code === 200) {
      messages.value.push({
        role: 'assistant',
        content: result.data?.reply || '我听到了。你可以先慢慢呼吸一下，再告诉我更多细节。',
        emotionType: result.data?.emotionType,
        riskFlag: result.data?.riskFlag,
      })
      saveCurrentChatHistory()
      scrollToBottom()
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
  <!-- AI 陪伴页：消息区和输入区组成完整倾诉入口。 -->
  <section class="chat-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">AI 陪伴</h1>
        <p class="page-description">把现在的心情说出来，先整理一个小片刻。</p>
      </div>
      <el-button size="small" @click="clearTodayChatHistory">清空今日对话</el-button>
    </div>

    <section class="soft-panel chat-shell">
      <div ref="chatBoxRef" class="chat-box">
        <div v-for="(message, index) in messages" :key="index" class="chat-row" :class="message.role">
          <div class="chat-bubble-wrap">
            <div class="chat-bubble">
              {{ message.content }}
            </div>

            <div v-if="message.role === 'assistant'" class="message-meta">
              <el-tag v-if="message.emotionType" size="small" effect="plain">
                识别为 {{ getEmotionTypeLabel(message.emotionType) }}
              </el-tag>
              <el-tag v-if="message.riskFlag" size="small" type="warning" effect="plain">
                需要多一点支持
              </el-tag>
            </div>

            <div class="message-actions">
              <el-button size="small" text type="primary" @click="saveMessageAsDiaryDraft(message)">
                记录成日记
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="prompt-row">
        <button v-for="prompt in promptChips" :key="prompt" type="button" @click="usePrompt(prompt)">
          {{ prompt }}
        </button>
      </div>

      <div class="chat-input">
        <el-input
          v-model="messageText"
          type="textarea"
          :rows="3"
          placeholder="输入你想和 AI 聊的内容"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <el-button type="primary" size="large" :loading="sending" @click="sendMessage">发送</el-button>
      </div>

      <p class="chat-note">{{ formatText('AI 回复用于日常情绪整理，重要感受也可以告诉身边可信任的人。') }}</p>
    </section>
  </section>
</template>

<style scoped>
/* AI 陪伴页面整体：头部和聊天面板纵向排列。 */
.chat-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* 聊天外壳：固定最小高度，输入区保持在面板底部。 */
.chat-shell {
  display: grid;
  grid-template-rows: minmax(360px, 1fr) auto auto auto;
  min-height: calc(100vh - 210px);
  padding: 18px;
}

/* 聊天消息区域：限制滚动范围，避免长对话撑开页面。 */
.chat-box {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 360px;
  max-height: 560px;
  padding: 8px 6px 18px;
  overflow-y: auto;
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

.chat-bubble-wrap {
  max-width: min(720px, 82%);
}

/* 消息气泡：固定最大宽度，保证文本可读。 */
.chat-bubble {
  padding: 12px 14px;
  border-radius: 8px;
  color: #203330;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-row.user .chat-bubble {
  background: #dff4f0;
}

.chat-row.assistant .chat-bubble {
  border: 1px solid #e2ece9;
  background: #ffffff;
}

.message-meta,
.message-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
}

.chat-row.user .message-meta,
.chat-row.user .message-actions {
  justify-content: flex-end;
}

/* 快捷开场：小按钮帮助用户开始表达。 */
.prompt-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 0 12px;
  border-top: 1px solid #e8efed;
}

.prompt-row button {
  padding: 7px 10px;
  border: 1px solid #d7e5e2;
  border-radius: 8px;
  background: #ffffff;
  color: #49645f;
  cursor: pointer;
}

.prompt-row button:hover {
  border-color: #1f9d8a;
  color: #126253;
}

/* 输入区域：文本框占主要空间，按钮保持固定宽度。 */
.chat-input {
  position: sticky;
  bottom: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 100px;
  gap: 12px;
  align-items: stretch;
  padding-top: 8px;
  background: rgba(255, 255, 255, 0.94);
}

.chat-note {
  margin: 10px 0 0;
  color: #778a85;
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 640px) {
  /* 小屏下气泡和输入区减少横向压力。 */
  .chat-shell {
    min-height: calc(100vh - 180px);
    padding: 14px;
  }

  .chat-box {
    min-height: 340px;
  }

  .chat-bubble-wrap {
    max-width: 92%;
  }

  .chat-input {
    grid-template-columns: 1fr;
  }
}
</style>
