package com.anrisys.projectcollabmanager.exception.projects;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
