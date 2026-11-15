package br.ufla.athena.integracoes.dominio;

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
@Table(name = "conexoes")
public class Conexao {

    @Id
    private UUID id;

    private UUID projetoId;

    @Enumerated(EnumType.STRING)
    private Provedor provedor;

    private String nome;

    private String urlBase;

    private String projetoExternoId;

    /** Revisão técnica de concorrência (ETag). */
    @Version
    private long revisao;

    private Instant criadoEm;

    private Instant atualizadoEm;

    protected Conexao() {
        // exigido pelo JPA
    }

    public static Conexao nova(UUID projetoId, Provedor provedor, String nome, String urlBase,
                               String projetoExternoId) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Conexao conexao = new Conexao();
        conexao.id = UUID.randomUUID();
        conexao.projetoId = projetoId;
        conexao.provedor = provedor;
        conexao.nome = nome;
        conexao.urlBase = urlBase;
        conexao.projetoExternoId = projetoExternoId;
        conexao.criadoEm = agora;
        conexao.atualizadoEm = agora;
        return conexao;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public Provedor getProvedor() {
        return provedor;
    }

    public String getNome() {
        return nome;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public String getProjetoExternoId() {
        return projetoExternoId;
    }

    public long getRevisao() {
        return revisao;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
