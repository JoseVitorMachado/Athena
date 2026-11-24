package br.ufla.athena.requisitos.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "versoes")
public class Versao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisito_id")
    private Requisito requisito;

    private int numero;

    @Enumerated(EnumType.STRING)
    private EstadoVersao estadoVersao;

    private String titulo;

    private String conteudo;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> criteriosAceite;

    private String hashConteudo;

    private Instant atualizadoEm;

    @Version
    private long revisao;

    protected Versao() {
        // exigido pelo JPA
    }

    Versao(Requisito requisito, int numero, String titulo, String conteudo,
           List<String> criteriosAceite, String hashConteudo, Instant agora) {
        this.id = UUID.randomUUID();
        this.requisito = requisito;
        this.numero = numero;
        this.estadoVersao = EstadoVersao.RASCUNHO;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.criteriosAceite = List.copyOf(criteriosAceite);
        this.hashConteudo = hashConteudo;
        this.atualizadoEm = agora;
    }

    public void editar(String titulo, String conteudo, List<String> criteriosAceite, String hash, Instant agora) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.criteriosAceite = List.copyOf(criteriosAceite);
        this.hashConteudo = hash;
        this.estadoVersao = EstadoVersao.RASCUNHO;
        this.atualizadoEm = agora;
    }

    public void alterarEstado(EstadoVersao estado) {
        this.estadoVersao = estado;
        this.atualizadoEm = Instant.now();
    }

    public long getRevisao() {
        return revisao;
    }

    public UUID getId() {
        return id;
    }

    public Requisito getRequisito() {
        return requisito;
    }

    public int getNumero() {
        return numero;
    }

    public EstadoVersao getEstadoVersao() {
        return estadoVersao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public List<String> getCriteriosAceite() {
        return criteriosAceite;
    }

    public String getHashConteudo() {
        return hashConteudo;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
