package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.exception.core.ResourceNotFoundException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCCollaborationRepository implements CollaborationRepository{
    private final DataSource dataSource;

    public JDBCCollaborationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Collaboration> findById(Long id) throws DataAccessException {
        final String sql = "SELECT * FROM collaborations WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return Optional.empty();

                return Optional.of(Collaboration.fromDB(
                        resultSet.getLong("id"),
                        resultSet.getLong("project_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch collaboration with id : " + id);
        }
    }

    @Override
    public Collaboration addUserToProject(CreateCollaborationRequest request) throws DataAccessException {
        final String sql = "INSERT INTO collaborations (project_id, user_id) VALUES(?, ?)";

        String message = "Failed to add user %d into project with id %d".formatted(request.userId(), request.projectId());
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, request.projectId());
            statement.setLong(2, request.userId());

            int infectedRow = statement.executeUpdate();

            if(infectedRow != 1) {
                throw new DataAccessException(message);
            }

            ResultSet resultSet = statement.getGeneratedKeys();

            return findById(resultSet.getLong(1)).orElseThrow(
                    () -> new ResourceNotFoundException("New collaboration")
            );

        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    @Override
    public void removeUserFromProject(Long projectId, Long userId) throws DataAccessException {
        final String sql = "DELETE FROM collaboration WHERE project_id = ? AND user_id = ?";
        final String message = "Failed to remove user with id %d from project with id : %d".formatted(userId, projectId);

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            Collaboration collaboration = findByProjectIdAndUserId(projectId, userId).orElseThrow(
                    () -> new DataAccessException(message)
            );

            statement.setLong(1, projectId);
            statement.setLong(2, userId);

            int infectedRow = statement.executeUpdate();

            if (infectedRow != 1) {
                throw new DataAccessException(message);
            }

        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    public Optional<Collaboration> findByProjectIdAndUserId(Long projectId, Long userId) throws DataAccessException {
        final String sql = "SELECT * FROM collaborations WHERE project_id = ? AND user_id = ?";
        final String message = "Failed to fetch the collaboration with projectId = %d and userId = %d".formatted(projectId, userId);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, projectId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new DataAccessException(message);
                }
                return Optional.of(Collaboration.fromDB(
                        resultSet.getLong("id"),
                        resultSet.getLong("project_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<List<UserDTO>> findMembersByProjectId(Long projectId) throws DataAccessException {
        final String sql = "SELECT u.id, u.email FROM users u JOIN collaborations c WHERE u.id = c.user_id AND project_id = ?";
        final String message = "Failed to find member in project with id: %d".formatted(projectId);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            List<UserDTO> members = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
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
            throw new DataAccessException(message);
        }
    }

    @Override
    public Optional<List<ProjectDTO>> findCollaborationsByUserId(Long projectId) throws DataAccessException {
        final String sql = "SELECT p.id, p.title, p.is_personal, p.owner FROM projects p JOIN collaborations c WHERE p.id = c.project_id AND c.project_id = ?";
        final String message = "Failed to fetch collaborations projects of users with id: %d".formatted(projectId);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            List<ProjectDTO> userProjects = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
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
            throw new DataAccessException(message);
        }
    }

    @Override
    public boolean existsByProjectIdAndUserId(Long projectId, Long userId) throws DataAccessException {
        final String sql = "COUNT (*) FROM collaborations WHERE project_id = ? AND user_id = ?";
        final String message = "Failed to fetch user collaboration data.";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return !resultSet.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }
}
