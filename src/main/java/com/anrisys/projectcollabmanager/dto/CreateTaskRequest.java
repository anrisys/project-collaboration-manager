package com.anrisys.projectcollabmanager.dto;

public record CreateTaskRequest(
        String title,
        String shortDescription,
        Long projectId,
        Long assigneeId
) {}
