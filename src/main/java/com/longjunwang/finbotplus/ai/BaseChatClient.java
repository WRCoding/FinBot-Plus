package com.longjunwang.finbotplus.ai;

import com.longjunwang.finbotplus.tools.ToolsManage;
import com.longjunwang.finbotplus.util.PromptUtil;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Data
public abstract class BaseChatClient {

    @Autowired
    private ToolsManage toolsManage;
    public ChatModel chatModel;
    public ChatClient chatClient;


    protected abstract void setChatModel();
    public abstract String getName();

    @PostConstruct
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
                .tools(toolsManage.getToolCallbacks())
                .call();
        if (responseType == String.class){
            return (T) callResponseSpec.content();
        }
        return callResponseSpec
                .entity(responseType);
    }

    public <T> T chat(MediaType mediaType, Resource resource, Class<T> responseType) {
        ChatClient.CallResponseSpec callResponseSpec = getReqSpecByMedia(mediaType, resource)
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

    public ChatClient.ChatClientRequestSpec getReqSpecByMedia(MediaType mediaType, Resource resource){
       var content = "这是一份" + mediaType.getType() + "文件,请你仔细分析";
        var userMessage = new UserMessage(content,
                List.of(new Media(mediaType, resource)));
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        return chatClient.prompt(new Prompt(List.of(PromptUtil.getSystemMessage("record.txt"), userMessage)));
    }

    private Prompt createPrompt(Message systemMessage, String userQuery) {
        return new Prompt(List.of(
                systemMessage,
                new UserMessage(userQuery)
        ));
    }

}
