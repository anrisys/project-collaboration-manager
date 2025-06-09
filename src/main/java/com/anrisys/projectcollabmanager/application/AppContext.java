package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.entity.User;

public class AppContext {
    public enum State {
        MAIN_MENU, AUTH_MENU, PROJECT_MENU, TASK_MENU
    }

    private User currentUser;
    private State currentState = State.MAIN_MENU;

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
        this.currentUser = null;
        this.currentState = State.MAIN_MENU;
    }
}
