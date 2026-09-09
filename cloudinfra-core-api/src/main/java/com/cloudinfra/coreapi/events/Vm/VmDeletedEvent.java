package com.cloudinfra.coreapi.events.Vm;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.model.VmStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VmDeletedEvent {
    private Long idUser;
    private Long idVm;
    private Long idServer;
    private Configuration configuration;
    private VmStatus status; // status at deletion time (RUNNING VMs free their RAM)
}
