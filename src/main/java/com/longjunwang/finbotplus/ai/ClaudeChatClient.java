package com.longjunwang.finbotplus.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClaudeChatClient extends BaseChatClient{

    @Value("${spring.ai.anthropic.base-url}")
    private String baseUrl;

    @Value("${spring.ai.anthropic.chat.options.model}")
    private String model;
    @PostConstruct
    public void init(){
        super.init();
    }

    @Override
    public String getName() {
        return Model.CLAUDE.name();
    }

    @Override
    protected void setChatModel() {
        var anthropicApi = new AnthropicApi(baseUrl, System.getenv("ANTHROPIC_API_KEY"));
        var chatOptions = AnthropicChatOptions.builder()
                .model(model)
                .build();
        chatModel = AnthropicChatModel.builder().anthropicApi(anthropicApi)
                .defaultOptions(chatOptions).build();

    }
}
