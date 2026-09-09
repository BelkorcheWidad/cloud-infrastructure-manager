package com.cloudinfra.coreapi.commands.DataCenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddDataCenterCommand {
    @TargetAggregateIdentifier
    private Long idDataCenter;
    private String city;
    private int capacity;
    private String region; // e.g., "eu-west-1", "us-east-1", "me-south-1"
}
