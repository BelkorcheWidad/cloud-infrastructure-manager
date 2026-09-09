package com.cloudinfra.vm.command.Aggregate;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.model.VmStatus;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.axonframework.modelling.command.EntityId;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
public class VmAggregate {

    @Id
    @EntityId
    private Long idVm;
    @Embedded
    private Configuration configuration;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private UserAggregate user;
    private Long idServer;

    @Enumerated(EnumType.STRING)
    private VmStatus status;

    /** Convenience constructor: a freshly created VM is RUNNING. */
    public VmAggregate(Long idVm, Configuration configuration,
                       UserAggregate user, Long idServer) {
        this(idVm, configuration, user, idServer, VmStatus.RUNNING);
    }
}