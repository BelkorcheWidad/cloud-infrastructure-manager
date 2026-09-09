package com.cloudinfra.coreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-model DTO: a lightweight summary of a VM hosted on a server.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VmSummaryDTO {
    private Long idVm;
}