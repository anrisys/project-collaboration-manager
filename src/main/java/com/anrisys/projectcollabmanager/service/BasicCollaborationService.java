package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.collaborations.CollaborationMembersNotFoundException;
import com.anrisys.projectcollabmanager.exception.collaborations.CollaborationNotFoundException;
import com.anrisys.projectcollabmanager.exception.collaborations.UserAlreadyCollaborationMemberException;
import com.anrisys.projectcollabmanager.repository.CollaborationRepository;

import java.util.List;

public class BasicCollaborationService implements CollaborationService{
    private final CollaborationRepository collaborationRepository;
    private final ProjectService projectService;
    private final UserService userService;

    public BasicCollaborationService(CollaborationRepository repository, ProjectService projectService, UserService userService) {
        this.collaborationRepository = repository;
        this.projectService = projectService;
        this.userService = userService;
    }

    @Override
    public List<ProjectDTO> listCollaborationProjects(Long userId) {
        return collaborationRepository.findCollaborationsByUserId(userId).orElseThrow(
                CollaborationNotFoundException::new
        );
    }

    @Override
    public List<UserDTO> listProjectMembers(Long projectId) {
        return collaborationRepository.findMembersByProjectId(projectId).orElseThrow(
                CollaborationMembersNotFoundException::new
        );
    }

    @Override
    public Collaboration inviteUserToProjectById(Long projectId, Long inviterUserId, Long inviteeUserId) {
        Project project = projectService.findProjectById(projectId, inviterUserId);

        boolean userMember = isUserMember(project.getId(), inviteeUserId);

        if (userMember) throw new UserAlreadyCollaborationMemberException();

        CreateCollaborationRequest request = new CreateCollaborationRequest(project.getId(), inviteeUserId);

        return collaborationRepository.addUserToProject(request);
    }

    @Override
    public Collaboration inviteUserToProjectByEmail(Long projectId, Long inviterUserId, String inviteeEmail) {
        UserDTO userDTO = userService.findByEmail(inviteeEmail);

        return inviteUserToProjectById(projectId, inviterUserId, userDTO.id());
    }

    @Override
    public void removeUserFromProjectById(Long projectId, Long projectOwnerId, Long memberId) {
        Project project = projectService.findProjectById(projectId, projectOwnerId);

        boolean userMember = isUserMember(project.getId(), memberId);

        if (!userMember) throw new CollaborationMembersNotFoundException();

        collaborationRepository.removeUserFromProject(projectId, memberId);
    }

    @Override
    public void removeUserFromProjectByUserEmail(Long projectId, Long projectOwnerId, String memberEmail) {
        UserDTO userDTO = userService.findByEmail(memberEmail);

        removeUserFromProjectById(projectId, projectOwnerId, userDTO.id());
    }

    @Override
    public boolean isUserMember(Long projectId, Long userId) {
        return collaborationRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public void leaveProjectCollaboration(Long projectId, Long userId) {
        boolean exists = collaborationRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!exists) throw new CollaborationNotFoundException();

        collaborationRepository.removeUserFromProject(projectId, userId);
    }
}
