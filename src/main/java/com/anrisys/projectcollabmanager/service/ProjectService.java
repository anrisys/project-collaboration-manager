package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.entity.Project;

import java.util.List;

public interface ProjectService {
    Project create(Project project);
    Project findProjectById(Long id, Long clientId);
    Project findProjectByTitle(String title, Long clientId);
    List<Project> listProjectsByOwner(Long ownerId);
    Project updateProject(Long projectId, Long clientId, Project project);
    Project deleteProject(Long id, Long clientId);
}
