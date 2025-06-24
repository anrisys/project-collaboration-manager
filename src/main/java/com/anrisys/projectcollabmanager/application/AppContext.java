package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.dto.ProjectDTO;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.ExitAppException;
import com.anrisys.projectcollabmanager.util.CLIInputUtil;

public class AppContext implements ReadOnlyAppContext {
    public enum State {
        START_MENU, MAIN_MENU, PERSONAL_PROJECT_MENU, COLLABORATION_MENU, TASK_MENU
    }

    private User currentUser;
    private State currentState = State.START_MENU;
    private ProjectDTO currentProjectState = null;

    public ProjectDTO getCurrentProjectState() {
        return currentProjectState;
    }

    public void setCurrentProjectState(ProjectDTO currentProjectState) {
        this.currentProjectState = currentProjectState;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        System.out.println("Are you sure to log out?\n");

        boolean isGoingToLogOut = CLIInputUtil.requestBooleanInput();

        if(isGoingToLogOut) {
            this.currentUser = null;
            this.currentState = State.START_MENU;
            throw new ExitAppException();
        }
    }
}
