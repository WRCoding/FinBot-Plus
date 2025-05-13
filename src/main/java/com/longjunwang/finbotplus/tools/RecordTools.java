package com.longjunwang.finbotplus.tools;

import cn.hutool.json.JSONUtil;
import com.longjunwang.finbotplus.entity.Record;
import com.longjunwang.finbotplus.entity.ToolResp;
import com.longjunwang.finbotplus.mapper.RecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecordTools {

    @Autowired
    private RecordMapper recordMapper;
    @Tool(description = "获取指定范围内的记录数据", name = "getRecordByRangeDateAndRemark")
    public String getRecordByRangeDateAndRemark(@ToolParam(description = "范围的起始时间,包含这天,时间格式转换为YYYY年mm月dd日 例如2025年03月01日") String startTime,
                                       @ToolParam(description = "范围的终止时间,不包含这天,时间格式转换为YYYY年mm月dd日 例如2025年03月10日") String endTime,
                                       @ToolParam(description = "查询的备注信息,例如: 麦当劳,盒马,高速费") String remark){
        log.info("startTime: {}, endTime: {}, remark: {}", startTime, endTime, remark);
        List<Record> records = new ArrayList<>();
        if (StringUtils.hasText(remark)){
             records = recordMapper.selectByRangeDateAndRemark(startTime, endTime, remark);
        }
        if (records.isEmpty()){
            records = recordMapper.selectByRangeDate(startTime, endTime);
        }
        Map<String, Double> sumByType = records.stream()
                .collect(Collectors.groupingBy(
                        Record::getType,
                        Collectors.summingDouble(record -> Double.parseDouble(record.getAmount()))
                ));
        Map<String, Object> extraMap = new HashMap<>();
        extraMap.put("金额总结", sumByType);
        extraMap.put("交易总数", records.size());
        ToolResp<List<Record>> toolResp = new ToolResp<>(records, extraMap);
        String jsonStr = JSONUtil.toJsonStr(toolResp);
        log.info("toolResp: {}", jsonStr);
        return jsonStr;
    }
}
