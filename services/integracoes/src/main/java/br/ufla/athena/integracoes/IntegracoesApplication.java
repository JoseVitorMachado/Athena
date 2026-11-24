package br.ufla.athena.integracoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IntegracoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegracoesApplication.class, args);
    }
}
