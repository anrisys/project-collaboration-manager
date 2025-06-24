package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.view.AuthView;
import com.anrisys.projectcollabmanager.view.CollaborationView;
import com.anrisys.projectcollabmanager.view.ProjectView;
import com.anrisys.projectcollabmanager.view.TaskView;

public record ViewRegistry(
        AuthView authView,
        ProjectView projectView,
        CollaborationView collaborationView,
        TaskView taskView)
{}
