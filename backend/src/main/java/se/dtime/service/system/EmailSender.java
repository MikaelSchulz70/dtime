package se.dtime.service.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.config.EmailSendConfig;
import se.dtime.model.error.BaseException;

import java.util.Properties;

@Service
public class EmailSender {
    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    private final static String REMINDER_SUBJECT = "Dtime!";
    private final static String REMINDER_TEXT = "En vänlig påminnelse att rapporera månadens tid J.\n\nMvh\nDtime";

    @Autowired
    public EmailSendConfig emailSendConfig;

    public void sendReminderEmail(String to) {
        if (StringUtils.isEmpty(to)) {
            return;
        }

        // Check if email is enabled
        if (!emailSendConfig.isMailEnabled()) {
            log.info("Email sending is disabled - mail.enabled is false");
            return;
        }

        // Check if email credentials are properly configured
        if (isDummyEmailConfiguration()) {
            log.warn("Email sending is disabled - using dummy configuration");
            return;
        }

        try {
            JavaMailSenderImpl emailSender = createMailSender();
            emailSender.setUsername(emailSendConfig.getReminderMailUsername());
            emailSender.setPassword(emailSendConfig.getReminderMailPassword());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailSendConfig.getReminderMailUsername());
            message.setTo(to);
            message.setSubject(REMINDER_SUBJECT);
            message.setText(REMINDER_TEXT);

            emailSender.send(message);
            log.info("Reminder email sent to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send email", e);
            throw new BaseException("system.failed.to.send.mail");
        }
    }

    private boolean isDummyEmailConfiguration() {
        return emailSendConfig.getReminderMailUsername().contains("@example.com") ||
                emailSendConfig.getReminderMailPassword().equals("dummy-password") ||
                emailSendConfig.getReminderMailPassword().equals("dev-password");
    }

    private JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailSendConfig.getHost());
        mailSender.setPort(emailSendConfig.getPort());
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", emailSendConfig.getProtocol());
        props.put("mail.smtp.auth", emailSendConfig.getAuth());
        props.put("mail.smtp.starttls.enable", emailSendConfig.getStartTSLEnable());
        props.put("spring.mail.properties.mail.smtp.ssl.enable", emailSendConfig.getSslEnable());

        return mailSender;
    }
}
