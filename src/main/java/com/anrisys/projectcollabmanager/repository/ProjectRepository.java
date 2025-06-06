package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Project save(Project project) throws DataAccessException;
    Optional<Project> findById(Long id) throws DataAccessException;
    Optional<Project> findByTitle(String title) throws DataAccessException;
    Optional<List<Project>> findByOwnerId(Long owner) throws DataAccessException;
    Project update(Long id, Project project) throws DataAccessException;
    Project delete(Long id) throws DataAccessException;
    boolean HasSameProjectName(Long owner, String title);
}
