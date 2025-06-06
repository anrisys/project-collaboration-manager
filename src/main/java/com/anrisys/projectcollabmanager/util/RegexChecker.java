package com.anrisys.projectcollabmanager.util;

import java.util.regex.Pattern;

public class RegexChecker {
    public static boolean patternMatches(String source, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(source)
                .matches();
    }
}
