package org.example.workload.config;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.example.workload.service.JwtService;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.example.workload.filter.JwtAuthenticationFilter.BEARER_PREFIX;
import static org.example.workload.filter.TraceIdFilter.TRACE_ID_KEY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class HeaderExtractionConsumerInterceptor implements RecordInterceptor<Object, Object> {
    private final JwtService jwtService;

    @Override
    public @Nullable ConsumerRecord<Object, Object> intercept(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer) {
        Header traceIdHeader = record.headers().lastHeader(TRACE_ID_KEY);
        if (traceIdHeader != null) {
            MDC.put(TRACE_ID_KEY, new String(traceIdHeader.value(), StandardCharsets.UTF_8));
        } else {
            MDC.put(TRACE_ID_KEY, UUID.randomUUID().toString());
        }
        String token = Optional.ofNullable(record.headers().lastHeader(AUTHORIZATION))
                .map(header -> new String(header.value(), StandardCharsets.UTF_8))
                .filter(tokenString -> tokenString.startsWith(BEARER_PREFIX))
                .map(tokenString -> tokenString.substring(BEARER_PREFIX.length()))
                .orElseThrow(() ->
                        new InsufficientAuthenticationException("Missing or damaged Authorization header on Kafka record"));
        try {
            if (!jwtService.isTokenExpired(token)) {
                String username = jwtService.extractUsername(token);
                List<SimpleGrantedAuthority> authorities = jwtService.extractAuthorities(token);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, authorities));
            }
        } catch (JwtException ex) {
            throw new InsufficientAuthenticationException("Invalid or expired token on Kafka record", ex);
        }
        return record;
    }

    @Override
    public void afterRecord(ConsumerRecord<Object, Object> record, Consumer<Object, Object> consumer){
        MDC.remove(TRACE_ID_KEY);
        SecurityContextHolder.clearContext();
    }
}
