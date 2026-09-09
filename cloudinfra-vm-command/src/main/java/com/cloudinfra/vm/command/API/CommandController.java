package com.cloudinfra.vm.command.API;


import com.cloudinfra.coreapi.DTO.UserDTO;
import com.cloudinfra.coreapi.DTO.VmDTO;
import com.cloudinfra.coreapi.commands.Vm.AddUserCommand;
import com.cloudinfra.coreapi.commands.Vm.AddVmCommand;
import com.cloudinfra.coreapi.commands.Vm.DeleteVmCommand;
import com.cloudinfra.coreapi.commands.Vm.StartVmCommand;
import com.cloudinfra.coreapi.commands.Vm.StopVmCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("command")
public class CommandController {

    private final CommandGateway commandGateway;

    public CommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    //add user
    @PostMapping("/user")
    public CompletableFuture<String> AddUser(@RequestBody UserDTO userDTO){
        CompletableFuture<String> response= commandGateway.send(
                      new AddUserCommand(userDTO.getIdUser(), userDTO.getName(), userDTO.getEmail()));

        return  response;
    }

    //add vm
    @PostMapping("user/{userid}/vm")
    public CompletableFuture<String> addVm(@PathVariable Long userid, @RequestBody VmDTO vmDTO ){
        CompletableFuture<String> response= commandGateway.send(
                new AddVmCommand(userid, vmDTO.getIdVm(), vmDTO.getConfiguration(), vmDTO.getIdServer()));
        return response;
    }


    // delete VM
    @DeleteMapping("user/{userid}/{idvm}")
    public CompletableFuture<String> removeVm(@PathVariable Long userid, @PathVariable Long idvm) {
        CompletableFuture<String> response= commandGateway.send(
                        new DeleteVmCommand(userid, idvm));
        return response;
    }

    // stop a RUNNING VM (frees its RAM on the server)
    @PostMapping("user/{id}/vms/{vmId}/stop")
    public CompletableFuture<String> stopVm(@PathVariable Long id, @PathVariable Long vmId) {
        CompletableFuture<String> response = commandGateway.send(
                        new StopVmCommand(id, vmId));
        return response;
    }

    // start a STOPPED VM (re-allocates its RAM on the server)
    @PostMapping("user/{id}/vms/{vmId}/start")
    public CompletableFuture<String> startVm(@PathVariable Long id, @PathVariable Long vmId) {
        CompletableFuture<String> response = commandGateway.send(
                        new StartVmCommand(id, vmId));
        return response;
    }
}

