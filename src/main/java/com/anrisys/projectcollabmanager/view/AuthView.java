package com.anrisys.projectcollabmanager.view;

import com.anrisys.projectcollabmanager.application.CLIMenuManager;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.ExitAppException;
import com.anrisys.projectcollabmanager.service.AuthService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

import java.util.Objects;

public class AuthView {
    private final AuthService service;
    public static final String emailRegexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String passwordRegexPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";

    public AuthView(AuthService service) {
        this.service = service;
    }

    public void register() {
        String email = promptEmail();
        String password = promptPassword();
        promptPasswordConfirmation(password);

        service.register(email, password);
        System.out.println("You have successfully registered.");
    }

    public void login() {
        String email = promptEmail();
        String password = promptPassword();

        User loggedInUser = service.register(email, password);

        CLIMenuManager.setCurrentUser(loggedInUser);

        System.out.printf("Hello, %s! %n", loggedInUser.getEmail());
    }

    public void logout() {
        System.out.println("Are you sure to log out?\n");

        boolean isGoingToLogOut = CLIInputUtil.requestBooleanInput();

        if(isGoingToLogOut) {
            CLIMenuManager.setCurrentUser(null);
            throw new ExitAppException();
        }
    }

    private String promptEmail() {
        while(true) {
            System.out.println("Email:");
            String email = CLIInputUtil.requestStringInput();
            if(email.matches(emailRegexPattern)) return email;
            System.out.println("Invalid email format");
        }
    }

    private String promptPassword() {
        while(true) {
            System.out.println("Password:");
            String password = CLIInputUtil.requestStringInput();
            if (password.matches(passwordRegexPattern)) return password;
            System.out.println("Password is not strong. ");
        }
    }

    private void promptPasswordConfirmation(String password) {
        while(true) {
            System.out.println("Re-insert password:");
            String password_confirmation = CLIInputUtil.requestStringInput();
            if (Objects.equals(password_confirmation, password)) return;
            System.out.println("Password confirmation does not match password");
        }
    }
}
