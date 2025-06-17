package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.user.UserNotFoundException;
import com.anrisys.projectcollabmanager.repository.UserRepository;

public class BasicUserService implements UserService{
    private final UserRepository userRepository;

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException(email)
        );

        return new UserDTO(user.getId(), user.getEmail());
    }
}
