package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Problema;
import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Gerencia baselines imutáveis e sua comparação com o Livro. */
@Service
public class BaselineService {
    private final JdbcTemplate banco;
    private final UsuarioAtual usuario;
    private final PapelAtual papel;
    private final EmissorMensagens emissor;

    public record VersaoItem(UUID requisitoId, UUID versaoId, UUID retratoId, String codigo, String titulo, int numero) { }
    public record Resposta(UUID baselineId, UUID projetoId, String nome, UUID autor, Instant data, List<VersaoItem> versoes) { }
    public record Resumo(UUID baselineId, UUID projetoId, String nome, UUID autor, Instant data, long quantidade) { }

    public BaselineService(JdbcTemplate banco, UsuarioAtual usuario, PapelAtual papel, EmissorMensagens emissor) {
        this.banco = banco; this.usuario = usuario; this.papel = papel; this.emissor = emissor;
    }

    @Transactional(readOnly = true)
    public Page<Resumo> listar(UUID projeto, Pageable pagina) {
        long total = banco.queryForObject("SELECT count(*) FROM baselines WHERE projeto_id=?", Long.class, projeto);
        List<Resumo> itens = banco.query("SELECT b.*, (SELECT count(*) FROM baseline_itens i WHERE i.baseline_id=b.id) quantidade FROM baselines b WHERE b.projeto_id=? ORDER BY b.criada_em DESC, b.id LIMIT ? OFFSET ?",
                (rs, n) -> new Resumo(uuid(rs, "id"), uuid(rs, "projeto_id"), rs.getString("nome"), uuid(rs, "autor_id"), rs.getTimestamp("criada_em").toInstant(), rs.getLong("quantidade")), projeto, pagina.getPageSize(), pagina.getOffset());
        return new PageImpl<>(itens, pagina, total);
    }

    @Transactional(readOnly = true)
    public Resposta obter(UUID projeto, UUID id) {
        List<Resposta> baselines = banco.query("SELECT * FROM baselines WHERE projeto_id=? AND id=?", (rs, n) ->
                new Resposta(uuid(rs, "id"), uuid(rs, "projeto_id"), rs.getString("nome"), uuid(rs, "autor_id"), rs.getTimestamp("criada_em").toInstant(), List.of()), projeto, id);
        if (baselines.isEmpty()) throw new Problema(HttpStatus.NOT_FOUND, "BASELINE_NAO_ENCONTRADA", "Baseline não encontrada.");
        Resposta b = baselines.get(0);
        return new Resposta(b.baselineId(), b.projetoId(), b.nome(), b.autor(), b.data(), itens(id));
    }

