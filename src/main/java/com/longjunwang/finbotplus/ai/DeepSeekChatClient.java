package com.longjunwang.finbotplus.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeepSeekChatClient extends BaseChatClient{

    @Value("${spring.ai.deepseek.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @PostConstruct
    public void init(){
        super.init();
    }

    @Override
    public String getName() {
        return Model.DEEPSEEK.name();
    }

    @Override
    protected void setChatModel() {
        var openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(System.getenv("DEEPSEEK_API_KEY")).build();
        var openAiChatOptions = OpenAiChatOptions.builder().model(model).temperature(0.7).build();
        chatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build();
    }


}
