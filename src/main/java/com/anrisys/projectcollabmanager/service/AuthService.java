package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;

public interface AuthService {
    UserDTO register(String email, String password);
    UserDTO login(String email, String password);
    void changePassword(Long id, String currentPassword, String newPassword);
    User changeEmail(Long id, String currentPassword, String newEmail);
    User deleteAccount(Long id, String currentPassword);
}
