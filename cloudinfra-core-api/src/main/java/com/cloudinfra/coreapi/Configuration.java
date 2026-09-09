package com.cloudinfra.coreapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data @AllArgsConstructor @NoArgsConstructor
public class Configuration implements Serializable {
    public int cpu;
    public int ram;
    public int disk;

}
