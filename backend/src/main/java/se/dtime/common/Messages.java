package se.dtime.common;

import org.springframework.stereotype.Component;

import java.util.ResourceBundle;

@Component
public class Messages {

    private static ResourceBundle bundle = ResourceBundle.getBundle("messages_en");

    public String get(String code) {
        return bundle.getString(code);
    }
}
