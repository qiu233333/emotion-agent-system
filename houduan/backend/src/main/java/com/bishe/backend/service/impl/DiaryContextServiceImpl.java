package com.bishe.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.mapper.EmotionDiaryMapper;
import com.bishe.backend.security.AuthContext;
import com.bishe.backend.service.DiaryContextService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 日记上下文业务实现类。
 *
 * <p>该实现会从数据库中读取当前登录用户的情绪日记，
 * 并把“今日完整信息”和“近期上下文摘要”整理成适合放入大模型 Prompt 的文本。</p>
 */
@Service
public class DiaryContextServiceImpl implements DiaryContextService {

    /**
     * 近期上下文统计的天数范围。
     */
    private static final int RECENT_CONTEXT_DAYS = 7;

    /**
     * 近期上下文最多读取的历史日记篇数。
     */
    private static final int RECENT_CONTEXT_LIMIT = 5;

    /**
     * 日记日期展示格式。
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 可能代表压力来源或重要生活场景的关键词。
     */
    private static final List<String> PRESSURE_SOURCE_WORDS = List.of(
            "考试", "复习", "论文", "毕设", "项目", "进度", "工作",
            "睡眠", "失眠", "家人", "朋友", "老师", "同学"
    );

    /**
     * 情绪日记数据访问对象，用于按当前用户和日期范围查询日记。
     */
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;

