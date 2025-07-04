package com.anrisys.projectcollabmanager.service.auth;

import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.repository.*;
import com.anrisys.projectcollabmanager.service.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseBasicAuthServiceIT {
    protected static DataSource dataSource;
    protected static UserRepository userRepository;
    protected static ProjectRepository projectRepository;
    protected static CollaborationRepository collaborationRepository;
    protected static CollaborationServiceImpl collaborationService;
    protected static ProjectService projectService;
    protected static AuthServiceImpl authService;
    protected static UserService userService;

    @BeforeAll
    static void beforeAll() {
        dataSource = DBConfig.getDataSource();
        userRepository = new JDBCUserRepository(dataSource);
        projectRepository = new JDBCProjectRepository(dataSource);
        collaborationRepository = new JDBCCollaborationRepository(dataSource);
        userService = new BasicUserService(userRepository);
        collaborationService = new CollaborationServiceImpl(collaborationRepository, null, userService);
        projectService = new ProjectServiceImpl(projectRepository, collaborationService);
        collaborationService.setProjectService(projectService);
        authService = new AuthServiceImpl(userRepository, projectService);
    }

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             Statement statement1 = connection.createStatement();
        ) {
            statement.execute("DELETE FROM users");
            statement1.execute("DELETE FROM projects");
        }
    }

    @AfterAll
    static void afterAll() {
        DBConfig.closeDataSource();
    }
}
