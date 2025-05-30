package com.longjunwang.finbotplus.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeepSeekChatClient extends OpenAIChatClient{

    @Value("${spring.ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Override
    public String getName() {
        return Model.DEEPSEEK.name();
    }

    @Override
    protected String getBaseUrl() {
        return baseUrl;
    }

    @Override
    protected String getApiKey() {
        return System.getenv("DEEPSEEK_API_KEY");
    }

    @Override
    protected OpenAiChatOptions getChatOptions() {
        return OpenAiChatOptions.builder().model(model).temperature(0.7).build();
    }


}
