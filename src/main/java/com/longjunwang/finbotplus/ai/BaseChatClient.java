package com.longjunwang.finbotplus.ai;

import com.longjunwang.finbotplus.util.PromptUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public abstract class BaseChatClient {

    @Autowired
    private ResourceLoader resourceLoader;
    protected ChatModel chatModel;
    protected ChatClient chatClient;
    private static final String DEFAULT_SYS_PROMPT = "你是一名通用助手, 今天的日期是: %s, 请你用中文回答我的问题";


    protected abstract void setChatModel();
    public abstract String getName();

    protected void init() {
        setChatModel(); // 由子类实现
        this.chatClient = ChatClient.create(chatModel);
    }


    public String chat(String userQuery) {
        return chat(PromptUtil.getDefaultSystemMessage(), userQuery, String.class);
    }

    public String chat(Message systemMessage, String userQuery) {
        return chat(systemMessage, userQuery, String.class);
    }


    public <T> T chat(Message systemMessage, String userQuery, Class<T> responseType) {
        ChatClient.CallResponseSpec callResponseSpec = getReqSpec(systemMessage, userQuery)
                .call();
        if (responseType == String.class ){
            return (T) callResponseSpec.content();
        }
        return callResponseSpec
                .entity(responseType);
    }

    public <T> T chat(Message systemMessage, String userQuery, Class<T> responseType, Object... tools) {
        ChatClient.CallResponseSpec callResponseSpec = getReqSpec(systemMessage, userQuery)
                .tools(tools)
                .call();
        if (responseType == String.class){
            return (T) callResponseSpec.content();
        }
        return callResponseSpec
                .entity(responseType);
    }

    public ChatClient.ChatClientRequestSpec getReqSpec(Message systemMessage, String userQuery){
        return chatClient.prompt(createPrompt(systemMessage, userQuery));
    }

    private Prompt createPrompt(Message systemMessage, String userQuery) {
        return new Prompt(List.of(
                systemMessage,
                new UserMessage(userQuery)
        ));
    }

}
