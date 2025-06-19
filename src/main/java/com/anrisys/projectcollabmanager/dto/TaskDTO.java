package com.anrisys.projectcollabmanager.dto;

import com.anrisys.projectcollabmanager.entity.Task;

public record TaskDTO(
        Long id,
        String title,
        Task.Status status
) {}
