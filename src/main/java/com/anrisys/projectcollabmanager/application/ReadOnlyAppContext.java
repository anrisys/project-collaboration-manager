package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.entity.User;

public interface ReadOnlyAppContext {
    User getCurrentUser();
    AppContext.State getCurrentState();
}
