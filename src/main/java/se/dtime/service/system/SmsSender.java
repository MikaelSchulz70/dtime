package se.dtime.service.system;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmsSender {
    private final static int MAX_SMS_LENGTH = 160;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${sms.url}")
    private String url;
    @Value("${sms.username}")
    private String userName;
    @Value("${sms.pwd}")
    private String password;

    public SmsSender() {

    }

    public SmsSender(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    public boolean sendSms(String sender, String message, String ... mobileNumbers) throws Exception {
        String urlPath = buildUrlPath(sender, message, mobileNumbers);
        if (urlPath == null) {
            log.error("Sms not sent. No phone numbers");
            return false;
        }

        try {
            WebClient webClient = webClientBuilder.baseUrl(this.url).build();
            Mono<String> result = webClient.
                    get().
                    uri(urlPath).
                    retrieve().
                    bodyToMono(String.class);

            result.subscribe(this::handleResponse);
            Thread.sleep(5000);

        } catch (Exception e) {
            log.error("Failed to send sms", e);
            throw e;
        }

        return true;
    }

    private void handleResponse(String result) {
        log.info("Sms response {}", result);
        if (StringUtils.isEmpty(result) || !result.startsWith("OK")) {
            throw new RuntimeException("Failed to send sms");
        }
    }

    //https://se-1.cellsynt.net/sms.php?username=demo&password=test123&destination=0046700123123
    //&type=text&charset=UTF-8&text=Testing%20123&originatortype=alpha&originator=Demo
    String buildUrlPath(String sender, String message, String[] mobileNumbers) {
        String mobileNumberList = buildPhoneNumberList(mobileNumbers);
        if (mobileNumberList == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("/sms.php");
        sb.append("?username=");
        sb.append(userName);
        sb.append("&password=");
        sb.append(password);
        sb.append("&destination=");
        sb.append(buildPhoneNumberList(mobileNumbers));
        sb.append("&type=text");
        sb.append("&charset=UTF-8");
        sb.append("&text=");
        sb.append(adjustMessage(message));
        sb.append("&originatortype=alpha&originator=");
        sb.append(sender);

        return sb.toString();
    }

    String adjustMessage(String message) {
        if (message == null) {
            return "";
        }

        return message.substring(0, Math.min(MAX_SMS_LENGTH, message.length()));
    }

    String buildPhoneNumberList(String[] mobileNumbers) {
        if (mobileNumbers == null) {
            return null;
        }

        return Arrays.stream(mobileNumbers).
                map(this::formatMobileNumber).
                filter(Objects::nonNull).
                collect(Collectors.joining(","));
    }

    String formatMobileNumber(String mobileNumber) {
        String formatedMobileNumber = null;
        if (StringUtils.isEmpty(mobileNumber)) {
            formatedMobileNumber = null;
        } else if (mobileNumber.startsWith("0046")) {
            formatedMobileNumber = mobileNumber;
        } else if (mobileNumber.startsWith("+46")) {
            formatedMobileNumber = mobileNumber.replace("+46", "0046");
        } else if (mobileNumber.startsWith("07")) {
            formatedMobileNumber = mobileNumber.replace("07", "00467");
        }

        if (StringUtils.isEmpty(formatedMobileNumber) ||
                !formatedMobileNumber.matches("[0-9]+") || formatedMobileNumber.length() < 7) {
            return null;
        }

        return formatedMobileNumber;
    }
}
