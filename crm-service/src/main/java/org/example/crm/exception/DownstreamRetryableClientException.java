package org.example.crm.exception;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.net.URI;

public class DownstreamRetryableClientException extends DownstreamClientErrorException {
    public DownstreamRetryableClientException(String serviceName, HttpMethod httpMethod,
                                              URI requestUri, HttpStatusCode statusCode, String detail) {
        super(serviceName, httpMethod, requestUri, statusCode, detail);
    }
}
