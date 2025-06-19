package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.dto.ProjectUpdateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.exception.projects.HasSameProjectNameException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCProjectRepository implements ProjectRepository{
    private final DataSource dataSource;

    public JDBCProjectRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Project save(ProjectCreateRequest request) throws DataAccessException {
        final String sql = "INSERT INTO projects(title, owner, is_personal, description) VALUES(?, ?, ?, ?)";

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

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to save new project");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if(!resultSet.next()) {
                    throw new DataAccessException("Failed to retrieve generated project ID");
                }

                Long id = resultSet.getLong(1);

                return findById(id).orElseThrow(() -> new DataAccessException("Failed to retrieve new project"));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create a project", e);
        }
    }

    @Override
    public Optional<Project> findById(Long id) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE id = ? LIMIT 1";

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            )
        {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    return Optional.of(Project.fromDB(
                            resultSet.getLong("id"),
                            resultSet.getString("title"),
                            resultSet.getLong("owner"),
                            resultSet.getBoolean("is_personal"),
                            resultSet.getString("description"),
                            resultSet.getTimestamp("created_at").toInstant(),
                            resultSet.getTimestamp("updated_at").toInstant()
                    ));
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Project> findByTitle(String title) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE title = ? LIMIT 1";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)
            )
        {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    return Optional.of(Project.fromDB(
                            resultSet.getLong("id"),
                            resultSet.getString("title"),
                            resultSet.getLong("owner"),
                            resultSet.getBoolean("is_personal"),
                            resultSet.getString("description"),
                            resultSet.getTimestamp("created_at").toInstant(),
                            resultSet.getTimestamp("updated_at").toInstant()
                    ));
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<List<ProjectDTO>> findByOwnerId(Long owner) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE owner = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        )
        {
            List<ProjectDTO> projects = new ArrayList<>();

            statement.setLong(1, owner);

            try(ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    while(resultSet.next()) {
                        projects.add(
                                new ProjectDTO(
                                        resultSet.getLong("id"),
                                        resultSet.getString("title"),
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project update(Long id, ProjectUpdateRequest request) throws DataAccessException {
        StringBuilder sql = new StringBuilder("UPDATE projects SET ");
        List<Object> params = new ArrayList<>();

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

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString());
        ) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i, params.get(i));
            }

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to update project" + request.title());
            }

            return findById(id).orElseThrow(
                    () -> new DataAccessException("Failed to fetch updated project" + request.title()
                    )
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Project delete(Long id) throws DataAccessException {
        final String deleteQuery = "DELETE FROM projects WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
        ) {
            Project project = findById(id).orElseThrow(
                    () -> new DataAccessException("Failed to delete project")
            );

            statement.setLong(1, id);

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
               throw new DataAccessException("Failed to delete a projects");
            }

            return project;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean HasSameProjectName(Long owner, String title) {
        final String sql = "SELECT * FROM projects WHERE owner = ? AND title = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, owner);
            statement.setString(2, title);

            try(ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
