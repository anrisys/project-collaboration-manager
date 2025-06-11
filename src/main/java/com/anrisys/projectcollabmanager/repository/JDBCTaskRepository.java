package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateTaskRequest;
import com.anrisys.projectcollabmanager.dto.TaskUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Task;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCTaskRepository implements TaskRepository{
    private final DataSource dataSource;

    public JDBCTaskRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Task save(CreateTaskRequest task) throws DataAccessException {
        final String sql =
                "INSERT INTO tasks(title, short_description, project_id, assignee_id) VALUES(?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ){
          statement.setString(1, task.title());
          statement.setString(2, task.shortDescription());
          statement.setLong(3, task.projectId());
          statement.setLong(4, task.assigneeId());

          int update = statement.executeUpdate();
          if(update != 1) {
              throw new DataAccessException("Failed to create task : " + task.title());
          }

          try (ResultSet resultSet = statement.getGeneratedKeys()) {
              if(!resultSet.next()) {
                  throw new DataAccessException("Failed to create task : " + task.title());
              }

              return findById(resultSet.getLong(1)).orElseThrow(
                      () -> new DataAccessException("Failed to fetch newly created task")
              );
          }

        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Optional<Task> findById(Long id) throws DataAccessException {
        final String query = "SELECT * FROM tasks WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ){
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(
                        getFromDB(resultSet)
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch task with id : " + id);
        }
    }

    @Override
    public Optional<Task> findByTitle(String title) throws DataAccessException {
        final String query = "SELECT * FROM tasks WHERE title = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(
                        getFromDB(resultSet)
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch task with title : " + title);
        }
    }

    @Override
    public Task deleteById(Long id) throws DataAccessException {
        final String deleteQuery = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteQuery)
        ) {
            Task task = findById(id).orElseThrow(
                    () -> new DataAccessException("Failed to fetch newly created task")
            );

            statement.setLong(1, task.getId());

            int affectedRow = statement.executeUpdate();
            if(affectedRow != 1) {
                throw new DataAccessException("Failed to delete a task with id : " + id);
            }

            return task;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete task with id : " + id);
        }
    }

    @Override
    public Optional<List<Task>> findAllByProjectId(Long projectId) throws DataAccessException {
        final String sql = "SELECT * FROM tasks WHERE project_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            try(ResultSet resultSet = statement.executeQuery()) {
                if(!resultSet.next()) return Optional.empty();
                List<Task> tasks = new ArrayList<>();
                while(resultSet.next()) {
                    tasks.add(getFromDB(resultSet));
                }
                return Optional.of(tasks);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch tasks in project with id : " + projectId);
        }
    }

    @Override
    public Optional<List<Task>> findAllByProjectIdAndAssigneeId(Long projectId, Long assigneeId) throws DataAccessException {
        final String sql = "SELECT * FROM tasks WHERE project_id = ? AND assignee_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setLong(2, assigneeId);

            try(ResultSet resultSet = statement.executeQuery()) {
                if(!resultSet.next()) return Optional.empty();
                List<Task> tasks = new ArrayList<>();
                while(resultSet.next()) {
                    tasks.add(getFromDB(resultSet));
                }
                return Optional.of(tasks);
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to fetch tasks in project with id : %d and with assignee id : %d".formatted(
                            projectId, assigneeId
                    )
            );
        }
    }

    @Override
    public Optional<List<Task>> findAllByProjectIdAndByStatus(Long projectId, Task.Status status) throws DataAccessException {
        final String sql = "SELECT * FROM tasks WHERE project_id = ? AND status = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setString(2, status.name());

            try(ResultSet resultSet = statement.executeQuery()) {
                if(!resultSet.next()) return Optional.empty();
                List<Task> tasks = new ArrayList<>();
                while(resultSet.next()) {
                    tasks.add(getFromDB(resultSet));
                }
                return Optional.of(tasks);
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to fetch tasks in project with id : %d and with task status %s".formatted(
                            projectId,status.name())
            );
        }
    }

    @Override
    public Task update(Long id, TaskUpdateRequest request) throws DataAccessException {
        StringBuilder query = new StringBuilder ("UPDATE tasks SET ");

        if (request.title() != null && request.shortDescription() != null) {
            query.append("title = ?, short_description = ?  ");
        }
        if(request.title() == null && request.shortDescription() != null) {
            query.append("short_description = ? ");
        }
        query.append("WHERE id = ?");

        try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query.toString())
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

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to update task with id : " + id);
            }

            return findById(id).orElseThrow(
                    () -> new DataAccessException("Failed to update task with id : " + id)
            );
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update task with id : " + id);
        }
    }

    @Override
    public Task updateAssignee(Long taskId, Long assigneeId) throws DataAccessException {
        final String sql = "UPDATE tasks SET assignee_id = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, assigneeId);
            statement.setLong(2, taskId);

            int infectedRow = statement.executeUpdate();

            if (infectedRow != 1) throw new DataAccessException("Failed to update assignee for task with id : " + taskId);

            return findById(taskId).orElseThrow(
                    () -> new DataAccessException("Failed to fetch newly updated task with id : " + taskId)
            );
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update assignee for task with id : " + taskId);
        }
    }

    @Override
    public Task updateStatus(Long taskId, Task.Status status) throws DataAccessException {
        final String sql = "UPDATE tasks SET status = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, status.name());
            statement.setLong(2, taskId);

            int infectedRow = statement.executeUpdate();

            if (infectedRow != 1) throw new DataAccessException(
                    "Failed to update status of task with id : %d into %s".formatted(
                            taskId, status.name()
                    )
            );

            return findById(taskId).orElseThrow(
                    () -> new DataAccessException(
                            "Failed to update status of task with id : %d into %s".formatted(
                                    taskId, status.name()
                            )
                    )
            );
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to update status of task with id : %d into %s".formatted(
                            taskId, status.name()
                    )
            );
        }
    }

    @Override
    public boolean existsByProjectIdAndTitle(Long projectId, String title) throws DataAccessException {
        final String sql = "COUNT(*) FROM tasks WHERE project_id = ? AND title = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setString(2, title);

            try(ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            // throw new DataAccessException("Failed to check if task in project with id : " + projectId + " and title : " + title);
            throw new DataAccessException("Failed to check if task in project with id : %d and title :  %s".formatted(projectId, title));
        }
    }

    @Override
    public boolean existsById(Long taskId) throws DataAccessException {
        final String sql = "COUNT(*) FROM tasks WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, taskId);

            try(ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch tasks with id : " + taskId);
        }
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
