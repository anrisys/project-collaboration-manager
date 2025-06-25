package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.*;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.UnauthorizedException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskAlreadyExistsException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskNotFoundException;
import com.anrisys.projectcollabmanager.repository.TaskRepository;

import java.util.List;
import java.util.Objects;

public class TaskServiceImpl implements TaskService{
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CollaborationService collaborationService;
    private final ProjectService projectService;

    public TaskServiceImpl(TaskRepository taskRepository, UserService userService, CollaborationService collaborationService, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.collaborationService = collaborationService;
        this.projectService = projectService;
    }

    @Override
    public Task create(CreateTaskRequest task, Long clientId) {
        boolean isTaskAlreadyExist = taskRepository.existsByProjectIdAndTitle(task.projectId(), task.title());

        if(isTaskAlreadyExist) {
            throw new TaskAlreadyExistsException(task.title());
        }

        return taskRepository.save(task);
    }

    @Override
    public Task createWithEmailAssignee(CreateTaskWithEmailAssignee request, Long clientId) {
        UserDTO userDTO = userService.findByEmail(request.emailAssignee());

        boolean isUserMember = collaborationService.isUserMember(request.projectId(), userDTO.id());

        if (!isUserMember) {
            throw new IllegalArgumentException("Assignee is not part of project");
        }

        var requestData = new CreateTaskRequest(request.title(), request.shortDescription(), request.projectId(), userDTO.id());

        return create(requestData, clientId);
    }

    @Override
    public Task getTaskById(Long taskId, Long clientId) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException(taskId)
        );

        boolean userProjectMember = isUserProjectMember(task.getProjectId(), clientId);

        if (!userProjectMember) throw new UnauthorizedException();

        return task;
    }

    @Override
    public Task getTaskByTitle(String title, Long clientId) {
        return null;
    }

    @Override
    public Task deleteById(Long taskId, Long clientId) {
        Task task = getTaskById(taskId, clientId);

        boolean userProjectOwner = isUserProjectOwner(clientId, task);

        if (!userProjectOwner) throw new UnauthorizedException();

        return taskRepository.deleteById(taskId);
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectId(Long projectId, Long clientId) {
        boolean userProjectMember = isUserProjectMember(projectId, clientId);

        if (!userProjectMember) throw new UnauthorizedException();

        return taskRepository.findAllByProjectId(projectId).orElseThrow(
                TaskNotFoundException::new
        );
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectIdAndAssigneeId(Long projectId, Long userId) {
        boolean userProjectMember = isUserProjectMember(projectId, userId);

        if (!userProjectMember) throw new UnauthorizedException();

        return taskRepository.findAllByProjectIdAndAssigneeId(projectId, userId).orElseThrow(
                TaskNotFoundException::new
        );
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
    public Task changeAssignee(Long taskId, Long clientId, String assigneeEmail) {
        Task taskById = getTaskById(taskId, clientId);

        boolean projectOwner = isUserProjectOwner(clientId, taskById);

        if (!projectOwner) throw new UnauthorizedException();

        UserDTO userDTO = userService.findByEmail(assigneeEmail);

        boolean projectMember = isUserProjectMember(taskById.getProjectId(), userDTO.id());

        if (!projectMember) throw new IllegalArgumentException("Assignee is not part of the project.");

        return taskRepository.updateAssignee(taskId, userDTO.id());
    }

    @Override
    public Task updateStatus(Long taskId, Long clientId, Task.Status status) {
        Task taskById = getTaskById(taskId, clientId);

        boolean userAssignee = isUserAssignee(taskId, clientId);

        boolean userProjectOwner = isUserProjectOwner(clientId, taskById);

        if (!userAssignee && !userProjectOwner) {
            throw new UnauthorizedException();
        }

        return taskRepository.updateStatus(taskId, status);
    }

    private boolean isUserAssignee(Long taskId, Long clientId) {
        Task taskById = getTaskById(taskId, clientId);

        return !Objects.equals(taskById.getAssigneeId(), clientId);
    }

    private boolean isUserProjectOwner(Long clientId, Task task) {
        Project project = projectService.findProjectById(task.getProjectId());

        return Objects.equals(project.getOwner(), clientId);
    }

    private boolean isUserProjectMember(Long projectId, Long clientId) {
        return collaborationService.isUserMember(projectId, clientId);
    }
}
