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

            // Repository layer
            UserRepository userRepository = new JDBCUserRepository(dataSource);
            ProjectRepository projectRepository = new JDBCProjectRepository(dataSource);
            CollaborationRepository collaborationRepository = new JDBCCollaborationRepository(dataSource);


            // Service layer
            UserService userService = new BasicUserService(userRepository);
            BasicCollaborationService collaborationService = new BasicCollaborationService(collaborationRepository, null, null);
            ProjectService projectService = new BasicProjectService(projectRepository, collaborationService);
            AuthService authService = new BasicAuthService(userRepository, projectService);

            collaborationService.setProjectService(projectService);

            // View layer
            AuthView authView = new AuthView(authService);
            ProjectView projectView = new ProjectView(projectService, appContext);
            CollaborationView collaborationView = new CollaborationView(collaborationService, appContext);

            ViewRegistry viewRegistry = new ViewRegistry(authView, projectView, collaborationView);
            CLIMenuManager menuManager = new CLIMenuManager(viewRegistry, appContext);
            menuManager.start();
        } finally {
            DBConfig.closeDataSource();
        }
    }
}
