package com.anrisys.projectcollabmanager.repository.jdbc;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.repository.CollaborationRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCCollaborationRepository implements CollaborationRepository {
    private final DataSource dataSource;
    private final static Logger log = LoggerFactory.getLogger(JDBCCollaborationRepository.class);

    public JDBCCollaborationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Collaboration> findById(Long id) throws DataAccessException {
        final String sql = "SELECT * FROM collaborations WHERE id = ?";
        final String methodName = "findById";
        final String message = "Failed to find collaborationId=%d".formatted(id);
        LoggerUtil.logSQL(log, methodName, sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) return Optional.empty();
                Collaboration collaboration = new Collaboration();
                Project project = new Project();
                project.setId(resultSet.getLong("project_id"));
                User member = new User();
                member.setId(resultSet.getLong("user_id"));
                collaboration.setId(resultSet.getLong("id"));
                collaboration.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                collaboration.setUpdatedAt(resultSet.getTimestamp("updated_at").toInstant());
                return Optional.of(collaboration);
            }
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException("Failed to fetch collaboration with id : " + id);
        }
    }

    @Override
    public Collaboration create(CreateCollaborationRequest request) throws DataAccessException {
        final String sql = "INSERT INTO collaborations (project_id, user_id) VALUES(?, ?)";

        String message = "Failed to add user %d into project with id %d".formatted(request.userId(), request.projectId());
        final String methodName = "addUserToProject";
        LoggerUtil.logSQL(log, methodName, sql);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, request.projectId());
            statement.setLong(2, request.userId());

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("[{}] Failed to create collaboration. Expected affected row 1 got {}", methodName, affectedRow);
                throw new DataAccessException(message);
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if(!resultSet.next()) {
                    log.warn("[{}] Failed to retrieve generated collaboration ID", methodName);
                    throw new DataAccessException("Failed to retrieve generated collaboration ID");
                }

                Long id = resultSet.getLong(1);

                return findById(id).orElseThrow(() -> {
                    log.warn("[{}] Failed to retrieve new collaboration", methodName);
                    return new DataAccessException("Failed to retrieve new collaboration");
                });
            }

        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        final String sql = "DELETE FROM collaboration WHERE id = ?";
        final String message = "Failed to collaborationId : %d".formatted(id);
        final String methodName = "delete";
        LoggerUtil.logSQL(log, methodName, sql);
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if (affectedRow != 1) {
                log.warn("[{}] Failed to delete collaboration. Expected affected row 1 got {}", methodName, affectedRow);
                throw new DataAccessException(message);
            }

        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    public Optional<Collaboration> findByProjectIdAndUserId(Long projectId, Long userId) throws DataAccessException {
        final String sql = "SELECT * FROM collaborations WHERE project_id = ? AND user_id = ?";
        final String message = "Failed to fetch the collaboration with projectId = %d and userId = %d".formatted(projectId, userId);
        final String methodName = "findByProjectIdAndUserId";

        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, projectId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) {
                    throw new DataAccessException(message);
                }
                Collaboration collaboration = new Collaboration();
                Project project = new Project();
                project.setId(resultSet.getLong("project_id"));
                User member = new User();
                member.setId(resultSet.getLong("user_id"));
                collaboration.setId(resultSet.getLong("id"));
                collaboration.setCreatedAt(resultSet.getTimestamp("created_at").toInstant());
                collaboration.setUpdatedAt(resultSet.getTimestamp("updated_at").toInstant());
                return Optional.of(collaboration);
            }
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<List<UserDTO>> findMembersByProjectId(Long projectId) throws DataAccessException {
        final String sql = "SELECT u.id, u.email FROM users u JOIN collaborations c WHERE u.id = c.user_id AND project_id = ?";
        final String message = "Failed to find member in project with id: %d".formatted(projectId);
        final String methodName = "findMembersByProjectId";
        LoggerUtil.logSQL(log, methodName, sql);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            List<UserDTO> members = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) return Optional.empty();
                while(resultSet.next()) {
                    members.add(new UserDTO(
                            resultSet.getLong("id"),
                            resultSet.getString("email"))
                    );
                }
            }
            return Optional.of(members);
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<List<ProjectDTO>> findCollaborationsByUserId(Long projectId) throws DataAccessException {
        final String sql = "SELECT p.id, p.title, p.is_personal, p.owner FROM projects p JOIN collaborations c WHERE p.id = c.project_id AND c.project_id = ?";
        final String message = "Failed to fetch collaborations projects of users with id: %d".formatted(projectId);
        final String methodName = "findCollaborationsByUserId";
        LoggerUtil.logSQL(log, methodName, sql);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            List<ProjectDTO> userProjects = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if (!resultSet.next()) return Optional.empty();
                while (resultSet.next()) {
                    userProjects.add(new ProjectDTO(
                            resultSet.getLong("id"),
                            resultSet.getString("title"),
                            resultSet.getBoolean("is_personal"),
                            resultSet.getLong("owner")
                    ));
                }
            }
            return Optional.of(userProjects);
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }

    @Override
    public boolean existsByProjectIdAndUserId(Long projectId, Long userId) throws DataAccessException {
        final String sql = "COUNT (*) FROM collaborations WHERE project_id = ? AND user_id = ?";
        final String message = "Failed to fetch user collaboration data.";
        final String methodName = "existsByProjectIdAndUserId";
        LoggerUtil.logSQL(log, methodName, sql);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                return !resultSet.next();
            }
        } catch (SQLException e) {
            LoggerUtil.logDatabaseError(log, methodName, message, e);
            throw new DataAccessException(message);
        }
    }
}
