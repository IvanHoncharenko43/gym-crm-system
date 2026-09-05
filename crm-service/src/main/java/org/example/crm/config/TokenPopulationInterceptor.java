package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenPopulationInterceptor implements ClientHttpRequestInterceptor {

    private final RequestHeaderContextResolver requestHeaderContextResolver;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        requestHeaderContextResolver.getAuthorizationHeader()
                .ifPresent(authHeader -> request.getHeaders().add(HttpHeaders.AUTHORIZATION, authHeader));
        return execution.execute(request, body);
    }
}
