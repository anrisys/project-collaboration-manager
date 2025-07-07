package com.anrisys.projectcollabmanager.util;

import org.slf4j.Logger;

public class LoggerUtil {
    public static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 2
                ? email.substring(0, 2) + "***" + email.substring(atIndex)
                : "***" + email.substring(atIndex);
    }

    public static void logSQL(Logger log, String methodName, String sql) {
        log.debug("[{}]: SQL: {}", methodName, sql);
    }

    public static void logSQLExecuted(Logger log, String methodName) {
        log.debug("[{}]: SQL executed.", methodName);
    }

    public static void logDatabaseError(Logger log, String methodName, String message, Exception e) {
        log.error("[{}] Database error.{}", methodName, message, e);
    }

    public static void logUnexpectedRowAffectedQuery(Logger log, String methodName, String message, int affectedRow) {
        log.warn("[{}] {}. Expected affected row 1 got {}", methodName, message, affectedRow);
    }
}
