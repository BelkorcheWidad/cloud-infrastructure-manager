package com.cloudinfra.coreapi.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor
@NoArgsConstructor
public class DataCenterDTO {
    private Long idDataCenter;
    private String city;
    private int capacity;
    private String region; // e.g., "eu-west-1", "us-east-1", "me-south-1"
}
