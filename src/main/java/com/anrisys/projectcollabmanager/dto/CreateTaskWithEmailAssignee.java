package com.anrisys.projectcollabmanager.dto;

public record CreateTaskWithEmailAssignee(
        String title,
        String shortDescription,
        Long projectId,
        String emailAssignee
) {
}