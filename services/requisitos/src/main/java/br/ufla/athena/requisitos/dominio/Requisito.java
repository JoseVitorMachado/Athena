package br.ufla.athena.requisitos.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "requisitos")
public class Requisito {

    @Id
    private UUID id;

    private UUID projetoId;

    private String codigo;

    private UUID documentoId;

    private UUID epicoId;

    private UUID responsavelId;

    private String tipo;

    private String titulo;

    @Enumerated(EnumType.STRING)
    private SituacaoRequisito situacao;

    /** Revisão técnica de concorrência (ETag). Não é a versão de conteúdo. */
    @Version
    private long revisao;

    private Instant criadoEm;

    private Instant atualizadoEm;

    @OneToMany(mappedBy = "requisito", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<Versao> versoes = new ArrayList<>();

    protected Requisito() {
        // exigido pelo JPA
    }

    /** Cria o requisito ATIVO já com a primeira candidata em RASCUNHO. */
    public static Requisito novo(UUID projetoId, int numeroCodigo, UUID documentoId, UUID epicoId,
                                 UUID responsavelId, String tipo, String titulo, String conteudo,
                                 List<String> criteriosAceite) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Requisito requisito = new Requisito();
        requisito.id = UUID.randomUUID();
        requisito.projetoId = projetoId;
        requisito.codigo = "REQ-" + numeroCodigo;
        requisito.documentoId = documentoId;
        requisito.epicoId = epicoId;
        requisito.responsavelId = responsavelId;
        requisito.tipo = tipo;
        requisito.titulo = titulo;
        requisito.situacao = SituacaoRequisito.ATIVO;
        requisito.criadoEm = agora;
        requisito.atualizadoEm = agora;
        requisito.versoes.add(new Versao(requisito, 1, titulo, conteudo, criteriosAceite, sha256(conteudo), agora));
        return requisito;
    }

    /** A candidata é a única versão ainda não publicada, quando existe. */
    public Optional<Versao> candidata() {
        return versoes.stream()
                .filter(versao -> versao.getEstadoVersao() != EstadoVersao.PUBLICADA)
                .findFirst();
    }

    /** A versão publicada corrente é a publicada de maior número, quando existe. */
    public Optional<Versao> versaoPublicada() {
        return versoes.stream()
                .filter(versao -> versao.getEstadoVersao() == EstadoVersao.PUBLICADA)
                .max((a, b) -> Integer.compare(a.getNumero(), b.getNumero()));
    }

    public Versao criarCandidataDaPublicada(Versao publicada) {
        int numero = versoes.stream().mapToInt(Versao::getNumero).max().orElse(0) + 1;
        Versao candidata = new Versao(this, numero, publicada.getTitulo(), publicada.getConteudo(),
                publicada.getCriteriosAceite(), publicada.getHashConteudo(), Instant.now());
        versoes.add(candidata);
        return candidata;
    }

    public void editarCandidata(Versao candidata, String titulo, String conteudo, List<String> criterios) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        candidata.editar(titulo, conteudo, criterios, sha256(conteudo), agora);
        this.titulo = titulo;
        this.atualizadoEm = agora;
    }

    private static String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public UUID getDocumentoId() {
        return documentoId;
    }

    public UUID getEpicoId() {
        return epicoId;
    }

    public UUID getResponsavelId() {
        return responsavelId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public SituacaoRequisito getSituacao() {
        return situacao;
    }

    public long getRevisao() {
        return revisao;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
