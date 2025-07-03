package com.anrisys.projectcollabmanager.util;

public class LoggerUtil {
    public static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 2
                ? email.substring(0, 2) + "***" + email.substring(atIndex)
                : "***" + email.substring(atIndex);
    }
}
