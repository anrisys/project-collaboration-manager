package com.anrisys.projectcollabmanager.repository;

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
    public Project save(Project project) throws DataAccessException {
        final String sql = "INSERT INTO projects(title, owner, description) VALUES(?, ?, ?)";

        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                        );
            ) {
            boolean hasSameProjectName = HasSameProjectName(project.getOwner(), project.getTitle());

            if (hasSameProjectName) {
                throw new HasSameProjectNameException();
            }

            statement.setString(1, project.getTitle());
            statement.setLong(2, project.getOwner());
            statement.setString(3, project.getDescription());

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to save new project");
            }

            ResultSet resultSet = statement.getGeneratedKeys();

            if(!resultSet.next()) {
                throw new DataAccessException("Failed to retrieve generated project ID");
            }

            Long id = resultSet.getLong(1);

            return findById(id).orElseThrow(() -> new DataAccessException("Failed to retrieve new project"));

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
    public Optional<List<Project>> findByOwnerId(Long owner) throws DataAccessException {
        final String sql = "SELECT * FROM projects WHERE owner = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        )
        {
            List<Project> projects = new ArrayList<>();

            statement.setLong(1, owner);

            try(ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    while(resultSet.next()) {
                        projects.add(
                                Project.fromDB(
                                        resultSet.getLong("id"),
                                        resultSet.getString("title"),
                                        resultSet.getLong("owner"),
                                        resultSet.getString("description"),
                                        resultSet.getTimestamp("created_at").toInstant(),
                                        resultSet.getTimestamp("updated_at").toInstant()
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
    public Project update(Long id, Project project) throws DataAccessException {
        final String sql = "UPDATE projects SET title, description VALUES (?, ?) WHERE id = ?";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, project.getTitle());
            statement.setString(2, project.getDescription());
            statement.setLong(3, id);

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to update project" + project.getTitle());
            }

            return findById(id).orElseThrow(
                    () -> new DataAccessException("Failed to fetch updated project" + project.getTitle()
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
