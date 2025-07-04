package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.collaborations.CollaborationMembersNotFoundException;
import com.anrisys.projectcollabmanager.exception.collaborations.CollaborationNotFoundException;
import com.anrisys.projectcollabmanager.exception.collaborations.UserAlreadyCollaborationMemberException;
import com.anrisys.projectcollabmanager.exception.core.UnauthorizedException;
import com.anrisys.projectcollabmanager.repository.CollaborationRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CollaborationServiceImpl implements CollaborationService, CollaborationInfoService{
    private final CollaborationRepository collaborationRepository;
    private ProjectService projectService;
    private final UserService userService;
    private final static Logger log = LoggerFactory.getLogger(CollaborationServiceImpl.class);

    public CollaborationServiceImpl(CollaborationRepository repository, ProjectService projectService, UserService userService) {
        this.collaborationRepository = repository;
        this.projectService = projectService;
        this.userService = userService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public List<ProjectDTO> listCollaborationProjects(Long userId) {
        final String methodName = "listCollaborationProjects";
        log.debug("[{}] Attempt to retrieve list project collaborations for userId={}", methodName, userId);
        return collaborationRepository.findCollaborationsByUserId(userId).orElseThrow(
                CollaborationNotFoundException::new
        );
    }

    @Override
    public List<UserDTO> listProjectMembers(Long projectId) {
        final String methodName = "listProjectMembers";
        log.debug("[{}] Attempt to retrieve list project members for projectId={}", methodName, projectId);
        return collaborationRepository.findMembersByProjectId(projectId).orElseThrow(
                CollaborationMembersNotFoundException::new
        );
    }

    @Override
    public Project showCollaborationProject(Long projectId, Long userId) {
        final String methodName = "showCollaborationProject";
        log.debug("[{}] Attempt to retrieve project collaboration data of projectId={} for userId={}", methodName, projectId, userId);
        Project project = projectService.findProjectById(projectId);

        boolean userMember = isUserMember(project.getId(), userId);

        if (!userMember) {
            log.warn("[{}] Unauthorized action attempt: userId={} is not member of projectId={}", methodName, userId, projectId);
            throw new UnauthorizedException();
        };

        return project;
    }

    @Override
    public Collaboration inviteUserToProjectById(Long projectId, Long inviterUserId, Long inviteeUserId) {
        final String methodName = "inviteUserToProjectById";
        log.debug("[{}] Attempt to add userId={} to projectId={} by userId={}", methodName, inviteeUserId, projectId, inviterUserId);
        Project project = projectService.findPersonalProjectById(projectId, inviterUserId);

        log.debug("[{}] Checking if userId={} already as member in projectId={}", methodName, inviteeUserId, projectId);
        boolean userMember = isUserMember(project.getId(), inviteeUserId);

        if (userMember) {
            log.warn("[{}] UserId={} already member of projectId={}", methodName, inviteeUserId, projectId);
            throw new UserAlreadyCollaborationMemberException();
        }

        CreateCollaborationRequest request = new CreateCollaborationRequest(project.getId(), inviteeUserId);

        Collaboration collaboration = collaborationRepository.create(request);

        log.info("[{}] UserId={} successfully added into projectId={} by userId={}", methodName, inviteeUserId, project, inviterUserId);
        return collaboration;
    }

    @Override
    public Collaboration inviteUserToProjectByEmail(Long projectId, Long inviterUserId, String inviteeEmail) {
        final String methodName = "inviteUserToProjectByEmail";
        log.debug("[{}] Attempt to add user with email={} to projectId={} by userId={}",
                methodName, LoggerUtil.maskEmail(inviteeEmail), projectId, inviterUserId);
        UserDTO userDTO = userService.findByEmail(inviteeEmail);
        Collaboration collaboration = inviteUserToProjectById(projectId, inviterUserId, userDTO.id());
        log.info("[{}] User with email={} successfully added into projectId={}", methodName, LoggerUtil.maskEmail(inviteeEmail), projectId);
        return collaboration;
    }

    @Override
    public void removeUserFromProjectById(Long projectId, Long projectOwnerId, Long memberId) {
        final String methodName = "removeUserFromProjectById";
        log.debug("[{}] Attempt to remove user with id={} from projectId={} by userId={}",
                methodName, memberId, projectId, projectOwnerId);
        Project project = projectService.findPersonalProjectById(projectId, projectOwnerId);

        boolean userMember = isUserMember(project.getId(), memberId);

        if (!userMember) {
            log.warn("[{}] UserId={} is not member in projectId={}", methodName, memberId, projectId);
            throw new CollaborationMembersNotFoundException();
        }

        collaborationRepository.delete(projectId);
        log.info("[{}] UserId={} is removed from projectId={}", methodName, memberId, projectOwnerId);
    }

    @Override
    public void removeUserFromProjectByUserEmail(Long projectId, Long projectOwnerId, String memberEmail) {
        final String methodName = "removeUserFromProjectByUserEmail";
        log.debug("[{}] Attempt to remove user by email={} from projectId={} by userId={}",
                methodName, LoggerUtil.maskEmail(memberEmail), projectId, projectOwnerId);

        UserDTO userDTO = userService.findByEmail(memberEmail);

        removeUserFromProjectById(projectId, projectOwnerId, userDTO.id());
        log.info("[{}] User with email={} is removed from projectId={}", methodName, LoggerUtil.maskEmail(memberEmail), projectOwnerId);
    }

    @Override
    public boolean isUserMember(Long projectId, Long userId) {
        log.debug("[isUserMember] Checking if userId={} member of projectId={}", userId, projectId);
        return collaborationRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public void leaveProjectCollaboration(Long projectId, Long userId) {
        final String methodName = "leaveProjectCollaboration";
        log.debug("[{}] Attempt to leave projectId={} by userId={}", methodName, projectId, userId);

        log.debug("[{}] Checking if userId={} member of projectId={}", methodName, userId, projectId);
        boolean exists = collaborationRepository.existsByProjectIdAndUserId(projectId, userId);

        if (!exists) {
            log.warn("[{}] Unauthorized action: userId={} is not part of projectId={}", methodName, userId, projectId);
            throw new CollaborationNotFoundException();
        }

        collaborationRepository.delete(projectId);
        log.info("[{}] UserId={} has left projectId={}", methodName, userId, projectId);
    }

    @Override
    public boolean hasCollaborators(Long projectId) {
        log.debug("[{}] Checking if projectId={} has members", "hasCollaborators", projectId);
        return collaborationRepository.findMembersByProjectId(projectId).isPresent();
    }
}
