package com.longjunwang.finbotplus.controller;

import com.longjunwang.finbotplus.entity.ApiResponse;
import com.longjunwang.finbotplus.entity.Query;
import com.longjunwang.finbotplus.entity.RecordMsg;
import com.longjunwang.finbotplus.entity.ResponseCode;
import com.longjunwang.finbotplus.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;

@RestController
@Slf4j
public class RecordController {

    @Autowired
    private RecordService recordService;

    @PostMapping("/receive/msg")
    public ApiResponse<String> receiveMsg(@RequestBody RecordMsg recordMsg) {
        try {
            String recordNo = recordService.insertRecord(recordMsg);
            if (Objects.isNull(recordNo)){
                return ApiResponse.fail(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "record插入失败,请检查");
            }
            return ApiResponse.success(recordNo);
        } catch (IOException e) {
            log.error("receiveMsg error, e", e);
            return ApiResponse.fail(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "receiveMsg发生异常, e: " + e.getMessage());
        }
    }

    @PostMapping("/update/msg")
    public ApiResponse<String> updateMsg(@RequestBody RecordMsg recordMsg){
        return recordService.updateRecord(recordMsg);
    }

    @PostMapping("/query")
    public ApiResponse<String> query(@RequestBody Query query){
        return recordService.query(query);
    }

    @PostMapping("/move")
    public ApiResponse<String> move(){
        try {
            recordService.move();
            return ApiResponse.success("数据迁移任务已启动");
        } catch (Exception e) {
            log.error("数据迁移任务启动失败", e);
            return ApiResponse.fail(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "数据迁移失败: " + e.getMessage());
        }
    }

}
