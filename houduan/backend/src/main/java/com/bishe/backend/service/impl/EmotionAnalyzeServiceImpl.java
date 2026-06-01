package com.bishe.backend.service.impl;

import com.bishe.backend.dto.EmotionAnalyzeResult;
import com.bishe.backend.service.EmotionAnalyzeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * NLP 情绪分析业务实现类。
 *
 * <p>当前实现采用“情绪词典 + 规则判断”的轻量方案：
 * 先统计积极词、消极词和程度词命中情况，再推断情绪极性、类型、分数和关键词。</p>
 */
@Service
public class EmotionAnalyzeServiceImpl implements EmotionAnalyzeService {

    /**
     * 积极情绪词典。
     */
    private static final List<String> POSITIVE_WORDS = List.of("开心", "高兴", "顺利", "轻松", "满意", "期待");

    /**
     * 消极情绪词典。
     */
    private static final List<String> NEGATIVE_WORDS = List.of("焦虑", "难受", "压力", "烦", "崩溃", "担心", "疲惫", "失眠");

    /**
     * 程度词词典，命中后会提高情绪强度分数。
     */
    private static final List<String> DEGREE_WORDS = List.of("很", "非常", "特别", "十分", "太", "极其", "超级", "一直", "总是", "严重");

    /**
     * 场景关键词词典，用于补充非情绪词关键词。
     */
    private static final List<String> SCENE_KEYWORDS = List.of(
            "考试", "复习", "学习", "效率", "论文", "毕设", "项目", "工作",
            "进度", "睡眠", "老师", "同学", "朋友", "家人", "生活", "休息"
    );

    /**
     * 分析日记文本情绪。
     *
     * @param content 用户输入的日记正文
     * @return 情绪分析结果
     */
    @Override
    public EmotionAnalyzeResult analyze(String content) {
        String text = StringUtils.hasText(content) ? content : "";
        List<String> positiveHits = findMatchedWords(text, POSITIVE_WORDS);
        List<String> negativeHits = findMatchedWords(text, NEGATIVE_WORDS);
        List<String> degreeHits = findMatchedWords(text, DEGREE_WORDS);

        EmotionAnalyzeResult result = new EmotionAnalyzeResult();
        result.setEmotionPolarity(resolvePolarity(positiveHits.size(), negativeHits.size()));
        result.setEmotionType(resolveEmotionType(text, result.getEmotionPolarity(), positiveHits, negativeHits));
        result.setEmotionScore(resolveEmotionScore(result.getEmotionPolarity(), positiveHits.size(),
                negativeHits.size(), degreeHits.size()));
        result.setKeywords(resolveKeywords(text, positiveHits, negativeHits));
        result.setAiSummary(resolveSummary(result, positiveHits, negativeHits, degreeHits));
        return result;
    }

    /**
     * 在文本中查找命中的词典词。
     *
     * @param text 待分析文本
     * @param dictionary 词典列表
     * @return 按词典顺序返回的命中词列表
     */
    private List<String> findMatchedWords(String text, List<String> dictionary) {
        List<String> matchedWords = new ArrayList<>();
        for (String word : dictionary) {
            if (text.contains(word)) {
                matchedWords.add(word);
            }
        }
        return matchedWords;
    }

    /**
     * 根据积极词和消极词命中数量判断情绪极性。
     *
     * @param positiveCount 积极词命中数量
     * @param negativeCount 消极词命中数量
     * @return positive、negative 或 neutral
     */
    private String resolvePolarity(int positiveCount, int negativeCount) {
        if (positiveCount > negativeCount) {
            return "positive";
        }
        if (negativeCount > positiveCount) {
            return "negative";
        }
        return "neutral";
    }

    /**
     * 根据命中词和文本内容推断具体情绪类型。
     *
     * @param text 待分析文本
     * @param polarity 情绪极性
     * @param positiveHits 积极词命中列表
     * @param negativeHits 消极词命中列表
     * @return 情绪类型
     */
    private String resolveEmotionType(String text, String polarity, List<String> positiveHits, List<String> negativeHits) {
        if ("positive".equals(polarity)) {
            return "开心";
        }
        if ("neutral".equals(polarity)) {
            return "平静";
        }
        if (containsAny(text, List.of("焦虑", "担心", "压力", "考试", "复习"))) {
            return "焦虑";
        }
        if (containsAny(text, List.of("疲惫", "失眠"))) {
            return "疲惫";
        }
        if (containsAny(text, List.of("烦", "愤怒", "生气"))) {
            return "愤怒";
        }
        if (containsAny(text, List.of("难受", "崩溃", "悲伤", "伤心"))) {
            return "悲伤";
        }
        return negativeHits.isEmpty() ? "平静" : "焦虑";
    }

    /**
     * 计算情绪强度分数。
     *
     * @param polarity 情绪极性
     * @param positiveCount 积极词命中数量
     * @param negativeCount 消极词命中数量
     * @param degreeCount 程度词命中数量
     * @return 1 到 5 之间的分数
     */
    private Integer resolveEmotionScore(String polarity, int positiveCount, int negativeCount, int degreeCount) {
        if ("neutral".equals(polarity)) {
            return degreeCount > 0 ? 2 : 1;
        }

        int dominantCount = Math.max(positiveCount, negativeCount);
        int score = 1 + dominantCount + degreeCount;
        return Math.max(1, Math.min(score, 5));
    }

    /**
     * 生成关键词字符串。
     *
     * @param text 待分析文本
     * @param positiveHits 积极词命中列表
     * @param negativeHits 消极词命中列表
     * @return 多个关键词用中文逗号连接的字符串
     */
    private String resolveKeywords(String text, List<String> positiveHits, List<String> negativeHits) {
        Set<String> keywords = new LinkedHashSet<>();
        keywords.addAll(positiveHits);
        keywords.addAll(negativeHits);
        keywords.addAll(findMatchedWords(text, SCENE_KEYWORDS));
        return String.join("、", keywords);
    }

    /**
     * 生成简短分析说明。
     *
     * @param result 情绪分析结果
     * @param positiveHits 积极词命中列表
     * @param negativeHits 消极词命中列表
     * @param degreeHits 程度词命中列表
     * @return 简短分析说明
     */
    private String resolveSummary(EmotionAnalyzeResult result, List<String> positiveHits,
                                  List<String> negativeHits, List<String> degreeHits) {
        List<String> emotionHits = "positive".equals(result.getEmotionPolarity()) ? positiveHits : negativeHits;
        if ("neutral".equals(result.getEmotionPolarity()) || emotionHits.isEmpty()) {
            return "文本中未出现明显积极或消极情绪词，整体情绪较为平静。";
        }

        String degreeText = degreeHits.isEmpty() ? "" : "，并出现程度词“" + String.join("、", degreeHits) + "”";
        return "文本中出现“" + String.join("、", emotionHits) + "”等情绪词" + degreeText
                + "，整体情绪判断为" + result.getEmotionType() + "。";
    }

    /**
     * 判断文本是否包含给定词列表中的任意词。
     *
     * @param text 待分析文本
     * @param words 词列表
     * @return true 表示至少命中一个词
     */
    private boolean containsAny(String text, List<String> words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
