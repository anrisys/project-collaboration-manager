package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(ProjectCreateRequest project) throws DataAccessException;
    Optional<Project> findById(Long id) throws DataAccessException;
    Optional<Project> findByTitle(String title) throws DataAccessException;
    Optional<List<ProjectDTO>> findByOwnerId(Long owner) throws DataAccessException;
    Project update(Long id, ProjectUpdateRequest project) throws DataAccessException;
    Project changeProjectType(Long id, boolean personalization);
    Project delete(Long id) throws DataAccessException;
    boolean HasSameProjectName(Long owner, String title);
}
