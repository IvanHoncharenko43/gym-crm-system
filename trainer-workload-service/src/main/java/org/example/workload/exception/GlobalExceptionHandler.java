package org.example.workload.exception;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed for one or more fields"
        );
        problemDetail.setTitle("Bad Request");

        Map<String, List<String>> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        }
        problemDetail.setProperty("invalidFields", errors);

        logAndTag(ex, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tagWithTraceId(problemDetail));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Invalid JSON request"
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("violations", ex.getMessage());

        logAndTag(ex, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(tagWithTraceId(problemDetail));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Constraint violation"
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("violations", exception.getMessage());

        logAndTag(exception, HttpStatus.BAD_REQUEST);
        return tagWithTraceId(problemDetail);
    }

    @ExceptionHandler(WorkloadNotFoundException.class)
    public ProblemDetail handleWorkloadNotFoundException(WorkloadNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage()
        );
        problemDetail.setTitle("Workload Not Found");

        logAndTag(exception, HttpStatus.NOT_FOUND);
        return tagWithTraceId(problemDetail);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ProblemDetail handleInvalidStateTransitionException(InvalidStateTransitionException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, exception.getMessage()
        );
        problemDetail.setTitle("Invalid State Transition");

        logAndTag(exception, HttpStatus.CONFLICT);
        return tagWithTraceId(problemDetail);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ProblemDetail handleInsufficientAuthenticationException(InsufficientAuthenticationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Authentication token is missing or invalid"
        );
        problemDetail.setTitle("Authentication Failure");

        logAndTag(exception, HttpStatus.UNAUTHORIZED);
        return tagWithTraceId(problemDetail);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Authentication failed due to an invalid or missing token"
        );
        problemDetail.setTitle("Authentication Failure");

        logAndTag(exception, HttpStatus.UNAUTHORIZED);
        return tagWithTraceId(problemDetail);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "Not enough rights to access the resource"
        );
        problemDetail.setTitle("Authorization Failure");

        logAndTag(exception, HttpStatus.FORBIDDEN);
        return tagWithTraceId(problemDetail);
    }

    @ExceptionHandler(Throwable.class)
    public ProblemDetail handleThrowable(Throwable throwable) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");

        log.error("Unhandled exception resolved to status {}", HttpStatus.INTERNAL_SERVER_ERROR, throwable);
        return tagWithTraceId(problemDetail);
    }

    private void logAndTag(Exception exception, HttpStatus status) {
        if (status.is5xxServerError()) {
            log.error("{} resolved to status {}", exception.getClass().getSimpleName(), status, exception);
        } else {
            log.warn("{} resolved to status {}: {}", exception.getClass().getSimpleName(), status, exception.getMessage());
        }
    }

    private ProblemDetail tagWithTraceId(ProblemDetail problemDetail) {
        problemDetail.setProperty(TRACE_ID_MDC_KEY, MDC.get(TRACE_ID_MDC_KEY));
        return problemDetail;
    }
}
