package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.user.UserNotFoundException;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicUserService implements UserService{
    private final UserRepository userRepository;
    private final static Logger logger = LoggerFactory.getLogger(BasicUserService.class);

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                UserNotFoundException::new
        );

        return new UserDTO(user.getId(), user.getEmail());
    }
}
