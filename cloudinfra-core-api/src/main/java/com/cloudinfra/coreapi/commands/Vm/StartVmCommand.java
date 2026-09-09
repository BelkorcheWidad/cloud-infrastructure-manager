package com.cloudinfra.coreapi.commands.Vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartVmCommand {
    @TargetAggregateIdentifier
    private Long idUser;
    private Long idVm;
}