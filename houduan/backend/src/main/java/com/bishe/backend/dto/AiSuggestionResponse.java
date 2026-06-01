package com.bishe.backend.dto;

import lombok.Data;

/**
 * AI 情绪建议响应对象。
 *
 * <p>该对象返回模型生成的建议，以及本次建议是否已经保存到日记表。</p>
 */
@Data
public class AiSuggestionResponse {

    /**
     * 日记 ID。不传 diaryId 生成建议时，该字段为空。
     */
    private Long diaryId;

    /**
     * 大语言模型生成的情绪建议。
     */
    private String aiSuggestion;

    /**
     * true 表示建议已经写入 emotion_diary.ai_suggestion。
     */
    private Boolean saved;
}
