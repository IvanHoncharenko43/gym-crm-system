package org.example.utils;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
