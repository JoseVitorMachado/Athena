package br.ufla.athena.projetosacesso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProjetosAcessoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetosAcessoApplication.class, args);
    }
}
