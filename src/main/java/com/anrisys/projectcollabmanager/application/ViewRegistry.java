package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.view.AuthView;
import com.anrisys.projectcollabmanager.view.ProjectView;

public class ViewRegistry {
    public final AuthView authView;
    public final ProjectView projectView;

    public ViewRegistry(AuthView authView, ProjectView projectView) {
        this.authView = authView;
        this.projectView = projectView;
    }
}
