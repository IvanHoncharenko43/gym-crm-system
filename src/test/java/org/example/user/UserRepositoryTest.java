package org.example.user;

import jakarta.persistence.LockModeType;
import org.apache.catalina.User;
import org.example.trainee.repository.TraineeEntity;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<String> queryString;

    @Mock
    private Query<UserEntity> queryUserEntity;

    @Spy
    @InjectMocks
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void save_PersistEntity_EntityDoesNotHaveUsername() {
        UserEntity user = new UserEntity();

        doReturn(Optional.empty()).when(userRepository).findByUsername(null);

        UserEntity createdUser = userRepository.save(user);
        verify(userRepository, times(1)).findByUsername(null);
        verify(session, times(1)).persist(user);
        assertEquals(user, createdUser);
    }

    @Test
    void save_MergeEntity_EntityHasId() {
        String username = "John.Doe";
        UserEntity user = new UserEntity();
        user.setUsername(username);

        doReturn(Optional.of(user)).when(userRepository).findByUsername(username);
        when(session.merge(user)).thenReturn(user);

        UserEntity result = userRepository.save(user);
        assertEquals(user, result);
        verify(userRepository, times(1)).findByUsername(username);
        verify(session, times(1)).merge(user);
    }

    @Test
    void findByUsername_ReturnUser_UsernameExists() {
        String username = "John.Doe";
        String hql = "FROM UserEntity u WHERE u.username = :username";
        UserEntity user = new UserEntity();
        user.setUsername(username);

        when(session.createQuery(hql, UserEntity.class)).thenReturn(queryUserEntity);
        when(queryUserEntity.setParameter("username", username)).thenReturn(queryUserEntity);
        when(queryUserEntity.uniqueResultOptional()).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userRepository.findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(session, times(1)).createQuery(hql, UserEntity.class);
        verify(queryUserEntity, times(1)).setParameter("username", username);
        verify(queryUserEntity, times(1)).uniqueResultOptional();
    }

    @Test
    void findUsernamesByBaseName_ReturnUsernamesList_BaseNameExist(){
        String baseName = "John.Doe";
        String hql = "SELECT u.username FROM UserEntity u WHERE u.username LIKE :baseName";
        List<String> usernames = List.of("John.Doe", "John.Doe1", "John.Doe2");

        when(session.createQuery(hql, String.class)).thenReturn(queryString);
        when(queryString.setParameter("baseName", baseName + "%")).thenReturn(queryString);
        when(queryString.setLockMode(LockModeType.PESSIMISTIC_WRITE)).thenReturn(queryString);
        when(queryString.getResultList()).thenReturn(usernames);

        List<String> result = userRepository.findUsernamesByBaseNameForUpdate(baseName);
        assertEquals(3, result.size());
        assertEquals(usernames, result);
        verify(session, times(1)).createQuery(hql, String.class);
        verify(queryString, times(1)).setParameter("baseName", baseName + "%");
        verify(queryString, times(1)).setLockMode(LockModeType.PESSIMISTIC_WRITE);
        verify(queryString, times(1)).getResultList();
    }

}
