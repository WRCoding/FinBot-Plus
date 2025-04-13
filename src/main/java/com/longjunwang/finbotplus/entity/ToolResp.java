package com.longjunwang.finbotplus.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ToolResp<T> {
    private T data;
    private Map<String, Object> extraMap;
}
