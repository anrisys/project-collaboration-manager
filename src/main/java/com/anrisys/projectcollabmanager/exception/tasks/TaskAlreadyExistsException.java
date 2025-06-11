package com.anrisys.projectcollabmanager.exception.tasks;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;

public class TaskAlreadyExistsException extends BusinessException {
    public TaskAlreadyExistsException(String title) {
        super("Task with title " + title + " already exists");
    }
}
