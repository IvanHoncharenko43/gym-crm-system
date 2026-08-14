package org.example.core.service;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import org.example.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
        for(FieldError error : ex.getBindingResult().getFieldErrors()){
            errors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        }
        problemDetail.setProperty("invalidFields", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Constraint violation"
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setProperty("violations", exception.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Invalid username or password"
        );
        problemDetail.setTitle("Authentication Failure");
        return problemDetail;
    }

    @ExceptionHandler(DisabledException.class)
    public ProblemDetail handleDisabledException(DisabledException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "The account is inactive"
        );
        problemDetail.setTitle("Authentication Failure");
        return problemDetail;
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLockedException(LockedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Account is temporarily locked due to too many failed login attempts"
        );
        problemDetail.setTitle("Authentication Failure");
        return problemDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Authentication failed during accessing the resource"
        );
        problemDetail.setTitle("Authentication Failure");
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "Authorization failed during accessing the resource"
        );
        problemDetail.setTitle("Authorization Failure");
        return problemDetail;
    }

    @ExceptionHandler(AccessForbiddenException.class)
    public ProblemDetail handleAccessForbiddenException(AccessForbiddenException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, exception.getMessage()
        );
        problemDetail.setTitle("Authorization Failure");
        return problemDetail;
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ProblemDetail handleAuthenticationFailedException(AuthenticationFailedException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, exception.getMessage()
        );
        problemDetail.setTitle("Authentication Failure");
        return problemDetail;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage()
        );
        problemDetail.setTitle("Entity Not Found");
        return problemDetail;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage()
        );
        problemDetail.setTitle("Invalid Password");
        return problemDetail;
    }

    @ExceptionHandler(InvalidRequestDataException.class)
    public ProblemDetail handleInvalidRequestDataException(InvalidRequestDataException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage()
        );
        problemDetail.setTitle("Invalid Request Data");
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()
        );
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }
}
