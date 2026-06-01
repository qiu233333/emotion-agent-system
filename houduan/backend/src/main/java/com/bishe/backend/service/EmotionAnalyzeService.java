package com.bishe.backend.service;

import com.bishe.backend.dto.EmotionAnalyzeResult;

/**
 * NLP 情绪分析业务接口。
 *
 * <p>该接口用于把用户输入的日记正文分析成结构化情绪结果。
 * 当前阶段先使用情绪词典和规则判断，后续可以替换为大模型或专业 NLP 服务。</p>
 */
public interface EmotionAnalyzeService {

    /**
     * 分析日记文本情绪。
     *
     * @param content 用户输入的日记正文
     * @return 情绪分析结果
     */
    EmotionAnalyzeResult analyze(String content);
}
