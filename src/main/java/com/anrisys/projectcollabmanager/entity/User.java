package com.anrisys.projectcollabmanager.entity;

import com.anrisys.projectcollabmanager.util.PasswordUtil;

import java.util.Objects;

public class User {
    private Long id;
    private String email;
    private String hashedPassword;

    public User() {
    }

    public User(String email, String rawPassword) {
        this.email = email;
        this.hashedPassword = PasswordUtil.hash(rawPassword);
    }

    public User(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public User(Long id, String email, String hashedPassword) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String rawPassword) {
        this.hashedPassword = PasswordUtil.hash(rawPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
