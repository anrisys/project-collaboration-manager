package com.anrisys.projectcollabmanager.entity;

import java.time.Instant;

public class Task {
    public enum Status {
        TODO, IN_PROGRESS, DONE
    }

    private Long id;
    private String title;
    private String shortDescription;
    private Long projectId;
    private Long assigneeId;
    private Status status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Task(Long id, String title, String shortDescription, Long projectId, Long assigneeId, Status status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.shortDescription = shortDescription;
        this.projectId = projectId;
        this.status = Status.TODO;
        this.assigneeId = assigneeId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Task fromDB(
            Long id,
            String title,
            String shortDescription,
            Long projectId,
            Long assigneeId,
            String status,
            Instant createdAt,
            Instant updatedAt)
    {
        return new Task(
                id,
                title,
                shortDescription,
                projectId,
                assigneeId,
                Status.valueOf(status), // Convert String to Enum
                createdAt,
                updatedAt
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
