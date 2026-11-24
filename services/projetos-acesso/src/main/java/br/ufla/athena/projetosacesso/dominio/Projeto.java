package br.ufla.athena.projetosacesso.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "projetos")
public class Projeto {

    @Id
    private UUID id;

    private UUID workspaceId;

    private String nome;

    private String descricao;

    private boolean arquivado;

    private UUID ownerId;

    /** Revisão técnica de concorrência (ETag). */
    @Version
    private long revisao;

    private Instant excluidoEm;

    private Instant criadoEm;

    private Instant atualizadoEm;

    protected Projeto() {
        // exigido pelo JPA
    }

    /** Quem cria o projeto torna-se o seu único Owner (livro §7.4). */
    public static Projeto novo(UUID workspaceId, String nome, String descricao, UUID criador) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Projeto projeto = new Projeto();
        projeto.id = UUID.randomUUID();
        projeto.workspaceId = workspaceId;
        projeto.nome = nome;
        projeto.descricao = descricao;
        projeto.arquivado = false;
        projeto.ownerId = criador;
        projeto.criadoEm = agora;
        projeto.atualizadoEm = agora;
        return projeto;
    }

    public void alterar(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.atualizadoEm = Instant.now();
    }

    /** Transfere a propriedade e atualiza a revisão do projeto. */
    public void transferirPropriedade(UUID novoOwner) { this.ownerId = novoOwner; this.atualizadoEm = Instant.now(); }

    public void arquivar() { this.arquivado = true; this.atualizadoEm = Instant.now(); }
    public void restaurar() { this.arquivado = false; this.atualizadoEm = Instant.now(); }
    public void excluirLogicamente(Instant instante) { this.excluidoEm = instante; this.atualizadoEm = instante; }
    public Instant getExcluidoEm() { return excluidoEm; }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isArquivado() {
        return arquivado;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public long getRevisao() {
        return revisao;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
