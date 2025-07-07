package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.user.UserNotFoundException;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicUserService implements UserService{
    private final UserRepository userRepository;
    private final static Logger log = LoggerFactory.getLogger(BasicUserService.class);

    public BasicUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO findByEmail(String email) {
        log.debug("[findByEmail] Attempt to find user with email={}", LoggerUtil.maskEmail(email));
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> {
                    log.warn("[findByEmail] User with email={} not found", LoggerUtil.maskEmail(email));
                    return new UserNotFoundException();
                }
        );

        return new UserDTO(user.getId(), user.getEmail());
    }
}
