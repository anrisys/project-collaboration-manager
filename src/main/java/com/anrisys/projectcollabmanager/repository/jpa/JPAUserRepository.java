package com.anrisys.projectcollabmanager.repository.jpa;

import com.anrisys.projectcollabmanager.dto.UserDTO;
import com.anrisys.projectcollabmanager.dto.UserRegisterRequest;
import com.anrisys.projectcollabmanager.entity.User;
import com.anrisys.projectcollabmanager.exception.core.DataAccessException;
import com.anrisys.projectcollabmanager.repository.UserRepository;
import com.anrisys.projectcollabmanager.util.JpaUtil;
import com.anrisys.projectcollabmanager.util.LoggerUtil;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class JPAUserRepository implements UserRepository {
    private final static Logger log = LoggerFactory.getLogger(JPAUserRepository.class);

    @Override
    public UserDTO save(UserRegisterRequest user) throws DataAccessException {
        final String methodName = "save";
        EntityManager em = null;
        EntityTransaction tx = null;
        String errMsg = "Failed to save user: ";
        try {
            em = JpaUtil.getEntityManager();
            tx = em.getTransaction();

            log.debug("[{}] Persisting new User entity for email={}", methodName, LoggerUtil.maskEmail(user.email()));
            tx.begin();

            User newUser = new User();
            newUser.setEmail(user.email());
            newUser.setHashedPassword(user.password());

            em.persist(newUser);
            em.flush();

            tx.commit();

            log.debug("[{}] Entity persisted, generated ID={}", methodName, newUser.getId());
            return new UserDTO(newUser.getId(), newUser.getEmail());
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, tx, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(tx, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    @Override
    public Optional<User> findById(Long id) throws DataAccessException {
        final String methodName = "findById";
        EntityManager em = null;
        final String errMsg = "Failed to find user";
        try {
            em = JpaUtil.getEntityManager();
            log.debug("[{}] Finding userId={}", methodName, id);
            User user = em.find(User.class, id);
            if (user == null) {
                return Optional.empty();
            } else {
                return Optional.of(user);
            }
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, null, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(null, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) throws DataAccessException {
        EntityManager em = null;
        final String methodName = "findByEmail";
        final String errMsg = "Failed to find user by email";
        try {
            em = JpaUtil.getEntityManager();
            String sql = "SELECT u FROM User u WHERE u.email = :email";
            TypedQuery<User> query = em.createQuery(sql, User.class);
            LoggerUtil.logSQL(log, methodName, sql);

            query.setParameter("email", email);
            LoggerUtil.logSQLExecuted(log, methodName);
            User user = query.getSingleResult();
            LoggerUtil.logSQLExecuted(log, methodName);
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, null, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(null, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    @Override
    public boolean existsByEmail(String email) throws DataAccessException {
        final String methodName = "existsByEmail";
        log.debug("[{}] Checking if email={} is registered", methodName, LoggerUtil.maskEmail(email));
        EntityManager em = null;
        String errMsg = "Failed to check email existence";
        try {
            em = JpaUtil.getEntityManager();
            return em.createQuery("SELECT COUNT(*) > 0 FROM user u WHERE u.email = :email", Boolean.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, null, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(null, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    @Override
    public User updateEmail(Long id, String newEmail) throws DataAccessException {
        EntityManager em = null;
        EntityTransaction tx = null;
        final String methodName  = "updateEmail";
        final String errMsg = "Failed to update email";
        try {
            em = JpaUtil.getEntityManager();
            tx = em.getTransaction();

            tx.begin();
            User user = em.find(User.class, id);

            if (user == null) {
                throw new EntityNotFoundException("User not found");
            }

            user.setEmail(newEmail);

            tx.commit();
            return user;
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, tx, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(tx, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    @Override
    public void updatePassword(Long id, String newPassword) throws DataAccessException {
        EntityManager em = null;
        EntityTransaction tx = null;
        final String methodName = "updatePassword";
        final String errMsg = "Failed to updatePassword";
        try {
            em = JpaUtil.getEntityManager();
            tx = em.getTransaction();

            tx.begin();

            User user = em.find(User.class, id);

            if (user == null) {
                throw new EntityNotFoundException("User not found");
            }
            tx.commit();
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, tx, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(tx, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }

    @Override
    public void deleteById(Long id) throws DataAccessException {
        EntityManager em = null;
        EntityTransaction tx = null;
        final String methodName = "deleteById";
        final String errMsg = "Failed to delete user";
        try {
            em = JpaUtil.getEntityManager();
            tx = em.getTransaction();

            tx.begin();
            User user = em.find(User.class, id);

            if (user != null) {
                throw new EntityNotFoundException("User not found");
            }

            em.remove(user);
            tx.commit();
        } catch (PersistenceException e) {
            throw JpaUtil.handlePersistenceException(e, tx, log, methodName, errMsg);
        } catch (Exception e) {
            throw JpaUtil.handleUnexpectedException(tx, log, errMsg, methodName, e);
        } finally {
            JpaUtil.closeEntityManager(em);
        }
    }
}
