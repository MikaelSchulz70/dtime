package se.dtime.service.oncall.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.dtime.model.EmailContainer;
import se.dtime.model.EmailPollContainer;
import se.dtime.model.error.DTimeException;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

@Slf4j
@Service
public class OnCallEmailReader {
    @Value("${oncall.mail.read.protocol}")
    private String protocol;
    @Value("${oncall.mail.read.host}")
    private String host;
    @Value("${oncall.mail.read.port}")
    private String port;
    @Value("${oncall.mail.username}")
    private String userName;
    @Value("${oncall.mail.pwd}")
    private String password;


    public EmailPollContainer readEmails(LocalDateTime fromDateTime, LocalDateTime toDateTime) throws DTimeException {
        log.info("Start reading on call mail");
        Properties properties = getServerProperties();
        Session session = Session.getDefaultInstance(properties);

        EmailPollContainer emailPollContainer = new EmailPollContainer();
        try (Store store = session.getStore(protocol)) {
            store.connect(userName, password);
            Folder folderInbox = store.getFolder("inbox");
            folderInbox.open(Folder.READ_ONLY);

            Message[] messages = folderInbox.search(createSearchTerm(fromDateTime, toDateTime));

            int messageCount = folderInbox.getMessageCount();
            log.info("Fetched {} emails of {}", messages.length, messageCount);
            emailPollContainer.setReadInLastPoll(messages.length);
            emailPollContainer.setMailInInboxInLastPoll(messageCount);

            for (Message msg : messages) {
                Address[] fromAddress = msg.getFrom();
                String from = fromAddress[0].toString();
                String subject = msg.getSubject();
                String toList = parseAddresses(msg.getRecipients(RecipientType.TO));
                String ccList = parseAddresses(msg.getRecipients(RecipientType.CC));
                String sentDate = msg.getSentDate().toString();

                String contentType = msg.getContentType();
                String messageContent = "";

                if (contentType.contains("text/plain") || contentType.contains("text/html")) {
                    try {
                        Object content = msg.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    } catch (Exception e) {
                        messageContent = "Error downloading content";
                        log.error(messageContent, e);
                    }
                } else if (contentType.contains("multipart")) {
                    try {
                        MimeMultipart mimeMultipart = (MimeMultipart) msg.getContent();
                        messageContent = getTextFromMimeMultipart(mimeMultipart);
                    } catch (Exception e) {
                        messageContent = "Error downloading content";
                        log.error(messageContent, e);
                    }
                }

                EmailContainer emailContainer = EmailContainer.builder().
                        toList(toList).
                        from(from).
                        ccList(ccList).
                        subject(subject).
                        sentDate(sentDate).
                        body(messageContent).
                        build();

                emailPollContainer.add(emailContainer);
            }

            // disconnect
            folderInbox.close(false);
        } catch (NoSuchProviderException e) {
            log.error("No provider for protocol {} ", protocol, e);
            throw new DTimeException("No provider for protocol", e);
        } catch (MessagingException e) {
            log.error("Could not connect to the message store", e);
            throw new DTimeException("Could not connect to the message store", e);
        }

        return emailPollContainer;
    }

    private SearchTerm createSearchTerm(LocalDateTime lastSessionDateTime, LocalDateTime now) {
        final Date fromDate = Date.from(lastSessionDateTime.minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());
        final Date toDate = Date.from(now.plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());

        SearchTerm olderThan = new ReceivedDateTerm(ComparisonTerm.LE, toDate);
        SearchTerm newerThan = new ReceivedDateTerm(ComparisonTerm.GE, fromDate);

        return new AndTerm(newerThan, olderThan);
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result = result + "\n" + bodyPart.getContent();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    /**
     * Returns a list of addresses in String format separated by comma
     *
     * @param address an array of Address objects
     * @return a string represents a list of addresses
     */
    private String parseAddresses(Address[] address) {
        String listAddress = "";

        if (address != null) {
            for (int i = 0; i < address.length; i++) {
                listAddress += address[i].toString() + ", ";
            }
        }
        if (listAddress.length() > 1) {
            listAddress = listAddress.substring(0, listAddress.length() - 2);
        }

        return listAddress;
    }

    private Properties getServerProperties() {
        Properties properties = new Properties();

        // server setting
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);

        // SSL setting
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", String.valueOf(port));

        return properties;
    }
}
