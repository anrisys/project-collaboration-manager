package com.anrisys.projectcollabmanager.exception.projects;

public class HasSameProjectNameException extends RuntimeException {
    public HasSameProjectNameException() {
        super("User already has project with the same name");
    }
}
