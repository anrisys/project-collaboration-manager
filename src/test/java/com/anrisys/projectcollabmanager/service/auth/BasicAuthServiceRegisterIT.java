package com.anrisys.projectcollabmanager.service.auth;

import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.auth.EmailAlreadyRegisteredException;
import com.anrisys.projectcollabmanager.exception.projects.ProjectNotFoundException;
import org.junit.jupiter.api.*;

import java.util.List;

public class BasicAuthServiceRegisterIT extends BaseBasicAuthServiceIT{
    @Test
    void registerUser_withValidCredential_returnsUser() {
        String email = "test@example.com";
        String password = "Password123";

        User user = authService.register(email, password);
        boolean personalProjectCreated = projectRepository.HasSameProjectName(user.getId(), "Personal");

        Assertions.assertNotNull(user);
        Assertions.assertNotNull(user.getId());
        Assertions.assertEquals(email, user.getEmail());
        Assertions.assertTrue(personalProjectCreated);
    }

    @Test
    @DisplayName("Register with duplicate email throws EmailAlreadyRegisteredException")
    void register_duplicateEmail_throwsException() {
        String email = "test@example.com";
        String password = "Password123!";
        authService.register(email, password);

        EmailAlreadyRegisteredException exception = Assertions.assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(email, "DifferentPass123!")
        );

        Assertions.assertEquals(
                "Email : " + email + " is already registered",
                exception.getMessage()
        );
    }

    @Test
    @Disabled
    void registerUser_invalidData_throwsException() {
    }
    
}
