package com.cloudinfra.dc.command.API;


import com.cloudinfra.coreapi.DTO.DataCenterDTO;
import com.cloudinfra.coreapi.DTO.ServerDTO;

import com.cloudinfra.coreapi.commands.DataCenter.AddDataCenterCommand;
import com.cloudinfra.coreapi.commands.DataCenter.AddServerCommand;
import com.cloudinfra.coreapi.commands.DataCenter.DecommissionServerCommand;
import com.cloudinfra.coreapi.commands.DataCenter.DeleteServerCommand;

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

    //add DataCenter
    @PostMapping("/datacenter")
    public CompletableFuture<String> AddDataCenter(@RequestBody DataCenterDTO dataCenterDTO){
        CompletableFuture<String> response= commandGateway.send(
                      new AddDataCenterCommand(dataCenterDTO.getIdDataCenter(), dataCenterDTO.getCity(),
                                               dataCenterDTO.getCapacity(), dataCenterDTO.getRegion()));

        return  response;
    }

    //add Server
    @PostMapping("/datacenter/{idDataCenter}/server")
    public CompletableFuture<String> addServer(@PathVariable Long idDataCenter, @RequestBody ServerDTO serverDTO ){
        CompletableFuture<String> response= commandGateway.send(
               new AddServerCommand(idDataCenter, serverDTO.getIdServer(), serverDTO.getConfiguration()));
        return response;
    }


    // delete Server
    @DeleteMapping("/datacenter/{idDataCenter}/{idServer}")
    public CompletableFuture<String> removeServer(@PathVariable Long idDataCenter, @PathVariable Long idServer) {
        CompletableFuture<String> response= commandGateway.send(
                        new DeleteServerCommand(idDataCenter, idServer));
        return response;
    }

    // decommission a server (lifecycle: refuses servers that still host VMs)
    @PostMapping("/datacenter/{id}/servers/{srvId}/decommission")
    public CompletableFuture<String> decommissionServer(@PathVariable Long id, @PathVariable Long srvId) {
        CompletableFuture<String> response = commandGateway.send(
                        new DecommissionServerCommand(id, srvId));
        return response;
    }
}

