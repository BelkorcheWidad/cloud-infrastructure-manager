package com.cloudinfra.vm.command.API;

import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import com.cloudinfra.vm.command.Aggregate.UserAggregate;
import com.cloudinfra.vm.command.Aggregate.VmAggregate;
import com.cloudinfra.vm.command.Repository.UserRepo;
import com.cloudinfra.vm.command.Repository.VmRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-side convenience API for the write service's persisted state (MySQL).
 */
@RestController
@RequestMapping("query")
@RequiredArgsConstructor
public class QueryController {

    private final UserRepo userRepo;
    private final VmRepo vmRepo;

    /** All users. */
    @GetMapping("/users")
    public List<UserAggregate> getUsers() {
        return userRepo.findAll();
    }

    /** A single user. */
    @GetMapping("/users/{idUser}")
    public UserAggregate getUser(@PathVariable Long idUser) {
        return userRepo.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + idUser + " not found"));
    }

    /** All VMs. */
    @GetMapping("/vms")
    public List<VmAggregate> getVms() {
        return vmRepo.findAll();
    }

    /** A single VM. */
    @GetMapping("/vms/{idVm}")
    public VmAggregate getVm(@PathVariable Long idVm) {
        return vmRepo.findById(idVm)
                .orElseThrow(() -> new ResourceNotFoundException("VM " + idVm + " not found"));
    }
}