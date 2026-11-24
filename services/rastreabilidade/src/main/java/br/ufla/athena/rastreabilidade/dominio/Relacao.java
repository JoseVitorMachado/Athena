package br.ufla.athena.rastreabilidade.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "relacoes")
public class Relacao {

    @Id
    private UUID id;

    private UUID projetoId;

    private UUID origemId;

    private UUID destinoId;

    @Enumerated(EnumType.STRING)
    private TipoRelacao tipo;

    private String justificativa;

    /** Revisão técnica de concorrência (ETag). */
    @Version
    private long revisao;

    private Instant criadoEm;

    private Instant atualizadoEm;

    protected Relacao() {
        // exigido pelo JPA
    }

    public static Relacao nova(UUID projetoId, UUID origemId, UUID destinoId, TipoRelacao tipo,
                               String justificativa) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Relacao relacao = new Relacao();
        relacao.id = UUID.randomUUID();
        relacao.projetoId = projetoId;
        relacao.origemId = origemId;
        relacao.destinoId = destinoId;
        relacao.tipo = tipo;
        relacao.justificativa = justificativa;
        relacao.criadoEm = agora;
        relacao.atualizadoEm = agora;
        return relacao;
    }

    public void alterar(UUID origemId, UUID destinoId, TipoRelacao tipo, String justificativa) {
        this.origemId = origemId;
        this.destinoId = destinoId;
        this.tipo = tipo;
        this.justificativa = justificativa;
        this.atualizadoEm = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public UUID getOrigemId() {
        return origemId;
    }

    public UUID getDestinoId() {
        return destinoId;
    }

    public TipoRelacao getTipo() {
        return tipo;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public long getRevisao() {
        return revisao;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
