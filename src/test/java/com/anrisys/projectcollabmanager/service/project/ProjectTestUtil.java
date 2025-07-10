package com.anrisys.projectcollabmanager.service.project;

import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.repository.jdbc.JDBCUserRepository;
import com.anrisys.projectcollabmanager.repository.UserRepository;

import javax.sql.DataSource;

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
