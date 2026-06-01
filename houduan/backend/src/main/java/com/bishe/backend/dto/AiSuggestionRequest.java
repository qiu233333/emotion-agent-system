package com.bishe.backend.dto;

import lombok.Data;

/**
 * AI 情绪建议请求对象。
 *
 * <p>如果传入 diaryId，后端会读取当前用户对应日记的内容和情绪分析结果，
 * 调用大模型生成建议后写回 emotion_diary.ai_suggestion 字段。</p>
 */
@Data
public class AiSuggestionRequest {

    /**
     * 日记 ID。传入该字段时，AI 建议会保存到对应日记中。
     */
    private Long diaryId;

    /**
     * 日记正文内容。不传 diaryId 时，可直接使用该字段生成建议。
     */
    private String content;

    /**
     * 情绪类型，例如开心、焦虑、悲伤、愤怒、疲惫、平静。
     */
    private String emotionType;

    /**
     * 情绪强度分数，范围为 1 到 5。
     */
    private Integer emotionScore;

    /**
     * 关键词字符串，多个关键词可用逗号或顿号分隔。
     */
    private String keywords;
}
