package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;

import java.util.List;

public interface ProjectService {
    Project create(ProjectCreateRequest project);
    Project findProjectById(Long id);
    Project findPersonalProjectById(Long id, Long clientId);
    Project findProjectByTitle(String title);
    Project findPersonalProjectByTitle(String title, Long clientId);
    List<ProjectDTO> listPersonalProjects(Long ownerId);

    Project updateProject(Long projectId, Long clientId, ProjectUpdateRequest request);
    Project deleteProject(Long id, Long clientId);

    void convertToCollaborative(Long projectId, Long userId);
    void revertToPersonal(Long projectId, Long userId);
}
