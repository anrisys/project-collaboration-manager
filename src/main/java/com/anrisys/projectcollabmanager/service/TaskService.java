package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.CreateTaskWithEmailAssignee;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.dto.TaskUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Task;

import java.util.List;

public interface TaskService {
    Task create(CreateTaskRequest task, Long clientId);
    Task createWithEmailAssignee(CreateTaskWithEmailAssignee request, Long clientId);
    Task getTaskById(Long taskId, Long clientId);
    Task getTaskByTitle(String title, Long clientId);
    Task deleteById(Long projectId, Long clientId);

    List<TaskDTO> getAllTaskByProjectId(Long projectId, Long clientId);
    List<TaskDTO> getAllTaskByProjectIdAndAssigneeId(Long projectId);
    List<TaskDTO> getAllTaskByProjectIdAndStatus(Long projectId, Long clientId);

    Task update(Long projectId, Long clientId, TaskUpdateRequest request);
    Task changeAssignee(Long taskId, Long clientId, Long assigneeId);
    Task updateStatus(Long taskId, Long clientId, Task.Status status);
}
