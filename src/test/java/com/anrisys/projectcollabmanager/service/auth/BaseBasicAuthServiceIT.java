package com.anrisys.projectcollabmanager.service.auth;

import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.repository.JDBCProjectRepository;
import com.anrisys.projectcollabmanager.repository.JDBCUserRepository;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.service.BasicAuthService;
import com.anrisys.projectcollabmanager.service.BasicProjectService;
import com.anrisys.projectcollabmanager.service.ProjectService;
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
    protected static ProjectService projectService;
    protected static BasicAuthService authService;

    @BeforeAll
    static void beforeAll() {
        dataSource = DBConfig.getDataSource();
        userRepository = new JDBCUserRepository(dataSource);
        projectRepository = new JDBCProjectRepository(dataSource);
        projectService = new BasicProjectService(projectRepository);
        authService = new BasicAuthService(userRepository, projectService);
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
