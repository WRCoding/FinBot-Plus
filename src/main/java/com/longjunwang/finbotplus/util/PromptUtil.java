package com.longjunwang.finbotplus.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class PromptUtil {

    private static final String RESOURCE_PATH = "prompts/%s";
    private static final String DEFAULT_RESOURCE_PATH = "prompts/default.st";
    public static Message getSystemMessageBySt(String path, Map<String, Object> map){
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH.formatted(path));
        if (!resource.exists()){
            return getDefaultSystemMessage();
        }
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(resource);
        return systemPromptTemplate.createMessage(map);
    }

    public static Message getSystemMessage(String path, String... chars){
        ClassPathResource resource = new ClassPathResource(RESOURCE_PATH.formatted(path));
        String template;
        try (InputStream inputStream = resource.getInputStream()) {
            template = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
            if (chars.length > 0){
                template = template.replace(chars[0], chars[1]);
            }
            log.info("template: {}", template);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new SystemMessage(template);
    }

    public static Message getDefaultSystemMessage(){
        ClassPathResource resource = new ClassPathResource(DEFAULT_RESOURCE_PATH);
        return new SystemPromptTemplate(resource).createMessage(Map.of("date", LocalDate.now().toString()));
    }
}
