package com.longjunwang.finbotplus.telegram;

import com.longjunwang.finbotplus.entity.ApiResponse;
import com.longjunwang.finbotplus.entity.Query;
import com.longjunwang.finbotplus.entity.RecordMsg;
import com.longjunwang.finbotplus.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Slf4j
@Service
public class TeleGramBot extends TelegramLongPollingBot {

    @Autowired
    private RecordService recordService;

    private boolean command = false;

    public TeleGramBot(DefaultBotOptions defaultBotOptions){
        super(defaultBotOptions);
    }

    @Override
    public String getBotUsername() {
        return "Record7Bot";
    }

    @Override
    public String getBotToken() {
        return "8083842836:AAGMo-RuQ-G0J9x6DYLSHLXcFoUhYz-rCDM";
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        User from = message.getFrom();
        log.info("userId: {}, text: {}", from.getId(), message.getText());
        if (message.isCommand()) {
            if (!command){
                command = true;
            }
            sendMsg(from.getId(), "请输入数据");
            return;
        }
        if (command){
            try {
                RecordMsg recordMsg = new RecordMsg(null, message.getText(), null);
                String recordNo = recordService.insertRecord(recordMsg);
                sendMsg(from.getId(), !recordNo.isEmpty() ? "插入成功: " + recordNo : "插入失败");
            } catch (IOException e) {
                sendMsg(from.getId(), e.getMessage());
            }finally {
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

    public void sendMsg(Long userID, String text){
        SendMessage sm;
        try {
            sm = SendMessage.builder()
                    .chatId(userID.toString()) //Who are we sending a message to
                    .text(text)
                    .parseMode("HTML").build();
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            log.error("sendMsg error :", e);
        }
    }
}
