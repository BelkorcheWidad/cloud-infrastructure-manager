package com.cloudinfra.dc.command.Aggregate;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.commands.DataCenter.ConfirmServerDecommissionCommand;
import com.cloudinfra.coreapi.commands.DataCenter.AddDataCenterCommand;
import com.cloudinfra.coreapi.commands.DataCenter.AddServerCommand;
import com.cloudinfra.coreapi.commands.DataCenter.DeleteServerCommand;
import com.cloudinfra.coreapi.exception.InsufficientCapacityException;
import com.cloudinfra.coreapi.exception.ResourceNotFoundException;

import com.cloudinfra.coreapi.events.DataCenter.DataCenterAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDeletedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDecommissionedEvent;
import com.cloudinfra.coreapi.model.ServerStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.List;

@Aggregate
@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class DataCenterAggregate {

    @AggregateIdentifier
    @Id
    private Long idDataCenter;
    private String city;
    private int capacity;
    private String region; // e.g., "eu-west-1", "us-east-1", "me-south-1"

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "dataCenter")
    @AggregateMember
    private List<ServerAggregate> servers;

    @CommandHandler
    public DataCenterAggregate(AddDataCenterCommand cmd) {
        if (cmd.getCapacity() <= 0) {
            throw new IllegalArgumentException("Data center capacity must be positive");
        }
        if (cmd.getRegion() == null || cmd.getRegion().isBlank()) {
            throw new IllegalArgumentException("Region is required");
        }
        AggregateLifecycle.apply(
              new DataCenterAddedEvent(cmd.getIdDataCenter(),cmd.getCity(),cmd.getCapacity(),cmd.getRegion()));
    }

    @EventSourcingHandler
    public void on(DataCenterAddedEvent event) {
        this.idDataCenter = event.getIdDataCenter();
        this.city = event.getCity();
        this.capacity = event.getCapacity();
        this.region = event.getRegion();
        this.servers = new ArrayList<>();
    }



    @CommandHandler
    public void addServer(AddServerCommand cmd) {
        Configuration config = cmd.getConfiguration();
        if (config == null || config.getRam() <= 0 || config.getCpu() <= 0 || config.getDisk() <= 0) {
            throw new IllegalArgumentException("Server configuration must have positive cpu, ram and disk values");
        }
        int usedRam = servers == null ? 0 : servers.stream()
                .mapToInt(s -> s.getConfiguration().getRam())
                .sum();
        if (usedRam + config.getRam() > capacity) {
            throw new InsufficientCapacityException(String.format(
                    "Data center %d has insufficient RAM: requested %d GB, only %d GB available",
                    idDataCenter, config.getRam(), capacity - usedRam));
        }
        AggregateLifecycle.apply(
               new ServerAddedEvent(cmd.getIdDataCenter(),cmd.getIdServer(),cmd.getConfiguration()));
    }

    @EventSourcingHandler
    public void on(ServerAddedEvent event){
        this.servers.add(new ServerAggregate(
                event.getIdServer(),event.getConfiguration(),this
        ));

    }


    @CommandHandler
    public void deleteServer(DeleteServerCommand cmd) {
        boolean exists = servers != null && servers.stream()
                .anyMatch(s -> s.getIdServer().equals(cmd.getIdServer()));
        if (!exists) {
            throw new ResourceNotFoundException(
                    "Server " + cmd.getIdServer() + " not found in data center " + cmd.getIdDataCenter());
        }
        AggregateLifecycle.apply(
               new ServerDeletedEvent(cmd.getIdDataCenter(),cmd.getIdServer()));
    }

    @EventSourcingHandler
    public void on(ServerDeletedEvent event){
        this.servers.removeIf(sv -> sv.getIdServer().equals(event.getIdServer()));
    }


    // ---------- Server decommissioning (lifecycle) ----------

    /**
     * Internal command: records the decommission on the aggregate after the
     * "no deployed VMs" invariant has been validated by the external
     * {@code DecommissionServerCommandHandler} (VM placement lives in the VM
     * event stream, which is not part of this aggregate's history).
     */
    @CommandHandler
    public void confirmDecommission(ConfirmServerDecommissionCommand cmd) {
        ServerAggregate srv = servers == null ? null : servers.stream()
                .filter(s -> s.getIdServer().equals(cmd.getIdServer()))
                .findFirst()
                .orElse(null);
        if (srv == null) {
            throw new ResourceNotFoundException(
                    "Server " + cmd.getIdServer() + " not found in data center " + cmd.getIdDataCenter());
        }
        if (srv.getStatus() == ServerStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Server " + cmd.getIdServer() + " is already decommissioned");
        }
        AggregateLifecycle.apply(
               new ServerDecommissionedEvent(cmd.getIdDataCenter(), cmd.getIdServer()));
    }

    @EventSourcingHandler
    public void on(ServerDecommissionedEvent event) {
        this.servers.stream()
                .filter(s -> s.getIdServer().equals(event.getIdServer()))
                .findFirst()
                .ifPresent(srv -> srv.setStatus(ServerStatus.DECOMMISSIONED));
    }

}
