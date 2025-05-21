package com.longjunwang.finbotplus.tools.mcp;



import java.util.List;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpSyncClient;

import org.springframework.ai.mcp.AsyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.autoconfigure.McpClientAutoConfiguration;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 */
@AutoConfiguration(after = { McpClientAutoConfiguration.class })
@EnableConfigurationProperties(McpClientCommonProperties.class)
public class McpToolCallbackAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "SYNC",
            matchIfMissing = true)
    public SyncMcpToolCallbackProvider mcpToolCallbacks(ObjectProvider<List<McpSyncClient>> syncMcpClients) {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(List::stream).toList();
        return new SyncMcpToolCallbackProvider(mcpClients);
    }

    @Bean
    @ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "type", havingValue = "ASYNC")
    public AsyncMcpToolCallbackProvider mcpAsyncToolCallbacks(ObjectProvider<List<McpAsyncClient>> mcpClientsProvider) {
        List<McpAsyncClient> mcpClients = mcpClientsProvider.stream().flatMap(List::stream).toList();
        return new AsyncMcpToolCallbackProvider(mcpClients);
    }

}
