package com.anrisys.projectcollabmanager.exception.auth;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;

public class EmailAlreadyRegisteredException extends BusinessException {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
