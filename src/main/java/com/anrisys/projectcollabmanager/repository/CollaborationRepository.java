package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import java.util.List;
import java.util.Optional;

public interface CollaborationRepository {
    Collaboration create(CreateCollaborationRequest request) throws DataAccessException;
    void delete(Long id) throws DataAccessException;

    Optional<List<UserDTO>> findMembersByProjectId(Long projectId) throws DataAccessException;
    Optional<List<ProjectDTO>> findCollaborationsByUserId(Long projectId) throws DataAccessException;

    boolean existsByProjectIdAndUserId(Long projectId, Long userId) throws DataAccessException;
}
