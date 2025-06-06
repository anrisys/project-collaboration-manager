package com.anrisys.projectcollabmanager.exception;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;
import com.anrisys.projectcollabmanager.exception.core.ValidationException;

public class GlobalExceptionHandler {
    public static void handle(Exception e) {
        if(e instanceof BusinessException) {
            System.out.println("[NOT FOUND ERROR] " + e.getMessage());
        } else if (e instanceof ValidationException) {
            System.out.println("[VALIDATION EXCEPTION] " + e.getMessage());
        } else {
            System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
