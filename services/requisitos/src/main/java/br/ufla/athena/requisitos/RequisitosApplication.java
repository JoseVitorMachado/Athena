package br.ufla.athena.requisitos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RequisitosApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequisitosApplication.class, args);
    }
}
