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

import java.util.List;
import java.util.Objects;

public class BasicProjectService implements ProjectService{

    private final ProjectRepository repository;
    private final CollaborationInfoService collaborationInfoService;

    public BasicProjectService(ProjectRepository repository, CollaborationInfoService collaborationInfoService) {
        this.repository = repository;
        this.collaborationInfoService = collaborationInfoService;
    }

    @Override
    public Project create(ProjectCreateRequest request) {
        boolean hasSameProjectName = repository.HasSameProjectName(request.owner(), request.title());

        if (hasSameProjectName) {
            throw new HasSameProjectNameException();
        }

        return repository.save(request);
    }

    @Override
    public Project findProjectById(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ProjectNotFoundException("Project with id " + id + "not found")
        );
    }

    @Override
    public Project findPersonalProjectById(Long id, Long clientId) {
        Project project = findProjectById(id);

        isActionPermitted(project.getOwner(), clientId);

        return project;
    }

    @Override
    public Project findProjectByTitle(String title) {
        return repository.findByTitle(title).orElseThrow(
                () -> new ProjectNotFoundException("Project with title : " + title + "not found")
        );
    }

    @Override
    public Project findPersonalProjectByTitle(String title, Long clientId) {
        Project project = findProjectByTitle(title);

        isActionPermitted(project.getOwner(), clientId);

        return project;
    }

    @Override
    public List<ProjectDTO> listPersonalProjects(Long ownerId) {
        return repository.findByOwnerId(ownerId).orElseThrow(
                () -> new ProjectNotFoundException("Personal project not found")
        );
    }

    @Override
    public Project updateProject(Long projectId, Long clientId, ProjectUpdateRequest request) {
        Project project = getProject(projectId);
        isActionPermitted(project.getOwner(), clientId);

        return repository.update(project.getId(), request);
    }

    @Override
    public Project deleteProject(Long projectId, Long clientId) {
        Project project = getProject(projectId);

        isActionPermitted(project.getOwner(), clientId);

        return repository.delete(projectId);
    }

    @Override
    public Project convertToCollaborative(Long projectId, Long userId) {
        Project project = getProject(projectId);

        isActionPermitted(project.getOwner(), userId);

        if (!project.isPersonal()) {
            throw new IllegalStateException("Project has been already collaborative.");
        }

        return repository.changeProjectType(project.getId(), false);
    }

    @Override
    public Project revertToPersonal(Long projectId, Long userId) {
        Project project = getProject(projectId);
        boolean hasCollaborators = collaborationInfoService.hasCollaborators(projectId);

        isActionPermitted(project.getOwner(), userId);

        if (project.isPersonal()) {
            throw new IllegalStateException("Project has been already personal.");
        }

        if(hasCollaborators) {
            throw new ProjectMembersExistException(projectId);
        }

        return repository.changeProjectType(project.getId(), true);
    }

    private void isActionPermitted(Long projectOwner, Long clientId) {
        if (!Objects.equals(projectOwner, clientId)) {
            throw new UnauthorizedException();
        }
    }

    private Project getProject(Long projectId) {
        return repository.findById(projectId).orElseThrow(
                () -> new ProjectNotFoundException("Project with id " + projectId + "not found")
        );
    }
}
