package com.longjunwang.finbotplus.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * 使用指定的模型进行对话
     * @param modelName 模型名称
     * @param userQuery 用户输入
     * @return 如果找到模型并成功调用则返回结果，否则返回空
     */
    public Optional<String> chatWithModel(String modelName, Message systemMessage, String userQuery) {
        return chatClients.stream()
                .filter(client -> client.getName().equalsIgnoreCase(modelName))
                .findFirst()
                .map(client -> {
                    try {
                        return client.chat(systemMessage, userQuery);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }


    public <T> Optional<T> chatWithModel(String modelName, Message systemMessage, String userQuery, Class<T> type) {
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
                });
    }




    /**
     * 依次尝试所有可用模型，直到获得成功响应
     * @param userQuery 用户输入
     * @return 如果任一模型成功响应则返回结果，否则返回空
     */
    public Optional<String> chatWithAnyModel(String userQuery) {
        for (BaseChatClient client : chatClients) {
            try {
                String response = client.chat(userQuery);
                if (response != null && !response.isEmpty()) {
                    return Optional.of(response);
                }
            } catch (Exception ignored) {
                // 继续尝试下一个模型
            }
        }
        return Optional.empty();
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