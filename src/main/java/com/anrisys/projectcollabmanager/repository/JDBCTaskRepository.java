package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.TaskDTO;
import com.anrisys.projectcollabmanager.dto.UpdateTaskRequest;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCTaskRepository implements TaskRepository{
    private final DataSource dataSource;
    private final static Logger log = LoggerFactory.getLogger(JDBCTaskRepository.class);

    public JDBCTaskRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Task save(CreateTaskRequest task) throws DataAccessException {
        final String sql =
                "INSERT INTO tasks(title, short_description, project_id, assignee_id) VALUES(?, ?, ?, ?)";
        final String exceptionMessage = "Failed to create task of projectId : %d".formatted(task.projectId());
        final String methodName = "save";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){
          statement.setString(1, task.title());
          statement.setString(2, task.shortDescription());
          statement.setLong(3, task.projectId());
          statement.setLong(4, task.assigneeId());

          int affectedRow = statement.executeUpdate();
          LoggerUtil.logSQLExecuted(log, methodName);
          if(affectedRow != 1) {
              LoggerUtil.logUnexpectedRowAffectedQuery(log, methodName, exceptionMessage, affectedRow);
              throw new DataAccessException(exceptionMessage);
          }

          try (ResultSet resultSet = statement.getGeneratedKeys()) {
              if(!resultSet.next()) {
                  log.warn("[{}] Failed to retrieve generated task ID", methodName);
                  throw new DataAccessException(exceptionMessage);
              }

              return findById(resultSet.getLong(1)).orElseThrow(
                      () -> {
                          String retrieveErrorMessage = "Failed to fetch newly created task of projectId " + task.projectId();
                          log.warn("[{}] {}", methodName, retrieveErrorMessage);
                          return new DataAccessException(retrieveErrorMessage);
                      }
              );
          }

        } catch (Exception e) {
            LoggerUtil.logDatabaseError(log, methodName, exceptionMessage, e);
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Optional<Task> findById(Long id) throws DataAccessException {
        final String sql = "SELECT * FROM tasks WHERE id = ?";
        final String methodName = "finalById";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ){
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(
                        getFromDB(resultSet)
                );
            }
        } catch (SQLException e) {
            String message = "Failed to fetch task with id : " + id;
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<Task> findByTitle(String title) throws DataAccessException {
        final String sql = "SELECT * FROM tasks WHERE title = ?";
        final String methodName = "findByTitle";
        LoggerUtil.logSQL(log, methodName, sql);
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(
                        getFromDB(resultSet)
                );
            }
        } catch (SQLException e) {
            String message = "Failed to fetch task with title : " + title;
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public void deleteById(Long id) throws DataAccessException {
        final String sql = "DELETE FROM tasks WHERE id = ?";
        final String exceptionMessage = "Failed to delete a task with id : %d".formatted(id);
        final String methodName = "deleteById";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("[{}] {}: expected affected row 1 got {}", methodName, exceptionMessage, affectedRow);
                throw new DataAccessException(exceptionMessage);
            }
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, exceptionMessage, e);
            throw new DataAccessException(exceptionMessage);
        }
    }

    @Override
    public Optional<List<TaskDTO>> findAllByProjectId(Long projectId) throws DataAccessException {
        final String sql = "SELECT id, title, status FROM tasks WHERE project_id = ?";
        final String methodName = "findAllByProjectId";
        LoggerUtil.logSQL(log, methodName, sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(!resultSet.next()) return Optional.empty();
                List<TaskDTO> tasks = new ArrayList<>();
                while(resultSet.next()) {
                    tasks.add(getTaskDTO(resultSet));
                }
                return Optional.of(tasks);
            }
        } catch (SQLException e) {
            String message = "Failed to fetch tasks in project with id : " + projectId;
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<List<TaskDTO>> findAllByProjectIdAndAssigneeId(Long projectId, Long assigneeId) throws DataAccessException {
        final String sql = "SELECT id, title, status FROM tasks WHERE project_id = ? AND assignee_id = ?";
        final String methodName = "findAllByProjectIdAndAssigneeId";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setLong(2, assigneeId);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(!resultSet.next()) return Optional.empty();
                List<TaskDTO> tasks = new ArrayList<>();
                while(resultSet.next()) {
                    tasks.add(getTaskDTO(resultSet));
                }
                return Optional.of(tasks);
            }
        } catch (SQLException e) {
            String message = "Failed to fetch tasks in project with id : %d and with assignee id : %d".formatted(
                    projectId, assigneeId
            );
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<List<TaskDTO>> findAllByProjectIdAndByStatus(Long projectId, Task.Status status) throws DataAccessException {
        final String sql = "SELECT id, title, status FROM tasks WHERE project_id = ? AND status = ?";
        final String methodName = "findAllByProjectIdAndByStatus";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setString(2, status.name());

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(!resultSet.next()) return Optional.empty();
                List<TaskDTO> tasks = new ArrayList<>();
                while(resultSet.next()) {
                    tasks.add(getTaskDTO(resultSet));
                }
                return Optional.of(tasks);
            }
        } catch (SQLException e) {
            String message = "Failed to fetch tasks in project with id : %d and with task status %s".formatted(
                    projectId, status.name());
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(
                    message
            );
        }
    }

    @Override
    public Task update(Long id, UpdateTaskRequest request) throws DataAccessException {
        StringBuilder sql = new StringBuilder ("UPDATE tasks SET ");
        final String methodName = "update";
        LoggerUtil.logSQL(log, methodName, sql.toString());
        if (request.title() != null && request.shortDescription() != null) {
            sql.append("title = ?, short_description = ?  ");
        }
        if(request.title() == null && request.shortDescription() != null) {
            sql.append("short_description = ? ");
        }
        sql.append("WHERE id = ?");

        String message = "Failed to update task with id : " + id;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            if (request.title() != null && request.shortDescription() != null) {
                statement.setString(1, request.title());
                statement.setString(2, request.shortDescription());
                statement.setLong(3, id);
            }
            if(request.title() == null && request.shortDescription() != null) {
                statement.setString(1, request.shortDescription());
                statement.setLong(2, id);
            }

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                LoggerUtil.logUnexpectedRowAffectedQuery(log, methodName, message, affectedRow);
                throw new DataAccessException(message);
            }

            return findById(id).orElseThrow(
                    () -> {
                        String retrieveErrorMessage = "Failed to fetch newly updated task with id : " + id;
                        log.warn("[{}] {}", methodName, retrieveErrorMessage);
                        return new DataAccessException(retrieveErrorMessage);
                    }
            );
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Task updateAssignee(Long taskId, Long assigneeId) throws DataAccessException {
        final String sql = "UPDATE tasks SET assignee_id = ? WHERE id = ?";
        final String methodName = "updateAssignee";
        LoggerUtil.logSQL(log, methodName, sql);
        String message = "Failed to update assignee for taskId : " + taskId;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, assigneeId);
            statement.setLong(2, taskId);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if (affectedRow != 1) {
                LoggerUtil.logUnexpectedRowAffectedQuery(log, methodName, message, affectedRow);
                throw new DataAccessException(message);
            };

            return findById(taskId).orElseThrow(
                    () -> {
                        String retrieveErrorMessage = "Failed to fetch newly updated assignee task with id : " + taskId;
                        log.warn("[{}] {}", methodName, retrieveErrorMessage);
                        return new DataAccessException(retrieveErrorMessage);
                    }
            );
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Task updateStatus(Long taskId, Task.Status status) throws DataAccessException {
        final String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        final String methodName = "updateStatus";
        LoggerUtil.logSQL(log, methodName, sql);
        String message = "Failed to update status of task with id : %d into %s".formatted(
                taskId, status.name()
        );
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, status.name());
            statement.setLong(2, taskId);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if (affectedRow != 1) {
                LoggerUtil.logUnexpectedRowAffectedQuery(log, methodName, message, affectedRow);
                throw new DataAccessException(message);
            }

            return findById(taskId).orElseThrow(
                    () -> {
                        String errorRetrieveMessage = "Failed to retrieve status updated task id : %d".formatted(taskId);
                        log.warn("[{}] {}", methodName, errorRetrieveMessage);
                        return new DataAccessException(errorRetrieveMessage);
                    }
            );
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public boolean existsByProjectIdAndTitle(Long projectId, String title) throws DataAccessException {
        final String sql = "COUNT(*) FROM tasks WHERE project_id = ? AND title = ?";
        final String methodName = "existsByProjectIdAndTitle";
        String message = "Failed to check if task in project with id : %d and title :  %s".formatted(projectId, title);
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setString(2, title);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) {
                    log.warn("[{}] {}", methodName, message);
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public boolean existsById(Long taskId) throws DataAccessException {
        final String sql = "COUNT(*) FROM tasks WHERE id = ?";
        final String methodName = "existsById";
        final String message = "Failed to fetch tasks with id : " + taskId;
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, taskId);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) {
                    log.warn("[{}] {}", methodName, message);
                    return false;
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    private static TaskDTO getTaskDTO(ResultSet resultSet) throws SQLException {
        return new TaskDTO(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                Task.Status.valueOf(resultSet.getString("status"))
        );
    }

    private static Task getFromDB(ResultSet resultSet) throws SQLException {
        return Task.fromDB(resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getString("short_description"),
                resultSet.getLong("project_id"),
                resultSet.getLong("assignee_id"),
                resultSet.getString("status"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("updated_at").toInstant()
        );
    }
}
