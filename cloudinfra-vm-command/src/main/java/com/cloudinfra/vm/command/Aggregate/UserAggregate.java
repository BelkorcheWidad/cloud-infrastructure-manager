package com.cloudinfra.vm.command.Aggregate;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.commands.Vm.AddUserCommand;
import com.cloudinfra.coreapi.commands.Vm.AddVmCommand;
import com.cloudinfra.coreapi.commands.Vm.DeleteVmCommand;
import com.cloudinfra.coreapi.commands.Vm.StartVmCommand;
import com.cloudinfra.coreapi.commands.Vm.StopVmCommand;
import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import com.cloudinfra.coreapi.events.Vm.UserAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmDeletedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStartedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStoppedEvent;
import com.cloudinfra.coreapi.model.VmStatus;
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
public class UserAggregate {

    @AggregateIdentifier
    @Id
    private Long idUser;
    private String name;
    private String email;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user")
    @AggregateMember
    private List<VmAggregate> vms;

    @CommandHandler
    public UserAggregate(AddUserCommand cmd) {
        if (cmd.getName() == null || cmd.getName().isBlank()) {
            throw new IllegalArgumentException("User name must not be empty");
        }
        AggregateLifecycle.apply(
                new UserAddedEvent(cmd.getIdUser(), cmd.getName(), cmd.getEmail()));
    }

    @EventSourcingHandler
    public void on(UserAddedEvent event) {
        this.idUser = event.getIdUser();
        this.name = event.getName();
        this.email = event.getEmail();
        this.vms = new ArrayList<>();
    }



    @CommandHandler
    public void addVm(AddVmCommand cmd) {
        Configuration config = cmd.getConfiguration();
        if (config == null || config.getRam() <= 0 || config.getCpu() <= 0 || config.getDisk() <= 0) {
            throw new IllegalArgumentException("VM configuration must have positive cpu, ram and disk values");
        }
        if (cmd.getIdServer() == null) {
            throw new IllegalArgumentException("A VM must be attached to a server");
        }
        AggregateLifecycle.apply(
                new VmAddedEvent(cmd.getIdUser(), cmd.getIdVm(), cmd.getConfiguration(), cmd.getIdServer()));
    }

    @EventSourcingHandler
    public void on(VmAddedEvent event){
        this.vms.add(new VmAggregate(event.getIdVm(),
                       event.getConfiguration(),
                                     this,
                                     event.getIdServer()));
    }


    // ---------- VM lifecycle: stop / start ----------

    @CommandHandler
    public void stopVm(StopVmCommand cmd) {
        VmAggregate vm = findVm(cmd.getIdVm());
        if (vm.getStatus() != VmStatus.RUNNING) {
            throw new IllegalStateException("VM must be RUNNING to be stopped (current: " + vm.getStatus() + ")");
        }
        AggregateLifecycle.apply(new VmStoppedEvent(
                cmd.getIdUser(), cmd.getIdVm(), vm.getIdServer(), vm.getConfiguration().getRam()));
    }

    @EventSourcingHandler
    public void on(VmStoppedEvent event) {
        findVm(event.getIdVm()).setStatus(VmStatus.STOPPED);
    }

    @CommandHandler
    public void startVm(StartVmCommand cmd) {
        VmAggregate vm = findVm(cmd.getIdVm());
        if (vm.getStatus() != VmStatus.STOPPED) {
            throw new IllegalStateException("VM must be STOPPED to be started (current: " + vm.getStatus() + ")");
        }
        // Optional capacity check: refuse to start if the server has no RAM left.
        // The authoritative check lives in the StatusProjection read model; here we
        // always allow and let the query side reflect the re-allocated RAM.
        AggregateLifecycle.apply(new VmStartedEvent(
                cmd.getIdUser(), cmd.getIdVm(), vm.getIdServer(), vm.getConfiguration().getRam()));
    }

    @EventSourcingHandler
    public void on(VmStartedEvent event) {
        findVm(event.getIdVm()).setStatus(VmStatus.RUNNING);
    }

    /** Locate a VM of this user or throw ResourceNotFoundException. */
    private VmAggregate findVm(Long idVm) {
        VmAggregate vm = vms == null ? null : vms.stream()
                .filter(v -> v.getIdVm().equals(idVm))
                .findFirst()
                .orElse(null);
        if (vm == null) {
            throw new ResourceNotFoundException("VM " + idVm + " not found for user " + idUser);
        }
        return vm;
    }

    @CommandHandler
    public void deleteVm(DeleteVmCommand cmd) {
        VmAggregate vm = findVm(cmd.getIdVm());
        AggregateLifecycle.apply(new VmDeletedEvent(
                cmd.getIdUser(), cmd.getIdVm(), vm.getIdServer(), vm.getConfiguration(), vm.getStatus()));
    }

    @EventSourcingHandler
    public void on(VmDeletedEvent event){
        this.vms.removeIf(vm -> vm.getIdVm().equals(event.getIdVm()));
    }

}
