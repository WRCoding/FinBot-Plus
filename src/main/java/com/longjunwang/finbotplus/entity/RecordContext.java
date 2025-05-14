package com.longjunwang.finbotplus.entity;

import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
@Data
public class RecordContext {
    private RecordMsg recordMsg;
    private MediaType mediaType;
    private Resource resource;
}
