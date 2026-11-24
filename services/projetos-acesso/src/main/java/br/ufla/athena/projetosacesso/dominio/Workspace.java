package br.ufla.athena.projetosacesso.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspaces")
public class Workspace {
    @Id private UUID id;
    private String nome;
    private UUID ownerId;
    private Instant excluidoEm;
    private UUID exclusaoId;
    @Version private long revisao;
    private Instant criadoEm;
    private Instant atualizadoEm;

    protected Workspace() { }

    public static Workspace novo(String nome, UUID ownerId) {
        Workspace workspace = new Workspace();
        workspace.id = UUID.randomUUID();
        workspace.nome = nome;
        workspace.ownerId = ownerId;
        workspace.criadoEm = Instant.now();
        workspace.atualizadoEm = workspace.criadoEm;
        return workspace;
    }

    public void renomear(String nome) { this.nome = nome; this.atualizadoEm = Instant.now(); }
    public void excluir(UUID exclusaoId, Instant instante) {
        this.exclusaoId = exclusaoId;
        this.excluidoEm = instante;
        this.atualizadoEm = instante;
    }
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public UUID getOwnerId() { return ownerId; }
    public Instant getExcluidoEm() { return excluidoEm; }
    public long getRevisao() { return revisao; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
