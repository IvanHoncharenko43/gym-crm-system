package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_HEADER;

@Component
@RequiredArgsConstructor
public class TraceIdPopulationInterceptor implements ClientHttpRequestInterceptor {
    private final RequestHeaderContextResolver requestHeaderContextResolver;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        requestHeaderContextResolver.getTraceIdHeader()
                .ifPresent(traceId -> request.getHeaders().add(TRACE_ID_HEADER, traceId));
        return execution.execute(request, body);
    }
}
