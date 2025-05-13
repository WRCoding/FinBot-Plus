package com.longjunwang.finbotplus.ai;

import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.longjunwang.finbotplus.ai.Model.GEMINI;

@Service
public class GeminiChatClient extends OpenAIChatClient{

    @Value("${spring.ai.gemini.base-url}")
    private String baseUrl;

    @Value("${spring.ai.gemini.model}")
    private String model;

    @Override
    public String getName() {
        return GEMINI.name();
    }

    @Override
    protected String getBaseUrl() {
        return baseUrl;
    }

    @Override
    protected String getApiKey() {
        return System.getenv("GEMINI_API_KEY");
    }

    @Override
    protected OpenAiChatOptions getChatOptions() {
        return OpenAiChatOptions.builder().model(model).temperature(0.7).build();
    }
}
