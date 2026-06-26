package org.example.shared;

import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractRepositoryTest {

//    @Data
    private static class TestEntity implements Identifiable {
        private Long id;
        private String name;

        public TestEntity(String name) { this.name = name; }
        public TestEntity(Long id, String name) { this.id = id; this.name = name; }

        @Override public Long getId() { return id; }
        @Override public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
    }

    private static class TestRepository extends AbstractRepository<TestEntity> {}

    private TestRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TestRepository();
    }

    @Test
    void create_AssignIdAndStoreEntity_EntityIsValid() {
        TestEntity entity = new TestEntity("Entity 1");
        TestEntity createdEntity = repository.create(entity);
        assertNotNull(createdEntity.getId(), "Repository should assign an ID");
        assertEquals(1L, createdEntity.getId());
        assertEquals("Entity 1", repository.getById(1L).get().getName());
    }

    @Test
    void create_ThrowException_EntityHasId() {
        TestEntity entity = new TestEntity(1L, "Entity 1");
        assertThrows(IllegalArgumentException.class, () -> repository.create(entity));
    }

    @Test
    void getById_ReturnEntity_IdExists() {
        repository.create(new TestEntity("Entity 1")); // ID 1
        Optional<TestEntity> retrieved = repository.getById(1L);
        assertTrue(retrieved.isPresent());
        assertEquals("Entity 1", retrieved.get().getName());
    }

    @Test
    void getById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<TestEntity> retrieved = repository.getById(99L);
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void getAll_ShouldReturnImmutableList_WithAllEntities() {
        repository.create(new TestEntity("Entity 1"));
        repository.create(new TestEntity("Entity 2"));
        List<TestEntity> allEntities = repository.getAll();
        assertEquals(2, allEntities.size());
        assertThrows(UnsupportedOperationException.class, () -> allEntities.add(new TestEntity("Entity 3")),
                "The returned list must be immutable");
    }

    @Test
    void update_UpdateEntity_ItExists() {
        TestEntity created = repository.create(new TestEntity("Entity 1"));
        TestEntity toUpdate = new TestEntity(created.getId(), "Updated Entity");
        TestEntity updated = repository.update(toUpdate);
        assertEquals("Updated Entity", updated.getName());
        assertEquals("Updated Entity", repository.getById(created.getId()).get().getName());
    }

    @Test
    void update_ThrowException_EntityDoesNotExist() {
        TestEntity nonExistentEntity = new TestEntity(99L, "Entity 99");
        assertThrows(IllegalArgumentException.class, () -> repository.update(nonExistentEntity));
    }

    @Test
    void deleteById_RemoveEntity_ItExists() {
        TestEntity created = repository.create(new TestEntity("Entity 1"));
        repository.deleteById(created.getId());
        assertTrue(repository.getById(created.getId()).isEmpty());
    }

    @Test
    void initStorage_InitializeMapAndSetMaxIdCounter() {
        Map<Long, TestEntity> initialData = new HashMap<>();
        initialData.put(10L, new TestEntity(10L, "Entity 10"));
        initialData.put(20L, new TestEntity(20L, "Entity 20"));

        repository.initStorage(initialData);
        assertTrue(repository.getById(20L).isPresent());
        TestEntity newEntity = repository.create(new TestEntity("New Entity"));
        assertEquals(21L, newEntity.getId());
    }

    @Test
    void initStorage_NotOverwrite_CalledTwice() {
        Map<Long, TestEntity> initialData = new HashMap<>();
        initialData.put(1L, new TestEntity(1L, "Entity 1"));

        repository.initStorage(initialData);
        Map<Long, TestEntity> overrideData = new HashMap<>();
        repository.initStorage(overrideData);
        assertFalse(repository.getAll().isEmpty(), "Storage should not be overwritten after the first initialization");
    }
}
