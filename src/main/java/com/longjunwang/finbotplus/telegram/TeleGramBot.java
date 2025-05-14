package com.longjunwang.finbotplus.telegram;

import com.longjunwang.finbotplus.ai.ChatModelManager;
import com.longjunwang.finbotplus.ai.ClaudeChatClient;
import com.longjunwang.finbotplus.entity.*;
import com.longjunwang.finbotplus.entity.ApiResponse;
import com.longjunwang.finbotplus.entity.Record;
import com.longjunwang.finbotplus.service.RecordService;
import com.longjunwang.finbotplus.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeleGramBot extends TelegramLongPollingBot {

    @Value("${telegram.token}")
    private String token;

    @Autowired
    private RecordService recordService;

    @Autowired
    private ChatModelManager chatModelManager;



    private boolean command = false;

    public TeleGramBot(DefaultBotOptions defaultBotOptions) {
        super(defaultBotOptions);
    }

    @Override
    public String getBotUsername() {
        return "Record7Bot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        User from = message.getFrom();
        if (Objects.nonNull(message.getPhoto()) &&  !message.getPhoto().isEmpty()){
            PhotoSize photo = message.getPhoto().stream().sorted(Comparator.comparingInt(PhotoSize::getFileSize)).toList().getLast();
            Resource resource = FileUtil.downloadFile(executeMethod(GetFile.builder().fileId(photo.getFileId()).build()));
            RecordContext context = new RecordContext();
            context.setResource(resource);
            context.setMediaType(MediaType.IMAGE_JPEG);
            try {
                String record = recordService.insertRecord(context);
                sendMsg(from.getId(), record);
            } catch (IOException e) {
                log.error("onUpdateReceived error ", e);
                sendMsg(from.getId(), e.getMessage());
            }
            return;
        }
        log.info("userId: {}, text: {}", from.getId(), message.getText());
        if (message.isCommand()) {
            if (!command) {
                command = true;
            }
            sendMsg(from.getId(), "请输入数据");
            return;
        }
        if (command) {
            try {
                RecordMsg recordMsg = new RecordMsg(null, message.getText(), null);
                RecordContext context = new RecordContext();
                context.setRecordMsg(recordMsg);
                String recordNo = recordService.insertRecord(context);
                sendMsg(from.getId(), !recordNo.isEmpty() ? "插入成功: " + recordNo : "插入失败");
            } catch (IOException e) {
                sendMsg(from.getId(), e.getMessage());
            } finally {
                command = false;
            }
            return;
        }
        String[] msg = message.getText().split(" ");
        String model = msg.length > 1 ? msg[0] : "CLAUDE";
        String text = msg.length > 1 ? msg[1] : msg[0];
        ApiResponse<String> response = recordService.query(new Query(model, text));
        sendMsg(from.getId(), response.getData());
    }


    public void sendMsg(Long userID, String text) {
        SendMessage sm;
        sm = SendMessage.builder()
                .chatId(userID.toString()) //Who are we sending a message to
                .text(text)
                .parseMode("HTML").build();
        executeMethod(sm);                        //Actually sending the message
    }

    private <T extends Serializable> T executeMethod(BotApiMethod<T> method) {
        try {
            return execute(method);
        } catch (TelegramApiException e) {
            log.error("executeMethod error :", e);
            return null;
        }
    }
}
