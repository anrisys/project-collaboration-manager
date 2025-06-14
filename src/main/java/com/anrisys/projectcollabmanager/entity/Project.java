package com.anrisys.projectcollabmanager.entity;

import java.time.Instant;
import java.util.Objects;

public class Project {
    private Long id;
    private String title;
    private Long owner;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public Project(Long id, String title, Long owner, String description, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Project create(String title, Long owner) {
        return new Project(null, title, owner, null, null, null);
    }

    public static Project createWithDescription(
            String title,
            Long owner,
            String description)
    {
        return new Project(null, title, owner, description, null, null);
    }

    public static Project fromDB(
            Long id,
            String title,
            Long owner,
            String description,
            Instant createdAt,
            Instant updatedAt)
    {
        return new Project(id, title, owner, description, createdAt, updatedAt);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) && Objects.equals(title, project.title) && Objects.equals(owner, project.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, owner);
    }
}
