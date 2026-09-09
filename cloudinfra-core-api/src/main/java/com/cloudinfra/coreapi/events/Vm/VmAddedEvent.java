package com.cloudinfra.coreapi.events.Vm;

import com.cloudinfra.coreapi.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VmAddedEvent {
    private Long idUser;
    private Long idVm;
    private Configuration configuration;
    private Long idServer;
}
