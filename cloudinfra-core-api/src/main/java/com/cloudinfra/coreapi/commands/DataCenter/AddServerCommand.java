package com.cloudinfra.coreapi.commands.DataCenter;

import com.cloudinfra.coreapi.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddServerCommand {

    @TargetAggregateIdentifier
    private Long idDataCenter;
    private Long idServer;
    private Configuration configuration;
}
