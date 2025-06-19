package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.dto.TaskUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(CreateTaskRequest task) throws DataAccessException;
    Optional<Task> findById(Long id) throws DataAccessException;
    Optional<Task> findByTitle(String title) throws DataAccessException;
    Task deleteById(Long id) throws DataAccessException;

    Optional<List<TaskDTO>> findAllByProjectId(Long projectId) throws DataAccessException;
    Optional<List<TaskDTO>> findAllByProjectIdAndAssigneeId(Long projectId, Long assigneeId) throws DataAccessException;
    Optional<List<TaskDTO>> findAllByProjectIdAndByStatus(Long projectId, Task.Status status) throws DataAccessException;

    Task update(Long id, TaskUpdateRequest request) throws DataAccessException;
    Task updateAssignee(Long taskId, Long assigneeId) throws DataAccessException;
    Task updateStatus(Long taskId, Task.Status status) throws DataAccessException;

    boolean existsByProjectIdAndTitle(Long projectId, String title) throws DataAccessException;
    boolean existsById(Long taskId) throws DataAccessException;
}
