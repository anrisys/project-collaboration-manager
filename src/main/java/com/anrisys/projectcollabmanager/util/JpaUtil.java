package com.anrisys.projectcollabmanager.util;

import com.anrisys.projectcollabmanager.application.DBConfig;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import jakarta.persistence.*;
import org.slf4j.Logger;

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

    public static DataAccessException handlePersistenceException(PersistenceException e,
                                                  EntityTransaction tx,
                                                  Logger log,
                                                  String methodName,
                                                  String errMsg) {
        if (tx != null) {
            rollbackTx(tx);
            errMsg = "Transaction rolled back. " + errMsg;
        }
        log.error("[{}] {}: due to persistence error {}", methodName, errMsg, e.getMessage());
        return new DataAccessException(errMsg, e);
    }

    public static DataAccessException handleUnexpectedException(EntityTransaction tx,
                                                  Logger log,
                                                  String errMsg,
                                                  String methodName,
                                                  Exception e) {
        if (tx != null) {
            rollbackTx(tx);
            errMsg = "Transaction rolled back. " + errMsg;
        }
        log.error("[{}] {}: Unexpected error occurred. ", methodName, errMsg, e);
        return new DataAccessException("Unexpected error occurred", e);
    }

    public static void closeEntityManager(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    private static void rollbackTx(EntityTransaction tx) {
        if (tx.isActive()) tx.rollback();
    }
}
