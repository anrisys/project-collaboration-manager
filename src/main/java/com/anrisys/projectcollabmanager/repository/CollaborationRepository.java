package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import java.util.List;
import java.util.Optional;

public interface CollaborationRepository {
    Optional<Collaboration> findById(Long id) throws DataAccessException;
    Collaboration create(CreateCollaborationRequest request) throws DataAccessException;
    Collaboration deleteById(Long id) throws DataAccessException;

    List<User> findMembersByProjectId(Long projectId) throws DataAccessException;
    List<Project> findProjectsByUserId(Long projectId) throws DataAccessException;

    boolean isUserJoinCollaboration(Long projectId, Long userId) throws DataAccessException;
}
