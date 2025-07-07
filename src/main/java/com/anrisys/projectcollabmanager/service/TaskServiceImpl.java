package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.*;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.UnauthorizedException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskAlreadyExistsException;
import com.anrisys.projectcollabmanager.exception.tasks.TaskNotFoundException;
import com.anrisys.projectcollabmanager.repository.TaskRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class TaskServiceImpl implements TaskService{
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CollaborationService collaborationService;
    private final ProjectService projectService;
    private final static Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    public TaskServiceImpl(TaskRepository taskRepository, UserService userService, CollaborationService collaborationService, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.collaborationService = collaborationService;
        this.projectService = projectService;
    }

    @Override
    public Task create(CreateTaskRequest task, Long clientId) {
        final String methodName = "create";
        log.debug("[{}] Attempt to create task for userId={}", methodName, clientId);

        log.debug("[{}] Checking if task with same title already exist", methodName);
        boolean isTaskAlreadyExist = taskRepository.existsByProjectIdAndTitle(task.projectId(), task.title());

        if(isTaskAlreadyExist) {
            log.warn("[{}] Task with same title already exist", methodName);
            throw new TaskAlreadyExistsException(task.title());
        }

        Task saved = taskRepository.save(task);
        log.info("[{}] Created taskId={} for userId={}", methodName, saved.getId(), clientId);
        return saved;
    }

    @Override
    public Task createWithEmailAssignee(CreateTaskWithEmailAssignee request, Long clientId) {
        final String methodName = "createWithEmailAssignee";
        log.debug("[{}] Attempt to create task with email assignee by userId={}", methodName, clientId);

        log.debug("[{}] Checking if assignee email registered", methodName);
        UserDTO userDTO = userService.findByEmail(request.emailAssignee());

        log.debug("[{}] Checking if projectId={} exist", methodName, request.projectId());
        Project projectById = projectService.findProjectById(request.projectId());

        log.debug("[{}] Checking if assigneeId={} is member of projectId={}", methodName, userDTO.id(), request.projectId());
        boolean isUserMember = collaborationService.isUserMember(projectById.getId(), userDTO.id());

        if (!isUserMember) {
            log.warn("[{}] Can't create task with assigneeEmail={}. Assignee is not part of the project",
                    methodName, LoggerUtil.maskEmail(request.emailAssignee()));
            throw new IllegalArgumentException("Assignee is not part of project");
        }

        var requestData = new CreateTaskRequest(request.title(), request.shortDescription(), projectById.getId(), userDTO.id());

        Task task = create(requestData, clientId);
        log.info("[{}] TaskId={} created for assigneeId={} by userId={}",
                methodName, task.getId(),task.getAssigneeId(), clientId);

        return task;
    }

    @Override
    public Task getTaskById(Long taskId, Long clientId) {
        final String methodName = "getTaskById";
        log.debug("[{}] Attempt to retrieve task byId={}", methodName, taskId);
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> {
                    log.debug("[{}] TaskId={} not found", methodName, taskId);
                    return new TaskNotFoundException(taskId);
                }
        );

        isUserProjectMember(methodName, task.getProjectId(), clientId);

        return task;
    }

    @Override
    public Task getTaskByTitle(String title, Long clientId) {
        return null;
    }

    @Override
    public Task deleteById(Long taskId, Long clientId) {
        final String methodName="deleteById";
        log.debug("[{}] Attempt to delete taskId={} by userId={}", methodName, taskId, clientId);
        Task task = getTaskById(taskId, clientId);

        isUserProjectOwner(methodName, clientId, task.getProjectId());

        taskRepository.deleteById(taskId);
        log.info("[{}] TaskId={} deleted by userId={}", methodName, taskId, clientId);
        return task;
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectId(Long projectId, Long clientId) {
        final String methodName = "getAllTaskByProjectId";
        log.debug("[{}] Attempt to retrieve project tasks by projectId={} by userId={}", methodName, projectId, clientId);

        isUserProjectMember(methodName, projectId, clientId);

        return taskRepository.findAllByProjectId(projectId).orElseThrow(
                TaskNotFoundException::new
        );
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectIdAndAssigneeId(Long projectId, Long clientId) {
        final String methodName = "getAllTaskByProjectIdAndAssigneeId";
        log.debug("[{}] Attempt to retrieve project tasks of projectId={} and userId={}", methodName, projectId, clientId);

        isUserProjectMember(methodName, projectId, clientId);

        return taskRepository.findAllByProjectIdAndAssigneeId(projectId, clientId).orElseThrow(
                TaskNotFoundException::new
        );
    }

    @Override
    public List<TaskDTO> getAllTaskByProjectIdAndStatus(Long projectId, Long clientId) {
        return List.of();
    }

    @Override
    public Task update(Long taskId, Long clientId, UpdateTaskRequest request) {
        final String methodName = "update";
        log.debug("[{}] Attempt to update taskId={} by userId={}", methodName, taskId, clientId);

        Task taskById = getTaskById(taskId, clientId);
        isUserProjectOwner(methodName, clientId, taskById.getId());

        Task updated = taskRepository.update(taskId, request);
        log.info("[{}] TaskId={} updated by userId={}", methodName, taskById.getId(), clientId);
        return updated;
    }

    @Override
    public Task changeAssignee(Long taskId, Long clientId, String assigneeEmail) {
        final String methodName = "changeAssignee";
        log.debug("[{}] Attempt to change assignee of taskId={} into assigneeEmail={} by userId={}",
                methodName, taskId, LoggerUtil.maskEmail(assigneeEmail), clientId);

        Task taskById = getTaskById(taskId, clientId);

        isUserProjectOwner(methodName, clientId, taskById.getId());

        log.debug("[{}] Checking email assignee={}  registered", methodName, LoggerUtil.maskEmail(assigneeEmail));
        UserDTO userDTO = userService.findByEmail(assigneeEmail);

        isUserProjectMember(methodName, taskById.getProjectId(), userDTO.id());

        Task task = taskRepository.updateAssignee(taskId, userDTO.id());
        log.info("[{}] Assignee changed for taskId={} by userId={}", methodName, task.getId(), clientId);
        return task;
    }

    @Override
    public Task updateStatus(Long taskId, Long clientId, Task.Status status) {
        final String methodName = "updateStatus";
        log.debug("[{}] Attempt to update status of taskId={} by userId={}", methodName, taskId, clientId);
        Task taskById = getTaskById(taskId, clientId);

        boolean userAssignee = isUserAssignee(methodName, taskId, clientId);

        boolean userProjectOwner = isUserProjectOwner(methodName, clientId, taskById.getProjectId());

        if (!userAssignee && !userProjectOwner) {
            log.warn("[{}] Unauthorized action. UserId={} is not assignee or project owner of taskId={}",
                    methodName, clientId, taskId);
            throw new UnauthorizedException();
        }

        Task updatedStatus = taskRepository.updateStatus(taskId, status);
        log.info("[{}] Status of taskId={} updated by userId={}", methodName, taskId, clientId);
        return updatedStatus;
    }

    private boolean isUserAssignee(String methodName, Long clientId, Long taskId) {
        log.debug("[{}] Checking if userId={} is assignee of taskId={}", methodName, clientId, taskId);
        Task taskById = getTaskById(taskId, clientId);

        return !Objects.equals(taskById.getAssigneeId(), clientId);
    }

    private boolean isUserProjectOwner(String methodName, Long clientId, Long projectId) {
        log.debug("[{}] Checking if userId={} is owner of projectId={}", methodName, clientId, projectId);
        log.debug("[{}] Checking if projectId={} exists", methodName, projectId);
        Project project = projectService.findProjectById(projectId);

        boolean isUserOwner = Objects.equals(project.getOwner(), clientId);

        if (!isUserOwner) {
            log.warn("[{}] Unauthorized action. UserId={} is now owner of projectId={}", methodName, clientId, projectId);
            throw new UnauthorizedException();
        }
        return true;
    }

    private void isUserProjectMember(String methodName, Long projectId, Long clientId) {
        log.debug("[{}] Checking if clientId={} is member of projectId={}", methodName, clientId, projectId);
        boolean userMember = collaborationService.isUserMember(projectId, clientId);

        if (!userMember) {
            log.warn("[{}] Unauthorized action. ClientId={} is not member of projectId={}",
                    methodName, clientId, projectId);
            throw new UnauthorizedException();
        }
    }
}
