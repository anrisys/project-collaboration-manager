package com.anrisys.projectcollabmanager.exception.user;

import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String email) {
        super("Email with email %s is not found".formatted(email));
    }
}
