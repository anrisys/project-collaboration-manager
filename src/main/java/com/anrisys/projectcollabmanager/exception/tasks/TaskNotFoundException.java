package com.anrisys.projectcollabmanager.exception.tasks;

import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

public class TaskNotFoundException extends ResourceNotFoundException {
    public TaskNotFoundException() {
        super("Tasks not found");
    }

    public TaskNotFoundException(Long id) {
        super("Task with id : %d is not found".formatted(id));
    }
}
