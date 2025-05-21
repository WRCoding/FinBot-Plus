package com.longjunwang.finbotplus.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChatModelManager {

    private final List<BaseChatClient> chatClients;


    @Autowired
    public ChatModelManager(List<BaseChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    public <T> T chatWithModelByMedia(String modelName, MediaType mediaType, Resource resource, Class<T> type) {
        return chatClients.stream()
                .filter(client -> client.getName().equalsIgnoreCase(modelName))
                .findFirst()
                .map(client -> {
                    try {
                        return client.chat(mediaType, resource, type);
                    } catch (Exception e) {
                        log.error("chatWithModelByMedia error", e);
                        return null;
                    }
                }).orElse(null);
    }


    public <T> T chatWithModel(String modelName, Message systemMessage, String userQuery, Class<T> type) {
        return chatClients.stream()
                .filter(client -> client.getName().equalsIgnoreCase(modelName))
                .findFirst()
                .map(client -> {
                    try {
                        return client.chat(systemMessage, userQuery, type);
                    } catch (Exception e) {
                        log.error("{} error, e", client.getName(), e);
                        return null;
                    }
                }).orElse(null);
    }




    /**
     * 依次尝试所有可用模型，直到获得成功响应
     * @param userQuery 用户输入
     * @return 如果任一模型成功响应则返回结果，否则返回空
     */
    public <T> T chatWithAnyModel(Message systemMessage, String userQuery, Class<T> type) {
        for (BaseChatClient client : chatClients) {
            try {
                return client.chat(systemMessage, userQuery, type);
            } catch (Exception e) {
                // 继续尝试下一个模型
                log.error("name: {}, e: {}", client.getName(), e.getMessage());
            }
        }
        return null;
    }

    /**
     * 获取所有可用的模型名称
     * @return 模型名称列表
     */
    public List<String> getAvailableModels() {
        return chatClients.stream()
                .map(BaseChatClient::getName)
                .toList();
    }
} 