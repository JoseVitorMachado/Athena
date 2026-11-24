package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Pagina;
import br.ufla.athena.requisitos.api.Problema;
import br.ufla.athena.requisitos.dominio.Epico;
import br.ufla.athena.requisitos.dominio.EpicoRepository;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Operações transacionais sobre épicos. */
@Service
public class EpicoService {
    private final EpicoRepository epicos;
    private final JdbcTemplate banco;
    private final EmissorMensagens emissor;
    private final UsuarioAtual usuario;
    public EpicoService(EpicoRepository epicos, JdbcTemplate banco, EmissorMensagens emissor, UsuarioAtual usuario) {
        this.epicos = epicos; this.banco = banco; this.emissor = emissor; this.usuario = usuario;
    }
    @Transactional(readOnly = true)
    public Pagina<Resposta> listar(UUID projeto, Pageable pagina, String self) {
        var itens = epicos.findByProjetoId(projeto, pagina);
        Map<UUID, Long> contagens = new HashMap<>();
        if (!itens.isEmpty()) {
            var ids = itens.getContent().stream().map(Epico::getId).toList();
            String marcas = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
            banco.query("SELECT epico_id, count(*) AS total FROM requisitos WHERE situacao='ATIVO' AND epico_id IN (" + marcas + ") GROUP BY epico_id", rs -> {
                contagens.put(rs.getObject("epico_id", UUID.class), rs.getLong("total"));
            }, ids.toArray());
        }
        return Pagina.de(itens.map(e -> resposta(e, contagens.getOrDefault(e.getId(), 0L))), self);
    }
    @Transactional(readOnly = true)
    public Encontrado obter(UUID projeto, UUID id) {
        Epico e = epicos.findByIdAndProjetoId(id, projeto).orElseThrow(EpicoService::naoEncontrado);
        return encontrado(e);
    }
    @Transactional
    public Encontrado criar(UUID projeto, JsonNode corpo) {
        validarObjeto(corpo);
        String titulo = titulo(corpo.get("titulo"));
        String descricao = descricao(corpo.get("descricao"));
        Epico e = epicos.saveAndFlush(Epico.criar(projeto, titulo, descricao));
        emitir("EpicoCriado", e);
        return encontrado(e);
    }
    @Transactional
    public Encontrado alterar(UUID projeto, UUID id, String ifMatch, JsonNode corpo) {
        Epico e = travar(projeto, id);
        Precondicao.verificar(ifMatch, e.getRevisao());
        validarObjeto(corpo);
        if (corpo.isEmpty()) throw invalido();
        e.alterar(corpo.has("titulo") ? titulo(corpo.get("titulo")) : e.getTitulo(),
                corpo.has("descricao") ? descricao(corpo.get("descricao")) : e.getDescricao());
        epicos.saveAndFlush(e);
        emitir("EpicoAlterado", e);
        return encontrado(e);
    }
    @Transactional
    public void excluir(UUID projeto, UUID id, String ifMatch) {
        banco.queryForObject("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", (rs, n) -> 0, "epicos:" + projeto);
        Epico e = travar(projeto, id);
        Precondicao.verificar(ifMatch, e.getRevisao());
        if (contar(id) > 0) throw new Problema(HttpStatus.CONFLICT, "EPICO_EM_USO", "Épico possui requisitos ativos.");
        banco.update("UPDATE requisitos SET epico_id=NULL, atualizado_em=now(), revisao=revisao+1 WHERE epico_id=? AND situacao='CANCELADO'", id);
        epicos.delete(e);
        emitir("EpicoRemovido", e);
    }
    private Epico travar(UUID projeto, UUID id) { return epicos.buscarParaEdicao(id, projeto).orElseThrow(EpicoService::naoEncontrado); }
    private long contar(UUID id) { return banco.queryForObject("SELECT count(*) FROM requisitos WHERE epico_id=? AND situacao='ATIVO'", Long.class, id); }
    private Encontrado encontrado(Epico e) { return new Encontrado(resposta(e, contar(e.getId())), e.getRevisao()); }
    private static Resposta resposta(Epico e, long total) { return new Resposta(e.getId(), e.getProjetoId(), e.getTitulo(), e.getDescricao(), total, e.getAtualizadoEm()); }
    private void emitir(String tipo, Epico e) { emissor.evento(tipo, e.getProjetoId(), null, null, UUID.randomUUID(), null,
            Map.of("projetoId", e.getProjetoId(), "epicoId", e.getId(), "titulo", e.getTitulo(), "autorId", usuario.obter())); }
    private static void validarObjeto(JsonNode corpo) {
        if (!corpo.isObject()) throw invalido();
        corpo.fieldNames().forEachRemaining(c -> { if (!c.equals("titulo") && !c.equals("descricao")) throw invalido(); });
    }
    private static String titulo(JsonNode n) {
        if (n == null || !n.isTextual() || n.asText().trim().isEmpty() || n.asText().trim().length() > 200) throw invalido();
        return n.asText().trim();
    }
    private static String descricao(JsonNode n) {
        if (n == null || n.isNull()) return null;
        if (!n.isTextual() || n.asText().length() > 2000) throw invalido();
        return n.asText();
    }
    private static Problema invalido() { return new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Dados de épico inválidos."); }
    private static Problema naoEncontrado() { return new Problema(HttpStatus.NOT_FOUND, "EPICO_NAO_ENCONTRADO", "Épico não encontrado."); }
    public record Resposta(UUID epicoId, UUID projetoId, String titulo, String descricao, long quantidadeRequisitos, Instant atualizadoEm) { }
    public record Encontrado(Resposta resposta, long revisao) { }
}
