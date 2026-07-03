package org.example.shared;

import lombok.Data;
import org.example.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractRepositoryTest {

    @Data
    private static class TestEntity implements Identifiable {
        private Long id;
        private String name;

        public TestEntity(String name) { this.name = name; }
        public TestEntity(Long id, String name) { this.id = id; this.name = name; }
    }

    private static class TestRepository extends AbstractRepository<TestEntity> {}

    private static final Long TEST_ENTITY1_ID = 1L;
    private static final TestEntity TEST_ENTITY1 = new TestEntity(TEST_ENTITY1_ID, "Test Entity #1");
    private static final Long TEST_ENTITY2_ID = 2L;
    private static final TestEntity TEST_ENTITY2 = new TestEntity(TEST_ENTITY2_ID, "Test Entity #2");

    private TestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestRepository();
        repository.initStorage(new ConcurrentHashMap<>());
    }

    @Test
    void create_AssignIdAndStoreEntity_EntityIsValid() {;
        TestEntity createdEntity = repository.create(TEST_ENTITY1);
        assertNotNull(createdEntity.getId());
        assertEquals(TEST_ENTITY1, createdEntity);
    }

    @Test
    void getById_ReturnEntity_IdExists() {
        repository.create(TEST_ENTITY1);
        Optional<TestEntity> result = repository.getById(TEST_ENTITY1_ID);
        assertTrue(result.isPresent());
        assertEquals(TEST_ENTITY1, result.get());
    }

    @Test
    void getById_ReturnEmpty_IdDoesNotExist() {
        Optional<TestEntity> retrieved = repository.getById(99L);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getAll_ReturnImmutableList_AllEntities() {
        repository.create(TEST_ENTITY1);
        repository.create(TEST_ENTITY2);
        List<TestEntity> allEntities = repository.getAll();
        assertEquals(2, allEntities.size());
        assertThrows(UnsupportedOperationException.class, () -> allEntities.add(new TestEntity("Test Entity #3")));
    }

    @Test
    void getAll_ReturnEmptyList_StorageIsEmpty() {
        List<TestEntity> result = repository.getAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void update_UpdateEntity_ItExists() {
        TestEntity created = repository.create(TEST_ENTITY1);
        TestEntity toUpdate = new TestEntity(created.getId(), "Updated Entity");
        TestEntity updated = repository.update(toUpdate);
        assertEquals("Updated Entity", updated.getName());
    }

    @Test
    void update_ThrowNotFoundException_EntityDoesNotExist() {
        TestEntity nonExistentEntity = new TestEntity(99L, "Test Entity #99");
        assertThrows(NotFoundException.class, () -> repository.update(nonExistentEntity));
    }

    @Test
    void deleteById_RemoveEntity_IdExists() {
        TestEntity created = repository.create(TEST_ENTITY1);
        repository.deleteById(created.getId());
        assertTrue(repository.getById(created.getId()).isEmpty());
    }

    @Test
    void initStorage_InitializeMapAndSetMaxIdCounter() {
        repository = new TestRepository();
        Map<Long, TestEntity> initialData = new ConcurrentHashMap<>();
        initialData.put(TEST_ENTITY1_ID, TEST_ENTITY1);
        initialData.put(20L, TEST_ENTITY2);

        repository.initStorage(initialData);
        assertEquals(2, repository.getAll().size());
        TestEntity newEntity = repository.create(new TestEntity("New Entity"));
        assertEquals(21L, newEntity.getId());
    }

    @Test
    void initStorage_NotOverwrite_CalledTwice() {
        repository = new TestRepository();
        Map<Long, TestEntity> initialData = new ConcurrentHashMap<>();
        initialData.put(TEST_ENTITY1_ID, TEST_ENTITY1);

        repository.initStorage(initialData);
        Map<Long, TestEntity> overrideData = new ConcurrentHashMap<>();
        repository.initStorage(overrideData);
        assertFalse(repository.getAll().isEmpty());
    }
}
