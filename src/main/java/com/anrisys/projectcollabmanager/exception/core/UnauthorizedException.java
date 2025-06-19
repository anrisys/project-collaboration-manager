package com.anrisys.projectcollabmanager.exception.core;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("You don't have access for this action");
    }
}
