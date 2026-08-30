package org.example.crm.config;

import lombok.RequiredArgsConstructor;
import org.example.crm.exception.DownstreamClientErrorException;
import org.example.crm.exception.DownstreamRetryableClientException;
import org.example.crm.exception.DownstreamServerErrorException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class DownstreamErrorStatusHandler implements RestClient.ResponseSpec.ErrorHandler {
    private final String serviceId;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        HttpStatusCode status = response.getStatusCode();
        if (status.is5xxServerError()) {
            throw new DownstreamServerErrorException(serviceId, request.getMethod(), request.getURI(), status, body);
        }
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value() || status.value() == HttpStatus.LOCKED.value()) {
            throw new DownstreamRetryableClientException(serviceId, request.getMethod(), request.getURI(), status, body);
        }
        throw new DownstreamClientErrorException(serviceId, request.getMethod(), request.getURI(), status, body);
    }
}
