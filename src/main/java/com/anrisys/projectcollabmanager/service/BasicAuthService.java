package com.anrisys.projectcollabmanager.service;

import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.auth.EmailAlreadyRegisteredException;
import com.anrisys.projectcollabmanager.exception.auth.InvalidCredentials;
import com.anrisys.projectcollabmanager.exception.core.BusinessException;
import com.anrisys.projectcollabmanager.exception.core.ValidationException;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.util.PasswordUtil;

public class BasicAuthService implements AuthService{

    private final UserRepository userRepository;
    private final ProjectService projectService;

    public BasicAuthService(UserRepository userRepository, ProjectService projectService) {
        this.userRepository = userRepository;
        this.projectService = projectService;
    }

    @Override
    public User register(String email, String password) {
        boolean isEmailRegistered = userRepository.existsByEmail(email);

        if(isEmailRegistered) {
            throw new EmailAlreadyRegisteredException("Email : " + email + " is already registered");
        }

        User user = new User(email, password);

        User registeredUser = userRepository.save(user);

        ProjectCreateRequest personalProject = new ProjectCreateRequest("Personal", registeredUser.getId(), null);

        projectService.create(personalProject);

        return registeredUser;
    }

    @Override
    public User login(String email, String password) {
        var user = userRepository.findByEmail(email).orElseThrow(
                InvalidCredentials::new
        );

        boolean isPasswordMatch = PasswordUtil.verify(password, user.getHashedPassword());

        if(!isPasswordMatch) {
            throw new InvalidCredentials();
        }

        return new User(user.getId(), user.getEmail());
    }

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = getUser(id);

        isCredentialValid(currentPassword, user);

        userRepository.updatePassword(user.getId(), PasswordUtil.hash(newPassword));
    }

    @Override
    public User changeEmail(Long id, String currentPassword, String newEmail) {
        var user = getUser(id);

        if(!userRepository.existsByEmail(newEmail)) {
            throw new BusinessException("Email is already in use");
        }

        isCredentialValid(currentPassword, user);

        return userRepository.updateEmail(user.getId(), newEmail);
    }

    @Override
    public User deleteAccount(Long id, String currentPassword) {
        User user = getUser(id);

        isCredentialValid(currentPassword, user);

        return userRepository.deleteById(id);
    }

    private static void isCredentialValid(String currentPassword, User user) {
        boolean isPasswordMatch = PasswordUtil.verify(currentPassword, user.getHashedPassword());

        if(!isPasswordMatch) {
            throw new BusinessException("Can't perform action.");
        }
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ValidationException("Invalid id")
        );
    }
}
