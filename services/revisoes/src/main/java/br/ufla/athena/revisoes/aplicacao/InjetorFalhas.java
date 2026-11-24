package br.ufla.athena.revisoes.aplicacao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InjetorFalhas {
    public enum Modo {
        NENHUMA, RECUSAR, SILENCIAR
    }

    private volatile Modo abrirLote;

    public InjetorFalhas(@Value("${athena.falhas.abrir-lote:NENHUMA}") Modo abrirLote) {
        this.abrirLote = abrirLote;
    }

    public Modo getAbrirLote() {
        return abrirLote;
    }

    public void definir(Modo modo) {
        this.abrirLote = java.util.Objects.requireNonNull(modo);
    }
}
