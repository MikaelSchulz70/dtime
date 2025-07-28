package se.dtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailSendConfig {
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.transport.protocol}")
    private String protocol;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String auth;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String startTSLEnable;
    @Value("${spring.mail.debug}")
    private String debug;
    @Value("${spring.mail.properties.mail.smtp.ssl.enable}")
    private String sslEnable;

    @Value("${reminder.mail.username}")
    private String reminderMailUsername;
    @Value("${reminder.mail.password}")
    private String reminderMailPassword;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getStartTSLEnable() {
        return startTSLEnable;
    }

    public void setStartTSLEnable(String startTSLEnable) {
        this.startTSLEnable = startTSLEnable;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(String sslEnable) {
        this.sslEnable = sslEnable;
    }

    public String getReminderMailUsername() {
        return reminderMailUsername;
    }

    public void setReminderMailUsername(String reminderMailUsername) {
        this.reminderMailUsername = reminderMailUsername;
    }

    public String getReminderMailPassword() {
        return reminderMailPassword;
    }

    public void setReminderMailPassword(String reminderMailPassword) {
        this.reminderMailPassword = reminderMailPassword;
    }
}
