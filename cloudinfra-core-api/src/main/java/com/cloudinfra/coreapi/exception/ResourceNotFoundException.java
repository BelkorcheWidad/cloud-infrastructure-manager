package com.cloudinfra.coreapi.exception;

/**
 * Thrown when a referenced resource (data center, server, user, VM)
 * does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}