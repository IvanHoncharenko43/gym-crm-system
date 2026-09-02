package org.example.crm.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
public class UsernameGenerator {

    private final Map<String, AtomicInteger> usernameCount = new ConcurrentHashMap<>();

    public String generate(String firstName, String lastName, Set<String> existingUsernames){
        String baseUsername = firstName + "." + lastName;
        AtomicInteger counter = usernameCount.computeIfAbsent(baseUsername, s -> new AtomicInteger(0));
        return Stream.generate(counter::incrementAndGet)
                .map(count -> count == 1 ? baseUsername : baseUsername + (count - 1))
                .filter(username -> !existingUsernames.contains(username))
                .findFirst()
                .orElseThrow();
    }
}
