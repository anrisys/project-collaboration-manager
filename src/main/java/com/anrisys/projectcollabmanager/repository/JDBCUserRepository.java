package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.util.PasswordUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JDBCUserRepository implements UserRepository{
    private final DataSource dataSource;

    public JDBCUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) throws DataAccessException {
        final String sql = "INSERT INTO users (email, hashed_password) VALUES(?, ?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        Statement.RETURN_GENERATED_KEYS
                )
            ) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getHashedPassword());

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to save new user");
            }

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if(!rs.next()) {
                    throw new DataAccessException("Failed to retrieve generated ID");
                }
                user.setId(rs.getLong(1));
            }

            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Failed saved new user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DataAccessException {
        final String sql = "SELECT * WHERE id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    User user = new User(
                            resultSet.getLong("id"),
                            resultSet.getString("email"),
                            resultSet.getString("hashed_password")
                    );
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        final String sql = "SELECT * FROM users WHERE email = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setString(1, email);

            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()) {
                    var user = new User(
                            resultSet.getLong("id"),
                            resultSet.getString("email"),
                            resultSet.getString("hashed_password")
                    );
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed find user", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) throws DataAccessException {
        final String sql = "SELECT COUNT(email) FROM users WHERE email = ?";

        try(
             Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
            ) {
            statement.setString(1, email);

            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to check email", e);
        }
    }

    @Override
    public User updateEmail(Long id, String newEmail) throws DataAccessException {
        final String sql = "UPDATE users SET email = ? WHERE id = ?";
        final String selectQuery = "SELECT id, email FROM users WHERE id = ?";

        try (
             Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(sql);
             PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
             ) {
            updateStatement.setString(1, newEmail);
            updateStatement.setLong(2, id);

            int affectedRow = updateStatement.executeUpdate();

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to update email");
            }

            selectStatement.setLong(1, id);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if(resultSet.next()) {
                    return new User(resultSet.getLong("id"), resultSet.getString("email"));
                }
                throw new DataAccessException("user not found after update");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePassword(Long id, String newPassword) throws DataAccessException {
        final String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setString(1, PasswordUtil.hash(newPassword));
            statement.setLong(2, id);

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
                throw  new DataAccessException("Failed to update user password");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user password", e);
        }
    }

    @Override
    public User deleteById(Long id) throws DataAccessException {
        final String sql = "DELETE FROM users WHERE id = ?";
        final String selectQuery = "COUNT (*) FROM users WHERE id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
            ) {

            Optional<User> user = findById(id);

            if(user.isEmpty()) {
                throw new DataAccessException("User not found");
            }

            statement.setLong(1, id);

            int affectedRow = statement.executeUpdate();

            if(affectedRow != 1) {
                throw new DataAccessException("Failed to delete a user account");
            }

            selectStmt.setLong(1, id);

            try(ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.getInt(1) != 0) {
                    throw new DataAccessException("Failed to delete the user");
                }
            }

            return user.get();
        } catch (SQLException e) {
            throw new DataAccessException("Failed delete the user", e);
        }
    }
}
