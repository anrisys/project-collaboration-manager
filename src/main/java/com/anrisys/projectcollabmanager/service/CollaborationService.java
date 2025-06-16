package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.entity.Collaboration;

public interface CollaborationService {
    Collaboration create(CreateCollaborationRequest request);
    Collaboration delete(Long collaborationId, Long clientId);
}
