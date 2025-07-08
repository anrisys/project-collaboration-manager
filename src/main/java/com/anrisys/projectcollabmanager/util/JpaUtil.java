package com.anrisys.projectcollabmanager.util;

import com.anrisys.projectcollabmanager.application.DBConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JpaUtil {
    private static final EntityManagerFactory entityManagerFactory;

    static {
        Map<String, Object> props = new HashMap<>();

        // Provide data source from HikariCP into Hibernate
        props.put("jakarta.persistence.nonJtaDataSource", DBConfig.getDataSource());

        entityManagerFactory = Persistence.createEntityManagerFactory("projectCollabManagerPU", props);
    }

    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    public static void shutdown() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            DBConfig.closeDataSource();
        }
    }
}
