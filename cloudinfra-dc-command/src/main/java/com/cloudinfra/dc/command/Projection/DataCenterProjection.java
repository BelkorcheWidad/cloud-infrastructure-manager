package com.cloudinfra.dc.command.Projection;

import com.cloudinfra.coreapi.events.DataCenter.DataCenterAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDeletedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDecommissionedEvent;
import com.cloudinfra.coreapi.events.Vm.VmAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmDeletedEvent;
import com.cloudinfra.coreapi.model.ServerStatus;

import com.cloudinfra.dc.command.Aggregate.DataCenterAggregate;
import com.cloudinfra.dc.command.Aggregate.ServerAggregate;
import com.cloudinfra.dc.command.Repository.DataCenterRepo;
import com.cloudinfra.dc.command.Repository.ServerRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

/**
 * Maintains the write-side persisted state (MySQL) from the event stream.
 * Also tracks VM placement per server (from the VM event stream) so the
 * decommissioning invariant can be validated on the command side.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataCenterProjection {

    public final DataCenterRepo dataCenterRepo;
    public final ServerRepo serverRepo;

    @EventHandler
    public void  AddDataCenter(DataCenterAddedEvent event)
    {
        DataCenterAggregate dataCenterAggregate = new DataCenterAggregate(
                event.getIdDataCenter(), event.getCity(), event.getCapacity(), event.getRegion(), null
        );
        dataCenterRepo.save(dataCenterAggregate);
    }

    @EventHandler
    public void AddServer (ServerAddedEvent event)  {
        DataCenterAggregate DC=  dataCenterRepo.findById(event.getIdDataCenter()).get();
        serverRepo.save(new ServerAggregate(event.getIdServer(), event.getConfiguration(),DC));
    }




    @EventHandler
    public void removeServer(ServerDeletedEvent event) {
        serverRepo.deleteById(event.getIdServer());
    }

    // ---------- VM placement tracking (from the VM event stream) ----------

    @EventHandler
    public void onVmAdded(VmAddedEvent event) {
        serverRepo.findById(event.getIdServer()).ifPresentOrElse(srv -> {
            srv.hostVm(event.getIdVm());
            serverRepo.save(srv);
            log.info("Write model: VM {} placed on server {}", event.getIdVm(), event.getIdServer());
        }, () -> log.warn("Write model: server {} not found, ignoring VM {} placement",
                event.getIdServer(), event.getIdVm()));
    }

    @EventHandler
    public void onVmDeleted(VmDeletedEvent event) {
        if (event.getIdServer() == null) return;
        serverRepo.findById(event.getIdServer()).ifPresentOrElse(srv -> {
            srv.releaseVm(event.getIdVm());
            serverRepo.save(srv);
            log.info("Write model: VM {} released from server {}", event.getIdVm(), event.getIdServer());
        }, () -> log.warn("Write model: server {} not found, ignoring VM {} deletion",
                event.getIdServer(), event.getIdVm()));
    }

    // ---------- Server lifecycle ----------

    @EventHandler
    public void onServerDecommissioned(ServerDecommissionedEvent event) {
        serverRepo.findById(event.getIdServer()).ifPresent(srv -> {
            srv.setStatus(ServerStatus.DECOMMISSIONED);
            serverRepo.save(srv);
            log.info("Write model: server {} decommissioned", event.getIdServer());
        });
    }
}
