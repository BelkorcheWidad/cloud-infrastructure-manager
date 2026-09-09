package com.cloudinfra.coreapi.events.Vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VmStartedEvent {
    private Long idUser;
    private Long idVm;
    private Long idServer;
    private int ram; // RAM consumed again by starting the VM
}