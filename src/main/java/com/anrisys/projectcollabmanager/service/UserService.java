package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.UserDTO;

public interface UserService {
    UserDTO findByEmail(String email);
}
