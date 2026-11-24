package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.mensageria.EmissorMensagens;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Resultado e resposta compartilham a transação da inbox (livro §6.8). */
@Component
public class ParticipanteSaga {
    private final JdbcTemplate banco;
    private final ObjectMapper json;
    private final EmissorMensagens emissor;

    public ParticipanteSaga(JdbcTemplate banco, ObjectMapper json, EmissorMensagens emissor) {
        this.banco = banco;
        this.json = json.copy().setDefaultPropertyInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS);
        this.emissor = emissor;
    }

    public void travar(UUID submissaoId, UUID commandId) {
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, "saga:" + submissaoId);
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, "comando:" + commandId);
    }

    public boolean repetir(Map<String, Object> envelope, boolean silenciar) {
        UUID comando = id(envelope, "commandId");
        var resultados = banco.query("SELECT message_type, resposta_tipo, resposta_payload::text FROM resultados_comando WHERE command_id = ?",
                (rs, n) -> new Resultado(rs.getString(1), rs.getString(2), ler(rs.getString(3))), comando);
        if (resultados.isEmpty()) {
            return false;
        }
        Resultado resultado = resultados.get(0);
        if (!resultado.comando().equals(envelope.get("messageType"))) {
            throw new IllegalArgumentException("commandId usado por outro tipo de comando");
        }
        if (!silenciar) {
            emitir(envelope, resultado.tipo(), resultado.payload());
        }
        return true;
    }

    public void concluir(Map<String, Object> envelope, String tipo, Map<String, Object> payload, boolean silenciar) {
        banco.update("INSERT INTO resultados_comando (command_id, message_type, resposta_tipo, resposta_payload, projeto_id) VALUES (?, ?, ?, CAST(? AS jsonb), ?)",
                id(envelope, "commandId"), envelope.get("messageType"), tipo, escrever(payload), id(envelope, "projetoId"));
        if (!silenciar) {
            emitir(envelope, tipo, payload);
        }
    }

    private void emitir(Map<String, Object> envelope, String tipo, Map<String, Object> payload) {
        emissor.evento(tipo, id(envelope, "projetoId"), id(envelope, "sagaId"), id(envelope, "commandId"),
                id(envelope, "correlationId"), id(envelope, "messageId"), payload);
    }

    public String escrever(Object valor) {
        try {
            return json.writeValueAsString(valor);
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível serializar o resultado", e);
        }
    }

    public Map<String, Object> ler(String valor) {
        try {
            return json.readValue(valor, new TypeReference<Map<String, Object>>() { });
        } catch (Exception e) {
            throw new IllegalArgumentException("Resultado inválido", e);
        }
    }

    public static UUID id(Map<String, Object> mapa, String campo) {
        return UUID.fromString(mapa.get(campo).toString());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> payload(Map<String, Object> envelope) {
        return (Map<String, Object>) envelope.get("payload");
    }

    private record Resultado(String comando, String tipo, Map<String, Object> payload) { }
}
