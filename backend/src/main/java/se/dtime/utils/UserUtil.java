package se.dtime.utils;

import se.dtime.model.UserExt;

public class UserUtil {

    public static boolean isUserAdmin(UserExt userExt) {
        return userExt != null && userExt.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
