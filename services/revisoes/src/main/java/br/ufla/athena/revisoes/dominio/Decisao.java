package br.ufla.athena.revisoes.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "decisoes")
public class Decisao {

    @Id
    private UUID id;

    private UUID loteId;

    @Enumerated(EnumType.STRING)
    private TipoDecisao decisao;

    private String justificativa;

    private UUID submissaoId;

    private UUID versaoId;

    private String hashConteudo;

    private UUID autor;

    private Instant data;

    @Enumerated(EnumType.STRING)
    private AplicacaoEmRequisitos aplicacaoEmRequisitos;

    protected Decisao() {
        // exigido pelo JPA
    }

    public static Decisao registrar(Lote lote, TipoDecisao decisao, String justificativa, UUID autor) {
        Decisao nova = new Decisao();
        nova.id = UUID.randomUUID();
        nova.loteId = lote.getId();
        nova.decisao = decisao;
        nova.justificativa = justificativa;
        nova.submissaoId = lote.getSubmissaoId();
        nova.versaoId = lote.getVersaoId();
        nova.hashConteudo = lote.getHashConteudo();
        nova.autor = autor;
        nova.data = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        // Só vira APLICADA quando Requisitos confirmar a aplicação (Parte 4).
        nova.aplicacaoEmRequisitos = AplicacaoEmRequisitos.PENDENTE;
        return nova;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLoteId() {
        return loteId;
    }

    public TipoDecisao getDecisao() {
        return decisao;
    }

    public String getJustificativa() {
        return justificativa;
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

    public UUID getAutor() {
        return autor;
    }

    public Instant getData() {
        return data;
    }

    public AplicacaoEmRequisitos getAplicacaoEmRequisitos() {
        return aplicacaoEmRequisitos;
    }
}
