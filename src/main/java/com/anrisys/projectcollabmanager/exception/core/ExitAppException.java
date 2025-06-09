package com.anrisys.projectcollabmanager.exception.core;

public class ExitAppException extends RuntimeException {
    public ExitAppException() {
        super("Exiting application... Good bye!");
    }
}
