package com.anrisys.projectcollabmanager.entity;

public class Task {
    private Long id;
    private String title;
    private String status;
    private Long projectId;
    private Long assigneeId;

    public Task() {}

    public Task(Long id, String title, String status, Long projectId, Long assigneeId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