    /**
     * 构建当前登录用户用于 AI 陪伴对话的日记上下文。
     *
     * @return 包含今日日记完整信息和近期日记摘要的上下文文本
     */
    @Override
    public String buildCurrentUserChatContext() {
        Long currentUserId = getCurrentUserId();
        List<EmotionDiary> todayDiaries = listTodayDiaries(currentUserId);
        List<EmotionDiary> recentDiaries = listRecentDiaries(currentUserId);

        return """
                【今日完整信息】
                %s

                【近期上下文摘要】
                %s
                """.formatted(buildTodaySection(todayDiaries), buildRecentSummary(recentDiaries));
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Long currentUserId = AuthContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalArgumentException("未登录，请先登录");
        }
        return currentUserId;
    }

    /**
     * 查询当前用户今天的全部日记。
     *
     * @param currentUserId 当前登录用户 ID
     * @return 今天的日记列表，按日记时间正序排列
     */
    private List<EmotionDiary> listTodayDiaries(Long currentUserId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        return emotionDiaryMapper.selectList(new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getUserId, currentUserId)
                .ge(EmotionDiary::getDiaryDate, todayStart)
                .lt(EmotionDiary::getDiaryDate, tomorrowStart)
                .orderByAsc(EmotionDiary::getDiaryDate)
                .orderByAsc(EmotionDiary::getId));
    }

    /**
     * 查询当前用户近期但不包含今天的历史日记。
     *
     * @param currentUserId 当前登录用户 ID
     * @return 近期历史日记列表，按日记时间倒序排列
     */
    private List<EmotionDiary> listRecentDiaries(Long currentUserId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime recentStart = todayStart.minusDays(RECENT_CONTEXT_DAYS);

        return emotionDiaryMapper.selectList(new LambdaQueryWrapper<EmotionDiary>()
                .eq(EmotionDiary::getUserId, currentUserId)
                .ge(EmotionDiary::getDiaryDate, recentStart)
                .lt(EmotionDiary::getDiaryDate, todayStart)
                .orderByDesc(EmotionDiary::getDiaryDate)
                .orderByDesc(EmotionDiary::getId)
                .last("LIMIT " + RECENT_CONTEXT_LIMIT));
    }

    /**
     * 构建今日日记完整信息文本。
     *
     * @param todayDiaries 今天的日记列表
     * @return 可写入 Prompt 的今日日记文本
     */
    private String buildTodaySection(List<EmotionDiary> todayDiaries) {
        if (todayDiaries.isEmpty()) {
            return "暂无今日日记。";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < todayDiaries.size(); i++) {
            appendDiaryDetail(builder, todayDiaries.get(i), i + 1);
        }
        return builder.toString().trim();
    }

    /**
     * 拼接单篇日记的完整信息。
     *
     * @param builder 文本构建器
     * @param diary 日记实体
     * @param index 日记序号
     */
    private void appendDiaryDetail(StringBuilder builder, EmotionDiary diary, int index) {
        builder.append(index).append(". 标题：").append(defaultText(diary.getTitle(), "暂无标题")).append('\n');
        builder.append("   日期：").append(formatDateTime(diary.getDiaryDate())).append('\n');
        builder.append("   内容：").append(defaultText(diary.getContent(), "暂无内容")).append('\n');
        builder.append("   心情标签：").append(defaultText(diary.getMoodTag(), "暂无")).append('\n');
        builder.append("   情绪极性：").append(defaultText(diary.getEmotionPolarity(), "暂无")).append('\n');
        builder.append("   情绪类型：").append(defaultText(diary.getEmotionType(), "暂无")).append('\n');
        builder.append("   情绪强度：").append(diary.getEmotionScore() == null ? "暂无" : diary.getEmotionScore()).append('\n');
        builder.append("   关键词：").append(defaultText(diary.getKeywords(), "暂无")).append('\n');
        builder.append("   AI摘要：").append(defaultText(diary.getAiSummary(), "暂无")).append('\n');
        builder.append("   AI建议：").append(defaultText(diary.getAiSuggestion(), "暂无")).append("\n\n");
    }

    /**
     * 构建近期日记的规则化摘要。
     *
     * @param recentDiaries 近期历史日记列表
     * @return 可写入 Prompt 的近期摘要文本
     */
    private String buildRecentSummary(List<EmotionDiary> recentDiaries) {
        if (recentDiaries.isEmpty()) {
            return "暂无近期历史日记。";
        }

        Map<String, Integer> emotionTypeCounts = countByText(recentDiaries, "emotionType");
        Map<String, Integer> polarityCounts = countByText(recentDiaries, "emotionPolarity");
        Double averageScore = resolveAverageScore(recentDiaries);
        List<String> topKeywords = collectTopKeywords(recentDiaries);
        Set<String> pressureSources = resolvePressureSources(recentDiaries);

        return """
                近 %d 天内读取到 %d 篇历史日记。
                情绪类型分布：%s。
                情绪极性分布：%s。
                平均情绪强度：%s。
                高频关键词：%s。
                可能压力来源：%s。
                近期趋势：%s。
                """.formatted(
                RECENT_CONTEXT_DAYS,
                recentDiaries.size(),
                formatCountMap(emotionTypeCounts),
                formatCountMap(polarityCounts),
                averageScore == null ? "暂无" : String.format("%.1f", averageScore),
                topKeywords.isEmpty() ? "暂无" : String.join("、", topKeywords),
                pressureSources.isEmpty() ? "暂无明显压力来源" : String.join("、", pressureSources),
                resolveTrend(polarityCounts, averageScore)
        ).trim();
    }

    /**
     * 按指定字段统计文本出现次数。
     *
     * @param diaries 日记列表
     * @param fieldName 需要统计的字段名称
     * @return 文本和出现次数的映射
     */
    private Map<String, Integer> countByText(List<EmotionDiary> diaries, String fieldName) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (EmotionDiary diary : diaries) {
            String text = switch (fieldName) {
                case "emotionType" -> diary.getEmotionType();
                case "emotionPolarity" -> diary.getEmotionPolarity();
                default -> "";
            };
            if (StringUtils.hasText(text)) {
                counts.merge(text, 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * 计算近期日记的平均情绪强度。
     *
     * @param diaries 日记列表
     * @return 平均情绪强度；没有评分时返回 null
     */
    private Double resolveAverageScore(List<EmotionDiary> diaries) {
        int scoreSum = 0;
        int scoreCount = 0;
        for (EmotionDiary diary : diaries) {
            if (diary.getEmotionScore() != null) {
                scoreSum += diary.getEmotionScore();
                scoreCount++;
            }
        }
        return scoreCount == 0 ? null : (double) scoreSum / scoreCount;
    }

    /**
     * 收集近期日记中出现频率最高的关键词。
     *
     * @param diaries 日记列表
     * @return 最多 5 个高频关键词
     */
    private List<String> collectTopKeywords(List<EmotionDiary> diaries) {
        Map<String, Integer> keywordCounts = new LinkedHashMap<>();
        for (EmotionDiary diary : diaries) {
            for (String keyword : splitKeywords(diary.getKeywords())) {
                keywordCounts.merge(keyword, 1, Integer::sum);
            }
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(keywordCounts.entrySet());
        entries.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));

        List<String> topKeywords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : entries) {
            topKeywords.add(entry.getKey());
            if (topKeywords.size() >= 5) {
                break;
            }
        }
        return topKeywords;
    }

    /**
     * 根据关键词字段拆分出单个关键词。
     *
     * @param keywords 数据库中保存的关键词字符串
     * @return 去重后的关键词列表
     */
    private List<String> splitKeywords(String keywords) {
        List<String> result = new ArrayList<>();
        if (!StringUtils.hasText(keywords)) {
            return result;
        }

        Set<String> keywordSet = new LinkedHashSet<>();
        String[] parts = keywords.split("[、，,\\s]+");
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                keywordSet.add(part.trim());
            }
        }
        result.addAll(keywordSet);
        return result;
    }

    /**
     * 从近期日记内容和关键词中提取可能的压力来源。
     *
     * @param diaries 日记列表
     * @return 压力来源关键词集合
     */
    private Set<String> resolvePressureSources(List<EmotionDiary> diaries) {
        Set<String> pressureSources = new LinkedHashSet<>();
        StringBuilder sourceText = new StringBuilder();
        for (EmotionDiary diary : diaries) {
            sourceText.append(defaultText(diary.getContent(), "")).append(' ');
            sourceText.append(defaultText(diary.getKeywords(), "")).append(' ');
        }

        String text = sourceText.toString();
        for (String word : PRESSURE_SOURCE_WORDS) {
            if (text.contains(word)) {
                pressureSources.add(word);
            }
        }
        return pressureSources;
    }

    /**
     * 根据情绪极性分布和平均分生成简短趋势判断。
     *
     * @param polarityCounts 情绪极性统计
     * @param averageScore 平均情绪强度
     * @return 近期趋势说明
     */
    private String resolveTrend(Map<String, Integer> polarityCounts, Double averageScore) {
        int positiveCount = polarityCounts.getOrDefault("positive", 0);
        int negativeCount = polarityCounts.getOrDefault("negative", 0);
        int neutralCount = polarityCounts.getOrDefault("neutral", 0);

        if (negativeCount >= positiveCount && negativeCount >= neutralCount
                && averageScore != null && averageScore >= 3.5) {
            return "近期消极情绪较明显，强度偏高。";
        }
        if (neutralCount >= positiveCount && neutralCount >= negativeCount) {
            return "近期整体较平稳。";
        }
        if (positiveCount >= negativeCount && positiveCount >= neutralCount) {
            return "近期积极体验较多。";
        }
        return "近期情绪有一定波动。";
    }

    /**
     * 将统计结果格式化为自然语言文本。
     *
     * @param countMap 文本和次数的映射
     * @return 格式化后的统计文本
     */
    private String formatCountMap(Map<String, Integer> countMap) {
        if (countMap.isEmpty()) {
            return "暂无";
        }

        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            parts.add(entry.getKey() + entry.getValue() + "次");
        }
        return String.join("、", parts);
    }

    /**
     * 格式化日记日期时间。
     *
     * @param dateTime 日记日期时间
     * @return 可展示的日期文本
     */
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "暂无" : DATE_TIME_FORMATTER.format(dateTime);
    }

    /**
     * 返回非空文本，否则返回默认值。
     *
     * @param text 原始文本
     * @param fallback 默认值
     * @return 可用于 Prompt 的文本
     */
    private String defaultText(String text, String fallback) {
        return StringUtils.hasText(text) ? text : fallback;
    }
}
