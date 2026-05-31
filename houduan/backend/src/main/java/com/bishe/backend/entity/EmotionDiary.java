package com.bishe.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 情绪日记实体类。
 *
 * <p>该类与数据库中的 {@code emotion_diary} 表对应，用于保存用户日记内容、
 * 情绪分析结果和 AI 建议。MyBatis-Plus 会自动把驼峰字段映射到下划线字段。</p>
 */
@Data
@TableName("emotion_diary")
public class EmotionDiary {

    /**
     * 日记主键 ID，对应数据库自增主键 {@code id}。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日记所属用户 ID。登录后由后端从 JWT 中解析当前用户并自动写入。
     */
    private Long userId;

    /**
     * 日记标题。
     */
    private String title;

    /**
     * 日记正文内容。
     */
    private String content;

    /**
     * 用户自定义心情标签，例如“开心”“焦虑”“平静”。
     */
    private String moodTag;

    /**
     * 情绪极性，例如 positive、negative、neutral。
     */
    private String emotionPolarity;

    /**
     * 情绪类型，例如开心、焦虑、悲伤、愤怒、平静。
     */
    private String emotionType;

    /**
     * 情绪强度分数，建议范围为 1 到 5。
     */
    private Integer emotionScore;

    /**
     * 日记关键词，多个关键词可以用逗号分隔。
     */
    private String keywords;

    /**
     * AI 对日记内容生成的摘要。
     */
    private String aiSummary;

    /**
     * AI 生成的情绪建议。
     */
    private String aiSuggestion;

    /**
     * 日记日期。当前 SQL 使用 datetime 类型，因此这里使用 LocalDateTime。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime diaryDate;

    /**
     * 记录创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 记录更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
