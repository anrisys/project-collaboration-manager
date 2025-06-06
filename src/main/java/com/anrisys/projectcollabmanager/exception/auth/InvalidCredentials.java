package com.anrisys.projectcollabmanager.exception.auth;

import com.anrisys.projectcollabmanager.exception.core.ValidationException;

public class InvalidCredentials extends ValidationException {
    public InvalidCredentials() {
        super("Email or password is wrong");
    }
}
