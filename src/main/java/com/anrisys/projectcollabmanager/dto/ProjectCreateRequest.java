package com.anrisys.projectcollabmanager.dto;

public record ProjectCreateRequest(String title, Long owner, boolean isPersonal ,String description) {
}
