package org.example.macrocycle.exception;

public class PlanGenerationFailedException extends RuntimeException {
    public PlanGenerationFailedException(String message) {
        super(message);
    }

    public PlanGenerationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
