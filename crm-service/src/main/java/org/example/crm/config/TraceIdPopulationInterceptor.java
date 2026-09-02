package org.example.crm.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Optional;

import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_HEADER;
import static org.example.crm.core.filter.TraceIdFilter.TRACE_ID_KEY;

@Component
public class TraceIdPopulationInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .map(ServletRequestAttributes::getRequest)
                .map(req -> (String) req.getAttribute(TRACE_ID_KEY))
                .ifPresent(traceId -> request.getHeaders().add(TRACE_ID_HEADER, traceId));
        return execution.execute(request, body);
    }
}
