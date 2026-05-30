<script setup lang="ts">
import { onMounted, ref } from 'vue'

import { getHello } from './api/hello'

const helloMessage = ref('正在请求后端 /hello ...')
const errorMessage = ref('')

async function loadHello() {
  errorMessage.value = ''
  helloMessage.value = '正在请求后端 /hello ...'

  try {
    const response = await getHello()
    helloMessage.value = response.data
  } catch (error) {
    helloMessage.value = ''
    errorMessage.value = error instanceof Error ? error.message : '请求失败'
  }
}

onMounted(loadHello)
</script>

<template>
  <main class="page">
    <section class="panel">
      <p class="eyebrow">Axios 联调</p>
      <h1>前后端连接测试</h1>

      <div class="result" :class="{ error: errorMessage }">
        <span class="label">GET /api/hello</span>
        <strong>{{ errorMessage || helloMessage }}</strong>
      </div>

      <button type="button" @click="loadHello">重新请求</button>
    </section>
  </main>
</template>

<style scoped>
.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  background: #f6f7f9;
}

.panel {
  width: min(100%, 520px);
  padding: 28px;
  border: 1px solid #e1e5ea;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 16px 40px rgb(17 24 39 / 8%);
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 700;
}

h1 {
  margin: 0 0 22px;
  color: #1f2937;
  font-size: 28px;
  line-height: 1.2;
}

.result {
  display: grid;
  gap: 8px;
  min-height: 88px;
  padding: 16px;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  background: #eff6ff;
}

.result.error {
  border-color: #fecaca;
  background: #fef2f2;
}

.label {
  color: #4b5563;
  font-size: 13px;
}

strong {
  overflow-wrap: anywhere;
  color: #111827;
  font-size: 20px;
}

button {
  width: 100%;
  min-height: 42px;
  margin-top: 18px;
  border: 0;
  border-radius: 8px;
  background: #2563eb;
  color: #ffffff;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

button:hover {
  background: #1d4ed8;
}
</style>
