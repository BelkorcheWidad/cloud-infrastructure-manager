package com.cloudinfra.dc.command.API;

import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import com.cloudinfra.dc.command.Aggregate.DataCenterAggregate;
import com.cloudinfra.dc.command.Aggregate.ServerAggregate;
import com.cloudinfra.dc.command.Repository.DataCenterRepo;
import com.cloudinfra.dc.command.Repository.ServerRepo;
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

    private final DataCenterRepo dataCenterRepo;
    private final ServerRepo serverRepo;

    /** All data centers. */
    @GetMapping("/datacenters")
    public List<DataCenterAggregate> getDataCenters() {
        return dataCenterRepo.findAll();
    }

    /** A single data center. */
    @GetMapping("/datacenters/{idDataCenter}")
    public DataCenterAggregate getDataCenter(@PathVariable Long idDataCenter) {
        return dataCenterRepo.findById(idDataCenter)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Data center " + idDataCenter + " not found"));
    }

    /** All servers. */
    @GetMapping("/servers")
    public List<ServerAggregate> getServers() {
        return serverRepo.findAll();
    }

    /** A single server. */
    @GetMapping("/servers/{idServer}")
    public ServerAggregate getServer(@PathVariable Long idServer) {
        return serverRepo.findById(idServer)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Server " + idServer + " not found"));
    }
}