package org.example.crm.exception;

import lombok.Getter;

@Getter
public class DownstreamUnavailableException extends RuntimeException {
    private final String serviceId;

    public DownstreamUnavailableException(String serviceId, Throwable throwable) {
        super(String.format("Downstream service %s is unavailable", serviceId), throwable);
        this.serviceId = serviceId;
    }
}
