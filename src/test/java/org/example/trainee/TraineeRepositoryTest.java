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
import org.mockito.Spy;
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

    @Spy
    @InjectMocks
    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void save_PersistEntity_EntityDoesNotHaveId() {
        TraineeEntity trainee = new TraineeEntity();

        doReturn(Optional.empty()).when(traineeRepository).findById(null);

        TraineeEntity createdEntity = traineeRepository.save(trainee);
        verify(traineeRepository, times(1)).findById(null);
        verify(session, times(1)).persist(trainee);
        assertEquals(trainee, createdEntity);
    }

    @Test
    void save_MergeEntity_EntityHasId() {
        Long id = 1L;
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(id);

        doReturn(Optional.of(trainee)).when(traineeRepository).findById(id);
        when(session.merge(trainee)).thenReturn(trainee);

        TraineeEntity result = traineeRepository.save(trainee);
        assertEquals(trainee, result);
        verify(traineeRepository, times(1)).findById(id);
        verify(session, times(1)).merge(trainee);
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
    void findById_ReturnEntity_IdExists() {
        TraineeEntity trainee = new TraineeEntity();
        Long id = 1L;
        trainee.setId(id);
        String hql = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.id = :id";

        when(session.createQuery(hql, TraineeEntity.class)).thenReturn(query);
        when(query.setParameter("id", id)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainee));

        Optional<TraineeEntity> result = traineeRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals(trainee, result.get());
        verify(session, times(1)).createQuery(hql, TraineeEntity.class);
        verify(query, times(1)).setParameter("id", id);
        verify(query, times(1)).uniqueResultOptional();
    }

    @Test
    void findById_ReturnEmpty_IdDoesNotExist() {
        String hql = "FROM TraineeEntity t JOIN FETCH t.user WHERE t.id = :id";
        when(session.createQuery(hql, TraineeEntity.class)).thenReturn(query);
        when(query.setParameter("id", 99L)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        Optional<TraineeEntity> result = traineeRepository.findById(99L);
        assertTrue(result.isEmpty());
        verify(session, times(1)).createQuery(hql, TraineeEntity.class);
        verify(query, times(1)).setParameter("id", 99L);
        verify(query, times(1)).uniqueResultOptional();
    }

    @Test
    void deleteByUsername_RemoveTrainee_UsernameExists(){
        String username = "John.Doe";
        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(new UserEntity());
        trainee.getUser().setUsername(username);

        doReturn(Optional.of(trainee)).when(traineeRepository).findByUsername(username);

        traineeRepository.deleteByUsername(username);
        verify(traineeRepository, times(1)).findByUsername(username);
        verify(session, times(1)).remove(trainee);
    }
}
