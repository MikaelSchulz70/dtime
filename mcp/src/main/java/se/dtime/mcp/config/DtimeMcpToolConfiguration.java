package se.dtime.mcp.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.dtime.mcp.service.AccountMcpTools;
import se.dtime.mcp.service.ReportMcpTools;
import se.dtime.mcp.service.SpecialDayMcpTools;
import se.dtime.mcp.service.SystemMcpTools;
import se.dtime.mcp.service.TaskContributorMcpTools;
import se.dtime.mcp.service.TaskMcpTools;
import se.dtime.mcp.service.TimeReportMcpTools;
import se.dtime.mcp.service.TimeReportStatusMcpTools;
import se.dtime.mcp.service.UserMcpTools;

@Configuration
public class DtimeMcpToolConfiguration {

    @Bean
    ToolCallbackProvider dtimeMcpTools(
            UserMcpTools userMcpTools,
            AccountMcpTools accountMcpTools,
            TaskMcpTools taskMcpTools,
            TaskContributorMcpTools taskContributorMcpTools,
            SpecialDayMcpTools specialDayMcpTools,
            TimeReportMcpTools timeReportMcpTools,
            TimeReportStatusMcpTools timeReportStatusMcpTools,
            ReportMcpTools reportMcpTools,
            SystemMcpTools systemMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(
                        userMcpTools,
                        accountMcpTools,
                        taskMcpTools,
                        taskContributorMcpTools,
                        specialDayMcpTools,
                        timeReportMcpTools,
                        timeReportStatusMcpTools,
                        reportMcpTools,
                        systemMcpTools)
                .build();
    }
}
