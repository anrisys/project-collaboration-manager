package com.anrisys.projectcollabmanager.repository.jdbc;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCProjectRepository implements ProjectRepository {
    private final DataSource dataSource;
    private final static Logger log = LoggerFactory.getLogger(JDBCProjectRepository.class);

    public JDBCProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Project save(ProjectCreateRequest request) throws DataAccessException {
        final String sql = "INSERT INTO projects(title, owner, is_personal, description) VALUES(?, ?, ?, ?)";
        final String methodName = "save";
        LoggerUtil.logSQL(log, methodName, sql);
        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                        );
            ) {

            statement.setString(1, request.title());
            statement.setLong(2, request.owner());
            statement.setBoolean(3, request.isPersonal());

            if (request.description() != null) {
                statement.setString(3, request.description());
            } else {
                statement.setNull(3, 1);
            }

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("[{}] Failed to create project. Expected affected row 1 got {}", methodName, affectedRow);
                throw new DataAccessException("Failed to save new project");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if(!resultSet.next()) {
                    log.warn("[{}] Failed to retrieve generated project ID", methodName);
                    throw new DataAccessException("Failed to retrieve generated project ID");
                }

                Long id = resultSet.getLong(1);

                return findById(id).orElseThrow(() -> {
                    log.warn("[{}] Failed to retrieve new project", methodName);
                    return new DataAccessException("Failed to retrieve new project");
                });
            }
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to create project with title={} for userId={}",
                    methodName, request.title(), request.owner());
            throw new DataAccessException("Failed to create a project", e);
        }
    }

    @Override
    public Optional<Project> findById(Long id) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE id = ? LIMIT 1";
        final String methodName = "findById";
        LoggerUtil.logSQL(log, methodName, sql);
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            )
        {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(resultSet.next()) {
                    Project project = new Project();
                    User owner = new User();
                    owner.setId(resultSet.getLong("owner"));
                    project.setId(resultSet.getLong("id"));
                    project.setTitle(resultSet.getString("title"));
                    project.setPersonal(resultSet.getBoolean("is_personal"));
                    project.setDescription(resultSet.getString("description"));
                    project.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                    project.setUpdatedAt(resultSet.getTimestamp("updated_at").toInstant());
                    return Optional.of(project);
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to retrieve project with id={}",
                    methodName, id);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Project> findByTitle(String title) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE title = ? LIMIT 1";
        final String methodName = "findByTitle";
        LoggerUtil.logSQL(log, methodName, sql);
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
            )
        {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(resultSet.next()) {
                    Project project = new Project();
                    User owner = new User();
                    owner.setId(resultSet.getLong("owner"));
                    project.setId(resultSet.getLong("id"));
                    project.setTitle(resultSet.getString("title"));
                    project.setPersonal(resultSet.getBoolean("is_personal"));
                    project.setDescription(resultSet.getString("description"));
                    project.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                    project.setUpdatedAt(resultSet.getTimestamp("updated_at").toInstant());
                    return Optional.of(project);
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to update project with title={}",
                    methodName, title);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<ProjectDTO>> findByOwnerId(Long owner) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE owner = ?";
        final String methodName = "findByOwnerId";
        LoggerUtil.logSQL(log, methodName, sql);
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        )
        {
            List<ProjectDTO> projects = new ArrayList<>();

            statement.setLong(1, owner);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(resultSet.next()) {
                    while(resultSet.next()) {
                        projects.add(
                                new ProjectDTO(
                                        resultSet.getLong("id"),
                                        resultSet.getString("title"),
                                        resultSet.getBoolean("is_personal"),
                                        resultSet.getLong("owner")
                                )
                        );
                    }
                    return Optional.of(projects);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to retrieve project for userId={}",
                    methodName, owner);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project update(Long id, ProjectUpdateRequest request) throws DataAccessException {
        StringBuilder sql = new StringBuilder("UPDATE projects SET ");
        List<Object> params = new ArrayList<>();
        final String methodName = "update";

        if(request.title() != null) {
            sql.append("title = ?, ");
            params.add(request.title());
        }

        if(request.description() != null) {
            sql.append("description = ?, ");
            params.add(request.description());
        }

        sql.append("WHERE id = ?");
        params.add(id);
        LoggerUtil.logSQL(log, methodName, sql.toString());
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString());
        ) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i, params.get(i));
            }

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("[{}] Failed to update project. Expected affected row 1 got {}", methodName, affectedRow);
                throw new DataAccessException("Failed to update project" + request.title());
            }

            return findById(id).orElseThrow(
                    () -> new DataAccessException("Failed to fetch updated project" + request.title()
                    )
            );
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to update project with id={}",
                    methodName, id);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project changeProjectType(Long id, boolean type) {
        final String sql = "UPDATE projects SET is_personal = ? WHERE id = ?";
        final String exceptionMessage = "Failed to change type of project with id : " + id;
        final String methodName = "changeProjectType";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, type);
            statement.setLong(2, id);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if (affectedRow != 1) {
                log.warn("[{}] Failed to change project type. Expected affected row 1 got {}", methodName, affectedRow);
                throw new DataAccessException(exceptionMessage);
            }

            return findById(id).orElseThrow(
                    () -> new DataAccessException(exceptionMessage)
            );
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to change project type of projectId={}",
                    methodName, id);
            throw new DataAccessException(exceptionMessage, e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        final String sql = "DELETE FROM projects WHERE id = ?";
        final String methodName = "delete";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, id);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("[{}] Failed to delete project. Expected affected row 1 got {}", methodName, affectedRow);
               throw new DataAccessException("Failed to delete a projects");
            }
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to delete project with id={}",
                    methodName, id);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean HasSameProjectName(Long owner, String title) {
        final String sql = "SELECT * FROM projects WHERE owner = ? AND title = ?";
        final String methodName = "hasSameProjectName";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, owner);
            statement.setString(2, title);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                return resultSet.next();
            }
        } catch (SQLException e) {
            log.error("[{}] Database error. Failed to check whether userId={} has same project title={}",
                    methodName, owner, title);
            throw new RuntimeException(e);
        }
    }
}
