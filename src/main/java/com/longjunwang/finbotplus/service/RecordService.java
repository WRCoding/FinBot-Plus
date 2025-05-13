package com.longjunwang.finbotplus.service;

import cn.hutool.json.JSONUtil;
import com.longjunwang.finbotplus.ai.ChatModelManager;
import com.longjunwang.finbotplus.entity.*;
import com.longjunwang.finbotplus.entity.Record;
import com.longjunwang.finbotplus.mapper.RecordMapper;
import com.longjunwang.finbotplus.util.IdGenerator;
import com.longjunwang.finbotplus.util.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecordService {

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private ChatModelManager chatModelManager;

    public String insertRecord(RecordMsg recordMsg) throws IOException {
        Record record = buildRecord(recordMsg.content());
        if (Objects.isNull(record)) {
            log.error("解析失败, recordMsg:{}", recordMsg);
            return null;
        } else {
            recordMapper.insertSelective(record);
        }
        return record.getRecordNo();
    }

    public ApiResponse<String> query(Query query){
        LocalDate currentDate = LocalDate.now();

        // 定义格式化模式（YYYY年mm月dd日）
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        String answer = chatModelManager.chatWithModel(query.model(), PromptUtil.getSystemMessage("analyse.txt", "{date}", currentDate.format(formatter)), query.question(), String.class)
                .orElse("请求有误,请检查数据");
        log.info(answer);
        return ApiResponse.success(answer);
    }


    public ApiResponse<String> updateRecord(RecordMsg recordMsg){
        Record record = recordMapper.selectByRecordNo(recordMsg.recordNo());
        if (Objects.isNull(record)){
            log.error("recordNo不存在, recordNo: {}", recordMsg.recordNo());
            return ApiResponse.fail(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "recordNo不存在, recordNo: " + recordMsg.recordNo());
        }
        if (!StringUtils.hasText(recordMsg.extraMsg()) ) {
            log.error("extraMsg.extraMsg不符合要求,不进行处理, extraMsg.msg: {}", recordMsg.extraMsg());
            return ApiResponse.fail(ResponseCode.INTERNAL_SERVER_ERROR.getCode(), "extraMsg.extraMsg不符合要求,不进行处理, extraMsg.msg: " + recordMsg.extraMsg());
        }
        String[] msg = recordMsg.extraMsg().split(" ");
        Record update = new Record();
        update.setRecordNo(record.getRecordNo());
        if (msg.length > 1){
            update.setType(msg[0]);
            update.setRemark(msg[1]);
        }else{
            update.setRemark(msg[0]);
        }
        recordMapper.updateSelective(update);
        return ApiResponse.success("更新成功,recordNo: " + recordMsg.recordNo());
    }

    private Record buildRecord(String msg) throws IOException {
        Record record = chatModelManager.chatWithModel("DEEPSEEK", PromptUtil.getSystemMessage("record.txt"), msg, Record.class).orElse(null);
        if (Objects.isNull(record)) {
            return null;
        }
        if (!StringUtils.hasText(record.getDate())){
            LocalDateTime currentDate = LocalDateTime.now();

            // 定义格式化模式（YYYY年mm月dd日）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
            String formattedDate = currentDate.format(formatter);
            record.setDate(formattedDate);
        }
        record.setRecordNo(IdGenerator.generateId());
        record.setRemark(record.getSubRemark());
        return record;
    }

    public void move(){
        log.info("开始数据迁移");
        try {
            // 连接data.db数据库
            Connection connection = DriverManager.getConnection("jdbc:sqlite:/Users/wanglongjun/project/RecordBot/data.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM transactions where transaction_time <= '2025年04月22日'");
            int count = 0;
            while (resultSet.next()) {
                try {
                    // 从transactions表读取数据
                    String amount = resultSet.getString("amount");
                    String transactionTime = resultSet.getString("transaction_time");
                    String publisher = resultSet.getString("publisher");
                    String remark = resultSet.getString("remark");
                    String type = resultSet.getString("type");
                    
                    // 转换为Record对象并保存
                    Record record = new Record();
                    record.setRecordNo(IdGenerator.generateId());
                    // 从transaction_time中提取日期部分作为date
                    record.setDate(transactionTime);
                    record.setAmount(amount);
                    record.setType(type != null ? type : "其他");
                    record.setRemark(remark);
                    record.setSubRemark(remark);
                    
                    // 保存到record表
                    recordMapper.insertSelective(record);
                    count++;
                } catch (Exception e) {
                    log.error("迁移单条数据异常", e);
                }
            }
            
            log.info("数据迁移完成，共迁移{}条记录", count);
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            log.error("数据迁移过程发生异常", e);
        }
    }
}
