package com.cloudinfra.query;

import com.cloudinfra.coreapi.AxonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({AxonConfig.class})
public class MsQueryMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsQueryMongoApplication.class, args);
    }
}