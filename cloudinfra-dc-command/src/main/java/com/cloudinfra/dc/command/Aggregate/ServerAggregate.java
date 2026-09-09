package com.cloudinfra.dc.command.Aggregate;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.model.ServerStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.modelling.command.EntityId;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class ServerAggregate {
    @Id
    @EntityId
    private Long idServer;
    @Embedded
    private Configuration configuration;

    @Enumerated(EnumType.STRING)
    private ServerStatus status;

    /**
     * IDs of the VMs hosted on this server, tracked from the VM event stream
     * (see DataCenterProjection). Persisted so the decommissioning invariant
     * ("no VMs left on the server") can be validated on the write side.
     */
    @ElementCollection
    @CollectionTable(name = "server_hosted_vms", joinColumns = @JoinColumn(name = "id_server"))
    private Set<Long> hostedVmIds = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private DataCenterAggregate dataCenter;

    public ServerAggregate(Long idServer, Configuration configuration, DataCenterAggregate dataCenter) {
        this.idServer = idServer;
        this.configuration = configuration;
        this.dataCenter = dataCenter;
        this.status = ServerStatus.ACTIVE;
        this.hostedVmIds = new HashSet<>();
    }

    /** True when at least one VM is deployed on this server. */
    public boolean hasRunningVms() {
        return hostedVmIds != null && !hostedVmIds.isEmpty();
    }

    public void hostVm(Long idVm) {
        if (hostedVmIds == null) hostedVmIds = new HashSet<>();
        hostedVmIds.add(idVm);
    }

    public void releaseVm(Long idVm) {
        if (hostedVmIds != null) hostedVmIds.remove(idVm);
    }

    /** Number of VMs currently hosted (any state). */
    public int nbHostedVms() {
        return hostedVmIds == null ? 0 : hostedVmIds.size();
    }

    public boolean isAcceptingVms() {
        return status == ServerStatus.ACTIVE;
    }
}
