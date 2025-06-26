package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.CreateTaskWithEmailAssignee;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.dto.UpdateTaskRequest;
import com.anrisys.projectcollabmanager.entity.Task;

import java.util.List;

public interface TaskService {
    Task create(CreateTaskRequest task, Long clientId);
    Task createWithEmailAssignee(CreateTaskWithEmailAssignee request, Long clientId);
    Task getTaskById(Long taskId, Long clientId);
    Task getTaskByTitle(String title, Long clientId);
    Task deleteById(Long taskId, Long clientId);

    List<TaskDTO> getAllTaskByProjectId(Long projectId, Long clientId);
    List<TaskDTO> getAllTaskByProjectIdAndAssigneeId(Long projectId, Long userId);
    List<TaskDTO> getAllTaskByProjectIdAndStatus(Long projectId, Long clientId);

    Task update(Long taskId, Long clientId, UpdateTaskRequest request);
    Task changeAssignee(Long taskId, Long clientId, String assigneeEmail);
    Task updateStatus(Long taskId, Long clientId, Task.Status status);
}
