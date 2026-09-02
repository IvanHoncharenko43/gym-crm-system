package org.example.crm.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BruteForceProtectionService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);
    private static final int HOUR = 3600000;

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    private record AttemptRecord(int failureCount, Instant lastFailureAt, Instant lockedUntil) {}

    public void recordFailure(String username) {
        attempts.compute(username, (key, existing) -> {
            Instant now = Instant.now();
            boolean currentlyLocked = existing != null && existing.lockedUntil() != null
                    && existing.lockedUntil().isAfter(now);
            if (currentlyLocked) {
                return existing;
            }
            boolean lockExpired = existing == null
                    || (existing.lockedUntil() != null && !existing.lockedUntil().isAfter(now))
                    || (existing.lockedUntil() == null && existing.lastFailureAt().isBefore(now.minus(LOCK_DURATION)));
            int newCount = lockExpired ? 1 : existing.failureCount() + 1;
            Instant lockedUntil = newCount >= MAX_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
            return new AttemptRecord(newCount, now, lockedUntil);
        });
    }

    public boolean isLocked(String username) {
        AttemptRecord record = attempts.get(username);
        return record != null && record.lockedUntil() != null && record.lockedUntil().isAfter(Instant.now());
    }

    public void reset(String username) {
        attempts.remove(username);
    }

    @Scheduled(fixedRate = HOUR)
    public void cleanUpStaleAttempts() {
        Instant now = Instant.now();
        attempts.entrySet().removeIf(entry -> {
            AttemptRecord record = entry.getValue();
            boolean lockExpired = record.lockedUntil() != null && !record.lockedUntil().isAfter(now);
            boolean neverLockedAndStale = record.lockedUntil() == null
                    && record.lastFailureAt().isBefore(now.minus(LOCK_DURATION));
            return lockExpired || neverLockedAndStale;
        });
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        recordFailure(event.getAuthentication().getName());
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            reset(event.getAuthentication().getName());
        }
    }
}
