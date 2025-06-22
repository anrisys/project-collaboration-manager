package com.anrisys.projectcollabmanager.exception.projects;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;

public class ProjectMembersExistException extends BusinessException {
    public ProjectMembersExistException(Long projectId) {
        super("Your project with id : %d still has members on it".formatted(projectId));
    }
}
