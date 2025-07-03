package com.anrisys.projectcollabmanager.service.auth;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.auth.InvalidCredentials;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BasicAuthServiceLoginIT extends BaseBasicAuthServiceIT{
    @Test
    void login_withCorrectCredential_returnUser() {
        String email = "test@example.com";
        String password = "Pass123!@#";

        authService.register(email, password);

        UserDTO loggedInUser = authService.login(email, password);

        Assertions.assertNotNull(loggedInUser);
        Assertions.assertNotNull(loggedInUser.id());
        Assertions.assertEquals(email, loggedInUser.email());
    }

    @Test
    void login_withIncorrectCredentials_throwsInvalidCredentials() {
        String email = "test@example.com";
        String password = "Pass123!@#";

        authService.register(email, password);

        InvalidCredentials exception = Assertions.assertThrows(
                InvalidCredentials.class,
                () ->authService.login(email, "WrongPass*()890")
        );

        Assertions.assertEquals("Email or password is wrong", exception.getMessage());
    }
}
