package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.auth.EmailAlreadyRegisteredException;
import com.anrisys.projectcollabmanager.exception.auth.InvalidCredentials;
import com.anrisys.projectcollabmanager.exception.core.BusinessException;
import com.anrisys.projectcollabmanager.exception.core.ValidationException;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import com.anrisys.projectcollabmanager.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthService implements AuthService{
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final static Logger log = LoggerFactory.getLogger(BasicAuthService.class);

    public BasicAuthService(UserRepository userRepository, ProjectService projectService) {
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    @Override
    public UserDTO register(String email, String password) {
        boolean isEmailRegistered = userRepository.existsByEmail(email);
        final String methodName = "register";

        log.debug("{} : Attempting register new user with email={}", methodName , LoggerUtil.maskEmail(email));

        if(isEmailRegistered) {
            log.warn("{}: Email is already registered - {}", methodName, LoggerUtil.maskEmail(email));
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User(email, password);

        UserDTO registeredUser = userRepository.save(user);
        log.info("{}: Registered new user with id {}", methodName, registeredUser.id());

        log.debug("{}: Attempting to create personal project for user id={}", methodName, registeredUser.id());
        ProjectCreateRequest personalProject = new ProjectCreateRequest("Personal", registeredUser.id(), true, null);
        projectService.create(personalProject);
        log.info("{}: Created personal project with title 'Personal' for user id={}", methodName, registeredUser.id());

        return new UserDTO(registeredUser.id(), registeredUser.email());
    }

    @Override
    public UserDTO login(String email, String password) {
        final String methodName = "login";
        log.debug("{}: Attempting to log in for email={}", methodName, LoggerUtil.maskEmail(email));

        var user = userRepository.findByEmail(email).orElseThrow(
                () -> {
                    log.warn("{}: Failed login with invalid credential for email {}", methodName, LoggerUtil.maskEmail(email));
                    return new InvalidCredentials();
                }
        );

        isCredentialValid(user.getHashedPassword(), user, "login");

        log.info("{}: Logged in for user with email={}", methodName, LoggerUtil.maskEmail(email));
        return new UserDTO(user.getId(), user.getEmail());
    }

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword) {
        final String methodName = "changePassword";
        log.debug("{}: Attempting to change password for user id={}", methodName, id);

        User user = getUser(id);

        isCredentialValid(currentPassword, user, "changePassword");

        userRepository.updatePassword(user.getId(), PasswordUtil.hash(newPassword));
        log.info("{}: Changed password for user id={}", methodName, id);
    }

    @Override
    public User changeEmail(Long id, String currentPassword, String newEmail) {
        final String methodName = "changeEmail";
        log.debug("{}: Attempting to change email for user id={}", methodName, id);

        var user = getUser(id);

        if(!userRepository.existsByEmail(newEmail)) {
            log.warn("changeEmail: Failed to change email. Email already in use: {}", LoggerUtil.maskEmail(newEmail));
            throw new BusinessException("Email is already in use");
        }

        isCredentialValid(currentPassword, user, "changeEmail");

        User updatedEmail = userRepository.updateEmail(user.getId(), newEmail);
        log.info("{}: Updated email for user id={}", methodName, id);
        return updatedEmail;
    }

    @Override
    public User deleteAccount(Long id, String currentPassword) {
        final String methodName = "deleteAccount";
        log.debug("{}: Attempting delete account for id={}", methodName, id);

        User user = getUser(id);
        isCredentialValid(currentPassword, user, "deleteAccount");

        userRepository.deleteById(user.getId());
        log.warn("{}: Deleted account for id={}", methodName, id);
        return user;
    }

    private static void isCredentialValid(String currentPassword, User user, String methodName) {
        log.debug("{}: Validate user credentials for user id={}", methodName, user.getId());
        boolean isPasswordMatch = PasswordUtil.verify(currentPassword, user.getHashedPassword());

        if(!isPasswordMatch) {
            log.warn("{}: Invalid credentials for user id={}", methodName, user.getId());
            throw new BusinessException("Can't perform action.");
        }

        log.debug("{}: Credential verified for userId={}", methodName, user.getId());
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> {
                    log.debug("User not found with id={}", id);
                    return new ValidationException("Invalid id");
                }
        );
    }
}
