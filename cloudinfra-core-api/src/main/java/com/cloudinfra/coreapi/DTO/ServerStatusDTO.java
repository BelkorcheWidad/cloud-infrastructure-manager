package com.cloudinfra.coreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-model DTO: full status of a server, including the list of VM IDs hosted on it.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerStatusDTO {
    private Long idServer;
    private Long idDataCenter;
    private Integer remainingRam;
    private List<Long> vmIds = new ArrayList<>();
}