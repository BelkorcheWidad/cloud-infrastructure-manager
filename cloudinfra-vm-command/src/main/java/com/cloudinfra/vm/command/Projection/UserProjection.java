package com.cloudinfra.vm.command.Projection;

import com.cloudinfra.coreapi.events.Vm.UserAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmDeletedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStartedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStoppedEvent;
import com.cloudinfra.coreapi.model.VmStatus;

import com.cloudinfra.vm.command.Aggregate.UserAggregate;
import com.cloudinfra.vm.command.Aggregate.VmAggregate;
import com.cloudinfra.vm.command.Repository.UserRepo;
import com.cloudinfra.vm.command.Repository.VmRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProjection {

    public final UserRepo userRepo;
    public final VmRepo vmRepo;

    @EventHandler
    public void  AddUser(UserAddedEvent event)
    {
        UserAggregate userAggregate=
                 new UserAggregate(event.getIdUser(),event.getName(),event.getEmail(), null);
    userRepo.save(userAggregate);
    }

    @EventHandler
    public void AddVm (VmAddedEvent event)  {
        UserAggregate usr= userRepo.findById(event.getIdUser()).get();
         vmRepo.save(new VmAggregate(event.getIdVm(), event.getConfiguration(),usr,event.getIdServer()));
    }



    @EventHandler
    public void removeVm(VmDeletedEvent event) {
        vmRepo.deleteById(event.getIdVm());
    }

    // ---------- VM lifecycle ----------

    @EventHandler
    public void on(VmStoppedEvent event) {
        vmRepo.findById(event.getIdVm()).ifPresent(vm -> {
            vm.setStatus(VmStatus.STOPPED);
            vmRepo.save(vm);
            log.info("Write model: VM {} stopped", event.getIdVm());
        });
    }

    @EventHandler
    public void on(VmStartedEvent event) {
        vmRepo.findById(event.getIdVm()).ifPresent(vm -> {
            vm.setStatus(VmStatus.RUNNING);
            vmRepo.save(vm);
            log.info("Write model: VM {} started", event.getIdVm());
        });
    }
}
