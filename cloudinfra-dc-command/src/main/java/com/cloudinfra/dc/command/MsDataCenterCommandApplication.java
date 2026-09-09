package com.cloudinfra.dc.command;

import com.cloudinfra.coreapi.AxonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AxonConfig.class})

public class MsDataCenterCommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsDataCenterCommandApplication.class, args);
    }

}
