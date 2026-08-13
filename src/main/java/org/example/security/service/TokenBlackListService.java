package org.example.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {
    private final static int HOUR = 3600000;
    private final Map<String, Date> blackList = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    public void blackListToken(String token){
        Date expirationDate = jwtService.extractExpiration(token);
        if(expirationDate.after(new Date())){
            blackList.put(token, expirationDate);
        }
    }

    public boolean isBlackListed(String token){
        return blackList.containsKey(token);
    }

    @Scheduled(fixedRate = HOUR)
    public void cleanExpiredTokens(){
        Date now = new Date();
        blackList.entrySet().removeIf(dateEntry -> dateEntry.getValue().before(now));
    }
}
