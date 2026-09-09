package com.cloudinfra.coreapi.model;

/**
 * Lifecycle states of a VM (like AWS EC2 instance states).
 */
public enum VmStatus {
    CREATING,    // VM is being provisioned
    RUNNING,     // VM is active and consuming RAM
    STOPPED,     // VM is paused (still occupies disk, frees RAM)
    TERMINATED   // VM is destroyed permanently
}