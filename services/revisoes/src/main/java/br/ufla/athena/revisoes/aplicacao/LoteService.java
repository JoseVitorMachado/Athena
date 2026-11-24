package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.api.DecisaoEntrada;
import br.ufla.athena.revisoes.api.DecisaoResposta;
import br.ufla.athena.revisoes.api.LoteResposta;
import br.ufla.athena.revisoes.api.Problema;
import br.ufla.athena.revisoes.dominio.Decisao;
import br.ufla.athena.revisoes.dominio.DecisaoRepository;
import br.ufla.athena.revisoes.dominio.EstadoLote;
import br.ufla.athena.revisoes.dominio.Lote;
import br.ufla.athena.revisoes.dominio.LoteRepository;
import br.ufla.athena.revisoes.dominio.TipoDecisao;
import br.ufla.athena.revisoes.mensageria.EmissorMensagens;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteService {

    private final LoteRepository lotes;
    private final DecisaoRepository decisoes;
    private final EmissorMensagens emissor;
    private final UsuarioAtual usuario;
    private final JdbcTemplate banco;
    private final ObjectMapper json;

    public LoteService(LoteRepository lotes, DecisaoRepository decisoes, EmissorMensagens emissor,
                       UsuarioAtual usuario, JdbcTemplate banco, ObjectMapper json) {
        this.lotes = lotes;
        this.decisoes = decisoes;
        this.emissor = emissor;
        this.usuario = usuario;
        this.banco = banco;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public Page<LoteResposta> listar(UUID projetoId, UUID requisitoId, EstadoLote estado, Pageable paginacao) {
        Specification<Lote> filtro = (lote, consulta, cb) -> cb.equal(lote.get("projetoId"), projetoId);
        if (requisitoId != null) {
            filtro = filtro.and((lote, consulta, cb) -> cb.equal(lote.get("requisitoId"), requisitoId));
        }
        if (estado != null) {
            filtro = filtro.and((lote, consulta, cb) -> cb.equal(lote.get("estado"), estado));
        }
        return lotes.findAll(filtro, paginacao).map(this::representar);
    }

    @Transactional(readOnly = true)
    public Optional<LoteComRevisao> buscar(UUID projetoId, UUID loteId) {
        return lotes.findByIdAndProjetoId(loteId, projetoId)
                .map(lote -> new LoteComRevisao(representar(lote), lote.getRevisao()));
    }

    @Transactional(readOnly = true)
    public Optional<DecisaoResposta> decisaoDoLote(UUID projetoId, UUID loteId) {
        Lote lote = lotes.findByIdAndProjetoId(loteId, projetoId).orElseThrow(LoteService::naoEncontrado);
        return decisoes.findByLoteId(lote.getId()).map(DecisaoResposta::de);
    }

    /**
     * Registra a decisão final do lote (livro §6.9).
     *
     * A decisão, o encerramento do lote e o evento na outbox são gravados na mesma
     * transação local: ou os três acontecem, ou nenhum. É o que evita o dual write
     * entre banco e broker (§6.8).
     *
     * @param ifMatch ETag do lote que o cliente viu; nulo quando o cabeçalho não veio
     */
    @Transactional
    public DecisaoResposta decidir(UUID projetoId, UUID loteId, String ifMatch, String chave, DecisaoEntrada entrada) {
        if (chave == null || chave.isBlank()) {
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "IDEMPOTENCY_KEY_AUSENTE",
                    "A decisão exige Idempotency-Key.");
        }
        String impressao = impressao(loteId, entrada);
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { },
                "decisao:" + projetoId + ":" + chave);
        banco.update("DELETE FROM chaves_idempotencia WHERE projeto_id = ? AND operacao = ? AND chave = ? AND criado_em <= now() - interval '24 hours'",
                projetoId, "criarDecisao", chave);
        var registros = banco.query("SELECT impressao, recurso_id FROM chaves_idempotencia WHERE projeto_id = ? AND operacao = ? AND chave = ?",
                (rs, n) -> new ChaveRegistrada(rs.getString(1), rs.getObject(2, UUID.class)),
                projetoId, "criarDecisao", chave);
        if (!registros.isEmpty()) {
            ChaveRegistrada registro = registros.get(0);
            if (!registro.impressao().equals(impressao)) {
                throw new Problema(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUTILIZADA",
                        "A chave já foi usada em outro pedido.");
            }
            return DecisaoResposta.de(decisoes.findById(registro.recursoId()).orElseThrow());
        }
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, "lote:" + loteId);
        Lote lote = lotes.findByIdAndProjetoId(loteId, projetoId).orElseThrow(LoteService::naoEncontrado);

        if (ifMatch == null || ifMatch.isBlank()) {
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_AUSENTE",
                    "A decisão exige If-Match com o ETag do lote.");
        }
        if (!ifMatch.trim().equals(etag(lote.getRevisao()))) {
            throw new Problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DESATUALIZADA",
                    "O lote mudou desde a última consulta. Consulte de novo antes de decidir.");
        }
        if (entrada.decisao().exigeJustificativa()
                && (entrada.justificativa() == null || entrada.justificativa().isBlank())) {
            throw new Problema(HttpStatus.BAD_REQUEST, "JUSTIFICATIVA_OBRIGATORIA",
                    "Rejeitar ou solicitar correções exige justificativa.");
        }
        if (lote.getEstado() != EstadoLote.ATIVO) {
            throw new Problema(HttpStatus.CONFLICT, "LOTE_NAO_ACEITA_DECISAO",
                    "O lote está " + lote.getEstado() + " e não aceita decisão.");
        }
        if (!lote.correspondeA(entrada.submissaoId(), entrada.versaoId(), entrada.hashConteudo())) {
            throw new Problema(HttpStatus.CONFLICT, "DECISAO_DE_OUTRA_TENTATIVA",
                    "Submissão, versão ou conteúdo informados não são os avaliados por este lote.");
        }
        if (entrada.decisao() == TipoDecisao.APROVAR
                && lote.temPendenciaBloqueanteAberta()) {
            throw new Problema(HttpStatus.CONFLICT, "PENDENCIA_BLOQUEANTE_ABERTA",
                    "Há pendência bloqueante não resolvida neste lote.");
        }

        Decisao decisao = decisoes.save(Decisao.registrar(lote, entrada.decisao(), entrada.justificativa(), usuario.obter()));
        lote.encerrar();
        emitirEvento(lote, decisao);
        decisoes.flush();
        banco.update("INSERT INTO chaves_idempotencia (projeto_id, operacao, chave, impressao, recurso_id) VALUES (?, ?, ?, ?, ?)",
                projetoId, "criarDecisao", chave, impressao, decisao.getId());
        return DecisaoResposta.de(decisao);
    }

    private String impressao(UUID loteId, DecisaoEntrada entrada) {
        try {
            byte[] conteudo = json.writeValueAsBytes(entrada);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(loteId.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest(conteudo));
        } catch (com.fasterxml.jackson.core.JsonProcessingException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Não foi possível calcular a impressão do pedido", e);
        }
    }

    private record ChaveRegistrada(String impressao, UUID recursoId) {
    }

    /** Envelope do evento conforme o livro §6.6. */
    private void emitirEvento(Lote lote, Decisao decisao) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("loteId", lote.getId().toString());
        payload.put("decisaoId", decisao.getId().toString());
        payload.put("decisao", decisao.getDecisao().name());
        payload.put("requisitoId", lote.getRequisitoId().toString());
        payload.put("submissaoId", lote.getSubmissaoId().toString());
        payload.put("versaoId", lote.getVersaoId().toString());
        payload.put("hashConteudo", lote.getHashConteudo());

        emissor.evento("DecisaoRevisaoRegistrada", lote.getProjetoId(), null, null,
                lote.getSubmissaoId(), null, payload);
    }

    private LoteResposta representar(Lote lote) {
        return LoteResposta.de(lote, decisoes.findByLoteId(lote.getId()).orElse(null));
    }

    public static String etag(long revisao) {
        return "\"" + revisao + "\"";
    }

    private static Problema naoEncontrado() {
        return new Problema(HttpStatus.NOT_FOUND, "LOTE_NAO_ENCONTRADO", "Lote não encontrado.");
    }

    public record LoteComRevisao(LoteResposta lote, long revisao) {
    }
}
