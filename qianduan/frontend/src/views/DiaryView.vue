<script setup lang="ts">
/**
 * 情绪日记页面。
 *
 * 当前页面提供新增日记表单，用户填写标题、内容和心情标签后，
 * 点击提交会调用后端 POST /api/diary 接口保存数据。
 */
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { addDiary } from '@/api/diary'

/**
 * 后端统一 Result 响应结构。
 */
interface Result<T = unknown> {
  code: number
  message?: string
  data?: T
}

// 日记表单数据，字段名和后端 EmotionDiary 实体保持一致。
const diaryForm = reactive({
  title: '',
  content: '',
  moodTag: '',
})

// 提交按钮 loading 状态，避免用户连续重复提交。
const submitLoading = ref(false)

/**
 * 清空日记表单。
 *
 * 保存成功后调用，方便用户继续新增下一篇日记。
 */
function resetDiaryForm() {
  diaryForm.title = ''
  diaryForm.content = ''
  diaryForm.moodTag = ''
}

/**
 * 提交新增日记请求。
 *
 * 这里只做最基础的非空判断，不加入复杂业务规则。
 */
async function submitDiary() {
  if (!diaryForm.title.trim() || !diaryForm.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  submitLoading.value = true

  try {
    const result = (await addDiary({
      title: diaryForm.title,
      content: diaryForm.content,
      moodTag: diaryForm.moodTag,
    })) as Result

    if (result.code === 200) {
      ElMessage.success('保存成功')
      resetDiaryForm()
    } else {
      ElMessage.error(result.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <!-- 情绪日记页：提供新增日记的基础表单。 -->
  <el-card class="page-card">
    <h2 class="page-title">情绪日记</h2>
    <p class="page-description">记录今天发生的事情、心情标签和情绪感受，提交后会保存到后端数据库。</p>

    <el-form class="diary-form" label-width="90px">
      <el-form-item label="标题">
        <el-input v-model="diaryForm.title" placeholder="请输入日记标题" />
      </el-form-item>

      <el-form-item label="内容">
        <el-input
          v-model="diaryForm.content"
          placeholder="请输入今天的情绪日记内容"
          type="textarea"
          :rows="6"
        />
      </el-form-item>

      <el-form-item label="心情标签">
        <el-input v-model="diaryForm.moodTag" placeholder="例如：开心、焦虑、平静" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitLoading" @click="submitDiary">提交</el-button>
        <el-button @click="resetDiaryForm">清空</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
/* 日记表单保持适中的宽度，方便阅读和填写。 */
.diary-form {
  max-width: 720px;
  margin-top: 24px;
}
</style>
