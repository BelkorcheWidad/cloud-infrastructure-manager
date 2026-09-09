package com.cloudinfra.coreapi.commands.Vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserCommand {
    @TargetAggregateIdentifier
    private Long idUser;
    private String name;
    private String email;
}
