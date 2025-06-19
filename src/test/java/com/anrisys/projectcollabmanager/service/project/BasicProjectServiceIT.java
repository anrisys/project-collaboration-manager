package com.anrisys.projectcollabmanager.service.project;

import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.dto.ProjectCreateRequest;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.projects.HasSameProjectNameException;
import com.anrisys.projectcollabmanager.exception.projects.ProjectNotFoundException;
import com.anrisys.projectcollabmanager.repository.JDBCProjectRepository;
import com.anrisys.projectcollabmanager.repository.JDBCUserRepository;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.service.BasicProjectService;
import com.anrisys.projectcollabmanager.service.ProjectService;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BasicProjectServiceIT {

    protected static DataSource dataSource;
    protected static User sampleUser;
    protected static UserRepository userRepository;
    protected static ProjectRepository projectRepository;
    protected static ProjectService projectService;

    @BeforeAll
    static void beforeAll() {
        dataSource = DBConfig.getDataSource();
        userRepository = new JDBCUserRepository(dataSource);
        projectRepository = new JDBCProjectRepository(dataSource);
        projectService = new BasicProjectService(projectRepository);

        User user = new User("sample@user.com", "Sample1234!@#$");
        sampleUser = userRepository.save(user);
    }

    @AfterEach
    void cleanProjectTable() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate("DELETE FROM projects");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void afterAll() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate("DELETE FROM users");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        DBConfig.closeDataSource();
    }

    @Test
    void createProject_withValidData_returnProject() {
        String projectTitle = "Test 1";
        ProjectCreateRequest project = new ProjectCreateRequest(projectTitle, sampleUser.getId(), null);

        Project savedProject = projectService.create(project);

        Assertions.assertNotNull(savedProject);
        Assertions.assertNotNull(savedProject.getId());
        Assertions.assertEquals(projectTitle, savedProject.getTitle());
        Assertions.assertEquals(sampleUser.getId(), savedProject.getOwner());
    }

    @Test
    @DisplayName("Create project with same saved project")
    void createProject_withSameTitle_returnHasSameProjectNameException() {
        String projectTitle = "Test 1";
        ProjectCreateRequest project = new ProjectCreateRequest(projectTitle, sampleUser.getId(), null);

        projectService.create(project);

        HasSameProjectNameException exception = Assertions.assertThrows(
                HasSameProjectNameException.class,
                () -> projectService.create(project)
        );

        Assertions.assertEquals(
                "User already has project with the same name",
                exception.getMessage()
        );
    }

    @Test
    void findProjectById_ByOwner_returnSavedProject() {
        String projectTitle = "Test 1";
        ProjectCreateRequest project = new ProjectCreateRequest(projectTitle, sampleUser.getId(), null);

        Project savedProject = projectService.create(project);

        Project foundProject = projectService.findPersonalProjectById(savedProject.getId(), sampleUser.getId());

        Assertions.assertNotNull(foundProject);
        Assertions.assertEquals(savedProject.getId(), foundProject.getId());
        Assertions.assertEquals(savedProject.getTitle(), foundProject.getTitle());
        Assertions.assertEquals(savedProject.getOwner(), foundProject.getOwner());
        Assertions.assertEquals(savedProject.getDescription(), foundProject.getDescription());
    }

    @Test
    void findProjectById_ByNonOwner_returnUnsupportedOperationExecution() {
        String projectTitle = "Test 1";
        ProjectCreateRequest project = new ProjectCreateRequest(projectTitle, sampleUser.getId(), null);

        Project savedProject = projectService.create(project);

        User user2 = userRepository.save(new User("user2@example.com", "User2!@#1234"));

        UnsupportedOperationException exception =
                Assertions.assertThrows(
                        UnsupportedOperationException.class,
                        () ->projectService.findPersonalProjectById(savedProject.getId(), user2.getId())
                );

        Assertions.assertNotNull(exception);
        Assertions.assertNull(exception.getMessage());
    }

    @Test
    void findProjectByTitle_ByOwner_() {
    }
}
