package org.example.crm.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlackListService {
    private final static int HOUR = 3600000;
    private final Map<String, Instant> blackList = new ConcurrentHashMap<>();

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public void blackListToken(String token){
        Instant cleanUpTime = Instant.now().plusMillis(jwtExpirationMs);
        blackList.put(token, cleanUpTime);
    }

    public boolean isBlackListed(String token){
        return blackList.containsKey(token);
    }

    @Scheduled(fixedRate = HOUR)
    public void cleanExpiredTokens(){
        blackList.entrySet().removeIf(dateEntry -> dateEntry.getValue().isBefore(Instant.now()));
    }
}
