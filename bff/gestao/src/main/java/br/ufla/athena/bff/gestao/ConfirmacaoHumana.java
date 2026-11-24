package br.ufla.athena.bff.gestao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Valida a presença humana antes de solicitar o comprovante ao domínio. */
final class ConfirmacaoHumana {
    private ConfirmacaoHumana() {}

    static ResponseEntity<JsonNode> emitir(ClienteDominio dominio, String projetosUrl, String servico,
                                             String destino, String caminho, JsonNode corpo) {
        if (corpo == null || !corpo.isObject() || !corpo.path("versaoId").isTextual()
                || !uuidValido(corpo.path("versaoId").asText())
                || !corpo.path("hashAcao").isTextual() || !corpo.path("hashAcao").asText().matches("[a-f0-9]{64}")
                || !corpo.path("confirmacao").isBoolean() || !corpo.path("confirmacao").booleanValue()
                || !corpo.path("senha").isTextual() || corpo.path("senha").asText().isBlank()) {
            throw new FalhaAutorizacao(400, "ENTRADA_INVALIDA", "Dados de confirmação inválidos.");
        }
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticacao instanceof JwtAuthenticationToken jwt) || !jwt.isAuthenticated()
                || !uuidValido(jwt.getToken().getSubject())) {
            throw new FalhaAutorizacao(401, "NAO_AUTENTICADO", "Usuário não autenticado.");
        }
        ObjectNode senha = JsonNodeFactory.instance.objectNode().put("senha", corpo.path("senha").asText());
        var resposta = dominio.post("projetos-acesso", projetosUrl,
                "/api/v1/internal/usuarios/" + jwt.getToken().getSubject() + "/verificacao-senha", senha);
        if (resposta.status().value() == 401) throw new FalhaAutorizacao(401, "SENHA_INVALIDA", "Senha inválida.");
        if (resposta.status().value() == 429) return RequisitosGestaoController.repassar(resposta);
        if (resposta.status().value() != 204) {
            throw new FalhaAutorizacao(503, "DOMINIO_INDISPONIVEL", "Não foi possível verificar a senha.");
        }
        ObjectNode encaminhado = JsonNodeFactory.instance.objectNode();
        encaminhado.set("versaoId", corpo.get("versaoId"));
        encaminhado.set("hashAcao", corpo.get("hashAcao"));
        encaminhado.put("confirmacao", true);
        return RequisitosGestaoController.repassar(dominio.post(servico, destino, caminho, encaminhado));
    }

    private static boolean uuidValido(String valor) {
        try { return valor != null && UUID.fromString(valor).toString().equalsIgnoreCase(valor); }
        catch (IllegalArgumentException e) { return false; }
    }
}
