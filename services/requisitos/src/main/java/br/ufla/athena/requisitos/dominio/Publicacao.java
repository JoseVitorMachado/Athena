package br.ufla.athena.requisitos.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Immutable;

/** Registro imutável de uma publicação (livro §4.3). */
@Entity
@Immutable
@Table(name = "publicacoes")
public class Publicacao {

    @Id
    private UUID id;

    private UUID requisitoId;

    private UUID versaoId;

    private UUID submissaoId;

    private UUID retratoId;

    private UUID autor;

    private Instant data;

    protected Publicacao() {
        // exigido pelo JPA
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequisitoId() {
        return requisitoId;
    }

    public UUID getVersaoId() {
        return versaoId;
    }

    public UUID getSubmissaoId() {
        return submissaoId;
    }

    public UUID getRetratoId() {
        return retratoId;
    }

    public UUID getAutor() {
        return autor;
    }

    public Instant getData() {
        return data;
    }
}
