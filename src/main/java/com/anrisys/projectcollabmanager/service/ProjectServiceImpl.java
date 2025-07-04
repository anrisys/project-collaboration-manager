package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.core.UnauthorizedException;
import com.anrisys.projectcollabmanager.exception.projects.HasSameProjectNameException;
import com.anrisys.projectcollabmanager.exception.projects.ProjectMembersExistException;
import com.anrisys.projectcollabmanager.exception.projects.ProjectNotFoundException;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProjectServiceImpl implements ProjectService{
    private final ProjectRepository repository;
    private final CollaborationInfoService collaborationInfoService;
    private final static Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    public ProjectServiceImpl(ProjectRepository repository, CollaborationInfoService collaborationInfoService) {
        this.repository = repository;
        this.collaborationInfoService = collaborationInfoService;
    }

    @Override
    public Project create(ProjectCreateRequest request) {
        final String methodName = "create";
        log.debug("[{}] Attempt to create project for user id={}", methodName, request.owner());

        boolean hasSameProjectName = repository.HasSameProjectName(request.owner(), request.title());

        if (hasSameProjectName) {
            log.warn("[{}] Project with same name already exist for user={}", methodName, request.owner());
            throw new HasSameProjectNameException();
        }

        Project saved = repository.save(request);
        log.info("[{}] Project created for user id={}", methodName, saved.getOwner());
        return saved;
    }

    @Override
    public Project findProjectById(Long id) {
        log.debug("[findProjectById] Attempt to retrieve project with id:{}", id);
        Optional<Project> result = repository.findById(id);
        if (result.isEmpty()) {
            log.debug("[findProjectById] Project not found for id={}", id);
            throw new ProjectNotFoundException("Project with id " + id + " not found");
        }

        return result.get();
    }

    @Override
    public Project findPersonalProjectById(Long id, Long clientId) {
        final String methodName = "findPersonalProjectById";
        log.debug("[{}] Attempt to find project with id={} for user id={}",
                methodName, id, clientId);

        Project project = findProjectById(id);

        isActionPermitted(methodName, project, clientId);

        return project;
    }

    @Override
    public Project findProjectByTitle(String title) {
        final String methodName="findProjectByTitle";
        log.debug("findProjectByTitle: Attempt to find project with title={}", title);
        return repository.findByTitle(title).orElseThrow(
                () -> {
                    log.debug("[{}] Project not found for title={}", methodName, title);
                    return new ProjectNotFoundException("Project with title : " + title + "not found");
                }
        );
    }

    @Override
    public Project findPersonalProjectByTitle(String title, Long clientId) {
        final String methodName="findPersonalProjectByTitle";
        log.debug("[{}] Attempt to find project with title={}", methodName ,title);
        Project project = findProjectByTitle(title);

        isActionPermitted(methodName, project, clientId);
        // Should returning find by method still be logged?
        return project;
    }

    @Override
    public List<ProjectDTO> listPersonalProjects(Long ownerId) {
        final String methodName="listPersonalProject";
        log.debug("[{}] Attempt to find list personal project for user id={}", methodName ,ownerId);
        return repository.findByOwnerId(ownerId).orElseThrow(
                () -> {
                    log.warn("[{}] list personal project not found for user id={}", methodName, ownerId);
                    return new ProjectNotFoundException("Personal project not found");
                }
        );
    }

    @Override
    public Project updateProject(Long projectId, Long clientId, ProjectUpdateRequest request) {
        final String methodName = "updatedProject";
        log.debug("[{}] Attempt to update project id={} for user id={}", methodName, projectId, clientId);

        Project project = getProject(projectId);
        isActionPermitted(methodName, project, clientId);

        Project updated = repository.update(project.getId(), request);
        log.info("[{}] Project with id={} updated by user id={}", methodName, projectId, clientId);
        return updated;
    }

    @Override
    public Project deleteProject(Long projectId, Long clientId) {
        final String methodName = "deleteProject";
        log.debug("[{}] Attempt to delete project id={} by user id={}", methodName, projectId, clientId);
        Project project = getProject(projectId);

        isActionPermitted(methodName, project, clientId);

        repository.delete(project.getId());

        log.info("[{}] Project id={} deleted by user id={}", methodName, project.getId(), clientId);
        return project;
    }

    @Override
    public Project convertToCollaborative(Long projectId, Long userId) {
        final String methodName = "convertToCollaborative";
        log.debug("[{}] Attempt to convert project id={} into collaborative type by user id={}", methodName, projectId, userId);
        Project project = getProject(projectId);

        isActionPermitted(methodName, project, userId);

        if (!project.isPersonal()) {
            log.warn("[{}] Failed to convert project type. Project id={} is already collaborative", methodName, projectId);
            throw new IllegalStateException("Project has been already collaborative.");
        }

        Project changedProjectType = repository.changeProjectType(project.getId(), false);
        log.info("[{}] Project type with id={} changed into collaborative by user={}", methodName, projectId, userId);
        return changedProjectType;
    }

    @Override
    public Project revertToPersonal(Long projectId, Long userId) {
        final String methodName = "revertToPersonal";
        Project project = getProject(projectId);
        boolean hasCollaborators = collaborationInfoService.hasCollaborators(projectId);

        isActionPermitted(methodName, project, userId);

        if (project.isPersonal()) {
            log.warn("[{}] Failed to revert project type. Project id={} is already personal.",
                    methodName, projectId);
            throw new IllegalStateException("Project has been already personal.");
        }

        if(hasCollaborators) {
            log.warn("[{}] Failed to revert project type. Project id={} still have members",
                    methodName, projectId);
            throw new ProjectMembersExistException(projectId);
        }

        Project changeProjectType = repository.changeProjectType(project.getId(), true);
        log.info("[{}] Project id={} type changed by user id={}", methodName, changeProjectType.getId(), userId);
        return changeProjectType;
    }

    private void isActionPermitted(String context, Project project, Long clientId) {
        log.debug("[{}] Checking if userId={} is owner of projectId={}", context, clientId, project.getId());

        if (!Objects.equals(project.getOwner(), clientId)) {
            log.warn("[{}] Unauthorized access attempt: userId={} is not the owner of projectId={}",
                    context, clientId, project.getId());
            throw new UnauthorizedException();
        }
    }

    private Project getProject(Long projectId) {
        log.debug("getProject: Attempt to find project id={}", projectId);
        return repository.findById(projectId).orElseThrow(
                () -> {
                    log.debug("getProject: Project id={} not found", projectId);
                    return new ProjectNotFoundException("Project with id " + projectId + "not found");
                }
        );
    }
}
