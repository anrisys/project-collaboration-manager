package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.GlobalExceptionHandler;
import com.anrisys.projectcollabmanager.exception.core.ExitAppException;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

public class CLIMenuManager {
    private final ViewRegistry viewRegistry;
    private final AppContext context;

    public CLIMenuManager(ViewRegistry viewRegistry, AppContext context) {
        this.viewRegistry = viewRegistry;
        this.context = context;
    }

    public void start() {
        while(true) {
            try {
                switch (context.getCurrentState()) {
                    case START_MENU -> showStartMenu();
                    case MAIN_MENU -> showMainMenu();
                    case PROJECT_MENU -> showProjectMenu();
                    case COLLABORATION_MENU -> showCollaborationMenu();
                    case TASK_MENU -> showTaskMenu();
                    default -> throw new ExitAppException();
                }
            } catch (ExitAppException e) {
                GlobalExceptionHandler.handle(e);
                break;
            } catch (Exception e) {
                GlobalExceptionHandler.handle(e);
            }
        }
    }

    private void showStartMenu() {
        System.out.println(
                """
                Choose your action:
                1. Login
                2. Register
                3. Exit
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 1 -> {
                User user = viewRegistry.authView().login();
                context.setCurrentUser(user);
            }
            case 2 -> {
                viewRegistry.authView().register();
                User user = viewRegistry.authView().login();
                context.setCurrentUser(user);
            }
            case 3 -> throw new ExitAppException();
            default -> System.out.println("Please enter valid action number");
        }
    }

    private void showMainMenu() {
        System.out.println(
                """
                Choose menu:
                1. Project menu
                2. Collaboration menu
                0. Log out
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 0 -> context.logout();
            case 1 -> context.setCurrentState(AppContext.State.PROJECT_MENU);
            case 2 -> context.setCurrentState(AppContext.State.TASK_MENU);
            default -> System.out.println("Please enter valid menu option");
        }
    }

    private void showProjectMenu() {
        System.out.println(
                """
                Choose actions:
                1. Create new project
                2. Show list of projects
                3. Show detail of a project
                4. Update a project
                5. Delete a project
                0. Back
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 0 -> context.setCurrentState(AppContext.State.MAIN_MENU);
            case 1 -> viewRegistry.projectView().createProject();
            case 2 -> viewRegistry.projectView().listProjects();
            case 3 -> viewRegistry.projectView().showProject();
            case 4 -> viewRegistry.projectView().updateProject();
            case 5 -> viewRegistry.projectView().deleteProject();
            default -> System.out.println("Please enter valid menu option");
        }
    }

    private void showCollaborationMenu() {
        System.out.println(
                """
                Choose actions:
                1. Show list collaboration projects
                2. Show list collaboration project members
                3. Add user into project collaboration
                4. Remove member from project collaboration
                5. Leave a project collaboration
                0. Back
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 0 -> context.setCurrentState(AppContext.State.MAIN_MENU);
            case 1 -> viewRegistry.collaborationView().listMyCollaborations();
            case 2 -> viewRegistry.collaborationView().listProjectMembers();
            case 3 -> viewRegistry.collaborationView().addUserToProjectCollaboration();
            case 4 -> viewRegistry.collaborationView().removeUserFromProject();
            case 5 -> viewRegistry.collaborationView().leaveProject();
            default -> System.out.println("Please enter valid menu option");
        }
    }

    private void showTaskMenu() {
        System.out.println("This is task menu");
    }
}
