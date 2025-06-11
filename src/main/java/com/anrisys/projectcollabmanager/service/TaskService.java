package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.TaskUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Task;

import java.util.List;

public interface TaskService {
    Task create(CreateTaskRequest task);
    Task findById(Long projectId, Long clientId);
    Task findByTitle(String title, Long clientId);
    Task deleteById(Long projectId, Long clientId);

    List<Task> getAllTaskByProjectId(Long projectId, Long clientId);
    List<Task> getAllTaskByProjectIdAndAssigneeId(Long projectId);
    List<Task> getAllTaskByProjectIdAndStatus(Long projectId, Long clientId);

    Task update(Long projectId, Long clientId, TaskUpdateRequest request);
    Task changeAssignee(Long taskId, Long clientId, Long assigneeId);
    Task updateStatus(Long taskId, Long clientId, Task.Status status);
}
