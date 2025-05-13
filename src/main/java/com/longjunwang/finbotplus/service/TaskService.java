package com.longjunwang.finbotplus.service;

import com.longjunwang.finbotplus.entity.ApiResponse;
import com.longjunwang.finbotplus.entity.Query;
import com.longjunwang.finbotplus.telegram.TeleGramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private RecordService recordService;

    @Autowired
    private TeleGramBot teleGramBot;

    @Value("${telegram.userId}")
    private String userId;


    @Scheduled(cron = "0 0 9 * * *")
    public void executeDailyTask() {
        ApiResponse<String> response = recordService.query(new Query("CLAUDE", "总结一下昨天的开支纪录"));
        String data = response.getData();
        teleGramBot.sendMsg(Long.parseLong(userId), data);
    }
}
