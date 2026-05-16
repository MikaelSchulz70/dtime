package se.dtime.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.dtime.mcp.service.DtimeMcpToolsService;

@Configuration
public class DtimeMcpToolConfiguration {

    @Bean
    ToolCallbackProvider dtimeMcpTools(DtimeMcpToolsService toolsService) {
        return MethodToolCallbackProvider.builder().toolObjects(toolsService).build();
    }
}
