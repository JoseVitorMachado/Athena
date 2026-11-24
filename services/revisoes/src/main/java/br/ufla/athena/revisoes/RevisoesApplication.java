package br.ufla.athena.revisoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RevisoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevisoesApplication.class, args);
    }
}
