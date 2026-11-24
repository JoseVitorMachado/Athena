package br.ufla.athena.rastreabilidade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RastreabilidadeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RastreabilidadeApplication.class, args);
    }
}
