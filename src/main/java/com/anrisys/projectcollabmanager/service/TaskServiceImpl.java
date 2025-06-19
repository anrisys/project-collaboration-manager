package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.dto.TaskUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.UnauthorizedException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskAlreadyExistsException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskNotFoundException;
import com.anrisys.projectcollabmanager.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

public class TaskServiceImpl implements TaskService{
    private final TaskRepository taskRepository;
    private final CollaborationService collaborationService;

    public TaskServiceImpl(TaskRepository taskRepository, CollaborationService collaborationService) {
        this.taskRepository = taskRepository;
        this.collaborationService = collaborationService;
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
    public Task getTaskById(Long taskId, Long clientId) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException(taskId)
        );

        boolean userHasPermission = collaborationService.isUserMember(task.getProjectId(), clientId);

        if (!userHasPermission) throw new UnauthorizedException();

        // What if the task isn't in collaboration project?
        // Should every personal project count/saved into collaboration? --> circular dependency

        return task;
    }

    @Override
    public Task getTaskByTitle(String title, Long clientId) {
        return null;
    }

    @Override
    public Task deleteById(Long projectId, Long clientId) {
        return null;
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectId(Long projectId, Long clientId) {
        return List.of();
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectIdAndAssigneeId(Long projectId) {
        return List.of();
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectIdAndStatus(Long projectId, Long clientId) {
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
