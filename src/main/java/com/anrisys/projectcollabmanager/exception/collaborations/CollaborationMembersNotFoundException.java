package com.anrisys.projectcollabmanager.exception.collaborations;

import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

public class CollaborationMembersNotFoundException extends ResourceNotFoundException {
    public CollaborationMembersNotFoundException() {
        super("There are no members in this collaborations");
    }
}
