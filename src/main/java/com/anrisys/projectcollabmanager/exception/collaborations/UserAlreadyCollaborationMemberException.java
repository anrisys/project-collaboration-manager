package com.anrisys.projectcollabmanager.exception.collaborations;

public class UserAlreadyCollaborationMemberException extends RuntimeException {
    public UserAlreadyCollaborationMemberException() {
        super("User is already part of the project");
    }
}
