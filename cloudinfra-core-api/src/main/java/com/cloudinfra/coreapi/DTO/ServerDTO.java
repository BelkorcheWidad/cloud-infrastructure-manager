package com.cloudinfra.coreapi.DTO;


import com.cloudinfra.coreapi.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDTO {
    private Long idServer;
    private Configuration configuration;
}
