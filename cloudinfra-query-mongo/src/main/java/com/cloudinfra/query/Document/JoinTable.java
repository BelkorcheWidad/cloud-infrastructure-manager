package com.cloudinfra.query.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Read model: live status of a data center.
 * Maintained exclusively from events (no access to command-side databases).
 */
@Document("datacenter_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinTable {
    @Id
    private Long idDataCenter;

    private String city;          // city of the data center
    private String region;         // e.g., "eu-west-1", "us-east-1"
    private Integer capacity;      // total RAM (GB) of the data center
    private Integer nbServers;    // number of servers provisioned
    private Integer nbVms;         // number of VMs deployed
    private Integer nbRunningVms;  // number of RUNNING VMs
    private Integer nbStoppedVms;  // number of STOPPED VMs
    private Integer remainingRam; // RAM (GB) still available
}
