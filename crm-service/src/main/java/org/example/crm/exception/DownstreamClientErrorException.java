package org.example.crm.exception;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;

import java.net.URI;

public class DownstreamClientErrorException extends DownstreamServiceException {
    public DownstreamClientErrorException(String serviceName, HttpMethod httpMethod, URI requestUri,
                                          HttpStatusCode statusCode, String responseBody) {
        super(serviceName, httpMethod, requestUri, statusCode, responseBody);
    }
}
