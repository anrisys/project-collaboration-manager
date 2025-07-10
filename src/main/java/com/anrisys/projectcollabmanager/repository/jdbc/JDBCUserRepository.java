package com.anrisys.projectcollabmanager.repository.jdbc;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.exception.user.UserNotFoundException;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import com.anrisys.projectcollabmanager.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class JDBCUserRepository implements UserRepository {
    private final DataSource dataSource;
    private final static Logger log = LoggerFactory.getLogger(JDBCUserRepository.class);

    public JDBCUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public UserDTO save(User user) throws DataAccessException {
        final String methodName = "save";
        final String sql = "INSERT INTO users (email, hashed_password) VALUES(?, ?)";

        LoggerUtil.logSQL(log, methodName, sql);

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
            log.debug("{} : Insert executed, affected rows: {}", methodName, affectedRow);

            if(affectedRow != 1) {
                log.warn("{} : Failed to save user: expected 1 affected row, got {}", methodName, affectedRow);
                throw new DataAccessException("Unexpected affected row count: " + affectedRow);
            }

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if(!rs.next()) {
                    log.error("{} : Insert succeeded but failed to retrieve generated ID for email: {}",
                            methodName,
                            LoggerUtil.maskEmail(user.getEmail()));
                    throw new DataAccessException("Failed to retrieve generated user ID");
                }
                long generatedId = rs.getLong(1);
                user.setId(generatedId);
            }
            LoggerUtil.logSQLExecuted(log, methodName);
            return new UserDTO(user.getId(), user.getEmail());
        } catch (SQLException e) {
            log.error("{} : Database error while saving user with email: {}", methodName, LoggerUtil.maskEmail(user.getEmail()), e);
            throw new DataAccessException("Failed to save new user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DataAccessException {
        final String methodName = "findById";
        final String sql = "SELECT id, email, hashed_password FROM users WHERE id = ?";

        LoggerUtil.logSQL(log, methodName, sql);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, methodName);
                if(resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setHashedPassword(resultSet.getString("hashed_password"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("{}: Database error. Failed to find user", methodName, e);
            throw new DataAccessException("Failed to find user", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        final String methodName = "findByEmail";
        final String sql = "SELECT id, email, hashed_password FROM users WHERE email = ?";

        LoggerUtil.logSQL(log, methodName, sql);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setString(1, email);

            try(ResultSet resultSet = statement.executeQuery()){
                LoggerUtil.logSQLExecuted(log, methodName);
                if(resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setHashedPassword(resultSet.getString("hashed_password"));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("{} : Database error. Failed to find user", methodName, e);
            throw new DataAccessException("Failed find user", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) throws DataAccessException {
        final String sql = "SELECT COUNT(email) FROM users WHERE email = ?";
        LoggerUtil.logSQL(log,"existsByEmail", sql);

        try(
             Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
            ) {
            statement.setString(1, email);

            try(ResultSet resultSet = statement.executeQuery()) {
                LoggerUtil.logSQLExecuted(log, "existsByEmail");
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            log.error("existsByEmail : Database error. Failed to check email", e);
            throw new DataAccessException("Failed to check email", e);
        }
    }

    @Override
    public User updateEmail(Long id, String newEmail) throws DataAccessException {
        final String sql = "UPDATE users SET email = ? WHERE id = ?";
        final String methodName = "updateEmail";

        LoggerUtil.logSQL(log, methodName, sql);

        try (
             Connection connection = dataSource.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(sql);
             ) {
            updateStatement.setString(1, newEmail);
            updateStatement.setLong(2, id);

            int affectedRow = updateStatement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("{} : Failed to update user email: expected 1 affected row, got {}", methodName, affectedRow);
                throw new DataAccessException("Failed to update email");
            }

            return findById(id).orElseThrow(
                    UserNotFoundException::new
            );

        } catch (SQLException e) {
            log.error("{} : Database error. Failed to update user email with id: {}", methodName, id, e);
            throw new DataAccessException("Database error. Failed to update user email", e);
        }
    }

    @Override
    public void updatePassword(Long id, String newPassword) throws DataAccessException {
        final String sql = "UPDATE users SET password = ? WHERE id = ?";
        final String methodName = "updatePassword";

        LoggerUtil.logSQL(log, methodName, sql);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ) {
            statement.setString(1, PasswordUtil.hash(newPassword));
            statement.setLong(2, id);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("{} : Failed to update user password: expected 1 affected row, got {}", methodName, affectedRow);
                throw  new DataAccessException("Failed to update user password");
            }

        } catch (SQLException e) {
            log.error("{} : Database error. Failed to update password by user id: {}", methodName, id, e);
            throw new DataAccessException("Failed to update user password", e);
        }
    }

    @Override
    public void deleteById(Long id) throws DataAccessException {
        final String sql = "DELETE FROM users WHERE id = ?";
        final String selectQuery = "COUNT (*) FROM users WHERE id = ?";
        final String methodName = "deleteById";

        LoggerUtil.logSQL(log, methodName, sql);

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
            ) {
            statement.setLong(1, id);

            int affectedRow = statement.executeUpdate();
            LoggerUtil.logSQLExecuted(log, methodName);

            if(affectedRow != 1) {
                log.warn("{} : Failed to delete user with id {}: expected 1 affected row, got {}", methodName,
                        id, affectedRow);
                throw new DataAccessException("Failed to delete a user account");
            }

            selectStmt.setLong(1, id);

            try(ResultSet resultSet = selectStmt.executeQuery()) {
                if (resultSet.getInt(1) != 0) {
                    log.warn("{} : Deleted user found: expected 0 data got {}", methodName,
                            resultSet.getInt(1));
                    throw new DataAccessException("Failed to delete the user");
                }
            }
        } catch (SQLException e) {
            log.error("{}: Database error. Failed to delete a user with id: {}", methodName, id, e);
            throw new DataAccessException("Failed delete the user", e);
        }
    }
}
