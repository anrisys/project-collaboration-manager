package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.CreateCollaborationRequest;
import com.anrisys.projectcollabmanager.entity.Collaboration;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
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

    @Override
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
    public Collaboration create(CreateCollaborationRequest request) throws DataAccessException {
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
    public Collaboration deleteById(Long id) throws DataAccessException {
        final String sql = "DELETE FROM collaboration WHERE id = ?";
        final String message = "Failed to delete collaboration with id : %d".formatted(id);

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            Collaboration collaboration = findById(id).orElseThrow(
                    () -> new DataAccessException(message)
            );

            statement.setLong(1, collaboration.getId());

            int infectedRow = statement.executeUpdate();

            if (infectedRow != 1) {
                throw new DataAccessException(message);
            }

            return collaboration;
        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    public Collaboration findByProjectIdAndUserId(Long projectId, Long userId) throws DataAccessException {
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
                return Collaboration.fromDB(
                        resultSet.getLong("id"),
                        resultSet.getLong("project_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getTimestamp("created_at").toInstant(),
                        resultSet.getTimestamp("updated_at").toInstant()
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    @Override
    public List<User> findMembersByProjectId(Long projectId) throws DataAccessException {
        final String sql = "SELECT u.id, u.email FROM users u JOIN collaborations c WHERE u.id = c.user_id AND project_id = ?";
        final String message = "Failed to find member in project with id: %d".formatted(projectId);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            List<User> members = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while(resultSet.next()) {
                    members.add(new User(
                            resultSet.getLong("id"),
                            resultSet.getString("email"))
                    );
                }
            }
            return members;
        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    @Override
    public List<Project> findProjectsByUserId(Long projectId) throws DataAccessException {
        final String sql = "SELECT * FROM projects p JOIN collaborations c WHERE p.id = c.project_id AND c.project_id = ?";
        final String message = "Failed to fetch collaborations projects of users with id: %d".formatted(projectId);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, projectId);

            List<Project> userProjects = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    userProjects.add(Project.fromDB(
                            resultSet.getLong("id"),
                            resultSet.getString("title"),
                            resultSet.getLong("owner"),
                            resultSet.getString("description"),
                            resultSet.getTimestamp("created_at").toInstant(),
                            resultSet.getTimestamp("created_at").toInstant()
                    ));
                }
            }
            return userProjects;
        } catch (SQLException e) {
            throw new DataAccessException(message);
        }
    }

    @Override
    public boolean isUserJoinCollaboration(Long projectId, Long userId) throws DataAccessException {
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
