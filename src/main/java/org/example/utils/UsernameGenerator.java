package org.example.utils;

import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class UsernameGenerator {

    public String generate(String firstName, String lastName, Set<String> existingUsernames){
        String baseUsername = firstName + "." + lastName;
        if (!existingUsernames.contains(baseUsername)) {
            return baseUsername;
        }
        int suffix = 1;
        String newUsername;
        do {
            newUsername = baseUsername + suffix;
            suffix++;
        } while (existingUsernames.contains(newUsername));
        return newUsername;
    }
}
