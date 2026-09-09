package com.cloudinfra.coreapi.events.Vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAddedEvent {
    private Long idUser;
    private String name;
    private String email;
}
