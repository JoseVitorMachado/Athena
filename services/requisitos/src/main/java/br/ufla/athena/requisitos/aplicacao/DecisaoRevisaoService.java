package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.Versao;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import br.ufla.athena.requisitos.mensageria.ManipuladorMensagem;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DecisaoRevisaoService implements ManipuladorMensagem {
    private static final Logger log = LoggerFactory.getLogger(DecisaoRevisaoService.class);
    private final RequisitoRepository requisitos;
    private final JdbcTemplate banco;
    private final EmissorMensagens emissor;

    public DecisaoRevisaoService(RequisitoRepository requisitos, JdbcTemplate banco, EmissorMensagens emissor) {
        this.requisitos = requisitos;
        this.banco = banco;
        this.emissor = emissor;
    }

    @Override
    public String messageType() {
        return "DecisaoRevisaoRegistrada";
    }

    @Override
    @Transactional
    public void tratar(Map<String, Object> envelope) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) envelope.get("payload");
        UUID projeto = id(envelope, "projetoId");
        UUID requisitoId = id(payload, "requisitoId");
        UUID versaoId = id(payload, "versaoId");
        UUID submissaoId = id(payload, "submissaoId");
        UUID decisaoId = id(payload, "decisaoId");
        UUID loteId = id(payload, "loteId");
        String decisao = (String) payload.get("decisao");
        if (!List.of("APROVAR", "REJEITAR", "SOLICITAR_CORRECOES").contains(decisao)) {
            throw new IllegalArgumentException("Decisão inválida");
        }
        banco.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", (rs, n) -> 0,
                "decisao:" + decisaoId);
        List<Map<String, Object>> anteriores = banco.queryForList(
                "SELECT resultado, motivo FROM decisoes_aplicadas WHERE decisao_id=?", decisaoId);
        String resultado;
        String motivo;
        if (!anteriores.isEmpty()) {
            resultado = (String) anteriores.get(0).get("resultado");
            motivo = (String) anteriores.get(0).get("motivo");
        } else {
            Requisito requisito = requisitos.buscarParaEdicao(requisitoId, projeto).orElse(null);
            Versao candidata = requisito == null ? null : requisito.candidata().orElse(null);
            List<UUID> submissoes = banco.query("SELECT id FROM submissoes WHERE requisito_id=? "
                    + "AND estado_saga='CONCLUIDA' ORDER BY criado_em DESC, id DESC LIMIT 1",
                    (rs, n) -> rs.getObject("id", UUID.class), requisitoId);
            UUID ultima = submissoes.isEmpty() ? null : submissoes.get(0);
            motivo = RegrasRevisao.motivoDescarte(candidata == null ? null : candidata.getId(),
                    candidata == null ? null : candidata.getEstadoVersao(),
                    candidata == null ? null : candidata.getHashConteudo(), ultima, versaoId,
                    (String) payload.get("hashConteudo"), submissaoId);
            resultado = motivo == null ? "APLICADA" : "DESCARTADA";
            if (motivo == null) {
                candidata.alterarEstado("APROVAR".equals(decisao) ? EstadoVersao.APROVADA : EstadoVersao.RASCUNHO);
            }
            banco.update("INSERT INTO decisoes_aplicadas (decisao_id, versao_id, resultado, motivo) VALUES (?,?,?,?)",
                    decisaoId, versaoId, resultado, motivo);
        }
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("decisaoId", decisaoId.toString());
        resposta.put("loteId", loteId.toString());
        resposta.put("submissaoId", submissaoId.toString());
        resposta.put("resultado", resultado);
        resposta.put("decisao", decisao);
        resposta.put("requisitoId", requisitoId.toString());
        if (motivo != null) {
            resposta.put("motivo", motivo);
        }
        requisitos.findByIdAndProjetoId(requisitoId, projeto)
                .map(Requisito::getCodigo).ifPresent(codigo -> resposta.put("codigo", codigo));
        List<UUID> autores = banco.query("SELECT autor_id FROM submissoes WHERE id=? AND requisito_id=? AND projeto_id=?",
                (rs, n) -> rs.getObject("autor_id", UUID.class), submissaoId, requisitoId, projeto);
        if (!autores.isEmpty() && autores.get(0) != null) resposta.put("submissorId", autores.get(0).toString());
        emissor.evento("DecisaoRevisaoAplicada", projeto, null, null, submissaoId,
                id(envelope, "messageId"), resposta);
        log.info("Decisão {}: {} {}", decisaoId, resultado, motivo == null ? "" : motivo);
    }

    private static UUID id(Map<String, Object> dados, String campo) {
        return UUID.fromString(dados.get(campo).toString());
    }
}
