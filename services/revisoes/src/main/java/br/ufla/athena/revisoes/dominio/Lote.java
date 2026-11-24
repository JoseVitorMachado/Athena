package br.ufla.athena.revisoes.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    private UUID id;

    private UUID projetoId;

    private UUID requisitoId;

    private UUID submissaoId;

    private UUID versaoId;

    private String hashConteudo;

    @Enumerated(EnumType.STRING)
    private EstadoLote estado;

    /** Revisão técnica de concorrência (ETag usado no If-Match da decisão). */
    @Version
    private long revisao;

    private Instant criadoEm;

    private Instant atualizadoEm;

    @OneToMany(mappedBy = "lote")
    @OrderBy("atualizadoEm ASC, id ASC")
    @BatchSize(size = 50)
    private List<Pendencia> pendencias = new ArrayList<>();

    protected Lote() {
        // exigido pelo JPA
    }

    public static Lote novo(UUID projetoId, UUID requisitoId, UUID submissaoId, UUID versaoId,
                            String hashConteudo) {
        Lote lote = new Lote();
        lote.id = UUID.randomUUID();
        lote.projetoId = projetoId;
        lote.requisitoId = requisitoId;
        lote.submissaoId = submissaoId;
        lote.versaoId = versaoId;
        lote.hashConteudo = hashConteudo;
        lote.estado = EstadoLote.PREPARADO;
        lote.criadoEm = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        lote.atualizadoEm = lote.criadoEm;
        return lote;
    }

    /** A decisão só vale para a tentativa corrente: mesma submissão, versão e conteúdo. */
    public boolean correspondeA(UUID submissaoId, UUID versaoId, String hashConteudo) {
        return this.submissaoId.equals(submissaoId)
                && this.versaoId.equals(versaoId)
                && this.hashConteudo.equals(hashConteudo);
    }

    public boolean temPendenciaBloqueanteAberta() {
        return pendencias.stream().anyMatch(pendencia -> pendencia.isBloqueante() && !pendencia.isResolvida());
    }

    /** A primeira decisão final válida encerra o lote. */
    public void encerrar() {
        this.estado = EstadoLote.ENCERRADO;
        this.atualizadoEm = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public UUID getRequisitoId() {
        return requisitoId;
    }

    public UUID getSubmissaoId() {
        return submissaoId;
    }

    public UUID getVersaoId() {
        return versaoId;
    }

    public String getHashConteudo() {
        return hashConteudo;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public long getRevisao() {
        return revisao;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public List<Pendencia> getPendencias() {
        return pendencias;
    }
}
