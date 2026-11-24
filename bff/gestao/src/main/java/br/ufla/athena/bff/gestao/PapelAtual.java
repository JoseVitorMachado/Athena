package br.ufla.athena.bff.gestao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Papel provisório; será substituído pelo papel do token na etapa de autenticação. */
@Component
public class PapelAtual {

    public enum Papel {
        LEITOR, REPORTER, EDITOR, APROVADOR, MANTENEDOR, OWNER;

        public boolean permite(Papel minimo) {
            return ordinal() >= minimo.ordinal();
        }
    }

    private final Papel papel;

    public PapelAtual(@Value("${athena.papel-demo:OWNER}") Papel papel) {
        this.papel = papel;
    }

    public Papel obter() {
        return papel;
    }
}
