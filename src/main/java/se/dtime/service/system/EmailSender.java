package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import se.dtime.config.EmailSendConfig;
import se.dtime.model.EmailContainer;
import se.dtime.model.error.BaseException;
import se.dtime.model.error.DTimeException;

import java.util.Properties;

@Slf4j
@Service
public class EmailSender {
    private final static String REMINDER_SUBJECT = "Dtime!";
    private final static String REMINDER_TEXT = "En vänlig påminnelse att rapporera månadens tid J.\n\nMvh\nDtime";

    @Autowired
    public EmailSendConfig emailSendConfig;

    public void sendReminderEmail(String to) {
        if (StringUtils.isEmpty(to)) {
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
        } catch (MailException e) {
            log.error("Failed to send email", e);
            throw new BaseException("system.failed.to.send.mail");
        }
    }

    public void sendForwardOnCallEmail(String to, EmailContainer emailContainer) {
        try {
            JavaMailSenderImpl emailSender = createMailSender();
            emailSender.setUsername(emailSendConfig.getOnCallMailUsername());
            emailSender.setPassword(emailSendConfig.getOnCallMailPassword());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailSendConfig.getOnCallMailUsername());
            message.setTo(to);
            message.setSubject(emailContainer.getSubject());
            message.setText(emailContainer.toString());

            emailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email", e);
            throw new DTimeException("Failed to send email", e);
        }
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
        props.put("mail.debug", emailSendConfig.getDebug());
        props.put("spring.mail.properties.mail.smtp.ssl.enable", emailSendConfig.getSslEnable());

        return mailSender;
    }
}
