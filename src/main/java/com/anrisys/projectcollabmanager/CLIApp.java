package com.anrisys.projectcollabmanager;

import com.anrisys.projectcollabmanager.application.AppContext;
import com.anrisys.projectcollabmanager.application.CLIMenuManager;
import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.application.ViewRegistry;
import com.anrisys.projectcollabmanager.repository.JDBCProjectRepository;
import com.anrisys.projectcollabmanager.repository.JDBCUserRepository;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.service.AuthService;
import com.anrisys.projectcollabmanager.service.BasicAuthService;
import com.anrisys.projectcollabmanager.service.BasicProjectService;
import com.anrisys.projectcollabmanager.service.ProjectService;
import com.anrisys.projectcollabmanager.view.AuthView;
import com.anrisys.projectcollabmanager.view.ProjectView;

import javax.sql.DataSource;

public class CLIApp {
    public static void main(String[] args) {
        try {
            DataSource dataSource = DBConfig.getDataSource();

            // Project Feature
            ProjectRepository projectRepository = new JDBCProjectRepository(dataSource);
            ProjectService projectService = new BasicProjectService(projectRepository);
            ProjectView projectView = new ProjectView(projectService);

            // Auth Feature
            UserRepository userRepository = new JDBCUserRepository(dataSource);
            AuthService authService = new BasicAuthService(userRepository, projectService);
            AuthView authView = new AuthView(authService);

            ViewRegistry viewRegistry = new ViewRegistry(authView, projectView);
            AppContext appContext = new AppContext();

            CLIMenuManager menuManager = new CLIMenuManager(viewRegistry, appContext);
            menuManager.start();
        } finally {
            DBConfig.closeDataSource();
        }
    }
}
