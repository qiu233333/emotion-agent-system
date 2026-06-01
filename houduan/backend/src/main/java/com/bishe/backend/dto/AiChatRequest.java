package com.bishe.backend.dto;

import lombok.Data;

/**
 * AI 陪伴对话请求对象。
 *
 * <p>当前只实现简单单轮对话，前端提交用户输入的 message，
 * 后端调用大模型返回一段温和简短的陪伴回复。</p>
 */
@Data
public class AiChatRequest {

    /**
     * 用户想和 AI 聊的内容。
     */
    private String message;
}
