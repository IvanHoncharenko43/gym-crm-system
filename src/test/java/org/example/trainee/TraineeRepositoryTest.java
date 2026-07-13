package org.example.trainee;

import org.example.trainee.repository.TraineeEntity;
import org.example.trainee.repository.TraineeRepository;
import org.example.user.repository.UserEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TraineeEntity> query;

    @InjectMocks
    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void findByUsername_ReturnTrainee_UsernameExists() {
        String username = "John.Doe";
        String hql = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setUsername(username);

        when(session.createQuery(hql, TraineeEntity.class)).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        Optional<TraineeEntity> result = traineeRepository.findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(session, times(1)).createQuery(hql, TraineeEntity.class);
        verify(query, times(1)).setParameter("username", username);
        verify(query, times(1)).uniqueResultOptional();
    }

    @Test
    void deleteByUsername_RemoveTrainee_UsernameExists(){
        String username = "John.Doe";
        String hql = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setUsername(username);

        when(session.createQuery(hql, TraineeEntity.class)).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        traineeRepository.deleteByUsername(username);
        verify(session, times(1)).createQuery(hql, TraineeEntity.class);
        verify(query, times(1)).setParameter("username", username);
        verify(query, times(1)).uniqueResultOptional();
        verify(session, times(1)).remove(trainee);
    }
}
