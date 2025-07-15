package com.anrisys.projectcollabmanager.repository;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.dto.UserRegisterRequest;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;

import java.util.Optional;

public interface UserRepository {
    UserDTO save(UserRegisterRequest user) throws DataAccessException;
    Optional<User> findById(Long id) throws DataAccessException;
    Optional<User> findByEmail(String email) throws DataAccessException;
    boolean existsByEmail(String email) throws DataAccessException;
    User updateEmail(Long id, String newEmail) throws DataAccessException;
    void updatePassword(Long id, String newPassword) throws DataAccessException;
    void deleteById(Long id) throws DataAccessException;
}
