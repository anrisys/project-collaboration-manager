package com.anrisys.projectcollabmanager.exception.projects;

import com.anrisys.projectcollabmanager.exception.core.BusinessException;

public class UserIsNotProjectMemberException extends BusinessException {
    public UserIsNotProjectMemberException() {
        super("User is not in the project");
    }
}
