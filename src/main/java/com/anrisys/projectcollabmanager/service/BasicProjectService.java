package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.projects.ProjectNotFoundException;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;

import java.util.List;
import java.util.Objects;

public class BasicProjectService implements ProjectService{

    private final ProjectRepository repository;

    public BasicProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public Project create(Project project) {
        return repository.save(project);
    }

    @Override
    public Project findProjectById(Long id, Long clientId) {
        Project project = getFoundProject(id);

        isActionPermitted(project.getOwner(), clientId);

        return getFoundProject(id);
    }

    @Override
    public Project findProjectByTitle(String title, Long clientId) {
        Project project = repository.findByTitle(title).orElseThrow(
                () -> new ProjectNotFoundException("Project with title : " + title + "not found")
        );

        isActionPermitted(project.getOwner(), clientId);

        return project;
    }

    @Override
    public List<Project> listProjectsByOwner(Long ownerId) {
        return repository.findByOwnerId(ownerId).orElseThrow(
                () -> new ProjectNotFoundException("Project for user not found")
        );
    }

    @Override
    public Project updateProject(Long projectId, Long clientId, ProjectUpdateRequest request) {
        Project project = getFoundProject(projectId);
        isActionPermitted(project.getOwner(), clientId);

        return repository.update(project.getId(), request);
    }

    @Override
    public Project deleteProject(Long projectId, Long clientId) {
        Project project = getFoundProject(projectId);

        isActionPermitted(project.getOwner(), clientId);

        return repository.delete(projectId);
    }

    private void isActionPermitted(Long projectOwner, Long clientId) {
        if (!Objects.equals(projectOwner, clientId)) {
            throw new UnsupportedOperationException();
        }
    }

    private Project getFoundProject(Long projectId) {
        return repository.findById(projectId).orElseThrow(
                () -> new ProjectNotFoundException("Project with id " + projectId + "not found")
        );
    }
}
