package br.ufla.athena.bff.gestao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BffGestaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffGestaoApplication.class, args);
    }
}
