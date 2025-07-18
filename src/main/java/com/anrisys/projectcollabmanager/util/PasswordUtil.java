package com.anrisys.projectcollabmanager.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean verify(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
