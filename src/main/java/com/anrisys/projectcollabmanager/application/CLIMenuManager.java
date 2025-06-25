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
                    case PERSONAL_PROJECT_MENU -> showPersonalProjectMenu();
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
                1. Personal projects menu
                2. Collaboration projects menu
                0. Log out
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 0 -> context.logout();
            case 1 -> context.setCurrentState(AppContext.State.PERSONAL_PROJECT_MENU);
            case 2 -> context.setCurrentState(AppContext.State.COLLABORATION_MENU);
            default -> System.out.println("Please enter valid menu option");
        }
    }

    private void showPersonalProjectMenu() {
        System.out.println(
                """
                Choose actions:
                1. Create new personal project
                2. Show list of projects
                3. Show detail of a project
                4. Update a project
                5. Change project into collaborative project
                6. Delete a project
                7. Go to a project
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
            case 5 -> viewRegistry.projectView().convertCollaborationProject();
            case 6 -> viewRegistry.projectView().deleteProject();
            case 7 -> {
                viewRegistry.projectView().goToProject();
                context.setCurrentState(AppContext.State.TASK_MENU);
            }
            default -> System.out.println("Please enter valid menu option");
        }
    }

    private void showCollaborationMenu() {
        System.out.println(
                """
                Choose actions:
                1. Show list collaboration projects
                2. Show collaboration project
                3. Show list collaboration project members
                4. Add user into project collaboration
                5. Remove member from project collaboration
                6. Leave a project collaboration
                7. Convert into personal project
                8. Go to a project
                0. Back
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 0 -> context.setCurrentState(AppContext.State.MAIN_MENU);
            case 1 -> viewRegistry.collaborationView().listMyCollaborations();
            case 2 -> viewRegistry.collaborationView().listProjectMembers();
            case 3 -> viewRegistry.collaborationView().showCollaborationProject();
            case 4 -> viewRegistry.collaborationView().addUserToProjectCollaboration();
            case 5 -> viewRegistry.collaborationView().removeUserFromProject();
            case 6 -> viewRegistry.collaborationView().leaveProject();
            case 7 -> viewRegistry.projectView().revertIntoPersonalProject();
            case 8 -> {
                viewRegistry.collaborationView().goToProject();
                context.setCurrentState(AppContext.State.TASK_MENU);
            }
            default -> System.out.println("Please enter valid menu option");
        }
    }

    private void showTaskMenu() {
        System.out.println(
                """
                Choose actions:
                1. Create new task
                2. Show all tasks
                3. Show my tasks
                4. Show task
                5. Update a task
                6. Delete a task
                7. Find task
                8. Change task assignee
                9. Update task status
                0. Back
                """
        );
        int action = CLIInputUtil.requestIntInput();
        switch (action) {
            case 0 -> {
                if (context.getCurrentProjectState().isPersonal()) {
                    context.setCurrentState(AppContext.State.PERSONAL_PROJECT_MENU);
                } else {
                    context.setCurrentState(AppContext.State.COLLABORATION_MENU);
                }
                context.setCurrentProjectState(null);
            }
            case 1 -> viewRegistry.taskView().create();
            case 2 -> viewRegistry.taskView().listTask();
            case 3 -> viewRegistry.taskView().myTaskList();
            case 4 -> viewRegistry.taskView().showTask();
            case 5 -> viewRegistry.taskView().updateTask();
            case 6 -> viewRegistry.taskView().deleteTask();
            case 7 -> viewRegistry.taskView().findTask();
            case 8 -> viewRegistry.taskView().changeTaskAssignee();
            case 9 -> viewRegistry.taskView().changeTaskStatus();
            default -> System.out.println("Please enter valid menu option");
        }
    }
}
