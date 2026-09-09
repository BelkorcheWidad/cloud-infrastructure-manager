package com.cloudinfra.query.Document;

import com.cloudinfra.coreapi.Configuration;
import com.cloudinfra.coreapi.model.ServerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model: which data center a server belongs to, how much RAM
 * is still free on it and which VMs are hosted on it.
 * Built purely from events.
 */
@Document("server")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDocument {
    @Id
    private Long idServer;

    private Long idDataCenter;
    private Configuration configuration; // full server capacity
    private Integer remainingRam;       // RAM (GB) not yet consumed by RUNNING VMs
    private ServerStatus status;         // lifecycle state of the server
    private List<Long> vmIds = new ArrayList<>(); // IDs of the VMs hosted on this server
}