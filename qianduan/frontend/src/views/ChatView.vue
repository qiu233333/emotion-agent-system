<script setup lang="ts">
/**
 * AI 陪伴页面。
 *
 * 用户输入一句话后会调用后端 POST /api/ai/chat，由后端再调用大语言模型
 * 生成温和简短的陪伴式回复。
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

import { chatWithAi } from '@/api/ai'

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

// 当前输入框内容。
const messageText = ref('')

// 消息列表，默认给一个轻量提示，说明可以直接倾诉。
const messages = ref<ChatMessage[]>([
  {
    role: 'assistant',
    content: '你可以把现在的感受告诉我，我会尽量用温和、简短的方式陪你梳理一下。',
  },
])

// 发送按钮 loading 状态，避免重复提交。
const sending = ref(false)

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
  messageText.value = ''
  sending.value = true

  try {
    const result = (await chatWithAi({ message: text })) as Result<AiChatData>

    if (result.code === 200) {
      messages.value.push({
        role: 'assistant',
        content: result.data?.reply || '我听到了。你可以先慢慢呼吸一下，再告诉我更多细节。',
      })
    } else {
      ElMessage.error(result.message || 'AI 回复失败')
    }
  } catch (error) {
    ElMessage.error('AI 回复失败')
  } finally {
    sending.value = false
  }
}
</script>

<template>
  <!-- AI陪伴页：调用后端大语言模型接口进行简单单轮陪伴对话。 -->
  <el-card class="page-card">
    <h2 class="page-title">AI陪伴</h2>
    <p class="page-description">和 AI 简短聊聊当前的心情，回复会尽量温和、具体、可执行。</p>

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
</style>