    @Transactional
    public Resposta criar(UUID projeto, JsonNode entrada) {
        var p = papel.obter();
        if (p == null || p.ordinal() < RegrasComentario.Papel.EDITOR.ordinal())
            throw new Problema(HttpStatus.FORBIDDEN, "PAPEL_INSUFICIENTE", "Papel insuficiente para criar baseline.");
        campos(entrada, Set.of("nome", "versoes"));
        JsonNode nomeNode = entrada.get("nome"), lista = entrada.get("versoes");
        if (nomeNode == null || !nomeNode.isTextual() || nomeNode.textValue().trim().isBlank()
                || nomeNode.textValue().trim().length() > 120 || lista == null || !lista.isArray() || lista.size() > 500) throw invalida();
        String nome = nomeNode.textValue().trim();
        List<VersaoItem> escolhidos = new ArrayList<>();
        if (lista.isEmpty()) {
            escolhidos.addAll(livro(projeto));
        } else {
            List<RegrasBaseline.Item> solicitados = new ArrayList<>();
            List<RegrasBaseline.Vinculo> vinculos = new ArrayList<>();
            for (JsonNode item : lista) {
                if (!item.isObject()) throw new Problema(HttpStatus.BAD_REQUEST, "ITEM_BASELINE_INVALIDO", "Item inválido: requisitoId não informado.");
                UUID requisito;
                try { requisito = id(item.get("requisitoId")); }
                catch (Problema e) { throw new Problema(HttpStatus.BAD_REQUEST, "ITEM_BASELINE_INVALIDO", "Item inválido para o requisito " + item.path("requisitoId").asText("não informado") + "."); }
                try { campos(item, Set.of("requisitoId", "versaoId", "retratoId")); }
                catch (Problema e) { throw itemInvalido(requisito); }
                UUID versao;
                UUID retrato;
                try { versao = id(item.get("versaoId")); retrato = id(item.get("retratoId")); }
                catch (Problema e) { throw itemInvalido(requisito); }
                solicitados.add(new RegrasBaseline.Item(requisito, versao, retrato));
                List<VersaoItem> encontrados = banco.query("SELECT r.id requisito_id, v.id versao_id, p.retrato_id, r.codigo, v.titulo, v.numero FROM requisitos r JOIN versoes v ON v.requisito_id=r.id JOIN publicacoes p ON p.versao_id=v.id AND p.requisito_id=r.id WHERE r.projeto_id=? AND r.id=? AND v.id=? AND v.estado_versao='PUBLICADA'", (rs, n) -> lerItem(rs), projeto, requisito, versao);
                for (VersaoItem encontrado : encontrados) {
                    vinculos.add(new RegrasBaseline.Vinculo(encontrado.requisitoId(), encontrado.versaoId(), encontrado.retratoId(), true));
                    escolhidos.add(encontrado);
                }
            }
            UUID invalido = RegrasBaseline.primeiroInvalido(solicitados, vinculos);
            if (invalido != null) throw itemInvalido(invalido);
        }
        if (escolhidos.isEmpty()) throw new Problema(HttpStatus.CONFLICT, "BASELINE_VAZIA", "Não há conteúdo publicado para a baseline.");
        UUID id = UUID.randomUUID(), autor = usuario.obter();
        int inseridos = banco.update("INSERT INTO baselines(id,projeto_id,nome,autor_id) VALUES (?,?,?,?) ON CONFLICT DO NOTHING", id, projeto, nome, autor);
        if (inseridos == 0) throw new Problema(HttpStatus.CONFLICT, "BASELINE_DUPLICADA", "Já existe uma baseline com este nome no projeto.");
        for (VersaoItem item : escolhidos) banco.update("INSERT INTO baseline_itens(baseline_id,requisito_id,versao_id,retrato_id,codigo,titulo,numero) VALUES (?,?,?,?,?,?,?)",
                id, item.requisitoId(), item.versaoId(), item.retratoId(), item.codigo(), item.titulo(), item.numero());
        emissor.evento("BaselineCriada", projeto, null, null, id, null, Map.of("projetoId", projeto, "baselineId", id, "nome", nome, "quantidade", escolhidos.size(), "autorId", autor));
        return obter(projeto, id);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ComparacaoBaseline.Resultado comparar(UUID projeto, UUID id) {
        Resposta baseline = obter(projeto, id);
        return ComparacaoBaseline.comparar(baseline.versoes().stream().map(BaselineService::conteudo).toList(),
                livro(projeto).stream().map(BaselineService::conteudo).toList());
    }

    private List<VersaoItem> livro(UUID projeto) {
        return banco.query("SELECT r.id requisito_id, v.id versao_id, p.retrato_id, r.codigo, v.titulo, v.numero FROM requisitos r JOIN LATERAL (SELECT * FROM versoes WHERE requisito_id=r.id AND estado_versao='PUBLICADA' ORDER BY numero DESC LIMIT 1) v ON true JOIN publicacoes p ON p.versao_id=v.id AND p.requisito_id=r.id WHERE r.projeto_id=? AND r.situacao='ATIVO' AND NOT r.fora_do_livro ORDER BY r.codigo, r.id", (rs, n) -> lerItem(rs), projeto);
    }
    private List<VersaoItem> itens(UUID id) {
        return banco.query("SELECT * FROM baseline_itens WHERE baseline_id=? ORDER BY codigo, requisito_id", (rs, n) -> lerItem(rs), id);
    }
    private static VersaoItem lerItem(ResultSet rs) throws SQLException {
        return new VersaoItem(uuid(rs, "requisito_id"), uuid(rs, "versao_id"), uuid(rs, "retrato_id"), rs.getString("codigo"), rs.getString("titulo"), rs.getInt("numero"));
    }
    private static ComparacaoBaseline.Conteudo conteudo(VersaoItem i) { return new ComparacaoBaseline.Conteudo(i.requisitoId(), i.versaoId(), i.codigo(), i.titulo(), i.numero()); }
    private static UUID uuid(ResultSet rs, String coluna) throws SQLException { return rs.getObject(coluna, UUID.class); }
    private static void campos(JsonNode no, Set<String> permitidos) {
        if (no == null || !no.isObject()) throw invalida();
        no.fieldNames().forEachRemaining(c -> { if (!permitidos.contains(c)) throw invalida(); });
    }
    private static UUID id(JsonNode no) {
        try {
            if (no == null || !no.isTextual()) throw invalida();
            UUID id = UUID.fromString(no.textValue());
            if (!id.toString().equalsIgnoreCase(no.textValue())) throw invalida();
            return id;
        } catch (IllegalArgumentException e) { throw invalida(); }
    }
    private static Problema invalida() { return new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Dados da baseline inválidos."); }
    private static Problema itemInvalido(UUID id) { return new Problema(HttpStatus.BAD_REQUEST, "ITEM_BASELINE_INVALIDO", "Item inválido para o requisito " + id + "."); }
}
