package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.exception.collaborations.CollaborationNotFoundException;
import com.anrisys.projectcollabmanager.repository.CollaborationRepository;

public class BasicCollaborationService implements CollaborationService{
    private final CollaborationRepository collaborationRepository;
    private final ProjectService projectService;

    public BasicCollaborationService(CollaborationRepository repository, ProjectService projectService) {
        this.collaborationRepository = repository;
        this.projectService = projectService;
    }

    @Override
    public Collaboration create(CreateCollaborationRequest request) {
        return collaborationRepository.create(request);
    }

    @Override
    public Collaboration delete(Long collaborationId, Long clientId) {
        Collaboration collaboration = collaborationRepository.findById(collaborationId).orElseThrow(
                () -> new CollaborationNotFoundException(collaborationId)
        );

        projectService.findProjectById(collaboration.getProjectId(), clientId);

        return collaborationRepository.deleteById(collaborationId);
    }
}
