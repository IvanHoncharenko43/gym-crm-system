package org.example.crm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.crm.exception.DownstreamClientErrorException;
import org.example.crm.exception.DownstreamRetryableClientException;
import org.example.crm.exception.DownstreamServerErrorException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class DownstreamErrorStatusHandler implements RestClient.ResponseSpec.ErrorHandler {
    private final String serviceId;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        if (status.is5xxServerError()) {
            throw new DownstreamServerErrorException(serviceId, request.getMethod(), request.getURI(), status, null);
        }
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        String detail = extractDetail(body);
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value() || status.value() == HttpStatus.LOCKED.value()) {
            throw new DownstreamRetryableClientException(serviceId, request.getMethod(), request.getURI(), status, detail);
        }
        throw new DownstreamClientErrorException(serviceId, request.getMethod(), request.getURI(), status, detail);
    }

    private String extractDetail(String body){
        String detail;
        try {
            ProblemDetail downstreamDetail = objectMapper.readValue(body, ProblemDetail.class);
            detail = downstreamDetail.getDetail();
        } catch (Exception e) {
            detail = "The downstream service rejected this request";
        }
        return detail;
    }
}
