package org.example.user;

import org.example.core.AbstractRepositoryTest;
import org.example.user.repository.UserEntity;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String username) {
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setPassword("password1234");
        user.setIsActive(true);
        return entityManager.persistAndFlush(user);
    }

    @Test
    void save_PersistUserEntity_EntityIsNew() {
        String username = "John.Doe";
        UserEntity user = new UserEntity();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setPassword("password1234");
        user.setIsActive(true);

        UserEntity savedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedUser.getId()).isNotNull();
        UserEntity existingUser = entityManager.find(UserEntity.class, savedUser.getId());
        assertThat(existingUser).isNotNull();
        assertThat(existingUser.getUsername()).isEqualTo(username);
    }

    @Test
    void save_MergeUserEntity_EntityHasId() {
        String username = "John.Doe";
        UserEntity user = persistUser(username);
        entityManager.clear();

        UserEntity toUpdate = userRepository.findById(user.getId()).orElseThrow();
        toUpdate.setPassword("newPassword");
        userRepository.save(toUpdate);
        entityManager.flush();
        entityManager.clear();

        UserEntity existingUser = entityManager.find(UserEntity.class, user.getId());
        assertThat(existingUser.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void findById_ReturnUser_IdExists() {
        String username = "John.Doe";
        UserEntity user = persistUser(username);
        entityManager.clear();

        Optional<UserEntity> existingUser = userRepository.findById(user.getId());

        assertThat(existingUser).isPresent();
        assertThat(existingUser.get().getUsername()).isEqualTo(username);
    }

    @Test
    void findById_ReturnEmpty_IdDoesNotExist() {
        Optional<UserEntity> user = userRepository.findById(99L);

        assertThat(user).isEmpty();
    }

    @Test
    void findByUsername_ReturnUser_UsernameExists() {
        String username = "John.Doe";
        persistUser(username);
        entityManager.clear();

        Optional<UserEntity> existingUser = userRepository.findByUsername(username);

        assertThat(existingUser).isPresent();
        assertThat(existingUser.get().getUsername()).isEqualTo(username);
    }

    @Test
    void findByUsername_ReturnEmpty_UsernameDoesNotExist() {
        Optional<UserEntity> user = userRepository.findByUsername("not.found");

        assertThat(user).isEmpty();
    }

    @Test
    void findUsernamesByBaseNameForUpdate_ReturnUsernamesList_BaseNameExists() {
        String baseUsername = "John.Doe";
        persistUser("Jane.Smith");
        persistUser(baseUsername + 1);
        persistUser(baseUsername + 2);
        entityManager.clear();

        List<String> usernames = userRepository.findUsernamesByBaseNameForUpdate(baseUsername);

        assertThat(usernames).containsExactlyInAnyOrder(baseUsername+1, baseUsername+2);
    }

    @Test
    void findUsernamesByBaseNameForUpdate_ReturnEmptyList_NoMatchExists() {
        persistUser("John.Doe");
        entityManager.clear();

        List<String> usernames = userRepository.findUsernamesByBaseNameForUpdate("Jane.Smith");

        assertThat(usernames).isEmpty();
    }
}
