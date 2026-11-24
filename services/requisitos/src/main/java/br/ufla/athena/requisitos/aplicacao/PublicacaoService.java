package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Problema;
import br.ufla.athena.requisitos.api.PublicacaoEntrada;
import br.ufla.athena.requisitos.api.PublicacaoResposta;
import br.ufla.athena.requisitos.dominio.Publicacao;
import br.ufla.athena.requisitos.dominio.PublicacaoRepository;
import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.Versao;
import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicacaoService {
    private static final String OPERACAO = "criarPublicacao";
    private final RequisitoRepository requisitos;
    private final PublicacaoRepository publicacoes;
    private final JdbcTemplate banco;
    private final EmissorMensagens emissor;
    private final EntityManager entidades;
    private final UUID autor;

    public PublicacaoService(RequisitoRepository requisitos, PublicacaoRepository publicacoes,
                             JdbcTemplate banco, EmissorMensagens emissor, EntityManager entidades,
                             @Value("${athena.usuario-demo}") UUID autor) {
        this.requisitos = requisitos;
        this.publicacoes = publicacoes;
        this.banco = banco;
        this.emissor = emissor;
        this.entidades = entidades;
        this.autor = autor;
    }

    @Transactional
    public PublicacaoResposta criar(UUID projeto, UUID requisitoId, PublicacaoEntrada entrada,
                                   String chave, String ifMatch) {
        if (chave == null || chave.isBlank()) {
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "IDEMPOTENCY_KEY_AUSENTE", "Informe Idempotency-Key.");
        }
        if (chave.length() > 200) {
            throw new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Chave muito longa.");
        }
        String impressao = IdempotenciaSubmissao.impressaoPublicacao(requisitoId, entrada.versaoId(),
                entrada.submissaoId(), entrada.comprovanteHumano());
        banco.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", (rs, n) -> 0,
                projeto + ":" + OPERACAO + ":" + chave);
        banco.update("DELETE FROM chaves_idempotencia WHERE projeto_id=? AND operacao=? AND chave=? "
                + "AND criado_em < now() - interval '24 hours'", projeto, OPERACAO, chave);
        List<Map<String, Object>> anteriores = banco.queryForList(
                "SELECT impressao, recurso_id FROM chaves_idempotencia WHERE projeto_id=? AND operacao=? AND chave=?",
                projeto, OPERACAO, chave);
        if (!anteriores.isEmpty()) {
            Map<String, Object> anterior = anteriores.get(0);
            if (!IdempotenciaSubmissao.mesmoPedido((String) anterior.get("impressao"), impressao)) {
                throw new Problema(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUTILIZADA", "A chave já foi usada em outro pedido.");
            }
            return buscar(projeto, requisitoId, (UUID) anterior.get("recurso_id"));
        }
        Requisito requisito = requisitos.buscarParaEdicao(requisitoId, projeto)
                .orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND, "REQUISITO_NAO_ENCONTRADO", "Requisito não encontrado."));
        Versao candidata = requisito.candidata().orElse(null);
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_AUSENTE", "Informe If-Match.");
        }
        if (candidata != null && !ifMatch.trim().equals("\"" + candidata.getRevisao() + "\"")) {
            throw new Problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DESATUALIZADA", "A candidata foi alterada.");
        }
        List<Map<String, Object>> submissoes = banco.queryForList(
                "SELECT id, versao_id, retrato_id FROM submissoes WHERE requisito_id=? AND estado_saga='CONCLUIDA' "
                        + "ORDER BY criado_em DESC, id DESC LIMIT 1", requisitoId);
        Map<String, Object> ultima = submissoes.isEmpty() ? null : submissoes.get(0);
        UUID ultimaId = ultima == null ? null : (UUID) ultima.get("id");
        if (ultima != null && !entrada.versaoId().equals(ultima.get("versao_id"))) {
            ultimaId = null;
        }
        String erro = RegrasRevisao.erroPublicacao(requisito.getSituacao(),
                candidata == null ? null : candidata.getId(), candidata == null ? null : candidata.getEstadoVersao(),
                entrada.versaoId(), ultimaId, entrada.submissaoId());
        if (erro != null) {
            throw new Problema(HttpStatus.CONFLICT, erro, "A publicação não atende às condições necessárias.");
        }
        UUID retrato = (UUID) ultima.get("retrato_id");
        if (retrato == null) {
            throw new IllegalStateException("Submissão concluída sem retrato");
        }
        Instant agora = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Publicacao publicacao = Publicacao.criar(requisitoId, candidata.getId(), entrada.submissaoId(), retrato, autor, agora);
        entidades.persist(publicacao);
        candidata.alterarEstado(EstadoVersao.PUBLICADA);
        banco.update("INSERT INTO chaves_idempotencia (chave, operacao, projeto_id, impressao, recurso_id, criado_em) "
                + "VALUES (?,?,?,?,?,?)", chave, OPERACAO, projeto, impressao, publicacao.getId(), Timestamp.from(agora));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("publicacaoId", publicacao.getId().toString());
        payload.put("requisitoId", requisitoId.toString());
        payload.put("versaoId", candidata.getId().toString());
        payload.put("numero", candidata.getNumero());
        payload.put("codigo", requisito.getCodigo());
        payload.put("titulo", candidata.getTitulo());
        payload.put("conteudo", candidata.getConteudo());
        payload.put("criteriosAceite", candidata.getCriteriosAceite());
        payload.put("hashConteudo", candidata.getHashConteudo());
        payload.put("submissaoId", entrada.submissaoId().toString());
        payload.put("retratoId", retrato.toString());
        payload.put("data", agora.toString());
        emissor.evento("VersaoPublicada", projeto, null, null, entrada.submissaoId(), null, payload);
        return PublicacaoResposta.de(publicacao);
    }

    @Transactional(readOnly = true)
    public PublicacaoResposta buscar(UUID projeto, UUID requisito, UUID publicacaoId) {
        if (requisitos.findByIdAndProjetoId(requisito, projeto).isEmpty()) {
            throw new Problema(HttpStatus.NOT_FOUND, "REQUISITO_NAO_ENCONTRADO", "Requisito não encontrado.");
        }
        Publicacao publicacao = publicacoes.findById(publicacaoId)
                .filter(item -> item.getRequisitoId().equals(requisito))
                .orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND, "PUBLICACAO_NAO_ENCONTRADA", "Publicação não encontrada."));
        return PublicacaoResposta.de(publicacao);
    }
}
