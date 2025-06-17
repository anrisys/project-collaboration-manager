package com.anrisys.projectcollabmanager.exception.collaborations;

import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

public class CollaborationNotFoundException extends ResourceNotFoundException {
    public CollaborationNotFoundException() {
        super("Project Collaborations not found");
    }
}
