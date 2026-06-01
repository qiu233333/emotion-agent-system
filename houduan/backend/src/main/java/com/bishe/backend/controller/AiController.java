package com.bishe.backend.controller;

import com.bishe.backend.common.Result;
import com.bishe.backend.dto.AiChatRequest;
import com.bishe.backend.dto.AiChatResponse;
import com.bishe.backend.dto.AiSuggestionRequest;
import com.bishe.backend.dto.AiSuggestionResponse;
import com.bishe.backend.entity.EmotionDiary;
import com.bishe.backend.service.EmotionDiaryService;
import com.bishe.backend.service.LLMService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 能力接口控制器。
 *
 * <p>该控制器提供大语言模型生成情绪建议和 AI 陪伴对话能力。
 * 项目配置了统一上下文路径 {@code /api}，因此完整访问路径为 {@code /api/ai/...}。</p>
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    /**
     * 大语言模型业务对象，用于生成建议和对话回复。
     */
    @Resource
    private LLMService llmService;

    /**
     * 情绪日记业务对象，用于读取当前用户日记并保存 aiSuggestion。
     */
    @Resource
    private EmotionDiaryService emotionDiaryService;

    /**
     * 生成情绪建议接口。
     *
     * @param request 情绪建议请求参数
     * @return AI 生成的情绪建议
     */
    @PostMapping("/suggestion")
    public Result<AiSuggestionResponse> generateSuggestion(@RequestBody AiSuggestionRequest request) {
        try {
            AiSuggestionRequest effectiveRequest = buildSuggestionRequest(request);
            String suggestion = llmService.generateSuggestion(effectiveRequest);
            boolean saved = saveSuggestionIfNeeded(effectiveRequest.getDiaryId(), suggestion);

            AiSuggestionResponse response = new AiSuggestionResponse();
            response.setDiaryId(effectiveRequest.getDiaryId());
            response.setAiSuggestion(suggestion);
            response.setSaved(saved);
            return Result.success(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * AI 陪伴对话接口。
     *
     * @param request 陪伴对话请求参数
     * @return AI 生成的回复内容
     */
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        try {
            AiChatResponse response = new AiChatResponse();
            response.setReply(llmService.chat(request));
            return Result.success(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 构造实际用于模型调用的情绪建议请求。
     *
     * @param request 前端请求参数
     * @return 补全后的请求参数
     */
    private AiSuggestionRequest buildSuggestionRequest(AiSuggestionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请提供日记内容或日记 ID");
        }
        if (request.getDiaryId() == null) {
            return request;
        }

        EmotionDiary diary = emotionDiaryService.getCurrentUserDiary(request.getDiaryId());
        if (diary == null) {
            throw new IllegalArgumentException("日记不存在或无权访问");
        }

        AiSuggestionRequest effectiveRequest = new AiSuggestionRequest();
        effectiveRequest.setDiaryId(diary.getId());
        effectiveRequest.setContent(diary.getContent());
        effectiveRequest.setEmotionType(diary.getEmotionType());
        effectiveRequest.setEmotionScore(diary.getEmotionScore());
        effectiveRequest.setKeywords(diary.getKeywords());
        return effectiveRequest;
    }

    /**
     * 如果请求绑定了日记 ID，则保存 AI 建议到数据库。
     *
     * @param diaryId 日记 ID
     * @param suggestion AI 建议文本
     * @return true 表示已保存，false 表示只是生成文本没有落库
     */
    private boolean saveSuggestionIfNeeded(Long diaryId, String suggestion) {
        if (diaryId == null) {
            return false;
        }

        EmotionDiary updatedDiary = emotionDiaryService.saveAiSuggestion(diaryId, suggestion);
        if (updatedDiary == null) {
            throw new IllegalArgumentException("日记不存在或无权访问");
        }
        return true;
    }
}
