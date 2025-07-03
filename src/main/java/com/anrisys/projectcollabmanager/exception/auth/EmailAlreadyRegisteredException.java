package com.anrisys.projectcollabmanager.exception.auth;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;
import com.anrisys.projectcollabmanager.util.LoggerUtil;

public class EmailAlreadyRegisteredException extends BusinessException {
    public EmailAlreadyRegisteredException() {
        super("Email is already registered");
    }
}
