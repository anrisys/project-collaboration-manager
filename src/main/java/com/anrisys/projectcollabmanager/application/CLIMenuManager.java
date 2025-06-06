package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.GlobalExceptionHandler;
import com.anrisys.projectcollabmanager.exception.core.ExitAppException;
import com.anrisys.projectcollabmanager.repository.JDBCProjectRepository;
import com.anrisys.projectcollabmanager.repository.JDBCUserRepository;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.service.AuthService;
import com.anrisys.projectcollabmanager.service.BasicAuthService;
import com.anrisys.projectcollabmanager.service.BasicProjectService;
import com.anrisys.projectcollabmanager.service.ProjectService;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;
import com.anrisys.projectcollabmanager.view.AuthView;

import javax.sql.DataSource;

public class CLIMenuManager {
    private final DataSource dataSource = DBConfig.getDataSource();
    private static User currentUser;

    // Project menu
    private final ProjectRepository projectRepository = new JDBCProjectRepository(dataSource);
    private final ProjectService projectService = new BasicProjectService(projectRepository);

    // Auth Menu
    private final UserRepository userRepository = new JDBCUserRepository(dataSource);
    private final AuthService authService = new BasicAuthService(userRepository, projectService);
    private final AuthView authView = new AuthView(authService);

    public enum State {
        MAIN_MENU,
        AUTH_MENU,
        PROJECT_MENU,
        TASK_MENU
    }

    private State currentState = State.MAIN_MENU;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        CLIMenuManager.currentUser = currentUser;
    }

    public void start() {
        while(true) {
            try {
                switch (currentState) {
                    case MAIN_MENU -> showMainMenu();
                    case AUTH_MENU -> showAuthMenu();
                    case PROJECT_MENU -> showProjectMenu();
                    case TASK_MENU -> showTaskMenu();        
                }
            } catch (ExitAppException e) {
                break;
            } catch (Exception e) {
                GlobalExceptionHandler.handle(e);
                break;
            }
        }
    }

    private void showMainMenu() {
        System.out.println("""
                Welcome to Project Collaboration Manager!
                Menu:
                1. Login
                2. Register
                3. Exit
                Your action:"""
        );

        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 1 -> {
                authView.login();
                currentState = State.PROJECT_MENU;
            }
            case 2 -> {
                authView.register();
                currentState = State.PROJECT_MENU;
            }
            case 3 -> authView.logout();
            default -> System.out.println("Invalid choice");
        }
    }

    private void showProjectMenu() {
        System.out.println("This is project menu");
        throw new ExitAppException();
    }

    private void showTaskMenu() {
    }

    private void showAuthMenu() {
    }


}
