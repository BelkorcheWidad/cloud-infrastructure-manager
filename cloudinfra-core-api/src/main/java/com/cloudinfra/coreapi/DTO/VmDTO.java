package com.cloudinfra.coreapi.DTO;


import com.cloudinfra.coreapi.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VmDTO {

    private Long idVm;
    private Configuration configuration;
    private Long idServer;
}
