package com.cloudinfra.coreapi.commands.DataCenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Internal command sent by {@link DecommissionServerCommandHandler} after the
 * "no running VMs" invariant has been validated. Applies the state change
 * on the DataCenter aggregate so the event is recorded in the event store.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmServerDecommissionCommand {
    @TargetAggregateIdentifier
    private Long idDataCenter;
    private Long idServer;
}