package com.bishe.backend.dto;

import lombok.Data;

/**
 * AI 陪伴对话响应对象。
 *
 * <p>该对象保存大语言模型返回给用户的陪伴式回复。</p>
 */
@Data
public class AiChatResponse {

    /**
     * AI 生成的回复内容。
     */
    private String reply;
}
