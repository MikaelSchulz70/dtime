package se.dtime.mcp.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "mcp.http")
public class McpHttpProperties {

    private boolean logAccess = true;

    public void setLogAccess(boolean logAccess) {
        this.logAccess = logAccess;
    }
}
