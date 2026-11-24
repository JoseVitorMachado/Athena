package br.ufla.athena.requisitos.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/** Épico de um projeto. */
@Entity
@Table(name = "epicos")
public class Epico {
    @Id private UUID id;
    private UUID projetoId;
    private String titulo;
    private String descricao;
    @Version private long revisao;
    private Instant criadoEm;
    private Instant atualizadoEm;

    protected Epico() { }

    public static Epico criar(UUID projeto, String titulo, String descricao) {
        Epico epico = new Epico();
        epico.id = UUID.randomUUID();
        epico.projetoId = projeto;
        epico.titulo = titulo;
        epico.descricao = descricao;
        epico.criadoEm = Instant.now();
        epico.atualizadoEm = epico.criadoEm;
        return epico;
    }

    public void alterar(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.atualizadoEm = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getProjetoId() { return projetoId; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public long getRevisao() { return revisao; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
