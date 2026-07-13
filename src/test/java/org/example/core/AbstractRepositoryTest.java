package org.example.core;

import lombok.Data;
import org.example.core.repository.AbstractRepository;
import org.example.core.repository.Identifiable;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AbstractRepositoryTest {

    @Data
    private static class TestEntity implements Identifiable {
        private Long id;
        private String name;

        public TestEntity(Long id, String name) { this.id = id; this.name = name; }
    }

    private static class TestRepository extends AbstractRepository<TestEntity> {
        public TestRepository(SessionFactory sessionFactory) {
            super(sessionFactory, TestEntity.class);
        }
    }

    private static final Long TEST_ENTITY1_ID = 1L;
    private static final TestEntity TEST_ENTITY1 = new TestEntity(TEST_ENTITY1_ID, "Test Entity #1");
    private static final Long TEST_ENTITY2_ID = 2L;
    private static final TestEntity TEST_ENTITY2 = new TestEntity(TEST_ENTITY2_ID, "Test Entity #2");

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query<TestEntity> query;

    @InjectMocks
    private TestRepository repository;

    @BeforeEach
    void setUp() {
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    @Test
    void create_PersistEntity_EntityIsValid() {
        TestEntity createdEntity = repository.create(TEST_ENTITY1);
        verify(session, times(1)).persist(TEST_ENTITY1);
        assertEquals(TEST_ENTITY1, createdEntity);
    }

    @Test
    void getById_ReturnEntity_IdExists() {
        when(session.find(TestEntity.class, TEST_ENTITY1_ID)).thenReturn(TEST_ENTITY1);

        Optional<TestEntity> result = repository.getById(TEST_ENTITY1_ID);

        assertTrue(result.isPresent());
        assertEquals(TEST_ENTITY1, result.get());
        verify(session, times(1)).find(TestEntity.class, TEST_ENTITY1_ID);
    }

    @Test
    void getById_ReturnEmpty_IdDoesNotExist() {
        when(session.find(TestEntity.class, 99L)).thenReturn(null);

        Optional<TestEntity> retrieved = repository.getById(99L);
        assertTrue(retrieved.isEmpty());
        verify(session, times(1)).find(TestEntity.class, 99L);
    }

    @Test
    void getAll_ShouldReturnList_WithAllEntities() {
        when(session.createQuery("FROM TestEntity", TestEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(TEST_ENTITY1, TEST_ENTITY2));

        List<TestEntity> allEntities = repository.getAll();
        assertEquals(2, allEntities.size());
        assertTrue(allEntities.contains(TEST_ENTITY1));
        assertTrue(allEntities.contains(TEST_ENTITY2));
    }

    @Test
    void update_MergeEntity_EntityExists() {
        when(session.merge(TEST_ENTITY1)).thenReturn(TEST_ENTITY1);

        TestEntity result = repository.update(TEST_ENTITY1);
        assertEquals(TEST_ENTITY1, result);
        verify(session, times(1)).merge(TEST_ENTITY1);
    }

    @Test
    void deleteById_RemoveEntity_IdExists() {
        when(session.find(TestEntity.class, TEST_ENTITY1_ID)).thenReturn(TEST_ENTITY1);

        repository.deleteById(TEST_ENTITY1_ID);
        verify(session, times(1)).remove(TEST_ENTITY1);
    }
}
