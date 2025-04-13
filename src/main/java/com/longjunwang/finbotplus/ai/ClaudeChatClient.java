package com.longjunwang.finbotplus.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClaudeChatClient extends BaseChatClient{

    @Autowired
    private AnthropicChatModel anthropicChatModel;

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
        chatModel = anthropicChatModel;
    }
}
