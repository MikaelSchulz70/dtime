package se.dtime.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
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

    @Value("${oncall.mail.username}")
    private String onCallMailUsername;
    @Value("${oncall.mail.pwd}")
    private String onCallMailPassword;
}
