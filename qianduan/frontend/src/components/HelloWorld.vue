<script setup lang="ts">
/**
 * Vue 模板欢迎标题组件。
 *
 * 该组件来自 Vite 默认模板，用于展示一段标题文字。
 */
defineProps<{
  /** 页面主标题文本。 */
  msg: string
}>()
</script>

<template>
  <!-- 欢迎标题和 Vite/Vue 链接说明。 -->
  <div class="greetings">
    <h1 class="green">{{ msg }}</h1>
    <h3>
      You’ve successfully created a project with
      <a href="https://vite.dev/" target="_blank" rel="noopener">Vite</a> +
      <a href="https://vuejs.org/" target="_blank" rel="noopener">Vue 3</a>.
    </h3>
  </div>
</template>

<style scoped>
h1 {
  font-weight: 500;
  font-size: 2.6rem;
  position: relative;
  top: -10px;
}

h3 {
  font-size: 1.2rem;
}

.greetings h1,
.greetings h3 {
  text-align: center;
}

@media (min-width: 1024px) {
  .greetings h1,
  .greetings h3 {
    text-align: left;
  }
}
</style>
