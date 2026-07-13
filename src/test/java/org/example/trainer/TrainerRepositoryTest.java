package org.example.trainer;

import org.example.trainer.repository.TrainerEntity;
import org.example.trainer.repository.TrainerRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TrainerRepositoryTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TrainerEntity> query;

    @InjectMocks
    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void findByUsername_ReturnTrainer_UsernameExists() {
        String username = "John.Doe";
        String hql = "FROM TrainerEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setUsername(username);

        when(session.createQuery(hql, TrainerEntity.class)).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainer));

        Optional<TrainerEntity> result = trainerRepository.findByUsername(username);
        assertTrue(result.isPresent());
        assertEquals(trainer, result.get());
        verify(session, times(1)).createQuery(hql, TrainerEntity.class);
        verify(query, times(1)).setParameter("username", username);
        verify(query, times(1)).uniqueResultOptional();
    }

    @Test
    void findByUsernames_ReturnTrainersList_UsernamesExist(){
        List<String> usernames = List.of("John.Doe", "Jane.Smith");
        TrainerEntity trainer1 = new TrainerEntity();
        TrainerEntity trainer2 = new TrainerEntity();
        List<TrainerEntity> trainers = List.of(trainer1, trainer2);
        String hql = "SELECT tr FROM TrainerEntity tr " +
                "JOIN FETCH tr.user " +
                "JOIN FETCH tr.specialization " +
                "WHERE tr.user.username IN :usernames";

        when(session.createQuery(hql, TrainerEntity.class)).thenReturn(query);
        when(query.setParameter("usernames", usernames)).thenReturn(query);
        when(query.getResultList()).thenReturn(trainers);

        List<TrainerEntity> result = trainerRepository.findByUsernames(usernames);
        assertEquals(trainers, result);
        assertEquals(2, result.size());
        verify(session, times(1)).createQuery(hql, TrainerEntity.class);
        verify(query, times(1)).setParameter("usernames", usernames);
        verify(query, times(1)).getResultList();
    }

    @Test
    void findUnassignedTrainersByTraineeUsername(){
        String traineeUsername = "John.Doe";
        String hql = """ 
                SELECT tr FROM TrainerEntity tr 
                JOIN FETCH tr.user
                JOIN FETCH tr.specialization
                WHERE NOT EXISTS (SELECT 1 FROM tr.trainees t WHERE t.user.username = :username)""";
        TrainerEntity trainer = new TrainerEntity();
        List<TrainerEntity> trainers = List.of(trainer);

        when(session.createQuery(hql, TrainerEntity.class)).thenReturn(query);
        when(query.setParameter("username", traineeUsername)).thenReturn(query);
        when(query.getResultList()).thenReturn(trainers);

        List<TrainerEntity> result = trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername);
        assertEquals(1, result.size());
        assertEquals(trainers, result);
        verify(session, times(1)).createQuery(hql, TrainerEntity.class);
        verify(query, times(1)).setParameter("username", traineeUsername);
        verify(query, times(1)).getResultList();
    }

    @Test
    void deleteByUsername_RemoveTrainer_UsernameExists(){
        String username = "John.Doe";
        String hql = "FROM TrainerEntity t JOIN FETCH t.user WHERE t.user.username = :username";
        TrainerEntity trainer = new TrainerEntity();
        trainer.setUser(new UserEntity());
        trainer.getUser().setUsername(username);

        when(session.createQuery(hql, TrainerEntity.class)).thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(trainer));

        trainerRepository.deleteByUsername(username);
        verify(session, times(1)).createQuery(hql, TrainerEntity.class);
        verify(query, times(1)).setParameter("username", username);
        verify(query, times(1)).uniqueResultOptional();
        verify(session, times(1)).remove(trainer);
    }
}
