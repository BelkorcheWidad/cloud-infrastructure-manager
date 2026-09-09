package com.cloudinfra.dc.command.Aggregate;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.commands.DataCenter.AddDataCenterCommand;
import com.cloudinfra.coreapi.commands.DataCenter.AddServerCommand;
import com.cloudinfra.coreapi.commands.DataCenter.DeleteServerCommand;
import com.cloudinfra.coreapi.events.DataCenter.DataCenterAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerAddedEvent;
import com.cloudinfra.coreapi.events.DataCenter.ServerDeletedEvent;
import com.cloudinfra.coreapi.exception.InsufficientCapacityException;
import com.cloudinfra.coreapi.exception.ResourceNotFoundException;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataCenterAggregateTest {

    private FixtureConfiguration<DataCenterAggregate> fixture;

    private final Configuration config16 = new Configuration(16, 16, 256);
    private final Configuration config8 = new Configuration(8, 8, 128);

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(DataCenterAggregate.class);
    }

    @Test
    void addDataCenter_publishesEvent() {
        fixture.givenNoPriorActivity()
               .when(new AddDataCenterCommand(1L, "Paris", 64, "eu-west-1"))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new DataCenterAddedEvent(1L, "Paris", 64, "eu-west-1"));
    }

    @Test
    void addDataCenter_rejectsNonPositiveCapacity() {
        fixture.givenNoPriorActivity()
               .when(new AddDataCenterCommand(1L, "Paris", 0, "eu-west-1"))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void addDataCenter_rejectsMissingRegion() {
        fixture.givenNoPriorActivity()
               .when(new AddDataCenterCommand(1L, "Paris", 64, null))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void addDataCenter_rejectsBlankRegion() {
        fixture.givenNoPriorActivity()
               .when(new AddDataCenterCommand(1L, "Paris", 64, "   "))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void addServer_publishesEvent() {
        fixture.givenCommands(new AddDataCenterCommand(1L, "Paris", 64, "eu-west-1"))
               .when(new AddServerCommand(1L, 10L, config16))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new ServerAddedEvent(1L, 10L, config16));
    }

    @Test
    void addServer_rejectsWhenDataCenterCapacityExceeded() {
        // data center has 8 GB, server needs 16 GB -> rejected
        fixture.givenCommands(new AddDataCenterCommand(1L, "Paris", 8, "eu-west-1"))
               .when(new AddServerCommand(1L, 10L, config16))
               .expectException(InsufficientCapacityException.class);
    }

    @Test
    void addServer_allowsExactCapacity() {
        // data center has 16 GB, server needs 16 GB -> exactly at the limit, allowed
        fixture.givenCommands(new AddDataCenterCommand(1L, "Paris", 16, "eu-west-1"))
               .when(new AddServerCommand(1L, 10L, config16))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new ServerAddedEvent(1L, 10L, config16));
    }

    @Test
    void addServer_rejectsInvalidConfiguration() {
        fixture.givenCommands(new AddDataCenterCommand(1L, "Paris", 64, "eu-west-1"))
               .when(new AddServerCommand(1L, 10L, new Configuration(0, 8, 128)))
               .expectException(IllegalArgumentException.class);
    }

    @Test
    void deleteServer_publishesEvent() {
        fixture.givenCommands(
                       new AddDataCenterCommand(1L, "Paris", 64, "eu-west-1"),
                       new AddServerCommand(1L, 10L, config16))
               .when(new DeleteServerCommand(1L, 10L))
               .expectSuccessfulHandlerExecution()
               .expectEvents(new ServerDeletedEvent(1L, 10L));
    }

    @Test
    void deleteServer_rejectsUnknownServer() {
        fixture.givenCommands(new AddDataCenterCommand(1L, "Paris", 64, "eu-west-1"))
               .when(new DeleteServerCommand(1L, 999L))
               .expectException(ResourceNotFoundException.class);
    }
}