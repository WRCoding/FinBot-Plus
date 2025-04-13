package com.longjunwang.finbotplus.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 记录分组统计结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordGroupSum {
    /**
     * 记录类型
     */
    private String type;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
} 