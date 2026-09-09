package com.cloudinfra.coreapi.events.DataCenter;

import com.cloudinfra.coreapi.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerAddedEvent {
    private Long idDataCenter;
    private Long idServer;
    private Configuration configuration;
}
