package com.cloudinfra.vm.command.API;

import com.cloudinfra.coreapi.exception.InsufficientCapacityException;
import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Translates business exceptions (thrown inside Axon aggregates) into
 * clean HTTP error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({InsufficientCapacityException.class, ResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleBusiness(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CommandExecutionException.class)
    public ResponseEntity<Map<String, String>> handleCommandExecution(CommandExecutionException ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        HttpStatus status = (cause instanceof InsufficientCapacityException
                || cause instanceof ResourceNotFoundException) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return build(status, cause.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}