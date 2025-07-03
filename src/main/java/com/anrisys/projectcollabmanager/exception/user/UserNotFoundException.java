package com.anrisys.projectcollabmanager.exception.user;

import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException() {
        super("User not found");
    }
}
