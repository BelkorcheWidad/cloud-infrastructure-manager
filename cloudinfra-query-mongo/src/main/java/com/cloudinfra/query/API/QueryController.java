package com.cloudinfra.query.API;

import com.cloudinfra.coreapi.DTO.ServerStatusDTO;
import com.cloudinfra.coreapi.DTO.VmSummaryDTO;
import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import com.cloudinfra.query.Document.JoinTable;
import com.cloudinfra.query.Document.ServerDocument;
import com.cloudinfra.query.Repository.JointableRepository;
import com.cloudinfra.query.Repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-side API: fast queries served directly from the MongoDB read models.
 */
@RestController
@RequestMapping("query")
@RequiredArgsConstructor
public class QueryController {

    private final JointableRepository jointableRepository;
    private final ServerRepository serverRepository;

    /** Status of every data center (capacity, servers, VMs, remaining RAM). */
    @GetMapping("/datacenters")
    public List<JoinTable> getDataCenters() {
        return jointableRepository.findAll();
    }

    /** Status of a single data center. */
    @GetMapping("/datacenters/{idDataCenter}")
    public JoinTable getDataCenter(@PathVariable Long idDataCenter) {
        return jointableRepository.findById(idDataCenter)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Data center " + idDataCenter + " not found"));
    }

    /** All servers tracked in the read model. */
    @GetMapping("/servers")
    public List<ServerDocument> getServers() {
        return serverRepository.findAll();
    }

    /** A single server with its remaining RAM. */
    @GetMapping("/servers/{idServer}")
    public ServerDocument getServer(@PathVariable Long idServer) {
        return serverRepository.findById(idServer)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Server " + idServer + " not found"));
    }

    /** All servers of a given data center. */
    @GetMapping("/datacenters/{idDataCenter}/servers")
    public List<ServerDocument> getServersByDataCenter(@PathVariable Long idDataCenter) {
        return serverRepository.findAll().stream()
                .filter(s -> idDataCenter.equals(s.getIdDataCenter()))
                .toList();
    }

    // ---------- VM placement queries (Task 3) ----------

    /** Full server status including the list of hosted VM IDs. */
    @GetMapping("/servers/{id}")
    public ServerStatusDTO getServerDetails(@PathVariable Long id) {
        ServerDocument srv = serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Server " + id + " not found"));
        return new ServerStatusDTO(
                srv.getIdServer(),
                srv.getIdDataCenter(),
                srv.getRemainingRam(),
                srv.getVmIds() == null ? List.of() : srv.getVmIds());
    }

    /** The VMs hosted on a given server ("Which VMs are running on Server X?"). */
    @GetMapping("/servers/{id}/vms")
    public List<VmSummaryDTO> getServerVms(@PathVariable Long id) {
        ServerDocument srv = serverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Server " + id + " not found"));
        return (srv.getVmIds() == null ? List.<Long>of() : srv.getVmIds()).stream()
                .map(VmSummaryDTO::new)
                .toList();
    }
}