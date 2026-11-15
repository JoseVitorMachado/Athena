package br.ufla.athena.bff.livro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BffLivroApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffLivroApplication.class, args);
    }
}
