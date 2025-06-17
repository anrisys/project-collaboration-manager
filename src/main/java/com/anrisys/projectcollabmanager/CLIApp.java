package com.anrisys.projectcollabmanager;

import com.anrisys.projectcollabmanager.application.AppContext;
import com.anrisys.projectcollabmanager.application.CLIMenuManager;
import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.application.ViewRegistry;
import com.anrisys.projectcollabmanager.repository.*;
import com.anrisys.projectcollabmanager.service.*;
import com.anrisys.projectcollabmanager.view.AuthView;
import com.anrisys.projectcollabmanager.view.CollaborationView;
import com.anrisys.projectcollabmanager.view.ProjectView;

import javax.sql.DataSource;

public class CLIApp {
    public static void main(String[] args) {
        try {
            DataSource dataSource = DBConfig.getDataSource();
            AppContext appContext = new AppContext();
            // Project Feature
            ProjectRepository projectRepository = new JDBCProjectRepository(dataSource);
            ProjectService projectService = new BasicProjectService(projectRepository);
            ProjectView projectView = new ProjectView(projectService, appContext);

            // Auth Feature
            UserRepository userRepository = new JDBCUserRepository(dataSource);
            AuthService authService = new BasicAuthService(userRepository, projectService);
            AuthView authView = new AuthView(authService);

            // User service
            UserService userService = new BasicUserService(userRepository);

            // Collaboration Feature
            CollaborationRepository collaborationRepository = new JDBCCollaborationRepository(dataSource);
            CollaborationService collaborationService = new BasicCollaborationService(collaborationRepository, projectService, userService);
            CollaborationView collaborationView = new CollaborationView(collaborationService, appContext);

            ViewRegistry viewRegistry = new ViewRegistry(authView, projectView, collaborationView);
            CLIMenuManager menuManager = new CLIMenuManager(viewRegistry, appContext);
            menuManager.start();
        } finally {
            DBConfig.closeDataSource();
        }
    }
}
