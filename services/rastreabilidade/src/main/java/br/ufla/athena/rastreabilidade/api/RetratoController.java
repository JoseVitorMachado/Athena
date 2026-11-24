package br.ufla.athena.rastreabilidade.api;

import br.ufla.athena.rastreabilidade.aplicacao.ParticipanteSaga;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/retratos")
public class RetratoController {
    private final JdbcTemplate banco;
    private final ParticipanteSaga participante;

    public RetratoController(JdbcTemplate banco, ParticipanteSaga participante) {
        this.banco = banco;
        this.participante = participante;
    }

    @GetMapping("/{retratoId}")
    public ResponseEntity<Map<String, Object>> obter(@PathVariable UUID projetoId, @PathVariable UUID retratoId) {
        var retratos = banco.query("SELECT * FROM retratos WHERE id = ? AND projeto_id = ?", (rs, n) -> {
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("retratoId", rs.getObject("id", UUID.class));
            resposta.put("submissaoId", rs.getObject("submissao_id", UUID.class));
            resposta.put("versaoId", rs.getObject("versao_id", UUID.class));
            resposta.put("hashConteudo", rs.getString("hash_conteudo"));
            resposta.put("estado", rs.getString("estado"));
            resposta.put("relacoes", participante.ler("{\"relacoes\":" + rs.getString("relacoes") + "}").get("relacoes"));
            resposta.put("criadoEm", rs.getTimestamp("criado_em").toInstant());
            return ResponseEntity.ok().eTag("\"" + rs.getString("estado") + "-" + rs.getTimestamp("atualizado_em").toInstant() + "\"").body(resposta);
        }, retratoId, projetoId);
        return retratos.stream().findFirst().orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "RETRATO_NAO_ENCONTRADO", "Retrato não encontrado."));
    }
}
