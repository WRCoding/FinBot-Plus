package com.longjunwang.finbotplus.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeepSeekChatClient extends BaseChatClient{

    @Autowired
    OpenAiChatModel openAiChatModel;

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
        chatModel = openAiChatModel;
    }


}
