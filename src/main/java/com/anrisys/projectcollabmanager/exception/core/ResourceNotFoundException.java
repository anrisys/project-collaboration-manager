package com.anrisys.projectcollabmanager.exception.core;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entity) {
        super("%s not found.".formatted(entity));
    }
}
