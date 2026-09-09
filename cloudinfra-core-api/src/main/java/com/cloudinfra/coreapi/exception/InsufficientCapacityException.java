package com.cloudinfra.coreapi.exception;

/**
 * Thrown when an operation exceeds the available capacity
 * of a data center or a server.
 */
public class InsufficientCapacityException extends RuntimeException {

    public InsufficientCapacityException(String message) {
        super(message);
    }
}