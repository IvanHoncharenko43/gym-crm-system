package org.example.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UsernameGeneratorTest {

    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UsernameGenerator();
    }

    @Test
    void generate_ReturnBaseUsername_WhenNoDuplicatesExist() {
        String actualUsername = usernameGenerator.generate("John", "Doe", Collections.emptySet());
        assertEquals("John.Doe", actualUsername);
    }

    @Test
    void generate_ReturnUsernameWithSuffix_WhenDuplicatesExist() {
        usernameGenerator.generate("John", "Doe", Collections.emptySet());
        assertEquals("John.Doe1",
                usernameGenerator.generate("John", "Doe", Set.of("John.Doe")));
        assertEquals("John.Doe2",
                usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "John.Doe1")));
        assertEquals("John.Doe3",
                usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "John.Doe1", "John.Doe2")));
    }

    @Test
    void generate_ReturnIndependentUsernames_ForDifferentNames() {
        String user1 = usernameGenerator.generate("John", "Doe", Set.of());
        String user2 = usernameGenerator.generate("Jane", "Smith", Set.of("John.Doe"));
        String user1Duplicate = usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "Jane.Smith"));
        assertEquals("John.Doe", user1);
        assertEquals("Jane.Smith", user2);
        assertEquals("John.Doe1", user1Duplicate);
    }
}
