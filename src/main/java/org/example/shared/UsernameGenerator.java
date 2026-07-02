package org.example.shared;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UsernameGenerator {
    private final Map<String, AtomicInteger> usernameCount = new ConcurrentHashMap<>();

    public String generate(String firstName, String lastName){
        String baseUsername = firstName + lastName;
        int count = usernameCount.computeIfAbsent(baseUsername, v -> new AtomicInteger(0))
                .incrementAndGet();
        if(count == 1){
            return baseUsername;
        }
        return baseUsername + (count-1);
    }

    public void initData(List<String> existingUsernames) {
        if (existingUsernames == null || existingUsernames.isEmpty()) {
            return;
        }
        for (String username : existingUsernames) {
            String baseUsername = username.replaceAll("\\d+$", "");
            usernameCount.computeIfAbsent(baseUsername, k -> new AtomicInteger(0))
                    .incrementAndGet();
        }
    }
}
