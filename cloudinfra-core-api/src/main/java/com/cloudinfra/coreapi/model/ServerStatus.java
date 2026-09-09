package com.cloudinfra.coreapi.model;

/**
 * Lifecycle states of a physical server.
 */
public enum ServerStatus {
    ACTIVE,          // Server is healthy and accepting VMs
    MAINTENANCE,     // Server is under maintenance, no new VMs
    DECOMMISSIONED   // Server is retired permanently
}