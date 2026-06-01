package com.bishe.backend.service;

import com.bishe.backend.dto.AiChatRequest;
import com.bishe.backend.dto.AiSuggestionRequest;

/**
 * 大语言模型调用业务接口。
 *
 * <p>该接口屏蔽具体模型供应商差异，业务代码只关心生成情绪建议和陪伴对话。
 * 当前实现使用 OpenAI 兼容的 Chat Completions 请求格式。</p>
 */
public interface LLMService {

    /**
     * 根据日记内容和情绪分析结果生成情绪建议。
     *
     * @param request 情绪建议请求参数
     * @return 大语言模型生成的建议文本
     */
    String generateSuggestion(AiSuggestionRequest request);

    /**
     * 生成 AI 陪伴对话回复。
     *
     * @param request 陪伴对话请求参数
     * @return 大语言模型生成的回复文本
     */
    String chat(AiChatRequest request);
}
