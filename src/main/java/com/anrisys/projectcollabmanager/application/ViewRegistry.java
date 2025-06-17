package com.anrisys.projectcollabmanager.application;

import com.anrisys.projectcollabmanager.view.AuthView;
import com.anrisys.projectcollabmanager.view.CollaborationView;
import com.anrisys.projectcollabmanager.view.ProjectView;

public record ViewRegistry(AuthView authView, ProjectView projectView, CollaborationView collaborationView) {}
