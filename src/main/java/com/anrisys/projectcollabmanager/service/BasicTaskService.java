package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.TaskUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskAlreadyExistsException;
import com.anrisys.projectcollabmanager.repository.TaskRepository;

import java.util.List;

public class BasicTaskService implements TaskService{
    private final TaskRepository taskRepository;

    public BasicTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task create(CreateTaskRequest task) {
        boolean isTaskAlreadyExist = taskRepository.existsByProjectIdAndTitle(task.projectId(), task.title());

        if(isTaskAlreadyExist) {
            throw new TaskAlreadyExistsException(task.title());
        }

        return taskRepository.save(task);
    }

    @Override
    public Task findById(Long projectId, Long clientId) {
        return null;
    }

    @Override
    public Task findByTitle(String title, Long clientId) {
        return null;
    }

    @Override
    public Task deleteById(Long projectId, Long clientId) {
        return null;
    }

    @Override
    public List<Task> getAllTaskByProjectId(Long projectId, Long clientId) {
        return List.of();
    }

    @Override
    public List<Task> getAllTaskByProjectIdAndAssigneeId(Long projectId) {
        return List.of();
    }

    @Override
    public List<Task> getAllTaskByProjectIdAndStatus(Long projectId, Long clientId) {
        return List.of();
    }

    @Override
    public Task update(Long projectId, Long clientId, TaskUpdateRequest request) {
        return null;
    }

    @Override
    public Task changeAssignee(Long taskId, Long clientId, Long assigneeId) {
        return null;
    }

    @Override
    public Task updateStatus(Long taskId, Long clientId, Task.Status status) {
        return null;
    }
}
