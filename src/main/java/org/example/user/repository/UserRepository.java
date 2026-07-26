package org.example.user.repository;

import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserRepository {

    private final SessionFactory sessionFactory;

    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public UserEntity save(UserEntity user){
        Optional<UserEntity> existingUser = findByUsername(user.getUsername());
        if (existingUser.isEmpty()) {
            getSession().persist(user);
            log.info("Created user with ID: {}", user.getId());
            return user;
        }
        UserEntity updatedUser = getSession().merge(user);
        log.info("Updated user with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    public Optional<UserEntity> findByUsername(String username){
        log.info("Started getting user by username");
        String hql = "FROM UserEntity u WHERE u.username = :username";
        return getSession().createQuery(hql, UserEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public List<String> findUsernamesByBaseNameForUpdate(String baseName){
        log.info("Started getting usernames by base name for update");
        String hql = "SELECT u.username FROM UserEntity u WHERE u.username LIKE :baseName";
        return getSession().createQuery(hql, String.class)
                .setParameter("baseName", baseName + "%")
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
    }

    private Session getSession(){
        return sessionFactory.getCurrentSession();
    }
}
