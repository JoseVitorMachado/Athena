package br.ufla.athena.revisoes.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pendencias")
public class Pendencia {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    private String descricao;

    private boolean bloqueante;

    private boolean resolvida;

    private UUID responsavelId;

    private Instant atualizadoEm;

    protected Pendencia() {
        // exigido pelo JPA
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isBloqueante() {
        return bloqueante;
    }

    public boolean isResolvida() {
        return resolvida;
    }

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
