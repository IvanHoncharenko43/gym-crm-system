package org.example.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final static int HOUR = 3600000;
    private final Map<String, Instant> blackList = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    public void blackListToken(String token){
        Instant expirationDate = jwtService.extractExpiration(token);
        if(expirationDate.isAfter(Instant.now())){
            blackList.put(token, expirationDate);
        }
    }

    public boolean isBlackListed(String token){
        return blackList.containsKey(token);
    }

    @Scheduled(fixedRate = HOUR)
    public void cleanExpiredTokens(){
        blackList.entrySet().removeIf(dateEntry -> dateEntry.getValue().isBefore(Instant.now()));
    }
}
