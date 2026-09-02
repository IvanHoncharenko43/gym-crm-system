package org.example.crm.exception;

import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.net.URI;

@Getter
public class DownstreamServiceException extends RuntimeException {
    private final String serviceName;
    private final HttpMethod httpMethod;
    private final URI requestUri;
    private final HttpStatusCode statusCode;
    private final String detail;

    protected DownstreamServiceException(String serviceName, HttpMethod httpMethod, URI requestUri,
                                         HttpStatusCode statusCode, String detail) {
        super("Downstream call to %s failed: %s %s returned %s"
                .formatted(serviceName, httpMethod, requestUri, statusCode));
        this.serviceName = serviceName;
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.statusCode = statusCode;
        this.detail = detail;
    }
}
