package com.bishe.backend.dto;

import lombok.Data;

/**
 * 情绪分析结果对象。
 *
 * <p>NLP 情绪分析模块会把一段日记文本转换成该对象，
 * 再由日记业务模块保存到 emotion_diary 表的情绪分析字段中。</p>
 */
@Data
public class EmotionAnalyzeResult {

    /**
     * 情绪极性，可选值为 positive、negative、neutral。
     */
    private String emotionPolarity;

    /**
     * 情绪类型，例如开心、焦虑、悲伤、愤怒、疲惫、平静。
     */
    private String emotionType;

    /**
     * 情绪强度分数，范围为 1 到 5。
     */
    private Integer emotionScore;

    /**
     * 关键词字符串，多个关键词用中文逗号分隔。
     */
    private String keywords;

    /**
     * 简短分析说明，用于解释本次情绪判断的依据。
     */
    private String aiSummary;
}
