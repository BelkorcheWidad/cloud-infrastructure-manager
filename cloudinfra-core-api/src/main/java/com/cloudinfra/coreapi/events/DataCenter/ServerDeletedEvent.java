package com.cloudinfra.coreapi.events.DataCenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDeletedEvent {
    private Long idDataCenter;
    private Long idServer;

}
