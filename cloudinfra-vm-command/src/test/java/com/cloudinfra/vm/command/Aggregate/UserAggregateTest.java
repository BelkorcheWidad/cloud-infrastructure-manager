package com.cloudinfra.vm.command.Aggregate;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.commands.Vm.AddUserCommand;
import com.cloudinfra.coreapi.commands.Vm.AddVmCommand;
import com.cloudinfra.coreapi.commands.Vm.DeleteVmCommand;
import com.cloudinfra.coreapi.commands.Vm.StartVmCommand;
import com.cloudinfra.coreapi.commands.Vm.StopVmCommand;
import com.cloudinfra.coreapi.events.Vm.UserAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmAddedEvent;
import com.cloudinfra.coreapi.events.Vm.VmDeletedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStartedEvent;
import com.cloudinfra.coreapi.events.Vm.VmStoppedEvent;
import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import com.cloudinfra.coreapi.model.VmStatus;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserAggregateTest {

    private FixtureConfiguration<UserAggregate> fixture;

    private final Configuration vmConfig = new Configuration(4, 8, 64);

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(UserAggregate.class);
    }

    @Test
    void addUser_publishesEvent() {
        fixture.givenNoPriorActivity()
               .when(new AddUserCommand(100L, "Alice", "alice@example.com"))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new UserAddedEvent(100L, "Alice", "alice@example.com"));
    }

    @Test
    void addUser_rejectsBlankName() {
        fixture.givenNoPriorActivity()
               .when(new AddUserCommand(100L, "  ", "alice@example.com"))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void addVm_publishesEvent() {
        fixture.givenCommands(new AddUserCommand(100L, "Alice", "alice@example.com"))
               .when(new AddVmCommand(100L, 1000L, vmConfig, 10L))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new VmAddedEvent(100L, 1000L, vmConfig, 10L));
    }

    @Test
    void addVm_rejectsMissingServer() {
        fixture.givenCommands(new AddUserCommand(100L, "Alice", "alice@example.com"))
               .when(new AddVmCommand(100L, 1000L, vmConfig, null))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void addVm_rejectsInvalidConfiguration() {
        fixture.givenCommands(new AddUserCommand(100L, "Alice", "alice@example.com"))
               .when(new AddVmCommand(100L, 1000L, new Configuration(4, -1, 64), 10L))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void deleteVm_publishesEventWithServerAndConfig() {
        fixture.givenCommands(
                       new AddUserCommand(100L, "Alice", "alice@example.com"),
                       new AddVmCommand(100L, 1000L, vmConfig, 10L))
               .when(new DeleteVmCommand(100L, 1000L))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new VmDeletedEvent(100L, 1000L, 10L, vmConfig, VmStatus.RUNNING));
    }

    // ---------- VM lifecycle: stop / start ----------

    @Test
    void stopVm_publishesEventWithFreedRam() {
        fixture.givenCommands(
                       new AddUserCommand(100L, "Alice", "alice@example.com"),
                       new AddVmCommand(100L, 1000L, vmConfig, 10L))
               .when(new StopVmCommand(100L, 1000L))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new VmStoppedEvent(100L, 1000L, 10L, vmConfig.getRam()));
    }

    @Test
    void stopVm_rejectsAlreadyStoppedVm() {
        fixture.givenCommands(
                       new AddUserCommand(100L, "Alice", "alice@example.com"),
                       new AddVmCommand(100L, 1000L, vmConfig, 10L),
                       new StopVmCommand(100L, 1000L))
               .when(new StopVmCommand(100L, 1000L))
               .expectException(IllegalStateException.class);
    }

    @Test
    void startVm_publishesEventWithAllocatedRam() {
        fixture.givenCommands(
                       new AddUserCommand(100L, "Alice", "alice@example.com"),
                       new AddVmCommand(100L, 1000L, vmConfig, 10L),
                       new StopVmCommand(100L, 1000L))
               .when(new StartVmCommand(100L, 1000L))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new VmStartedEvent(100L, 1000L, 10L, vmConfig.getRam()));
    }

    @Test
    void startVm_rejectsAlreadyRunningVm() {
        fixture.givenCommands(
                       new AddUserCommand(100L, "Alice", "alice@example.com"),
                       new AddVmCommand(100L, 1000L, vmConfig, 10L))
               .when(new StartVmCommand(100L, 1000L))
               .expectException(IllegalStateException.class);
    }

    @Test
    void stopVm_rejectsUnknownVm() {
        fixture.givenCommands(new AddUserCommand(100L, "Alice", "alice@example.com"))
               .when(new StopVmCommand(100L, 9999L))
               .expectException(ResourceNotFoundException.class);
    }

    @Test
    void deleteVm_rejectsUnknownVm() {
        fixture.givenCommands(new AddUserCommand(100L, "Alice", "alice@example.com"))
               .when(new DeleteVmCommand(100L, 9999L))
               .expectException(ResourceNotFoundException.class);
    }
}