package com.cloudinfra.query.Projection;

import com.cloudinfra.coreapi.events.DataCenter.DataCenterAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDeletedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDecommissionedEvent;
import com.cloudinfra.coreapi.events.Vm.VmAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmDeletedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStartedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStoppedEvent;
import com.cloudinfra.coreapi.model.ServerStatus;
import com.cloudinfra.coreapi.model.VmStatus;
import com.cloudinfra.query.Document.JoinTable;
import com.cloudinfra.query.Document.ServerDocument;
import com.cloudinfra.query.Repository.JointableRepository;
import com.cloudinfra.query.Repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Builds and maintains the read models (MongoDB) from the event stream.
 * This service never calls the command services: everything is derived
 * from events, keeping the microservices fully decoupled.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatusProjection {

    private final JointableRepository jointableRepository;
    private final ServerRepository serverRepository;

    // ---------- Data center events ----------

    @EventHandler
    public void onDataCenterAdded(DataCenterAddedEvent event) {
        JoinTable status = new JoinTable(
                event.getIdDataCenter(),
                event.getCity(),
                event.getRegion(),
                event.getCapacity(),
                0,   // nbServers
                0,   // nbVms
                0,   // nbRunningVms
                0,   // nbStoppedVms
                event.getCapacity() // all RAM available initially
        );
        jointableRepository.save(status);
        log.info("Read model: data center {} created in region {} with capacity {} GB",
                event.getIdDataCenter(), event.getRegion(), event.getCapacity());
    }

    // ---------- Server events ----------

    @EventHandler
    public void onServerAdded(ServerAddedEvent event) {
        // Track the server in the read model (server -> data center mapping)
        ServerDocument server = new ServerDocument(
                event.getIdServer(),
                event.getIdDataCenter(),
                event.getConfiguration(),
                event.getConfiguration().getRam(), // all server RAM available initially
                ServerStatus.ACTIVE,
                new ArrayList<>()
        );
        serverRepository.save(server);

        // Update the data center status
        JoinTable status = jointableRepository.findById(event.getIdDataCenter()).orElse(null);
        if (status != null) {
            status.setNbServers(status.getNbServers() + 1);
            status.setRemainingRam(status.getRemainingRam() - event.getConfiguration().getRam());
            jointableRepository.save(status);
        }
        log.info("Read model: server {} added to data center {} ({} GB RAM)",
                event.getIdServer(), event.getIdDataCenter(), event.getConfiguration().getRam());
    }

    @EventHandler
    public void onServerDeleted(ServerDeletedEvent event) {
        ServerDocument server = serverRepository.findById(event.getIdServer()).orElse(null);
        if (server == null) {
            log.warn("Read model: server {} not found, ignoring deletion", event.getIdServer());
            return;
        }
        // Free the server RAM back to the data center
        JoinTable status = jointableRepository.findById(event.getIdDataCenter()).orElse(null);
        if (status != null) {
            status.setNbServers(Math.max(0, status.getNbServers() - 1));
            status.setRemainingRam(status.getRemainingRam() + server.getConfiguration().getRam());
            jointableRepository.save(status);
        }
        serverRepository.delete(server);
        log.info("Read model: server {} removed from data center {}",
                event.getIdServer(), event.getIdDataCenter());
    }

    @EventHandler
    public void onServerDecommissioned(ServerDecommissionedEvent event) {
        ServerDocument server = serverRepository.findById(event.getIdServer()).orElse(null);
        if (server == null) {
            log.warn("Read model: server {} not found, ignoring decommission", event.getIdServer());
            return;
        }
        server.setStatus(ServerStatus.DECOMMISSIONED);
        serverRepository.save(server);
        log.info("Read model: server {} decommissioned", event.getIdServer());
    }

    // ---------- VM events ----------

    @EventHandler
    public void onVmAdded(VmAddedEvent event) {
        ServerDocument server = serverRepository.findById(event.getIdServer()).orElse(null);
        if (server == null) {
            log.warn("Read model: server {} not found, ignoring VM {} addition",
                    event.getIdServer(), event.getIdVm());
            return;
        }
        // Track VM placement on the server
        if (server.getVmIds() == null) {
            server.setVmIds(new ArrayList<>());
        }
        server.getVmIds().add(event.getIdVm());

        // Consume RAM on the server (a new VM starts RUNNING)
        server.setRemainingRam(server.getRemainingRam() - event.getConfiguration().getRam());
        serverRepository.save(server);

        // Update the data center status
        JoinTable status = jointableRepository.findById(server.getIdDataCenter()).orElse(null);
        if (status != null) {
            status.setNbVms(status.getNbVms() + 1);
            status.setNbRunningVms(safe(status.getNbRunningVms()) + 1);
            status.setRemainingRam(status.getRemainingRam() - event.getConfiguration().getRam());
            jointableRepository.save(status);
        }
        log.info("Read model: VM {} deployed on server {} (-{} GB RAM)",
                event.getIdVm(), event.getIdServer(), event.getConfiguration().getRam());
    }

    @EventHandler
    public void onVmDeleted(VmDeletedEvent event) {
        if (event.getIdServer() == null) {
            log.warn("Read model: VM {} deleted without server reference, ignoring", event.getIdVm());
            return;
        }
        ServerDocument server = serverRepository.findById(event.getIdServer()).orElse(null);
        if (server == null) {
            log.warn("Read model: server {} not found, ignoring VM {} deletion",
                    event.getIdServer(), event.getIdVm());
            return;
        }
        // Remove VM placement from the server
        if (server.getVmIds() != null) {
            server.getVmIds().remove(event.getIdVm());
        }

        // Only RUNNING VMs were consuming RAM; STOPPED VMs had already freed it
        boolean wasRunning = event.getStatus() == null || event.getStatus() == VmStatus.RUNNING;
        if (wasRunning) {
            server.setRemainingRam(server.getRemainingRam() + event.getConfiguration().getRam());
        }
        serverRepository.save(server);

        // Update the data center status
        JoinTable status = jointableRepository.findById(server.getIdDataCenter()).orElse(null);
        if (status != null) {
            status.setNbVms(Math.max(0, status.getNbVms() - 1));
            if (wasRunning) {
                status.setNbRunningVms(Math.max(0, safe(status.getNbRunningVms()) - 1));
                status.setRemainingRam(status.getRemainingRam() + event.getConfiguration().getRam());
            } else {
                status.setNbStoppedVms(Math.max(0, safe(status.getNbStoppedVms()) - 1));
            }
            jointableRepository.save(status);
        }
        log.info("Read model: VM {} removed from server {} (+{} GB RAM)",
                event.getIdVm(), event.getIdServer(), wasRunning ? event.getConfiguration().getRam() : 0);
    }

    // ---------- VM lifecycle events ----------

    @EventHandler
    public void onVmStopped(VmStoppedEvent event) {
        // Free RAM on the server (STOPPED VMs don't consume RAM)
        serverRepository.findById(event.getIdServer()).ifPresent(server -> {
            server.setRemainingRam(server.getRemainingRam() + event.getRam());
            serverRepository.save(server);

            JoinTable status = jointableRepository.findById(server.getIdDataCenter()).orElse(null);
            if (status != null) {
                status.setNbRunningVms(Math.max(0, safe(status.getNbRunningVms()) - 1));
                status.setNbStoppedVms(safe(status.getNbStoppedVms()) + 1);
                status.setRemainingRam(status.getRemainingRam() + event.getRam());
                jointableRepository.save(status);
            }
        });
        log.info("Read model: VM {} stopped (+{} GB RAM freed)", event.getIdVm(), event.getRam());
    }

    @EventHandler
    public void onVmStarted(VmStartedEvent event) {
        // Re-allocate RAM on the server
        serverRepository.findById(event.getIdServer()).ifPresent(server -> {
            server.setRemainingRam(server.getRemainingRam() - event.getRam());
            serverRepository.save(server);

            JoinTable status = jointableRepository.findById(server.getIdDataCenter()).orElse(null);
            if (status != null) {
                status.setNbRunningVms(safe(status.getNbRunningVms()) + 1);
                status.setNbStoppedVms(Math.max(0, safe(status.getNbStoppedVms()) - 1));
                status.setRemainingRam(status.getRemainingRam() - event.getRam());
                jointableRepository.save(status);
            }
        });
        log.info("Read model: VM {} started (-{} GB RAM allocated)", event.getIdVm(), event.getRam());
    }

    private static int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
