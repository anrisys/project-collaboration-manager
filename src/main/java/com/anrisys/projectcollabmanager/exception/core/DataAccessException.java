package com.anrisys.projectcollabmanager.exception.core;

// For handling SQL Exception - Checked Exceptions (Infrastructure error)
public class DataAccessException extends RuntimeException{
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
