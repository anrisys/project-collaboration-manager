package com.anrisys.projectcollabmanager.entity;

import java.time.Instant;

public class Collaboration {
    private Long id;
    private Long projectId;
    private Long userId;
    private Instant createdAt;
    private Instant updatedAt;

    public Collaboration(Long id, Long projectId, Long userId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Collaboration fromDB(Long id, Long projectId, Long userId, Instant createdAt, Instant updatedAt) {
        return new Collaboration(id, projectId, userId, createdAt, updatedAt);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
