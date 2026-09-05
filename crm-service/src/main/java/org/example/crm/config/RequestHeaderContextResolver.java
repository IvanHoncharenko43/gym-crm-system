package org.example.crm.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;

@Component
public class RequestHeaderContextResolver {

    public Optional<String> getAuthorizationHeader() {
        return Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getRequest)
                .map(req -> req.getHeader(HttpHeaders.AUTHORIZATION));
    }

    public Optional<String> getTraceIdHeader() {
        return Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getRequest)
                .map(req -> (String) req.getAttribute(TRACE_ID_KEY));
    }
}
