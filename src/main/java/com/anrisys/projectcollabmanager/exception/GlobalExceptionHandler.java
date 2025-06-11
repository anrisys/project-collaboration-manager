package com.anrisys.projectcollabmanager.exception;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;
import com.anrisys.projectcollabmanager.exception.core.ExitAppException;
import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;
import com.anrisys.projectcollabmanager.exception.core.ValidationException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskAlreadyExistsException;

public class GlobalExceptionHandler {
    public static void handle(Exception e) {
        if(e instanceof BusinessException) {
            System.out.println("[ERROR ] " + e.getMessage());
        } else if (e instanceof ValidationException) {
            System.out.println("[VALIDATION ERROR] " + e.getMessage());
        } else if (e instanceof ResourceNotFoundException) {
            System.out.println(e.getMessage());
        } else if (e instanceof ExitAppException) {
            System.out.println(e.getMessage());
        } else {
            System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}
