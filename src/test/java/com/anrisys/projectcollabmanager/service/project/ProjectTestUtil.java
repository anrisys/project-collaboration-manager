package com.anrisys.projectcollabmanager.service.project;

import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.entity.Project;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.repository.JDBCUserRepository;
import com.anrisys.projectcollabmanager.repository.ProjectRepository;
import com.anrisys.projectcollabmanager.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ProjectTestUtil {
    protected static DataSource dataSource;
    protected static UserRepository userRepository;

    static {
        dataSource = DBConfig.getDataSource();
        userRepository = new JDBCUserRepository(dataSource);
    }

    static User createSampleUser() {
        User user = new User("sample@user.com", "Sample1234!@#$");

        return userRepository.save(user);
    }

    static void deleteSampleUser() {
        User sampleUser = userRepository.findByEmail("sample@user.com").orElseThrow();

        userRepository.deleteById(sampleUser.getId());
    }
}
