package com.longjunwang.finbotplus.ai;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

public abstract class OpenAIChatClient extends BaseChatClient{
    @Override
    protected void setChatModel() {
        var openAiApi = OpenAiApi.builder().baseUrl(getBaseUrl()).apiKey(getApiKey()).build();
        chatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(getChatOptions()).build();
    }

    protected abstract String getBaseUrl();

    protected abstract String getApiKey();

    protected abstract OpenAiChatOptions getChatOptions();
}
