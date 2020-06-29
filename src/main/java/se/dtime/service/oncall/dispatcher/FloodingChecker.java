package se.dtime.service.oncall.dispatcher;

import se.dtime.model.EmailContainer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FloodingChecker {
    private static Map<Integer, LocalDateTime> floodingGuard = new HashMap<>();
    private static LocalDateTime lastCleanTime = LocalDateTime.now();

    static boolean isFlooding(LocalDateTime now, EmailContainer emailContainer) {
        Integer key = createKey(emailContainer);
        LocalDateTime lastDateTime = floodingGuard.putIfAbsent(key, now);

        boolean isFlooding = lastDateTime != null && now.isBefore(lastDateTime.plusHours(1));
        if (!isFlooding) {
            floodingGuard.put(key, now);
        }

        clearFloodGuard();

        return isFlooding;
    }

    static private Integer createKey(EmailContainer emailContainer) {
        String keyStr = emailContainer.getFrom() + "_" + emailContainer.getSubject() + "_" + emailContainer.getBody();
        return keyStr.hashCode();
    }

    static private void clearFloodGuard() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(lastCleanTime.plusDays(1))) {
            floodingGuard.clear();
            lastCleanTime = now;
        }
    }

}
