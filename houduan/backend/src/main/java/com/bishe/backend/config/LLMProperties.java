package com.bishe.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大语言模型配置类。
 *
 * <p>该配置从 application.yaml 中读取，application.yaml 再从环境变量
 * LLM_API_KEY、LLM_BASE_URL、LLM_MODEL 中取值，避免把密钥写死在代码里。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LLMProperties {

    /**
     * 大语言模型 API Key，对应环境变量 LLM_API_KEY。
     */
    private String apiKey;

    /**
     * OpenAI 兼容接口基础地址，对应环境变量 LLM_BASE_URL。
     */
    private String baseUrl;

    /**
     * 模型名称，对应环境变量 LLM_MODEL。
     */
    private String model;

    /**
     * 请求超时时间，单位为秒。
     */
    private Integer timeoutSeconds = 30;
}
