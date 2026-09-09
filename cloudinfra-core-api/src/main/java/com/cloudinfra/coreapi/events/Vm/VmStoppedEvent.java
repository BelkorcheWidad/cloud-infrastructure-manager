package com.cloudinfra.coreapi.events.Vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VmStoppedEvent {
    private Long idUser;
    private Long idVm;
    private Long idServer;
    private int ram; // RAM freed by stopping the VM
}