package com.anrisys.projectcollabmanager.exception.collaborations;

import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

public class CollaborationNotFoundException extends ResourceNotFoundException {
    public CollaborationNotFoundException(Long collaborationId) {
        super("Collaboration with id : %d is not found".formatted(collaborationId));
    }
}
