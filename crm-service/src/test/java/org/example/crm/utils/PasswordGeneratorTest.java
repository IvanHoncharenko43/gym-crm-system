package org.example.crm.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void generate_ReturnStringOfExactLength10() {
        String password = passwordGenerator.generate();
        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void generate_ContainOnlyAllowedCharacters() {
        String allowedCharactersRegex = "^[A-Za-z0-9]+$";
        String password = passwordGenerator.generate();
        assertTrue(password.matches(allowedCharactersRegex));
    }

    @Test
    void generate_GenerateUniquePasswords() {
        int numberOfPasswordsToGenerate = 1000;
        Set<String> generatedPasswords = new HashSet<>();
        for (int i = 0; i < numberOfPasswordsToGenerate; i++) {
            generatedPasswords.add(passwordGenerator.generate());
        }
        assertEquals(numberOfPasswordsToGenerate, generatedPasswords.size());
    }
}
