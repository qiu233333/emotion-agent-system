package com.bishe.backend.service.impl;

import com.bishe.backend.config.LLMProperties;
import com.bishe.backend.dto.AiChatRequest;
import com.bishe.backend.dto.AiSuggestionRequest;
import com.bishe.backend.service.LLMService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大语言模型调用业务实现类。
 *
 * <p>当前实现直接调用 OpenAI 兼容的 {@code /chat/completions} 接口。
 * 这样后续切换 DeepSeek 或其他兼容服务时，只需要修改 baseURL、apiKey 和 model 配置。</p>
 */
@Service
public class LLMServiceImpl implements LLMService {

    /**
     * 系统提示词，约束 AI 回复边界和风格。
     */
    private static final String SYSTEM_PROMPT = """
            你是一个情绪记录与管理系统中的 AI 陪伴助手。
            请遵守以下要求：
            1. 不做医学诊断，不替代心理咨询或治疗。
            2. 回复要温和、简短、尊重用户感受。
            3. 建议要具体可执行，避免空泛说教。
            4. 如果用户出现自伤、自杀、伤害他人等高风险表达，请建议用户尽快联系现实中的可信任对象、学校老师、家人或专业帮助，必要时联系当地紧急救助渠道。
            """;

    /**
     * 大语言模型配置。
     */
    @Resource
    private LLMProperties llmProperties;

    /**
     * JSON 序列化和反序列化工具。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 根据日记内容和情绪分析结果生成情绪建议。
     *
     * @param request 情绪建议请求参数
     * @return 大语言模型生成的建议文本
     */
    @Override
    public String generateSuggestion(AiSuggestionRequest request) {
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("请提供日记内容后再生成 AI 建议");
        }

        String userPrompt = """
                请根据下面这篇情绪日记生成情绪建议。

                日记内容：%s
                情绪类型：%s
                情绪强度：%s
                关键词：%s

                请输出 2 到 4 条温和、具体、可执行的建议，总字数控制在 120 字以内。
                """.formatted(
                request.getContent(),
                defaultText(request.getEmotionType(), "未识别"),
                request.getEmotionScore() == null ? "未评分" : request.getEmotionScore(),
                defaultText(request.getKeywords(), "无")
        );

        return callChatCompletions(userPrompt, 0.7, 500);
    }

    /**
     * 生成 AI 陪伴对话回复。
     *
     * @param request 陪伴对话请求参数
     * @return 大语言模型生成的回复文本
     */
    @Override
    public String chat(AiChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new IllegalArgumentException("请输入想和 AI 聊的内容");
        }

        String userPrompt = """
                用户正在和你进行情绪陪伴对话。
                用户说：%s

                请给出温和简短的回应，先共情，再给一个很小、可执行的下一步建议。
                """.formatted(request.getMessage());

        return callChatCompletions(userPrompt, 0.8, 400);
    }

    /**
     * 调用 OpenAI 兼容的 Chat Completions 接口。
     *
     * @param userPrompt 用户提示词
     * @param temperature 采样温度
     * @param maxTokens 最大输出 token 数
     * @return 模型返回的正文内容
     */
    private String callChatCompletions(String userPrompt, double temperature, int maxTokens) {
        validateConfig();

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", llmProperties.getModel());
            requestBody.put("messages", buildMessages(userPrompt));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(buildChatCompletionsUrl()))
                    .timeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                    .header("Authorization", "Bearer " + llmProperties.getApiKey())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(resolveTimeoutSeconds()))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return parseAssistantContent(response);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("AI 服务调用失败，请稍后再试");
        }
    }

    /**
     * 构造 OpenAI 兼容 messages 数组。
     *
     * @param userPrompt 用户提示词
     * @return messages 列表
     */
    private List<Map<String, String>> buildMessages(String userPrompt) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(buildMessage("system", SYSTEM_PROMPT));
        messages.add(buildMessage("user", userPrompt));
        return messages;
    }

    /**
     * 构造单条对话消息。
     *
     * @param role 消息角色，例如 system 或 user
     * @param content 消息内容
     * @return 单条消息键值对
     */
    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * 解析模型返回内容。
     *
     * @param response HTTP 响应对象
     * @return assistant message content
     */
    private String parseAssistantContent(HttpResponse<String> response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String apiMessage = rootNode.path("error").path("message").asText();
                if (StringUtils.hasText(apiMessage)) {
                    throw new IllegalStateException("AI 服务返回错误：" + apiMessage);
                }
                throw new IllegalStateException("AI 服务暂时不可用，请稍后再试");
            }

            String content = rootNode.path("choices").path(0).path("message").path("content").asText();
            if (!StringUtils.hasText(content)) {
                throw new IllegalStateException("AI 暂时没有返回有效内容，请稍后再试");
            }
            return content.trim();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("AI 响应解析失败，请稍后再试");
        }
    }

    /**
     * 校验大语言模型配置是否完整。
     */
    private void validateConfig() {
        if (!StringUtils.hasText(llmProperties.getApiKey())
                || "your_api_key_here".equals(llmProperties.getApiKey())) {
            throw new IllegalStateException("大语言模型 API Key 未配置，请先设置 LLM_API_KEY");
        }
        if (!StringUtils.hasText(llmProperties.getBaseUrl())
                || "https://api.example.com".equals(llmProperties.getBaseUrl())) {
            throw new IllegalStateException("大语言模型 Base URL 未配置，请先设置 LLM_BASE_URL");
        }
        if (!StringUtils.hasText(llmProperties.getModel())
                || "your_model_name".equals(llmProperties.getModel())) {
            throw new IllegalStateException("大语言模型名称未配置，请先设置 LLM_MODEL");
        }
    }

    /**
     * 拼接 Chat Completions 接口地址。
     *
     * @return 完整接口地址
     */
    private String buildChatCompletionsUrl() {
        String baseUrl = llmProperties.getBaseUrl().trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (baseUrl.endsWith("/chat/completions")) {
            return baseUrl;
        }
        return baseUrl + "/chat/completions";
    }

    /**
     * 获取请求超时时间。
     *
     * @return 正整数超时时间，单位为秒
     */
    private Integer resolveTimeoutSeconds() {
        Integer timeoutSeconds = llmProperties.getTimeoutSeconds();
        return timeoutSeconds == null || timeoutSeconds <= 0 ? 30 : timeoutSeconds;
    }

    /**
     * 返回非空文本，否则返回默认值。
     *
     * @param text 原始文本
     * @param fallback 默认值
     * @return 可用于 prompt 的文本
     */
    private String defaultText(String text, String fallback) {
        return StringUtils.hasText(text) ? text : fallback;
    }
}
