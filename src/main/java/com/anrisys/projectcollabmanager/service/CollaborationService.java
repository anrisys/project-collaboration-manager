package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.entity.Project;

import java.util.List;

public interface CollaborationService {
    List<ProjectDTO> listCollaborationProjects(Long userId);
    List<UserDTO> listProjectMembers(Long projectId);
    Project showCollaborationProject(Long projectId, Long userId);
    Collaboration inviteUserToProjectById(Long projectId, Long inviterUserId, Long inviteeUserId);
    Collaboration inviteUserToProjectByEmail(Long projectId, Long inviterUserId, String inviteeEmail);
    void removeUserFromProjectById(Long projectId, Long projectOwnerId, Long memberId);
    void removeUserFromProjectByUserEmail(Long projectId, Long projectOwnerId, String memberEmail);
    boolean isUserMember(Long projectId, Long userId);
    void leaveProjectCollaboration(Long projectId, Long userId);
}
