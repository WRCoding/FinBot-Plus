package com.longjunwang.finbotplus.tools;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ToolsManage {

    private final ApplicationContext context;

//    private final SyncMcpToolCallbackProvider syncMcpToolCallbackProvider;

    public ToolsManage(ApplicationContext context) {
        this.context = context;
//        this.syncMcpToolCallbackProvider = syncMcpToolCallbackProvider;
    }

    @Getter
    private final List<ToolCallback> toolCallbacks = new ArrayList<>();


    @PostConstruct
    public void registerToolCallBack(){
        RecordTools recordTools = context.getBean(RecordTools.class);
        Method[] methods = recordTools.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Tool.class)){
                Tool tool = method.getAnnotation(Tool.class);
                String description = tool.description();
                String name = tool.name();
                ToolCallback toolCallback = MethodToolCallback.builder()
                        .toolDefinition(ToolDefinition.builder(method)
                                .description(description)
                                .name(name)
                                .build())
                        .toolMethod(method)
                        .toolObject(recordTools)
                        .build();
                toolCallbacks.add(toolCallback);
            }
        }
//        toolCallbacks.addAll(List.of(syncMcpToolCallbackProvider.getToolCallbacks()));
//        printToolCallbacks();
    }

    private void printToolCallbacks() {
        for (ToolCallback toolCallback : toolCallbacks) {
            log.info("tool: name: {}, desc: {}", toolCallback.getToolDefinition().name(), toolCallback.getToolDefinition().description());
        }
    }

}
