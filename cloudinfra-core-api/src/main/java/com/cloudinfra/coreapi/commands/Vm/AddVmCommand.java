package com.cloudinfra.coreapi.commands.Vm;

import com.cloudinfra.coreapi.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddVmCommand {
    @TargetAggregateIdentifier
    private Long idUser;
    private Long idVm;
    private Configuration configuration;
    private Long idServer;
}
